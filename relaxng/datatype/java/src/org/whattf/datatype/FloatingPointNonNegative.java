/*
 * Copyright (c) 2007 Mozilla Foundation
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

public class FloatingPointNonNegative extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final FloatingPointNonNegative THE_INSTANCE = new FloatingPointNonNegative();
    
    /**
     * 
     */
    private FloatingPointNonNegative() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            throw newDatatypeException("The empty string is not a valid non-negative floating point number.");
        }
        boolean dotSeen = false;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            if (!dotSeen && (c == '.')) {
                dotSeen = true;
            } else if (!isAsciiDigit(c)) {
                if (dotSeen) {
                    throw newDatatypeException(i, "Expected a digit but saw ", c, " instead.");                           
                } else {
                    throw newDatatypeException(i, "Expected a digit or a dot but saw ", c, " instead.");                                               
                }
            }
        }
    }

    @Override
    public String getName() {
        return "non-negative floating point number";
    }

}
