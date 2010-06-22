/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

public class DataUri {

    public static boolean startsWithData(String uri) {
        return uri != null && uri.length() >= 5
                && (uri.charAt(0) == 'd' || uri.charAt(0) == 'D')
                && (uri.charAt(1) == 'a' || uri.charAt(1) == 'A')
                && (uri.charAt(2) == 't' || uri.charAt(2) == 'T')
                && (uri.charAt(3) == 'a' || uri.charAt(3) == 'A')
                && (uri.charAt(4) == ':');
    }
    
    private enum State {
        AT_START, IN_SUPERTYPE, AT_SUBTYPE_START, IN_SUBTYPE, SEMICOLON_SEEN, WS_BEFORE_SEMICOLON, IN_PARAM_NAME, EQUALS_SEEN, IN_QUOTED_STRING, IN_UNQUOTED_STRING, IN_QUOTED_PAIR, CLOSE_QUOTE_SEEN
    }
    
    private String contentType;
    
    private InputStream inputStream;
    
    /**
     * @throws IOException, MalformedURLException
     * 
     */
    protected void init(IRI uri) throws IOException, MalformedURLException {
        if (!uri.getScheme().equals("data")) {
            throw new IllegalArgumentException("The input did not start with data:.");
        }

        if (uri.getRawFragment() != null) {
            throw new MalformedURLException("Fragment is not allowed for data: URIs according to RFC 2397. But if strictly comply with RFC 3986, ignore this error.");
        }

        InputStream is = new PercentDecodingReaderInputStream(new StringReader(uri.getRawPath()));
        StringBuilder sb = new StringBuilder();
        State state = State.AT_START;
        int i = 0; // string counter
        for (;;i++) {
            int b = is.read();
            if (b == -1) {
                throw new MalformedURLException("Premature end of URI.");
            }
            if (b >= 0x80) {
                throw new MalformedURLException("Non-ASCII character in MIME type part of the data URI.");                
            }
            char c = (char) b;
            sb.append(c);
            switch (state) {
                case AT_START:
                    if (isTokenChar(c)) {
                        state = State.IN_SUPERTYPE;
                        continue;
                    } else if (c == ';') {
                        sb.setLength(0);
                        sb.append("text/plain;");
                        state = State.SEMICOLON_SEEN;
                        continue;
                    } else if (c == ',') {
                        contentType = "text/plain;charset=US-ASCII";
                        inputStream = is;
                        return;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character or a semicolon but saw ",
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
                    } else if (c == ',') {
                        contentType = sb.substring(0, sb.length() - 1);
                        inputStream = is;
                        return;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character, whitespace, a semicolon or a comma but saw ",
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
                    } else if (c == ',') {
                        // let's see if we had ;base64,
                        int baseFirst = sb.length() - 8;
                        if (baseFirst >= 0 && ";base64,".equals(sb.substring(baseFirst, sb.length()))) {
                            contentType = sb.substring(0, baseFirst);
                            inputStream = new Base64InputStream(is);
                            return;
                        }
                    } else {
                        throw newDatatypeException(i, 
                                "Expected an equals sign, a comma or a token character but saw ",
                                        c, " instead.");
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
                    } else if (c == ',') {
                        contentType = sb.substring(0, sb.length() - 1);
                        inputStream = is;
                        return;
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
                    } else if (c == ',') {
                        contentType = sb.substring(0, sb.length() - 1);
                        inputStream = is;
                        return;
                    } else {
                        throw newDatatypeException(i, 
                                "Expected a token character, whitespace, a semicolon, or a comma but saw ",
                                        c, " instead.");
                    }
            }
        }

    }

    /**
     * @throws IOException, MalformedURLException
     * 
     */
    public DataUri(String uri) throws IOException, MalformedURLException {
        IRIFactory fac = new IRIFactory();
        fac.shouldViolation(true, false);
        fac.securityViolation(true, false);
        fac.dnsViolation(true, false);
        fac.mintingViolation(false, false);
        fac.useSpecificationIRI(true);
        init(fac.construct(uri));
    }

    /**
     * @throws IOException, MalformedURLException
     * 
     */
    public DataUri(IRI uri) throws IOException, MalformedURLException {
        init(uri);
    }

    private IOException newDatatypeException(int i, String head, char c, String tail) {
        return new DataUriException(i, head, c, tail);
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

    /**
     * Returns the contentType.
     * 
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the inputStream.
     * 
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }
}
