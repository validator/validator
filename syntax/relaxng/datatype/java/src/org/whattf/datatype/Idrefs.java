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
 * This datatype shall accept any string that consists of one or more characters and 
 * contains at least one character that is not a whitespace character.
 * <p>The ID-type of this datatype is IDREFS.
 * @version $Id$
 * @author hsivonen
 */
public final class Idrefs extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Idrefs THE_INSTANCE = new Idrefs();
    
    /**
     * Package-private constructor
     */
    private Idrefs() {
        super();
    }

    /**
     * Checks that the value is a proper list of HTML5 ids.
     * @param literal the value
     * @param context ignored
     * @throws DatatypeException if the value isn't valid
     * @see org.relaxng.datatype.Datatype#checkValid(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    public void checkValid(CharSequence literal) throws DatatypeException {
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            if (!isWhitespace(c)) {
                return;
            }
        }
        throw newDatatypeException("An IDREFS value must contain at least one non-whitespace character.");
    }   

    @Override
    public String getName() {
        return "id references";
    }
}
