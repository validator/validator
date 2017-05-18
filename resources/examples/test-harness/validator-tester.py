#!/usr/bin/python

# Copyright (c) 2007-2008 Mozilla Foundation
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.

import httplib
import os
import sys
import re
import urlparse
import urllib
import string
import gzip
import StringIO
import simplejson
import shutil


class Location:
    def __init__(self, line, column):
        self.line = line
        self.column = column

    def __cmp__(self, other):
        c = cmp(self.line, other.line)
        if c:
            return c
        else:
            return cmp(self.column, other.column)


class ValidationErrorMessage:
    def __init__(self, json):
        self.fatal = 0
        if 'subtype' in json and json['subtype'] == 'fatal':
            self.fatal = 1
        self.message = json.get('message', None)
        # XXX last line and column required
        self.last = Location(json['lastLine'], json['lastColumn'])
        if 'firstLine' in json and 'firstColumn' in json:
            self.first = Location(json['firstLine'], json['firstColumn'])
        elif 'firstColumn' in json:
            self.first = Location(json['lastLine'], json['firstColumn'])
        else:
            self.first = self.last

    def __cmp__(self, other):
        c = cmp(self.last, other.last)
        if c:
            return c
        else:
            return cmp(self.first, other.first)

    def contains(self, other):
        return (other.last <= self.last) and \
            (other.first <= self.last) and \
            (other.first >= self.first) and \
            (other.last >= self.first)

    def toDict(self):
        return {'message': self.message,
                'lastLine': self.last.line,
                'lastColumn': self.last.column,
                'firstLine': self.first.line,
                'firstColumn': self.first.column}

    def __repr__(self):
        return repr(self.toDict())

    def __unicode__(self):
        return u"%d,%d;%d,%d: %s" % (self.first.line,
                                     self.first.column,
                                     self.last.line,
                                     self.last.column,
                                     self.message)


class ValidationErrorMessageEncoder(simplejson.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, ValidationErrorMessage):
            return obj.toDict()
        return simplejson.JSONEncoder.default(self, obj)


class ValidatorTester:
    def __init__(self, dbpath, serviceuri):
        self.dbpath = dbpath
        self.serviceuri = serviceuri
        f = open(dbpath, 'rb')
        self.database = self.loadDatabase(f)
        f.close()

    def dump(self):
        tempPath = self.dbpath + '.tmp'
        f = open(tempPath, 'wb')
        self.dumpDatabase(f, self.database)
        f.close()
        shutil.move(tempPath, self.dbpath)

    def errorsForUri(self, uri):
        connection = None
        response = None
        status = 302
        redirectCount = 0
        url = self.serviceuri + '?out=json&' + urllib.urlencode({'doc': uri})

        while (status == 302 or status == 301 or status == 307) and redirectCount < 10:
            if redirectCount > 0:
                url = response.getheader('Location')
            parsed = urlparse.urlsplit(url)
            if parsed[0] != 'http':
                sys.stderr.write('URI scheme %s not supported.\n' % parsed[0])
                sys.exit(7)
            if redirectCount > 0:
                connection.close()  # previous connection
            connection = httplib.HTTPConnection(parsed[1])
            connection.connect()
            connection.putrequest("GET", "%s?%s" % (parsed[2], parsed[3]), skip_accept_encoding=1)
            connection.putheader("Accept-Encoding", 'gzip')
            connection.endheaders()
            response = connection.getresponse()
            status = response.status
            redirectCount += 1

        if status != 200:
            sys.stderr.write('%s %s\n' % (status, response.reason))
            sys.exit(5)

        if response.getheader('Content-Encoding', 'identity').lower() == 'gzip':
            response = gzip.GzipFile(fileobj=StringIO.StringIO(response.read()))
        jsonResp = simplejson.load(response)

        connection.close()

        rv = []

        for msg in jsonResp["messages"]:
            if msg['type'] == "info":
                pass
            elif msg['type'] == "error":
                if 'lastLine' in msg and 'lastColumn' in msg:
                    rv.append(ValidationErrorMessage(msg))
            else:
                raise Exception()  # XXX

        rv.sort()
        return rv

    def loadDatabase(self, handle):
        jsonRep = simplejson.load(handle)
        database = {}
        for uri, errors in jsonRep.iteritems():
            list = map(lambda e: ValidationErrorMessage(e), errors)
            list.sort()
            database[uri] = list
        return database

    def dumpDatabase(self, handle, database):
        simplejson.dump(database, handle, cls=ValidationErrorMessageEncoder, sort_keys=True, indent=2)
        handle.write('\n')

# User-facing commands

    def dumpReference(self, uri, handle):
        simplejson.dump({uri: self.database[uri]}, handle, cls=ValidationErrorMessageEncoder, sort_keys=True, indent=2)
        handle.write('\n')
        handle.close()

    def dumpUri(self, uri, handle):
        simplejson.dump({uri: self.errorsForUri(uri)}, handle, cls=ValidationErrorMessageEncoder, sort_keys=True, indent=2)
        handle.write('\n')
        handle.close()

    def addToDatabase(self, uri):
        self.database[uri] = self.errorsForUri(uri)
        self.dump()

    def deleteFromDatabase(self, uri):
        self.database.pop(uri)
        self.dump()

    def mergeToDatabase(self, handle):
        db = self.loadDatabase(handle)
        self.database.update(db)
        self.dump()

    def checkUri(self, uri):
        actualErrs = self.errorsForUri(uri)
        expectedErrs = self.database[uri]
        if len(actualErrs) == 0 and len(expectedErrs) == 0:
            return
        elif len(expectedErrs) == 0:
            print (u"%s Expected no errors but saw: %s" % (uri, unicode(actualErrs[0]))).encode('utf-8')
        elif len(actualErrs) == 0:
            print (u"%s Expected %s but saw no errors." % (uri, unicode(expectedErrs[0]))).encode('utf-8')
        elif expectedErrs[0].contains(actualErrs[0]):
            return
        else:
            print (u"%s Expected %s but saw %s." % (uri, unicode(expectedErrs[0]), str(actualErrs[0]))).encode('utf-8')

    def checkAll(self):
        for uri in self.database.iterkeys():
            self.checkUri(uri)

# End user-facing commands

    def runCommandLine(self, argv):
        if argv[0] == 'dumpref':
            self.dumpReference(argv[1], self.argsToHandle(argv[2:], 0))
        elif argv[0] == 'dumpuri':
            self.dumpUri(argv[1], self.argsToHandle(argv[2:], 0))
        elif argv[0] == 'adduri':
            self.addToDatabase(argv[1])
        elif argv[0] == 'deluri':
            self.deleteFromDatabase(argv[1])
        elif argv[0] == 'checkuri':
            self.checkUri(argv[1])
        elif argv[0] == 'checkall':
            self.checkAll()
        elif argv[0] == 'mergedb':
            self.mergeToDatabase(self.argsToHandle(argv[1:], 1))
        else:
            raise Exception("Unknown command %s" % argv[0])

    def argsToHandle(self, argv, input):
        if len(argv) == 0:
            if input:
                return sys.stdin
            else:
                return sys.stdout
        elif len(argv) == 1:
            if input:
                return open(argv[0], 'rb')
            else:
                return open(argv[0], 'wb')
        else:
            raise Exception("Too many arguments.")


def main():
    dbfile = 'db.json'
    serviceuri = 'http://html5.validator.nu/'
    argv = sys.argv[1:]
    while len(argv) > 0 and argv[0].startswith('--'):
        if argv[0].startswith('--db='):
            dbfile = argv[0][5:]
            argv = argv[1:]
        elif argv[0].startswith('--service='):
            serviceuri = argv[0][10:]
            argv = argv[1:]
        else:
            raise Exception("Unknown argument %s" % argv[0])
    vt = ValidatorTester(dbfile, serviceuri)
    vt.runCommandLine(argv)


if __name__ == "__main__":
    main()
