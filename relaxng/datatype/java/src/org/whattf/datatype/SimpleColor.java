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

import org.relaxng.datatype.DatatypeException;

public class SimpleColor extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final SimpleColor THE_INSTANCE = new SimpleColor();

    /**
     * Package-private constructor
     */
    private SimpleColor() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        if (literal.length() != 7) {
            throw newDatatypeException("Incorrect length for color string.");
        }
        char c = literal.charAt(0);
        if (c != '#') {
            throw newDatatypeException(0,
                    "Color starts with incorrect character ", c,
                    ". Expected the number sign.");
        }
        for (int i = 1; i < 7; i++) {
            c = literal.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))) {
                throw newDatatypeException(0, "", c,
                        " is not a valid hexadecimal digit.");
            }
        }
    }

    @Override public String getName() {
        return "simple color";
    }

}
