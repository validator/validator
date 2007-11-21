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

import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.regexp.RegExpImpl;
import org.relaxng.datatype.DatatypeException;

/**
 * This datatype shall accept the strings that are allowed as the value of the Web Forms 2.0 
 * <a href="http://whatwg.org/specs/web-forms/current-work/#pattern"><code>pattern</code></a> 
 * attribute.
 * @version $Id$
 * @author hsivonen
 */
public final class Pattern extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Pattern THE_INSTANCE = new Pattern();

    /**
     * Package-private constructor
     */
    private Pattern() {
        super();
    }

    /**
     * Checks that the value compiles as an anchored JavaScript regular expression.
     * @param literal the value
     * @param context ignored
     * @throws DatatypeException if the value isn't valid
     * @see org.relaxng.datatype.Datatype#checkValid(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    public void checkValid(CharSequence literal)
            throws DatatypeException {
        // TODO find out what kind of thread concurrency guarantees are made
        RegExpImpl rei = new RegExpImpl();
        String anchoredRegex = "^(?:" + literal + ")$";
        try {
            rei.compileRegExp(null, anchoredRegex, "");
        } catch (EcmaError ee) {
            throw newDatatypeException(ee.getErrorMessage());
        }
    }

    @Override
    public String getName() {
        return "pattern";
    }
}
