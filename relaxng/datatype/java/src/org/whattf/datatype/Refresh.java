/*
 * Copyright (c) 2008 Mozilla Foundation
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

public final class Refresh extends IriRef {

    /**
     * The singleton instance.
     */
    public static final Refresh THE_INSTANCE = new Refresh();

    private Refresh() {
        super();
    }

    private enum State {
        AT_START, DIGIT_SEEN, SEMICOLON_SEEN, SPACE_SEEN, U_SEEN, R_SEEN, L_SEEN, EQUALS_SEEN

    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            throw newDatatypeException("Empty literal.");
        }
        State state = State.AT_START;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            switch (state) {
                case AT_START:
                    if (isAsciiDigit(c)) {
                        state = State.DIGIT_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a digit, but saw ", c, " instead.");
                    }
                case DIGIT_SEEN:
                    if (isAsciiDigit(c)) {
                        continue;
                    } else if (c == ';') {
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a digit or a semicolon, but saw ", c,
                                " instead.");
                    }
                case SEMICOLON_SEEN:
                    if (isWhitespace(c)) {
                        state = State.SPACE_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a space character, but saw ", c,
                                " instead.");
                    }
                case SPACE_SEEN:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (c == 'u' || c == 'U') {
                        state = State.U_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(
                                i,
                                "Expected a space character or the letter \u201Cu\u201D, but saw ",
                                c, " instead.");
                    }
                case U_SEEN:
                    if (c == 'r' || c == 'R') {
                        state = State.R_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected the letter \u201Cr\u201D, but saw ",
                                c, " instead.");
                    }
                case R_SEEN:
                    if (c == 'l' || c == 'L') {
                        state = State.L_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected the letter \u201Cl\u201D, but saw ",
                                c, " instead.");
                    }
                case L_SEEN:
                    if (c == '=') {
                        state = State.EQUALS_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected \u201C=\u201D, but saw ", c,
                                " instead.");
                    }
                case EQUALS_SEEN:
                    super.checkValid(literal.subSequence(i, literal.length()));
                    return;
            }
        }
        switch (state) {
            case AT_START:
                throw newDatatypeException("Expected a digit, but the literal ended.");
            case DIGIT_SEEN:
                return;
            case SEMICOLON_SEEN:
                throw newDatatypeException("Expected a space character, but the literal ended.");
            case SPACE_SEEN:
                throw newDatatypeException("Expected a space character or the letter \u201Cu\u201D, but the literal ended.");
            case U_SEEN:
                throw newDatatypeException("Expected the letter \u201Cr\u201D, but the literal ended.");
            case R_SEEN:
                throw newDatatypeException("Expected the letter \u201Cl\u201D, but the literal ended.");
            case L_SEEN:
                throw newDatatypeException("Expected \u201C=\u201D, but the literal ended.");
            case EQUALS_SEEN:
                throw newDatatypeException("Expected an IRI reference, but the literal ended.");
        }
    }

    @Override
    public String getName() {
        return "refresh";
    }

}
