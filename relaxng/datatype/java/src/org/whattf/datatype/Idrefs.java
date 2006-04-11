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

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

public class Idrefs extends AbstractDatatype {

    public Idrefs() {
        super();
    }

    public void checkValid(String literal, ValidationContext context) throws DatatypeException {
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            if (!(c == ' ' || c == '\t' || c == '\n' || c == '\r')) {
                return;
            }
        }
        throw new DatatypeException("An IDREFS value must contain at least one non-whitespace character.");
    }   
    
    /**
     * @see org.whattf.datatype.Id#getIdType()
     */
    public int getIdType() {
        return Datatype.ID_TYPE_IDREFS;
    }
}
