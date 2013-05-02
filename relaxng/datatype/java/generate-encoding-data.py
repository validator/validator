#!/usr/bin/python

# Copyright (c) 2013 Mozilla Foundation
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

import json

class Label:
  def __init__(self, label, preferred):
    self.label = label
    self.preferred = preferred
  def __cmp__(self, other):
    return cmp(self.label, other.label)

preferred = []

labels = []

data = json.load(open("encodings.json", "r"))

for group in data:
  for encoding in group["encodings"]:
    preferred.append(encoding["name"])
    for label in encoding["labels"]:
      labels.append(Label(label, encoding["name"]))

preferred.sort()
labels.sort()

print """/*
 * Copyright (c) 2013 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

/*
 * THIS IS A GENERATED FILE. PLEASE DO NOT EDIT.
 * Instead, please regenerate using generate-encoding-data.py
 */

package org.whattf.datatype.data;

import java.util.Arrays;

public class CharsetData {

    private static final String[] LABELS = {"""

for label in labels:
  print "        \"%s\"," % label.label

print """    };
    
    private static final String[] PREFERRED_FOR_LABELS = {"""

for label in labels:
  print "        \"%s\"," % label.preferred

print """    };

    private static final String[] PREFERRED = {"""

for label in preferred:
  print "        \"%s\"," % label
        
print """    };

    public static boolean isPreferred(String label) {
        return Arrays.binarySearch(PREFERRED, label) > -1;
    }
    
    public static String preferredForLabel(String label) {
        int index = Arrays.binarySearch(LABELS, label);
        if (index < 0) {
            return null;
        }
        return PREFERRED_FOR_LABELS[index];
    }
}"""
