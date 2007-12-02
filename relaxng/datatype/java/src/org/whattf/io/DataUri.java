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

package org.whattf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUri {

    private static final Pattern DATA = Pattern.compile("^[dD][aA][tT][aA]:.*$");

    private enum State {
        AT_START, IN_SUPERTYPE, AT_SUBTYPE_START, IN_SUBTYPE, SEMICOLON_SEEN, WS_BEFORE_SEMICOLON, IN_PARAM_NAME, EQUALS_SEEN, IN_QUOTED_STRING, IN_UNQUOTED_STRING, IN_QUOTED_PAIR, CLOSE_QUOTE_SEEN
    }
    
    private String type;
    private String charset;
    
    /**
     * @throws IOException 
     * 
     */
    public DataUri(String uri) throws IOException {
        Matcher m = DATA.matcher(uri);
        if (!m.matches()) {
            throw new IllegalArgumentException("The input did not start with data:.");
        }
        InputStream is = new PercentDecodingReaderInputStream(new StringReader(uri));
        is.skip(5);
        
        boolean collectingCharsetValue = false;
        int i = 0; // silence compiler 
        State state = State.AT_START;
        for (;;) {
            int b = is.read();
            if (b == -1) {
                throw new MalformedURLException("Premature end of URI.");
            }
            if (b >= 0x80) {
                throw new MalformedURLException("Non-ASCII character in MIME type part of the data URI.");                
            }
            char c = (char) b;
            StringBuilder sb = new StringBuilder();
            switch (state) {
                case AT_START:
                    if (isTokenChar(c)) {
                        sb.append(c);
                        state = State.IN_SUPERTYPE;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character but saw ",
                                        c, " instead.");
                    }
                case IN_SUPERTYPE:
                    if (isTokenChar(c)) {
                        sb.append(c);
                        continue;
                    } else if (c == '/') {
                        sb.append(c);
                        state = State.AT_SUBTYPE_START;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character or \u201C/\u201D but saw ",
                                        c, " instead.");
                    }
                case AT_SUBTYPE_START:
                    if (isTokenChar(c)) {
                        sb.append(c);
                        state = State.IN_SUBTYPE;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character but saw ",
                                        c, " instead.");
                    }
                case IN_SUBTYPE:
                    if (isTokenChar(c)) {
                        sb.append(c);
                        continue;
                    } else if (c == ';') {
                        type = sb.toString();
                        sb.setLength(0);
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else if (isWhitespace(c)) {
                        type = sb.toString();
                        sb.setLength(0);
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
                        sb.append(c);
                        state = State.IN_PARAM_NAME;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected whitespace or a token character but saw ",
                                        c, " instead.");
                    }
                case IN_PARAM_NAME:
                    if (isTokenChar(c)) {
                        sb.append(c);
                        continue;
                    } else if (c == '=') {
                        String name = sb.toString();
                        sb.setLength(0);
                        collectingCharsetValue = "charset".equals(name);                        
                        state = State.EQUALS_SEEN;
                        continue;
                    }
                case EQUALS_SEEN:
                    if (c == '\"') {
                        state = State.IN_QUOTED_STRING;
                        continue;
                    } else if (isTokenChar(c)) {
                        sb.append(c);
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
                        if (collectingCharsetValue) {
                            charset = sb.toString();
                        }
                        sb.setLength(0);
                        state = State.CLOSE_QUOTE_SEEN;
                        continue;
                    } else if (isQDTextChar(c)) {
                        sb.append(c);
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a non-control ASCII character but saw ",
                                        c, " instead.");
                    }
                case IN_QUOTED_PAIR:
                    if (c <= 127) {
                        sb.append(c);
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
                        sb.append(c);
                        continue;
                    } else if (c == ';') {
                        if (collectingCharsetValue) {
                            charset = sb.toString();
                        }
                        sb.setLength(0);
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else if (isWhitespace(c)) {
                        if (collectingCharsetValue) {
                            charset = sb.toString();
                        }
                        sb.setLength(0);
                        state = State.WS_BEFORE_SEMICOLON;
                        continue;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character, whitespace or a semicolon but saw ",
                                        c, " instead.");
                    }
            }
        }

    }

    private IOException newDatatypeException(int i, String string, char c, String string2) {
        // TODO Auto-generated method stub
        return null;
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

    /**
     * Checks if a UTF-16 code unit represents a whitespace character (U+0020, 
     * U+0009, U+000D or U+000A).
     * @param c the code unit
     * @return <code>true</code> if whitespace, <code>false</code> otherwise
     */
    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }
}
