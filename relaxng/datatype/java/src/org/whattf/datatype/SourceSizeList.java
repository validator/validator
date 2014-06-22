/*
 * Copyright (c) 2014 Mozilla Foundation
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

public class SourceSizeList extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final SourceSizeList THE_INSTANCE = new SourceSizeList();

    protected SourceSizeList() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        int offset = 0;
        StringBuilder unparsedSize = new StringBuilder();
        StringBuilder extract = new StringBuilder();
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            extract.append(c);
            if (',' == c) {
                unparsedSize.append(literal.subSequence(offset, i));
                checkSize(unparsedSize, extract);
                unparsedSize.setLength(0);
                offset = i + 1;
            }
        }
        unparsedSize.append(literal.subSequence(offset, literal.length()));
        checkSize(unparsedSize, extract);
    }

    private void checkSize(StringBuilder unparsedSize, StringBuilder extract) {
        trimTrailingWhitespace(unparsedSize);
        if (unparsedSize.length() == 0) {
            // error empty
            return;
        }
    }

    private void trimTrailingWhitespace(StringBuilder sb) {
        for (int i = sb.length(); i > 0; i--) {
            char c = sb.charAt(i - 1);
            if (' ' == c || '\t' == c || '\n' == c || '\f' == c || '\r' == c) {
                sb.setLength(i - 1);
            } else {
                return;
            }
        }
    }

    @Override public String getName() {
        return "source size list";
    }

}
