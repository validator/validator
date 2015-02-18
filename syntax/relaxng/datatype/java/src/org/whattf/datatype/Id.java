/*
 * Copyright (c) 2006 Henri Sivonen
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

/**
 * This datatype shall accept any string that consists of one or more characters 
 * and does not contain any whitespace characters.
 * <p>The ID-type of this datatype is ID.
 * @version $Id$
 * @author hsivonen
 */
public class Id extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Id THE_INSTANCE = new Id();

    /**
     * Package-private constructor
     */
    protected Id() {
        super();
    }

    /**
     * Checks that the value is a proper HTML5 id.
     * @param literal the value
     * @param context ignored
     * @throws DatatypeException if the value isn't valid
     * @see org.relaxng.datatype.Datatype#checkValid(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    public void checkValid(CharSequence literal)
            throws DatatypeException {
        int len = literal.length();
        if (len == 0) {
            throw newDatatypeException("An ID must not be the empty string.");
        }
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (isWhitespace(c)) {
                throw newDatatypeException(i, "An ID must not contain whitespace.");
            }
        }
    }

    @Override
    public String getName() {
        return "id";
    }
}
