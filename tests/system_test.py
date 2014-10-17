#!/usr/bin/env python

import re
import json
import sqlite3
import unittest
import urllib2
from urllib import quote

SQLITE_FILE = 'msf.db'
SERVER_ROOT = 'http://localhost:8080'
HTTP_TIMEOUT = 10  # require HTTP replies within this many seconds

def http_get(path):
    """Issues a GET request and returns: (status_code, headers, content)."""
    u = urllib2.urlopen(SERVER_ROOT + path, timeout=HTTP_TIMEOUT)
    return u.getcode(), dict(u.headers), u.read()

def http_post(path, content, headers={}):
    """Issues a POST request and returns: (status_code, headers, content)."""
    req = urllib2.Request(SERVER_ROOT + path, content, headers)
    u = urllib2.urlopen(req, timeout=HTTP_TIMEOUT)
    return u.getcode(), dict(u.headers), u.read()

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
        status_code, headers, content = http_post(
            path, json.dumps(data), {'Content-Type': 'application/json'})
        self.assertEqual(200, status_code)

    def test_json_serialization(self):
        http_post('/patients', 'id=test.1&given_name={"}&status=suspected')

        # Verify that special characters in data don't cause JSON syntax errors.
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        self.assertEqual('{"}', patients[0]['given_name'])

    def test_unicode(self):
        """Test support for unicode in the Patient API."""
        # Add one patient; confirm it appears in the list of all patients.
        unicode_name = quote(u"T\u00F8m".encode("utf8"))
        http_post('/patients', "id=test.1&given_name=%s&status=suspected" % (
            unicode_name,))
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        # Test if the name stayed the same
        self.assertEqual(u"T\u00F8m", patients[0]['given_name'])

        # Test if patients can be retrieved by unicode characters.
        self.assertEqual(1, len(self.get_json('/patients?given_name=%s' % (
            unicode_name,))))
        # Test if patients can be searched by unicode characters
        self.assertEqual(1, len(self.get_json('/patients?search=%s' % (
            unicode_name,))))

    def test_list_patients(self):
        # List an empty database.
        self.assertEqual([], self.get_json('/patients'))

        # Add one patient; confirm it appears in the list of all patients.
        http_post('/patients', 'id=test.1&given_name=Tom&status=suspected')
        self.assertEqual(1, len(self.get_json('/patients')))

        # Test matching on single fields.
        self.assertEqual(0, len(self.get_json('/patients?status=foo')))
        self.assertEqual(1, len(self.get_json('/patients?status=suspected')))
        self.assertEqual(0, len(self.get_json('/patients?given_name=Bob')))
        self.assertEqual(1, len(self.get_json('/patients?given_name=Tom')))

        # Test matching on multiple fields.
        self.assertEqual(0, len(self.get_json(
            '/patients?given_name=Tom&status=foo')))
        self.assertEqual(0, len(self.get_json(
            '/patients?given_name=Bob&status=suspected')))
        self.assertEqual(1, len(self.get_json(
            '/patients?given_name=Tom&status=suspected')))

        # Test searching by substring.
        self.assertEqual(0, len(self.get_json('/patients?search=x')))
        self.assertEqual(1, len(self.get_json('/patients?search=Tom')))
        self.assertEqual(1, len(self.get_json('/patients?search=om')))
        self.assertEqual(1, len(self.get_json('/patients?search=To')))

    def test_add_new_patient(self):
        # TODO(ping): The POST API should take JSON, not form-encoded data.
        # self.post_json('/patients', {'id': 'test.1', 'given_name': 'Tom'})
        http_post('/patients', 'id=test.1&given_name=Tom&status=suspected')

        # Verify that the new patient appears in the list of all patients.
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        self.assertEqual('Tom', patients[0]['given_name'])


if __name__ == '__main__':
    unittest.main()
