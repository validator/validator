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

public class Int extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Int THE_INSTANCE = new Int();
    
    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            throw newDatatypeException("The empty string is not a valid integer.");
        }
        char c = literal.charAt(0);
        if (!(c == '-' || isAsciiDigit(c))) {
            throw newDatatypeException(0, "Expected a minus sign or a digit but saw ", c, " instead.");            
        }
        for (int i = 1; i < literal.length(); i++) {
            c = literal.charAt(i);
            if (!isAsciiDigit(c)) {
                throw newDatatypeException(i, "Expected a digit but saw ", c, " instead.");                            
            }
        }
    }

    @Override
    public String getName() {
        return "integer";
    }

}
