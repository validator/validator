/*
 * Copyright (c) 2011-2018 Mozilla Foundation
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

public final class ScriptDocumentation extends CdoCdcPair {

    private enum State {
        BEFORE_DOCUMENTATION, SLASH, IN_COMMENT, IN_LINE_COMMENT, STAR,
        LESS_THAN, SLASH_AFTER_LESS_THAN, LETTER_AFTER_SLASH_AFTER_LESS_THAN
    }

    /**
     * The singleton instance.
     */
    public static final ScriptDocumentation THE_INSTANCE = new ScriptDocumentation();

    private ScriptDocumentation() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        State state = State.BEFORE_DOCUMENTATION;
        StringBuilder fauxTagName = new StringBuilder();
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            switch (state) {
                case BEFORE_DOCUMENTATION:
                    switch (c) {
                        case ' ':
                        case '\t':
                        case '\n':
                            continue;
                        case '/':
                            if (i == literal.length() - 1) {
                                throw newDatatypeException(
                                        "Expected asterisk or slash but content"
                                                + " ended with a single slash"
                                                + " instead.");
                            }
                            state = State.SLASH;
                            continue;
                        case '<':
                            fauxTagName.setLength(0);
                            state = State.LESS_THAN;
                            continue;
                        default:
                            throw newDatatypeException(
                                    "Expected space, tab, newline, or slash but"
                                            + " found \u201c" + c
                                            + "\u201d instead.");
                    }
                case SLASH:
                    switch (c) {
                        case '*':
                            state = State.IN_COMMENT;
                            continue;
                        case '/':
                            state = State.IN_LINE_COMMENT;
                            continue;
                        default:
                            throw newDatatypeException(
                                    "Expected asterisk or slash but found \u201c"
                                            + c + "\u201d instead.");
                    }
                case IN_COMMENT:
                    switch (c) {
                        case '*':
                            state = State.STAR;
                            continue;
                        default:
                            continue;
                    }
                case STAR:
                    switch (c) {
                        case '/':
                            state = State.BEFORE_DOCUMENTATION;
                            continue;
                        default:
                            continue;
                    }
                case IN_LINE_COMMENT:
                    switch (c) {
                        case '\n':
                            state = State.BEFORE_DOCUMENTATION;
                            continue;
                        default:
                            continue;
                    }
                case LESS_THAN:
                    switch (c) {
                        case '/':
                            state = State.SLASH_AFTER_LESS_THAN;
                            continue;
                        default:
                            throw newDatatypeException(
                                    "Expected space, tab, newline, or slash but"
                                            + " found \u201c<\u201d instead.");
                    }
                case SLASH_AFTER_LESS_THAN:
                    if (Character.isLetter(c)) {
                        fauxTagName.append(c);
                        state = State.LETTER_AFTER_SLASH_AFTER_LESS_THAN;
                        continue;
                    } else {
                        throw newDatatypeException(
                                "Expected space, tab, newline, or slash but"
                                        + " found \u201c<\u201d instead.");
                    }
                case LETTER_AFTER_SLASH_AFTER_LESS_THAN:
                    if (Character.isLetter(c) || Character.isDigit(c)) {
                        fauxTagName.append(c);
                        continue;
                    } else if (c == '>') {
                        String tagName = fauxTagName.toString();
                        throw newDatatypeException(
                                "Found \u201c</" + tagName
                                        + ">\u201d in \u201cscript\u201d content."
                                        + " Typo for \u201c</script>\u201d?", true);
                    } else {
                        throw newDatatypeException(
                                "Expected space, tab, newline, or slash but"
                                        + " found \u201c<\u201d instead.");
                    }
                default:
                    throw newDatatypeException("Content ended prematurely.");
            }
        }
        if (state == State.IN_LINE_COMMENT) {
            throw newDatatypeException("Content contains a line starting with"
                    + " the character sequence \u201c//\u201d but not ending"
                    + " with a newline.");
        }
        if (state == State.IN_COMMENT || state == State.STAR) {
            throw newDatatypeException("Content contains the character"
                    + " sequence \u201c/*\u201d without a later occurrence of"
                    + " the character sequence \u201c*/\u201d.");
        }
        super.checkValid(literal);
    }

    @Override
    public String getName() {
        return "script documentation";
    }

}
