/*
 * Copyright (c) 2011-2014 Mozilla Foundation
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

package org.whattf.datatype;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashSet;

import org.relaxng.datatype.DatatypeException;

public class MetaName extends AbstractDatatype {

    private static final HashSet<String> registeredMetaNames = new HashSet<String>();

    static {
        // Standard metadata names from the spec
        registeredMetaNames.add("application-name");
        registeredMetaNames.add("author");
        registeredMetaNames.add("description");
        registeredMetaNames.add("generator");
        registeredMetaNames.add("keywords");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                MetaName.class.getClassLoader().getResourceAsStream(
                        "nu/validator/localentities/files/meta-name-extensions")));
        // Read in registered metadata names from cached copy of the registry
        try {
            String read = br.readLine();
            while (read != null) {
                registeredMetaNames.add(read);
                read = br.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The singleton instance.
     */
    public static final MetaName THE_INSTANCE = new MetaName();

    /**
     * Package-private constructor
     */
    private MetaName() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        String token = toAsciiLowerCase(literal);
        if ("".equals(literal)) {
            throw newDatatypeException("The empty string is not a valid keyword.");
        }
        if (!registeredMetaNames.contains(token)) {
            throw newDatatypeException("Keyword ", token, " is not registered.");
        }
    }

    @Override public String getName() {
        return "metadata name";
    }

}
