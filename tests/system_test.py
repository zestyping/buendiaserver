#!/usr/bin/env python

import re
import json
import sqlite3
import unittest
import urllib2

SQLITE_FILE = 'msf.db'
SERVER_ROOT = 'http://localhost:8080'
HTTP_TIMEOUT = 10  # require HTTP replies within this many seconds

def http_request(request):
    """Issues an HTTP request, treating HTTPErrors as repsonses."""
    try:
        response = urllib2.urlopen(request, timeout=HTTP_TIMEOUT)
    except urllib2.HTTPError, error:
        response = error  # an HTTPError is also a response object
    return response.getcode(), dict(response.headers), response.read()

def http_get(path):
    """Issues a GET request and returns: (status_code, headers, content)."""
    return http_request(urllib2.Request(SERVER_ROOT + path))

def http_post(path, content, headers={}):
    """Issues a POST request and returns: (status_code, headers, content)."""
    return http_request(urllib2.Request(SERVER_ROOT + path, content, headers))

def http_put(path, content, headers={}):
    """Issues a PUT request and returns: (status_code, headers, content)."""
    request = urllib2.Request(SERVER_ROOT + path, content, headers)
    request.get_method = lambda: 'PUT'
    return http_request(request)

def http_form_post(path, content):
    return http_post(path, content, headers={
        'Content-Type': 'application/x-www-form-urlencoded'})

def http_json_post(path, content):
    return http_post(path, content, headers={
        'Content-Type': 'application/json'})

def http_put(path, content, headers={}):
    """Issues a PUT request and returns: (status_code, headers, content)."""
    req = urllib2.Request(SERVER_ROOT + path, content, headers)
    req.get_method = lambda: 'PUT'
    u = urllib2.urlopen(req, timeout=HTTP_TIMEOUT)
    return u.getcode(), dict(u.headers), u.read()

def http_json_put(path, content):
    return http_put(path, content, headers={
        'Content-Type': 'application/json'})

def reset_db():
    """Clears all existing tables in the SQLite database."""
    c = sqlite3.Connection(SQLITE_FILE)
    tables = c.execute("select tbl_name from sqlite_master where type='table'")
    for table in tables:
        c.execute('delete from %s' % table)
    c.commit()
    c.close()


class SystemTest(unittest.TestCase):
    def setUp(self):
        reset_db()

    def get_json(self, path):
        """Issues a GET request and decodes the response as JSON."""
        status_code, headers, content = http_get(path)
        self.assertEqual(200, status_code)
        # Ensure the content is properly encoded to unicode
        charset_match = re.match(r".*charset=(.+)", headers['content-type'])
        if charset_match:
            content = unicode(content, charset_match.group(1))
        return json.loads(content)

    def post_json(self, path, data):
        """Issues a POST request containing the given data encoded in JSON."""
        status_code, headers, content = http_json_post(path, json.dumps(data))
        self.assertEqual(200, status_code)
        return json.loads(content)

    def put_json(self, path, data):
        """Issues a PUT request containing the given data encoded in JSON."""
        status_code, headers, content = http_json_put(path, json.dumps(data))
        self.assertEqual(200, status_code)
        return json.loads(content)

    def test_json_serialization(self):
        self.post_json(
            '/patients', {'given_name': '{"}', 'status': 'suspected'})

        # Verify that special characters in data don't cause JSON syntax errors.
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        self.assertEqual('{"}', patients[0]['given_name'])

    def test_unicode(self):
        """Test support for unicode in the Patient API."""
        name = u'T\u00F8m'
        param = urllib2.quote(name.encode('utf-8'))  # URL-encoded

        # Add one patient; confirm it appears in the list of all patients.
        self.post_json('/patients', {'given_name': name, 'status': 'suspected'})
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        # Test if the name stayed the same
        self.assertEqual(name, patients[0]['given_name'])

        # Test if patients can be retrieved by unicode characters.
        self.assertEqual(1, len(self.get_json('/patients?given_name=' + param)))
        # Test if patients can be searched by unicode characters
        self.assertEqual(1, len(self.get_json('/patients?search=' + param)))

    def test_list_patients(self):
        """Testing retrieval of patients."""
        # List an empty database.
        self.assertEqual([], self.get_json('/patients'))

        # Add one patient; confirm it appears in the list of all patients.
        self.post_json(
            '/patients', {'given_name': 'Tom', 'status': 'suspected'})
        self.assertEqual(1, len(self.get_json('/patients')))

        # Test matching on single fields.
        self.assertEqual(0, len(self.get_json('/patients?status=foo')))
        self.assertEqual(1, len(self.get_json('/patients?status=suspected')))
        self.assertEqual(0, len(self.get_json('/patients?given_name=Bob')))
        self.assertEqual(1, len(self.get_json('/patients?given_name=Tom')))

        # TODO: Add an API feature to do a case-insensitive match on names.

        # Test matching on multiple fields.
        self.post_json(
            '/patients', {'given_name': 'Frank', 'status': 'discharged'})
        self.assertEqual(0, len(self.get_json(
            '/patients?given_name=Tom&status=foo')))
        self.assertEqual(0, len(self.get_json(
            '/patients?given_name=Bob&status=suspected')))
        self.assertEqual(0, len(self.get_json(
            '/patients?given_name=Frank&status=suspected')))
        self.assertEqual(1, len(self.get_json(
            '/patients?given_name=Frank&status=discharged')))
        self.assertEqual(1, len(self.get_json(
            '/patients?given_name=Tom&status=suspected')))

        # Test searching by substring.
        self.assertEqual(0, len(self.get_json('/patients?search=x')))
        self.assertEqual(1, len(self.get_json('/patients?search=Tom')))
        self.assertEqual(1, len(self.get_json('/patients?search=om')))
        self.assertEqual(1, len(self.get_json('/patients?search=To')))

        # Test searching is case-insensitive.
        self.assertEqual(1, len(self.get_json('/patients?search=tom')))

    def test_escaping_input(self):
        """Testing all incoming variables are properly escaped."""
        name = 'T\'o"m'

        # Add one patient that contains quotes.
        self.post_json('/patients', {'given_name': name, 'status': 'suspected'})

        # Test the patient is stored and can be retrieved.
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))

        # Test the name of the patient is still the same.
        self.assertEqual(name, patients[0]['given_name'])

        # Test search is properly escaping the incoming variables.
        self.assertEqual(1, len(self.get_json(
            '/patients?search=' + urllib2.quote(name))))

        # Test field match is properly escaping the incoming variables.
        self.assertEqual(1, len(self.get_json(
            '/patients?given_name=' + urllib2.quote(name))))

    def test_add_new_patient(self):
        # TODO(ping): The POST API should take JSON, not form-encoded data.
        self.post_json(
            '/patients', {'given_name': 'Tom', 'status': 'suspected'})

        # Verify that the new patient appears in the list of all patients.
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        self.assertEqual('Tom', patients[0]['given_name'])

    def test_edit_patient(self):
        """Test editing patients with PUT."""
        # Create patient that will be edited.
        patient1_id = self.post_json(
            '/patients', {'given_name': 'Tom', 'status': 'suspected'})['id']
        # Create patient that will be used as control.
        patient2_id = self.post_json(
            '/patients', {'given_name': 'Bob', 'status': 'suspected'})['id']

        # Check if patient Tom's name can be updated
        self.put_json('/patients/' + patient1_id, {'given_name': 'John'})
        patients = self.get_json('/patients?id=' + patient1_id)
        self.assertEqual(1, len(patients))
        self.assertEqual('John', patients[0]['given_name'])
        self.assertEqual('suspected', patients[0]['status'])

        # Check if patient Tom (now John)'s status can be updated
        self.put_json('/patients/' + patient1_id, {'status': 'discharged'})
        patients = self.get_json('/patients?id=' + patient1_id)
        self.assertEqual(1, len(patients))
        self.assertEqual('John', patients[0]['given_name'])
        self.assertEqual('discharged', patients[0]['status'])

        # Check if the control patient stayed the same
        patients = self.get_json('/patients?id=' + patient2_id)
        self.assertEqual(1, len(patients))
        self.assertEqual('Bob', patients[0]['given_name'])
        self.assertEqual('suspected', patients[0]['status'])

    def test_post_content_type(self):
        # Bad JSON syntax should be caught for application/json content.
        code, headers, response = http_post('/patients', '{', {
            'Content-Type': 'application/json'})
        self.assertEqual(400, code)

        code, headers, response = http_post('/patients', '{', {
            'Content-Type': 'application/json; charset=utf-8'})
        self.assertEqual(400, code)

        # JSON objects should be accepted.
        code, headers, response = http_post('/patients', '{}', {
            'Content-Type': 'application/json'})
        self.assertEqual(200, code)

        # JSON non-objects should be rejected.
        code, headers, response = http_post('/patients', '3', {
            'Content-Type': 'application/json'})
        self.assertEqual(400, code)

        code, headers, response = http_post('/patients', '"a"', {
            'Content-Type': 'application/json'})
        self.assertEqual(400, code)

        # No JSON parsing should occur if the content type isn't JSON.
        # At the moment, a 200 status is the only way to tell that parsing
        # didn't occur, even though an incorrect type should also give 400.
        code, headers, response = http_post('/patients', '{', {
            'Content-Type': 'application/x-www-form-urlencoded'})
        self.assertEqual(200, code)


if __name__ == '__main__':
    unittest.main()
