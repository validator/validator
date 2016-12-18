/*
 * Copyright (c) 2016 Mozilla Foundation
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

import org.relaxng.datatype.DatatypeException;

import java.util.Arrays;

public final class CustomElementName extends AbstractDatatype {

    private enum State {
        EXPECTING_LOWERCASE_ALPHA, EXPECTING_HYPHEN, EXPECTING_PNECHAR
    }

    private String[] PROHIBITED_NAMES = { //
            "annotation-xml", //
            "color-profile", //
            "font-face", //
            "font-face-format", //
            "font-face-name", //
            "font-face-src", //
            "font-face-uri", //
            "missing-glyph" //
    };

    public static boolean isPCENChar(int c) {
        return (c == '-' //
                || c == '.' //
                || (c >= '0' && c <= '9') //
                || c == '_' //
                || (c >= 'a' && c <= 'z') //
                || c == 0xB7 //
                || (c >= 0xC0 && c <= 0xD6) //
                || (c >= 0xD8 && c <= 0xF6) //
                || (c >= 0xF8 && c <= 0x37D) //
                || (c >= 0x37F && c <= 0x1FFF) //
                || (c >= 0x37F && c <= 0x1FFF) //
                || (c >= 0x200C && c <= 0x200D) //
                || (c >= 0x203F && c <= 0x2040) //
                || (c >= 0x203F && c <= 0x2040) //
                || (c >= 0x2070 && c <= 0x218F) //
                || (c >= 0x2C00 && c <= 0x2FEF) //
                || (c >= 0x3001 && c <= 0xD7FF) //
                || (c >= 0xF900 && c <= 0xFDCF) //
                || (c >= 0xFDF0 && c <= 0xFFFD) //
                || (c >= 0x10000 && c <= 0xEFFFF) //
        );
    }

    /**
     * The singleton instance.
     */
    public static final CustomElementName THE_INSTANCE = new CustomElementName();

    private CustomElementName() {
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        State state = State.EXPECTING_LOWERCASE_ALPHA;
        if (literal.length() == 0) {
            throw newDatatypeException("Must be non-empty.");
        }
        for (int c : literal.codePoints().toArray()) {
            if (state == State.EXPECTING_LOWERCASE_ALPHA) {
                if (c >= 'a' && c <= 'z') {
                    state = State.EXPECTING_HYPHEN;
                    continue;
                } else {
                    throw newDatatypeException(
                            "Must begin with a lowercase ASCII letter.");
                }
            }
            if (c >= 'A' && c <= 'Z') {
                throw newDatatypeException( //
                        "Uppercase ASCII letters are not allowed.");
            }
            if (c == '-') {
                state = State.EXPECTING_PNECHAR;
                continue;
            }
            if (isPCENChar(c)) {
                continue;
            }
            throw newDatatypeException(
                    String.format("Code point \u201c%s\u201d is not allowed",
                            String.format("U+%04x", c).toUpperCase()));
        }
        if (state == State.EXPECTING_HYPHEN) {
            throw newDatatypeException(
                    "Must contain \"\u201c-\u201d\" (hyphen)");
        }
        if (Arrays.binarySearch(PROHIBITED_NAMES, literal) > -1) {
            throw newDatatypeException(String.format(
                    "Element name \u201c%s\u201d is not allowed.", literal));
        }
    }

    @Override
    public String getName() {
        return "custom element name";
    }

}
