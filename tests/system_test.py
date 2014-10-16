#!/usr/bin/env python

import json
import sqlite3
import unittest
import urllib2

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
    tables = c.execute('select tbl_name from sqlite_master where type="table"')
    for table in tables:
        c.execute('delete from %s' % table)
    c.close()


class SystemTest(unittest.TestCase):
    def setUp(self):
        reset_db()

    def get_json(self, path):
        """Issues a GET request and decodes the response as JSON."""
        status_code, headers, content = http_get(path)
        self.assertEqual(200, status_code)
        return json.loads(content)

    def post_json(self, path, data):
        """Issues a POST request containing the given data encoded in JSON."""
        status_code, headers, content = http_post(
            path, json.dumps(data), {'Content-Type': 'application/json'})
        self.assertEqual(200, status_code)

    def test_list_patients(self):
        self.assertEqual([], self.get_json('/patients'))

    def test_add_new_patient(self):
        self.post_json('/patients', {'id': 'test.1', 'given_name': 'Tom'})

        # Verify that the new patient appears in the list of all patients.
        patients = self.get_json('/patients')
        self.assertEqual(1, len(patients))
        self.assertEqual('Tom', patients[0]['given_name'])

        # Verify that the new patient can be retrieved by ID.
        patient = self.get_json('/patients/test.1')
        self.assertEqual('Tom', patient['given_name'])


if __name__ == '__main__':
    unittest.main()
