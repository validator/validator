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

import org.relaxng.datatype.DatatypeException;

public final class ScriptDocumentation extends Script {

    private enum State {
        BEFORE_DOCUMENTATION, SLASH, IN_COMMENT, IN_LINE_COMMENT, STAR
    }

    /**
     * The singleton instance.
     */
    public static final ScriptDocumentation THE_INSTANCE = new ScriptDocumentation();

    private ScriptDocumentation() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        State state = State.BEFORE_DOCUMENTATION;
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
                                throw newDatatypeException("Expected asterisk or slash but content ended with a "
                                        + "single slash instead.");
                            }
                            state = State.SLASH;
                            continue;
                        default:
                            throw newDatatypeException("Expected space, tab, newline, or slash but found \u201c"
                                    + c + "\u201d instead.");
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
                            throw newDatatypeException("Expected asterisk or slash but found \u201c"
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
        return;
    }

    @Override public String getName() {
        return "script documentation";
    }

}
