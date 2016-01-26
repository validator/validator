/*
 * Copyright (c) 2015 Mozilla Foundation
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

import org.relaxng.datatype.DatatypeException;

public final class SandboxAllowList extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final SandboxAllowList THE_INSTANCE = new SandboxAllowList();

    private SandboxAllowList() {
        super();
    }

    private static final HashSet<String> allowedKeywords = new HashSet<>();

    static {
        allowedKeywords.add("allow-forms");
        allowedKeywords.add("allow-modals");
        allowedKeywords.add("allow-pointer-lock");
        allowedKeywords.add("allow-popups");
        allowedKeywords.add("allow-popups-to-escape-sandbox");
        allowedKeywords.add("allow-same-origin");
        allowedKeywords.add("allow-scripts");
        allowedKeywords.add("allow-top-navigation");
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        Set<String> tokensSeen = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        int len = literal.length();
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (isWhitespace(c) && builder.length() > 0) {
                checkToken(literal, builder, i, tokensSeen);
                builder.setLength(0);
            } else if (!isWhitespace(c)) {
                builder.append(toAsciiLowerCase(c));
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
            throw newDatatypeException(i - 1, "Duplicate keyword \u201c"
                    + token + "\u201d.");
        }
        tokensSeen.add(token);
        if (!allowedKeywords.contains(token)) {
            throw newDatatypeException(i - 1, "The string \u201c" + token
                    + "\u201d is not a valid keyword.");
        }
    }

    @Override public String getName() {
        return "sandbox allow list";
    }
}
