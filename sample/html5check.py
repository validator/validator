#!/usr/bin/python

# Copyright (c) 2007 Mozilla Foundation
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

argv = sys.argv[1:]

forceXml = 0
forceHtml = 0
encoding = None
fileName = None
contentType = None
inputHandle = None

for arg in argv:
  if '--help' == arg:
    print '-h : force text/html'
    print '-x : force application/xhtml+xml'
    print '--encoding=foo : declare encoding foo'
    print 'One file argument allowed. Leave out to read from stdin.' 
    sys.exit(0)
  elif '-x' == arg:
    forceXml = 1
  elif '-h' == arg:
    forceHtml = 1
  elif arg.startswith("--encoding="):
    encoding = arg[11:]
  else:
    if fileName:
      sys.stderr.write('Cannot have more than one input file.\n')
      sys.exit(1)
    fileName = arg
    
if forceXml and forceHtml:
  sys.stderr.write('Cannot force HTML and XHTML at the same time.\n')
  sys.exit(2)
  
if forceXml:
  contentType = 'application/xhtml+xml'
elif forceHtml:
  contentType = 'text/html'
elif fileName:
  if fileName.endswith('.xhtml'):
    contentType = 'application/xhtml+xml'
  elif fileName.endswith('.html'):
    contentType = 'text/html'
  else:
    sys.stderr.write('Unable to guess Content-Type from file name. Please force the type.\n')
    sys.exit(3)
else:
  sys.stderr.write('Need to force HTML or XHTML when reading from stdin.\n')
  sys.exit(4)

if encoding:
  contentType = '%s; charset=%s' % (contentType, encoding)

if fileName:
  inputHandle = open(fileName, "rb")
else:
  inputHandle = sys.stdin

data = inputHandle.read()
  
connection = httplib.HTTPConnection('html5.validator.nu')
connection.connect()
connection.putrequest("POST", "/?out=text", skip_accept_encoding=1)
connection.putheader("Content-Type", contentType)
connection.putheader("Content-Length", len(data))
connection.endheaders()
connection.send(data)

response = connection.getresponse()

#XXX handle redirects

if response.status != 200:
  sys.stderr.write('%s %s\n' % (response.status, response.reason))
  sys.exit(5)

sys.stderr.write(response.read())

connection.close()
