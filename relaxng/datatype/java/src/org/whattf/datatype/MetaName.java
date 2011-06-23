/*
 * Copyright (c) 2011 Mozilla Foundation
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

import java.util.Arrays;

import org.relaxng.datatype.DatatypeException;

public class MetaName extends AbstractDatatype {

    private static final String[] VALID_NAMES = {
        "apple-mobile-web-app-capable", // extension
        "apple-mobile-web-app-status-bar-style", // extension
        "application-name",
        "author",
        "baiduspider", // extension
        "csrf-param", // extension
        "csrf-token", // extension
        "dcterms.creator", // extension
        "dcterms.description", // extension
        "dcterms.issued", // extension
        "dcterms.language", // extension
        "dcterms.modified", // extension
        "dcterms.subject", // extension
        "dcterms.title", // extension
        "description",
        "format-detection", // extension
        "generator",
        "google-site-verification", // extension
        "googlebot", // extension
        "keywords",
        "msvalidate.01", // extension
        "rating", // extension
        "robots", // extension
        "slurp", // extension
        "teoma", // extension
        "verify-v1", // extension
        "viewport", // extension
        "yandex-verification" // extension
    };
    
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
        if (Arrays.binarySearch(VALID_NAMES, token) < 0) {
            throw newDatatypeException("Keyword ", token, " is not registered.");
        }
    }

    @Override public String getName() {
        return "metadata name";
    }

}
