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
import string
import gzip
import StringIO
import urllib

for line in sys.stdin:
    try:
        url = line.strip()

        status = 302
        redirectCount = 0

        while (status == 302 or status == 301 or status == 307) and redirectCount < 10:
            if redirectCount > 0:
                url = response.getheader('Location')
            parsed = urlparse.urlsplit(url)
            if parsed[0] != 'http':
                sys.stderr.write('URI scheme %s not supported.\n' % parsed[0])
            if redirectCount > 0:
                connection.close()  # previous connection
            connection = httplib.HTTPConnection(parsed[1])
            connection.connect()
            connection.putrequest("GET", "%s?%s" % (parsed[2], parsed[3]), skip_accept_encoding=1)
            connection.putheader("Accept", 'application/xhtml+xml, */*; q=0.1')
            connection.putheader("Host", 'parsed[1]')
            connection.endheaders()
            response = connection.getresponse()
            status = response.status
            redirectCount += 1

        if status != 200:
            sys.stderr.write('%s %s %s\n' % (status, response.reason, url))
            continue

        if response.getheader('Content-Type', 'text/html').lower().strip().startswith('application/xhtml+xml'):
            sys.stdout.write(url)

        connection.close()
    except:
        pass
