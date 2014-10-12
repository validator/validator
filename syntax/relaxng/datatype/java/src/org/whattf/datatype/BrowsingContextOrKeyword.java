/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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

public class BrowsingContextOrKeyword extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final BrowsingContextOrKeyword THE_INSTANCE = new BrowsingContextOrKeyword();

    /**
     * 
     */
    private BrowsingContextOrKeyword() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            throw newDatatypeException("Browsing context name must be at least one character long.");
        }
        if (literal.charAt(0) == '_') {
            String kw = toAsciiLowerCase(literal.toString().substring(1));
            if (!("blank".equals(kw) || "self".equals(kw) || "top".equals(kw) || "parent".equals(kw))) {
                throw newDatatypeException("Reserved keyword ", kw, " used.");
            }
        } else {
            return;
        }
    }

    @Override
    public String getName() {
        return "browsing context name or keyword";
    }

}
