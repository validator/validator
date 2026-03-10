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

import nu.validator.vendor.relaxng.datatype.DatatypeException;

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

    private static boolean isDisallowedInCustomElementName(int c) {
        return (c >= 'A' && c <= 'Z') //
                || c == ' ' || c == '\t' || c == '\n' //
                || c == '\f' || c == '\r' //
                || c == 0x00 //
                || c == '/' //
                || c == '>';
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
            if (isDisallowedInCustomElementName(c)) {
                if (c >= 'A' && c <= 'Z') {
                    throw newDatatypeException( //
                            "Uppercase ASCII letters are not allowed.");
                }
                throw newDatatypeException(
                    String.format("Code point “%s” is not allowed",
                            String.format("U+%04x", c).toUpperCase()));
            }
            if (c == '-') {
                state = State.EXPECTING_PNECHAR;
            }
        }
        if (state == State.EXPECTING_HYPHEN) {
            throw newDatatypeException(
                    "Must contain \"“-”\" (hyphen)");
        }
        if (Arrays.binarySearch(PROHIBITED_NAMES, literal) > -1) {
            throw newDatatypeException(String.format(
                    "Element name “%s” is not allowed.", literal));
        }
    }

    @Override
    public String getName() {
        return "custom element name";
    }

}
