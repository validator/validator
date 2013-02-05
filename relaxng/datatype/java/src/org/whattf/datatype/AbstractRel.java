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

import java.util.HashSet;
import java.util.Set;

import org.relaxng.datatype.DatatypeException;

abstract class AbstractRel extends AbstractDatatype {

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        // There are currently no registered rel tokens with a colon in them
        // so don't bother supporting the colon case until there are 
        // registered tokens with a colon.
        Set<String> tokensSeen = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        int len = literal.length();
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (isWhitespace(c) && builder.length() > 0) {
                checkToken(builder, i, tokensSeen);
                builder.setLength(0);
            } else {
                builder.append(toAsciiLowerCase(c));
            }
        }
        if (builder.length() > 0) {
            checkToken(builder, len, tokensSeen);
        }
    }

    private void checkToken(StringBuilder builder, int i, Set<String> tokensSeen) throws DatatypeException {
        String token = builder.toString();
        if (tokensSeen.contains(token)) {
            throw newDatatypeException(i - 1, "Duplicate keyword ", token, ".");
        }
        tokensSeen.add(token);
        if (!isRegistered(token)) {
            try {
                Html5DatatypeLibrary dl = new Html5DatatypeLibrary();
                Iri iri = (Iri) dl.createDatatype("iri");
                iri.checkValid(token);
            } catch (DatatypeException e) {
                throw newDatatypeException(i - 1, "The string ", token,
                        " is not a registered keyword or absolute URL.");
            }
        }
    }

    protected abstract boolean isRegistered(String token);

}
