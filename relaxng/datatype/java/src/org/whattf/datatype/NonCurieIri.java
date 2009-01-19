/*
 * Copyright (c) 2009 Mozilla Foundation
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
import org.relaxng.datatype.ValidationContext;


public class NonCurieIri extends Iri {

    /**
     * The singleton instance.
     */
    public static final NonCurieIri THE_INSTANCE = new NonCurieIri();
    
    private NonCurieIri() {
        super();
    }

    @Override
    public String getName() {
        return "non-CURIE IRI";
    }

    /**
     * @see org.whattf.datatype.AbstractDatatype#isContextDependent()
     */
    @Override public boolean isContextDependent() {
        return true;
    }

    /**
     * @see org.whattf.datatype.AbstractDatatype#checkValid(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    @Override public void checkValid(String literal, ValidationContext context)
            throws DatatypeException {
        int index = literal.indexOf(':');
        if (index > -1) {
            String prefix = literal.substring(0, index);
            String mapping = context.resolveNamespacePrefix(prefix);
            if (mapping != null && !((mapping.length() == prefix.length() + 1) && mapping.startsWith(prefix) && mapping.charAt(prefix.length()) == ':')) {
                throw newDatatypeException(index, "The prefix ", prefix, " has a namespace mapping in this context.");
            }
        }
        super.checkValid(literal);
    }
}
