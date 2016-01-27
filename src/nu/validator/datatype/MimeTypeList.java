/*
 * Copyright (c) 2007-2015 Mozilla Foundation
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

public class MimeTypeList extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MimeTypeList THE_INSTANCE = new MimeTypeList();

    private enum State {
        WS_BEFORE_TYPE, IN_TYPE, ASTERISK_TYPE_SEEN, ASTERISK_AND_SLASH_SEEN, WS_BEFORE_COMMA, SLASH_SEEN, IN_SUBTYPE, BEFORE_EXTENSION, IN_EXTENSION
    }

    private MimeTypeList() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        State state = State.WS_BEFORE_TYPE;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            switch (state) {
                case WS_BEFORE_TYPE:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (c == '*') {
                        state = State.ASTERISK_TYPE_SEEN;
                    } else if (c == '.') {
                        state = State.BEFORE_EXTENSION;
                        continue;
                    } else if (isTokenChar(c)) {
                        state = State.IN_TYPE;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected whitespace, a token character or"
                                        + " \u201C*\u201D but saw ",
                                c, " instead.");
                    }
                case ASTERISK_TYPE_SEEN:
                    if (c == '/') {
                        state = State.ASTERISK_AND_SLASH_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected \u201C/\u201D but saw ", c,
                                " instead.");
                    }
                case ASTERISK_AND_SLASH_SEEN:
                    if (c == '*') {
                        state = State.WS_BEFORE_COMMA;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected \u201C*\u201D but saw ", c,
                                " instead.");
                    }
                case IN_TYPE:
                    if (c == '/') {
                        state = State.SLASH_SEEN;
                        continue;
                    } else if (isTokenChar(c)) {
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a token character or \u201C/\u201D"
                                        + " but saw ",
                                c, " instead.");
                    }
                case BEFORE_EXTENSION:
                    if (isTokenChar(c)) {
                        state = State.IN_EXTENSION;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a token character but saw ", c,
                                " instead.");
                    }
                case IN_EXTENSION:
                    if (isWhitespace(c)) {
                        state = State.WS_BEFORE_COMMA;
                        continue;
                    } else if (c == ',') {
                        state = State.WS_BEFORE_TYPE;
                        continue;
                    } else if (isTokenChar(c)) {
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a token character, whitespace or a"
                                        + " comma but saw ",
                                c, " instead.");
                    }
                case SLASH_SEEN:
                    if (c == '*') {
                        state = State.WS_BEFORE_COMMA;
                        continue;
                    } else if (isTokenChar(c)) {
                        state = State.IN_SUBTYPE;
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a token character or \u201C*\u201D"
                                        + " but saw ",
                                c, " instead.");
                    }
                case IN_SUBTYPE:
                    if (isWhitespace(c)) {
                        state = State.WS_BEFORE_COMMA;
                        continue;
                    } else if (c == ',') {
                        state = State.WS_BEFORE_TYPE;
                        continue;
                    } else if (isTokenChar(c)) {
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected a token character, whitespace or a"
                                        + " comma but saw ",
                                c, " instead.");
                    }
                case WS_BEFORE_COMMA:
                    if (c == ',') {
                        state = State.WS_BEFORE_TYPE;
                        continue;
                    } else if (isWhitespace(c)) {
                        continue;
                    } else {
                        throw newDatatypeException(i,
                                "Expected whitespace or a comma but saw ", c,
                                " instead.");
                    }
            }
        }
        switch (state) {
            case IN_SUBTYPE:
            case IN_EXTENSION:
            case WS_BEFORE_COMMA:
                return;
            case ASTERISK_AND_SLASH_SEEN:
                throw newDatatypeException(
                        "Expected \u201C*\u201D but the literal ended.");
            case ASTERISK_TYPE_SEEN:
                throw newDatatypeException(
                        "Expected \u201C/\u201D but the literal ended.");
            case IN_TYPE:
                throw newDatatypeException(
                        "Expected \u201C/\u201D but the literal ended.");
            case SLASH_SEEN:
                throw newDatatypeException(
                        "Expected subtype but the literal ended.");
            case WS_BEFORE_TYPE:
                throw newDatatypeException(
                        "Expected a MIME type but the literal ended.");
            case BEFORE_EXTENSION:
                throw newDatatypeException(
                        "Expected a file extension but the literal ended.");
        }
    }

    private boolean isTokenChar(char c) {
        return (c >= 33 && c <= 126) && !(c == '(' || c == ')' || c == '<'
                || c == '>' || c == '@' || c == ',' || c == ';' || c == ':'
                || c == '\\' || c == '\"' || c == '/' || c == '[' || c == ']'
                || c == '?' || c == '=' || c == '{' || c == '}');
    }

    @Override
    public String getName() {
        return "MIME type list";
    }

}
