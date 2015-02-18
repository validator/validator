/*
 * Copyright (c) 2007 Mozilla Foundation
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

public class MimeType extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MimeType THE_INSTANCE = new MimeType();
    
    private enum State {
        AT_START, IN_SUPERTYPE, AT_SUBTYPE_START, IN_SUBTYPE, SEMICOLON_SEEN, WS_BEFORE_SEMICOLON, IN_PARAM_NAME, EQUALS_SEEN, IN_QUOTED_STRING, IN_UNQUOTED_STRING, IN_QUOTED_PAIR, CLOSE_QUOTE_SEEN
    }

    private MimeType() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        State state = State.AT_START;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            switch (state) {
                case AT_START:
                    if (isTokenChar(c)) {
                        state = State.IN_SUPERTYPE;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character but saw ",
                                        c, " instead.");
                    }
                case IN_SUPERTYPE:
                    if (isTokenChar(c)) {
                        continue;
                    } else if (c == '/') {
                        state = State.AT_SUBTYPE_START;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character or \u201C/\u201D but saw ",
                                        c, " instead.");
                    }
                case AT_SUBTYPE_START:
                    if (isTokenChar(c)) {
                        state = State.IN_SUBTYPE;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character but saw ",
                                        c, " instead.");
                    }
                case IN_SUBTYPE:
                    if (isTokenChar(c)) {
                        continue;
                    } else if (c == ';') {
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else if (isWhitespace(c)) {
                        state = State.WS_BEFORE_SEMICOLON;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character, whitespace or a semicolon but saw ",
                                        c, " instead.");
                    }
                case WS_BEFORE_SEMICOLON:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (c == ';') {
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected whitespace or a semicolon but saw ",
                                        c, " instead.");
                    }
                case SEMICOLON_SEEN:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (isTokenChar(c)) {
                        state = State.IN_PARAM_NAME;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected whitespace or a token character but saw ",
                                        c, " instead.");
                    }
                case IN_PARAM_NAME:
                    if (isTokenChar(c)) {
                        continue;
                    } else if (c == '=') {
                        state = State.EQUALS_SEEN;
                        continue;
                    }
                case EQUALS_SEEN:
                    if (c == '\"') {
                        state = State.IN_QUOTED_STRING;
                        continue;
                    } else if (isTokenChar(c)) {
                        state = State.IN_UNQUOTED_STRING;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a double quote or a token character but saw ",
                                        c, " instead.");
                    }
                case IN_QUOTED_STRING:
                    if (c == '\\') {
                        state = State.IN_QUOTED_PAIR;
                        continue;
                    } else if (c == '\"') {
                        state = State.CLOSE_QUOTE_SEEN;
                        continue;
                    } else if (isQDTextChar(c)) {
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a non-control ASCII character but saw ",
                                        c, " instead.");
                    }
                case IN_QUOTED_PAIR:
                    if (c <= 127) {
                        state = State.IN_QUOTED_STRING;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected an ASCII character but saw ",
                                        c, " instead.");
                    }
                case CLOSE_QUOTE_SEEN:
                    if (c == ';') {
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else if (isWhitespace(c)) {
                        state = State.WS_BEFORE_SEMICOLON;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected an ASCII character but saw ",
                                        c, " instead.");
                    }
                case IN_UNQUOTED_STRING:
                    if (isTokenChar(c)) {
                        continue;
                    } else if (c == ';') {
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else if (isWhitespace(c)) {
                        state = State.WS_BEFORE_SEMICOLON;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character, whitespace or a semicolon but saw ",
                                        c, " instead.");
                    }
            }
        }
        switch (state) {
            case IN_SUBTYPE:
            case IN_UNQUOTED_STRING:
            case CLOSE_QUOTE_SEEN:
                return;
            case AT_START:
                throw newDatatypeException( 
                        "Expected a MIME type but saw the empty string.");
            case IN_SUPERTYPE:
            case AT_SUBTYPE_START:
                throw newDatatypeException(literal.length() - 1, 
                        "Subtype missing.");
            case EQUALS_SEEN:
            case IN_PARAM_NAME:
                throw newDatatypeException(literal.length() - 1, 
                        "Parameter value missing.");
            case IN_QUOTED_PAIR:
            case IN_QUOTED_STRING:
                throw newDatatypeException(literal.length() - 1, 
                        "Unfinished quoted string.");
            case SEMICOLON_SEEN:
                throw newDatatypeException(literal.length() - 1, 
                        "Semicolon seen but there was no parameter following it.");
            case WS_BEFORE_SEMICOLON:
                throw newDatatypeException(literal.length() - 1, 
                        "Extraneous trailing whitespace.");
        }
    }

    private boolean isQDTextChar(char c) {
        return (c >= ' ' && c <= 126) || (c == '\n') || (c == '\r')
                || (c == '\t');
    }

    private boolean isTokenChar(char c) {
        return (c >= 33 && c <= 126)
                && !(c == '(' || c == ')' || c == '<' || c == '>' || c == '@'
                        || c == ',' || c == ';' || c == ':' || c == '\\'
                        || c == '\"' || c == '/' || c == '[' || c == ']'
                        || c == '?' || c == '=' || c == '{' || c == '}');
    }

    @Override
    public String getName() {
        return "MIME type";
    }

}
