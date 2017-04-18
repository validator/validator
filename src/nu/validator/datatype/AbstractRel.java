/*
 * Copyright (c) 2011-2014 Mozilla Foundation
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

package nu.validator.datatype;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import java.util.HashMap;
import java.util.Map;

import org.relaxng.datatype.DatatypeException;

abstract class AbstractRel extends AbstractDatatype {

    private static final Pattern CURIE = Pattern.compile(
            "(([[:A-Z_a-z\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u02FF\u0370-\u037D\u037F-\u1FFF\u200C-\u200D\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]][[-.0-9:A-Z_a-z\u00B7\u00C0-\u00D6\u00D8-\u00F6\u00F8-\u037D\u037F-\u1FFF\u200C-\u200D\u203F\u2040\u2070-\u218F\u2C00-\u2FEF\u3001-\uD7FF\uF900-\uFDCF\uFDF0-\uFFFD]]*)?:)[^ ]*");

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        // There are currently no registered rel tokens with a colon in them
        // so don't bother supporting the colon case until there are
        // registered tokens with a colon.
        Set<String> tokensSeen = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        int len = literal.length();
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (isWhitespace(c) && builder.length() > 0) {
                checkToken(literal, builder, i, tokensSeen);
                builder.setLength(0);
            } else if (!isWhitespace(c)) {
                builder.append(c);
            }
        }
        if (builder.length() > 0) {
            checkToken(literal, builder, len, tokensSeen);
        }
    }

    private void checkToken(CharSequence literal, StringBuilder builder, int i,
            Set<String> tokensSeen) throws DatatypeException {
        String token = builder.toString();
        if (tokensSeen.contains(token)) {
            throw newDatatypeException(i - 1, "Duplicate keyword ", token, ".");
        }
        tokensSeen.add(token);
        if (!isRegistered(literal, token)) {
            if ("1".equals(
                    System.getProperty("nu.validator.schema.rdfa-full"))) {
                if (!CURIE.matcher(token).matches()) {
                    errNotRegistered(i - 1, token);
                }
            } else {
                errNotRegistered(i - 1, token);
            }
        }
    }

    protected void errSynonym(String token, Map<String, String> map)
            throws DatatypeException {
        // Synonyms for current keywords
        for (Map.Entry m : map.entrySet()) {
            if (token.toLowerCase().equals(m.getKey())) {
                throw newDatatypeException("The keyword \u201c" + m.getKey()
                        + "\u201d for the \u201crel\u201d"
                        + " attribute should not be used."
                        + " Consider using \u201c" + m.getValue()
                        + "\u201d instead.", true);
            }
        }
    }

    private void errNotRegistered(int position, String token)
            throws DatatypeException {
        throw newDatatypeException(position, "The string ", token,
                " is not a registered keyword.");
    }

    protected abstract boolean isRegistered(CharSequence literal, String token)
            throws DatatypeException;

}
