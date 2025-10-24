/**
 * Copyright (c) 2013-2014 Santiago M. Mola <santi@mola.io>
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package io.mola.galimatias;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.mola.galimatias.URLUtils.*;

final class URLParser {

    private final URL base;
    private final String input;
    private final URL url;
    private final ParseURLState stateOverride;
    private URLParsingSettings settings;

    private int startIdx;
    private int endIdx;
    private int idx;
    private boolean isEOF;
    private int c;

    public URLParser(final String input) {
        this(null, input, null, null);
    }

    public URLParser(final URL base, final String input) {
        this(base, input, null, null);
    }

    public URLParser(final String input, final URL url, final ParseURLState stateOverride) {
        this(null, input, url, stateOverride);
    }

    public URLParser(final URL base, final String input, final URL url, final ParseURLState stateOverride) {
        this.base = base;
        this.input = input;
        this.url = url;
        this.stateOverride = stateOverride;
        this.settings = URLParsingSettings.create();
    }

    public URLParser settings(final URLParsingSettings settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Parse URL states as defined by WHATWG URL spec.
     *
     * http://url.spec.whatwg.org/#scheme-start-state
     */
    public static enum ParseURLState {
        SCHEME_START,
        SCHEME,
        SCHEME_DATA,
        NO_SCHEME,
        RELATIVE_OR_AUTHORITY,
        RELATIVE,
        RELATIVE_SLASH,
        AUTHORITY_FIRST_SLASH,
        AUTHORITY_SECOND_SLASH,
        AUTHORITY_IGNORE_SLASHES,
        AUTHORITY,
        FILE_HOST,
        HOST,
        PORT,
        RELATIVE_PATH_START,
        RELATIVE_PATH,
        QUERY,
        FRAGMENT
    }

    private void setIdx(final int i) {
        this.idx = i;
        this.isEOF = i >= endIdx;
        this.c = (isEOF || idx < startIdx)? 0x00 : input.codePointAt(i);
    }

    private void incIdx() {
        final int charCount = Character.charCount(this.c);
        setIdx(this.idx + charCount);
    }

    private void decrIdx() {
        if (idx <= startIdx) {
            setIdx(idx - 1);
            return;
        }
        final int charCount = Character.charCount(this.input.codePointBefore(idx));
        setIdx(this.idx - charCount);
    }

    private char at(final int i) {
        if (i >= endIdx) {
            return 0x00;
        }
        return input.charAt(i);
    }

    private void handleError(GalimatiasParseException parseException) throws GalimatiasParseException {
        this.settings.errorHandler().error(parseException);
    }

    private void handleError(String message) throws GalimatiasParseException {
        handleError(new GalimatiasParseException(message, idx));
    }

    private void handleFatalError(GalimatiasParseException parseException) throws GalimatiasParseException {
        this.settings.errorHandler().fatalError(parseException);
        throw parseException;
    }

    private void handleFatalError(String message) throws GalimatiasParseException {
        handleFatalError(new GalimatiasParseException(message, idx));
    }

    private void handleInvalidPercentEncodingError() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Percentage (\"%\") is not followed by two hexadecimal digits")
                .withParseIssue(ParseIssue.INVALID_PERCENT_ENCODING)
                .withPosition(idx)
                .build());
    }

    private void handleBackslashAsDelimiterError() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Backslash (\"\\\") used as path segment delimiter")
                .withParseIssue(ParseIssue.BACKSLASH_AS_DELIMITER)
                .withPosition(idx)
                .build());
    }

    private void handleIllegalWhitespaceError() throws GalimatiasParseException {
        handleError(GalimatiasParseException.builder()
                .withMessage("Tab, new line or carriage return found")
                .withParseIssue(ParseIssue.ILLEGAL_WHITESPACE)
                .withPosition(idx)
                .build());
    }

    private void handleIllegalCharacterError(String message, int codePoint) throws GalimatiasParseException {
        if (codePoint == ' ') {
            message += ": space is not allowed";
        } else if (codePoint == '\t') {
            message += ": tab is not allowed";
        } else if (codePoint == '\n') {
            message += ": line break is not allowed";
        } else if (codePoint == '\r') {
            message += ": carriage return is not allowed";
        } else {
            message += ": \u201c" + new String(Character.toChars(codePoint)) + "\u201d is not allowed";
        }
        handleError(GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(ParseIssue.ILLEGAL_CHARACTER)
                .withPosition(idx)
                .build());
    }

    private void handleFatalMissingSchemeError() throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage("Missing scheme")
                .withPosition(idx)
                .withParseIssue(ParseIssue.MISSING_SCHEME)
                .build());
    }

    private void handleFatalIllegalCharacterError(String message, int codePoint) throws GalimatiasParseException {
        message += ": \u201c" + new String(Character.toChars(codePoint)) + "\u201d is not allowed";
        handleFatalError(GalimatiasParseException.builder()
                .withMessage(message)
                .withParseIssue(ParseIssue.ILLEGAL_CHARACTER)
                .withPosition(idx)
                .build());
    }

    private void handleFatalInvalidHostError(Exception exception) throws GalimatiasParseException {
        handleFatalError(GalimatiasParseException.builder()
                .withMessage("Invalid host: " + exception.getMessage())
                .withParseIssue(ParseIssue.INVALID_HOST)
                .withPosition(idx)
                .withCause(exception)
                .build());
    }

    // Based on http://src.chromium.org/viewvc/chrome/trunk/src/url/third_party/mozilla/url_parse.cc
    // http://url.spec.whatwg.org/#parsing
    //
    public URL parse() throws GalimatiasParseException {

        if (input == null) {
            throw new NullPointerException("null input");
        }

        final StringBuilder buffer = new StringBuilder(input.length()*2);

        String encodingOverride = "utf-8";
        String scheme = (url == null)? null : url.scheme();
        StringBuilder schemeData = (url == null)? new StringBuilder() : new StringBuilder(url.schemeData());
        String username = (url == null)? null : url.username();
        String password = (url == null)? null : url.password();
        Host host = (url == null)? null : url.host();
        int port = (url == null)? -1 : url.port();
        boolean relativeFlag = (url != null) && url.isHierarchical();
        boolean atFlag = false; // @-flag
        boolean bracketsFlag = false; // []-flag
        List<String> pathSegments = (url == null || stateOverride == ParseURLState.RELATIVE_PATH_START)? new ArrayList<String>() : url.pathSegments();
        StringBuilder query = (url == null || url.query() == null || stateOverride == ParseURLState.QUERY)? null : new StringBuilder(url.query());
        StringBuilder fragment = (url == null || url.fragment() == null|| stateOverride == ParseURLState.FRAGMENT)? null : new StringBuilder(url.fragment());

        final StringBuilder usernameBuffer = new StringBuilder(buffer.length());
        StringBuilder passwordBuffer = null;

        endIdx = input.length();
        setIdx(startIdx);

        // Skip leading and trailing spaces
        while (Character.isWhitespace(c)) {
            incIdx();
            startIdx++;
        }
        while (endIdx > startIdx && Character.isWhitespace(input.charAt(endIdx - 1))) {
            endIdx--;
        }

        ParseURLState state = (stateOverride == null)? ParseURLState.SCHEME_START : stateOverride;

        // WHATWG URL 5.2.8: Keep running the following state machine by switching on state, increasing pointer by one
        //                   after each time it is run, as long as pointer does not point past the end of input.
        boolean terminate = false;
        while (!terminate) {

            if (idx > endIdx) {
                break;
            }

            //log.trace("STATE: {} | IDX: {} | C: {} | {}", state.name(), idx, c, new String(Character.toChars(c)));

            switch (state) {

                case SCHEME_START: {

                    // WHATWG URL .8.1: If c is an ASCII alpha, append c, lowercased, to buffer, and set state to scheme state.
                    if (isASCIIAlpha(c)) {
                        buffer.appendCodePoint(Character.toLowerCase(c));
                        state = ParseURLState.SCHEME;
                    } else {
                        // WHATWG URL .8.2: Otherwise, if state override is not given, set state to no scheme state,
                        //                  and decrease pointer by one.
                        if (stateOverride == null) {
                            state = ParseURLState.NO_SCHEME;
                            decrIdx();
                        } else {
                            handleFatalError("Scheme must start with alpha character.");
                        }
                    }
                    break;
                }
                case SCHEME: {
                    // WHATWG URL .8.1: If c is an ASCII alphanumeric, "+", "-", or ".", append c, lowercased, to buffer.
                    if (isASCIIAlphanumeric(c) || c == '+' || c == '-' || c == '.') {
                        buffer.appendCodePoint(Character.toLowerCase(c));
                    }

                    // WHATWG URL .8.2: Otherwise, if c is ":", set url's scheme to buffer, buffer to the empty string,
                    //                  and then run these substeps:
                    else if (c == ':') {
                        scheme = buffer.toString();
                        buffer.setLength(0);

                        // WHATWG URL .1: If state override is given, terminate this algorithm.
                        if (stateOverride != null) {
                            terminate = true;
                            break;
                        }

                        // WHATWG URL .2: If url's scheme is a relative scheme, set url's relative flag.
                        relativeFlag = isRelativeScheme(scheme);

                        //XXX: This is a deviation from the URL Specification in its current form, in favour of
                        //     URIs as specified in RFC 3986. That is, if we find scheme://, we expect a hierarchical URI.
                        //     See https://www.w3.org/Bugs/Public/show_bug.cgi?id=24170
                        //TODO: We left this out to pass W3C's web-platform-tests. It should probably be back for old RFCs?
                        //if (!relativeFlag) {
                        //    relativeFlag = input.regionMatches(idx + 1, "//", 0, 2);
                        //}

                        // WHATWG URL .3: If url's scheme is "file", set state to relative state.
                        if ("file".equals(scheme)) {
                            state = ParseURLState.RELATIVE;
                        }
                        // WHATWG URL .4: Otherwise, if url's relative flag is set, base is not null and base's
                        //                     scheme is equal to url's scheme, set state to relative or authority state.
                        else if (relativeFlag && base != null && base.scheme().equals(scheme)) {
                            state = ParseURLState.RELATIVE_OR_AUTHORITY;
                        }
                        // WHATWG URL .5: Otherwise, if url's relative flag is set, set state to authority first slash state.
                        else if (relativeFlag) {
                            state = ParseURLState.AUTHORITY_FIRST_SLASH;
                        }
                        // WHAT WG URL .6: Otherwise, set state to scheme data state.
                        else {
                            state = ParseURLState.SCHEME_DATA;
                        }

                    }

                    // WHATWG URL: Otherwise, if state override is not given, set buffer to the empty string,
                    //                  state to no scheme state, and start over (from the first code point in input).
                    else if (stateOverride == null) {
                        buffer.setLength(0);
                        state = ParseURLState.NO_SCHEME;
                        idx = -1; // Note that it'll be incremented by 1 after the switch
                    }

                    // WHATWG URL: Otherwise, if c is the EOF code point, terminate this algorithm.
                    else if (isEOF) {
                        terminate = true;
                    }

                    // WHATWG URL: Otherwise, parse error, terminate this algorithm.
                    else {
                        handleFatalIllegalCharacterError("Illegal character in scheme", c);
                    }

                    break;
                }

                case SCHEME_DATA: {

                    // WHATWG URL: If c is "?", set url's query to the empty string and state to query state.
                    if (c == '?') {
                        query = new StringBuilder();
                        state = ParseURLState.QUERY;
                    }
                    // WHATWG URL: Otherwise, if c is "#", set url's fragment to the empty string and state to fragment state.
                    else if (c == '#') {
                        fragment = new StringBuilder();
                        state = ParseURLState.FRAGMENT;
                    }
                    // WHATWG URL: Otherwise, run these substeps:
                    else {

                        // WHATWG URL: If c is not the EOF code point, not a URL code point, and not "%", parse error.
                        if (!isEOF && c != '%' && !isURLCodePoint(c)) {
                            handleIllegalCharacterError("Illegal character in scheme data", c);
                        }

                        if (c == '%') {
                            // WHATWG URL: If c is "%" and remaining does not start with two ASCII hex digits, parse error.
                            if (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2))) {
                                handleInvalidPercentEncodingError();
                            } else {
                                schemeData.append((char)c)
                                        .append(Character.toUpperCase(input.charAt(idx+1)))
                                        .append(Character.toUpperCase(input.charAt(idx+2)));
                                setIdx(idx+2);
                                break;
                            }
                        }

                        // WHATWG URL: If c is none of EOF code point, U+0009, U+000A, and U+000D, utf-8 percent encode
                        //             c using the simple encode set, and append the result to url's scheme data.
                        if (!isEOF && c != 0x0009 && c != 0x000A && c != 0x000D) {
                            utf8PercentEncode(c, EncodeSet.SIMPLE, schemeData);
                        }
                        //TODO: Shouldn't the "else" clause give parse error?

                    }

                    break;
                }

                case NO_SCHEME: {
                    if (base == null || !isRelativeScheme(base.scheme())) {
                        handleFatalMissingSchemeError();
                    }
                    state = ParseURLState.RELATIVE;
                    idx--;
                    break;
                }

                case RELATIVE_OR_AUTHORITY: {
                    if (c == '/' && at(idx+1) == '/') {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        idx++;
                    } else {
                        handleError("Relative scheme (" + scheme + ") is not followed by \"://\"");
                        state = ParseURLState.RELATIVE;
                        idx--;
                    }
                    break;
                }

                case RELATIVE: {
                    relativeFlag = true;

                    if (!"file".equals(scheme)) {
                        scheme = (base == null)? null : base.scheme();
                    }

                    if (isEOF) {
                        host = (base == null)? null : base.host();
                        port = (base == null || base.port() == base.defaultPort())? -1 : base.port();
                        pathSegments = (base == null)? null : base.pathSegments();
                        query = (base == null || base.query() == null)? null : new StringBuilder(base.query());
                    } else if (c == '/' || c == '\\') {
                        if (c == '\\') {
                            handleBackslashAsDelimiterError();
                        }
                        state = ParseURLState.RELATIVE_SLASH;
                    } else if (c == '?') {
                        host = (base == null)? null : base.host();
                        port = (base == null || base.port() == base.defaultPort())? -1 : base.port();
                        pathSegments = (base == null)? null : base.pathSegments();
                        query = new StringBuilder();
                        state = ParseURLState.QUERY;
                    } else if (c == '#') {
                        host = (base == null)? null : base.host();
                        port = (base == null || base.port() == base.defaultPort())? -1 : base.port();
                        pathSegments = (base == null)? null : base.pathSegments();
                        query = (base == null || base.query() == null)? null : new StringBuilder(base.query());
                        fragment = new StringBuilder();
                        state = ParseURLState.FRAGMENT;
                    } else {
                        if (!"file".equals(scheme) ||
                            !isASCIIAlpha(c) ||
                            (at(idx+1) != ':' && at(idx+1) != '|') ||
                            (idx + 1 == endIdx - 1) ||
                            (idx + 2 < endIdx &&
                                    at(idx+2) != '/' && at(idx+2) != '\\' && at(idx+2) != '?' && at(idx+2) != '#')
                                ) {

                            host = (base == null)? null : base.host();
                            port = (base == null || base.port() == base.defaultPort())? -1 : base.port();
                            pathSegments = (base == null)? new ArrayList<String>() : base.pathSegments();
                            // Pop path
                            if (!pathSegments.isEmpty()) {
                                pathSegments.remove(pathSegments.size() - 1);
                            }
                        }
                        state = ParseURLState.RELATIVE_PATH;
                        idx--;
                    }
                    break;
                }

                case RELATIVE_SLASH: {
                    if (c == '/' || c == '\\') {
                        if (c == '\\') {
                            handleBackslashAsDelimiterError();
                        }
                        if ("file".equals(scheme)) {
                            state = ParseURLState.FILE_HOST;
                        } else {
                            state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        }
                    } else {
                        if (!"file".equals(scheme)) {
                            host = (base == null)? null : base.host();
                            port = (base == null || base.port() == base.defaultPort())? -1 : base.port();
                        }
                        state = ParseURLState.RELATIVE_PATH;
                        decrIdx();
                    }
                    break;
                }

                case AUTHORITY_FIRST_SLASH: {
                    if (c == '/') {
                        state = ParseURLState.AUTHORITY_SECOND_SLASH;
                    } else {
                        handleError("Expected a slash (\"/\")");
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        decrIdx();
                    }
                    break;
                }

                case AUTHORITY_SECOND_SLASH: {
                    if (c == '/') {
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                    } else {
                        handleError("Expected a slash (\"/\")");
                        state = ParseURLState.AUTHORITY_IGNORE_SLASHES;
                        decrIdx();
                    }
                    break;
                }

                case AUTHORITY_IGNORE_SLASHES: {
                    if (c != '/' && c != '\\') {
                        state = ParseURLState.AUTHORITY;
                        decrIdx();
                    } else {
                        handleError("Unexpected slash or backslash");
                    }
                    break;
                }

                case AUTHORITY: {
                    // If c is "@", run these substeps:
                    if (c == '@') {
                        if (atFlag) {
                            handleError("User or password contains an at symbol (\"@\") not percent-encoded");
                            buffer.insert(0, "%40");
                        }
                        atFlag = true;


                        for (int i = 0; i < buffer.codePointCount(0, buffer.length()); i++) {
                            final int otherChar = buffer.codePointAt(i);
                            final char startChar = buffer.charAt(i);
                            if (
                                    otherChar == 0x0009 ||
                                    otherChar == 0x000A ||
                                    otherChar == 0x000D
                                ) {
                                handleIllegalWhitespaceError();
                                continue;
                            }
                            if (!isURLCodePoint(startChar) && startChar != '%') {
                                handleIllegalCharacterError("Illegal character in user or password", otherChar);
                            }
                            if (otherChar == '%') {
                                if (i + 2 >= buffer.length() || !isASCIIHexDigit(buffer.charAt(i+1)) || !isASCIIHexDigit(buffer.charAt(i+2))) {
                                    handleInvalidPercentEncodingError();
                                } else if (isASCIIHexDigit(buffer.charAt(i+1)) && isASCIIHexDigit(buffer.charAt(i+2))) {
                                    buffer.setCharAt(i + 1, Character.toUpperCase(buffer.charAt(i + 1)));
                                    buffer.setCharAt(i + 2, Character.toUpperCase(buffer.charAt(i + 2)));
                                }
                            }
                            if (otherChar == ':' && passwordBuffer == null) {
                                passwordBuffer = new StringBuilder(buffer.length() - i);
                                continue;
                            }
                            if (passwordBuffer != null) {
                                utf8PercentEncode(otherChar, EncodeSet.DEFAULT, passwordBuffer);
                            } else {
                                utf8PercentEncode(otherChar, EncodeSet.DEFAULT, usernameBuffer);
                            }
                        }

                        buffer.setLength(0);

                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        setIdx(idx - buffer.length() - 1);
                        if (atFlag) {
                            username = usernameBuffer.toString();
                            if (passwordBuffer != null) {
                                password = passwordBuffer.toString();
                            }
                        }
                        buffer.setLength(0);
                        state = ParseURLState.HOST;
                    } else {
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case FILE_HOST: {

                    if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        idx--;
                        if (buffer.length() == 2 && isASCIIAlpha(buffer.charAt(0)) &&
                                (buffer.charAt(1) == ':' || buffer.charAt(1) == '|')) {
                            state = ParseURLState.RELATIVE_PATH;
                        } else if (buffer.length() == 0) {
                            state = ParseURLState.RELATIVE_PATH_START;
                        } else {
                            try {
                                host = Host.parseHost(buffer.toString());
                            } catch (GalimatiasParseException ex) {
                                handleFatalInvalidHostError(ex);
                            }
                            buffer.setLength(0);
                            state = ParseURLState.RELATIVE_PATH_START;
                        }
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        handleIllegalWhitespaceError();
                    } else {
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case HOST: { //XXX: WHATWG defines HOSTNAME as an alias, useless here.
                    if (c == ':' && !bracketsFlag) {
                        try {
                            host = Host.parseHost(buffer.toString());
                        } catch (GalimatiasParseException ex) {
                            handleFatalInvalidHostError(ex);
                        }
                        buffer.setLength(0);
                        state = ParseURLState.PORT;
                        if (stateOverride == ParseURLState.HOST) {
                            terminate = true;
                        }
                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        decrIdx();
                        try {
                            host = Host.parseHost(buffer.toString());
                        } catch (GalimatiasParseException ex) {
                            handleFatalInvalidHostError(ex);
                        }
                        buffer.setLength(0);
                        state = ParseURLState.RELATIVE_PATH_START;
                        if (stateOverride != null) {
                            terminate = true;
                        }
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        handleIllegalWhitespaceError();
                    } else {
                        if (c == '[') {
                            bracketsFlag = true;
                        } else if (c == ']') {
                            bracketsFlag = false;
                        }
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case PORT: {
                    if (isASCIIDigit(c)) {
                        buffer.appendCodePoint(c);
                    } else if (isEOF || c == '/' || c == '\\' || c == '?' || c == '#') {
                        // Remove leading zeroes
                        while (buffer.length() > 0 && buffer.charAt(0) == 0x0030 && buffer.length() > 1) {
                            buffer.deleteCharAt(0);
                        }
                        //XXX: This is redundant with URL constructor
                        if (buffer.toString().equals(getDefaultPortForScheme(scheme))) {
                            buffer.setLength(0);
                        }
                        if (buffer.length() == 0) {
                            port = -1;
                        } else {
                            String portMsg = "Port number must be less than 65536";
                            try {
                                port = Integer.parseInt(buffer.toString());
                                if (port > 65535) {
                                    handleError(portMsg);
                                }
                            } catch (NumberFormatException e) {
                                handleError(portMsg);
                            }
                        }
                        if (stateOverride != null) {
                            terminate = true;
                        } else {
                            buffer.setLength(0);
                            state = ParseURLState.RELATIVE_PATH_START;
                            idx--;
                        }
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        handleIllegalWhitespaceError();
                    } else {
                        handleFatalIllegalCharacterError("Illegal character in port", c);
                    }
                    break;
                }

                case RELATIVE_PATH_START: {
                    if (c == '\\') {
                        handleBackslashAsDelimiterError();
                    }
                    state = ParseURLState.RELATIVE_PATH;
                    if (c != '/' && c != '\\') {
                        decrIdx();
                    }
                    break;
                }

                case RELATIVE_PATH: {
                    if (isEOF || c == '/' || c == '\\' || (stateOverride == null && (c == '?' || c == '#'))) {
                        if (c == '\\') {
                            handleBackslashAsDelimiterError();
                        }
                        final String lowerCasedBuffer = buffer.toString().toLowerCase(Locale.ENGLISH);
                        if ("%2e".equals(lowerCasedBuffer)) {
                            buffer.setLength(0);
                            buffer.append('.');
                        } else if (
                                ".%2e".equals(lowerCasedBuffer) ||
                                "%2e.".equals(lowerCasedBuffer) ||
                                "%2e%2e".equals(lowerCasedBuffer)
                                ) {
                            buffer.setLength(0);
                            buffer.append("..");
                        }
                        if ("..".equals(buffer.toString())) {
                            // Pop path
                            if (!pathSegments.isEmpty()) {
                                pathSegments.remove(pathSegments.size() - 1);
                            }
                            if (c != '/' && c != '\\') {
                                pathSegments.add("");
                            }

                        } else if (".".equals(buffer.toString()) && c != '/' && c != '\\') {
                            pathSegments.add("");
                        } else if (!".".equals(buffer.toString())) {
                            if ("file".equals(scheme) && pathSegments.isEmpty() &&
                                    buffer.length() == 2 &&
                                    isASCIIAlpha(buffer.charAt(0)) &&
                                    buffer.charAt(1) == '|') {
                                buffer.setCharAt(1, ':');
                            }
                            pathSegments.add(buffer.toString());
                        }
                        buffer.setLength(0);
                        if (c == '?') {
                            query = new StringBuilder();
                            state = ParseURLState.QUERY;
                        } else if (c == '#') {
                            fragment = new StringBuilder();
                            state = ParseURLState.FRAGMENT;
                        }

                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        handleIllegalWhitespaceError();
                    } else {
                        if (!isURLCodePoint(c) && c != '%') {
                            handleIllegalCharacterError("Illegal character in path segment", c);
                        }

                        if (c == '%') {
                            if (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2))) {
                                handleInvalidPercentEncodingError();
                            } else {
                                buffer.append((char)c)
                                        .append(Character.toUpperCase(input.charAt(idx+1)))
                                        .append(Character.toUpperCase(input.charAt(idx+2)));
                                setIdx(idx+2);
                                break;
                            }
                        }

                        utf8PercentEncode(c, EncodeSet.DEFAULT, buffer);
                    }
                    break;
                }

                case QUERY: {

                    //XXX: When we come from stateOverride, query buffer is null
                    if (query == null) {
                        query = new StringBuilder();
                    }

                    if (isEOF || (stateOverride == null && c == '#')) {
                        if (relativeFlag) {
                            encodingOverride = "utf-8";
                        }
                        final byte[] bytes = buffer.toString().getBytes(UTF_8);
                        for (int i = 0; i < bytes.length; i++) {
                            final byte b = bytes[i];
                            if (b < 0x21 || b > 0x7E || b == 0x22 || b == 0x23 || b == 0x3C || b == 0x3E || b == 0x60) {
                                percentEncode(b, query);
                            } else {
                                query.append((char) b);
                            }
                        }
                        buffer.setLength(0);
                        if (c == '#') {
                            fragment = new StringBuilder();
                            state = ParseURLState.FRAGMENT;
                        }
                    }  else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        handleIllegalWhitespaceError();
                    } else {
                        if (!isURLCodePoint(c) && c != '%') {
                            handleIllegalCharacterError("Illegal character in query", c);
                        }
                        if (c == '%') {
                            if (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2))) {
                                handleInvalidPercentEncodingError();
                            } else {
                                buffer.append((char)c)
                                        .append(Character.toUpperCase(input.charAt(idx+1)))
                                        .append(Character.toUpperCase(input.charAt(idx+2)));
                                setIdx(idx+2);
                                break;
                            }
                        }
                        buffer.appendCodePoint(c);
                    }
                    break;
                }

                case FRAGMENT: {

                    //XXX: When we come from stateOverride, fragment buffer is null
                    if (fragment == null) {
                        fragment = new StringBuilder();
                    }

                    if (isEOF) {
                        // Do nothing
                    } else if (c == 0x0009 || c == 0x000A || c == 0x000D) {
                        handleIllegalWhitespaceError();
                    } else {
                        if (!isURLCodePoint(c) && c != '%') {
                            handleIllegalCharacterError("Illegal character in fragment", c);
                        }
                        if (c == '%') {
                            if (!isASCIIHexDigit(at(idx+1)) || !isASCIIHexDigit(at(idx+2))) {
                                handleInvalidPercentEncodingError();
                            } else {
                                fragment.append((char)c)
                                        .append(Character.toUpperCase(input.charAt(idx+1)))
                                        .append(Character.toUpperCase(input.charAt(idx+2)));
                                setIdx(idx+2);
                                break;
                            }
                        }

                        utf8PercentEncode(c, EncodeSet.SIMPLE, fragment);

                    }
                    break;
                }

            }

            if (idx == -1) {
                setIdx(startIdx);
            } else {
                incIdx();
            }

        }

        return new URL(scheme, schemeData.toString(),
                username, password,
                host, port, pathSegments,
                (query == null)? null : query.toString(),
                (fragment == null)? null : fragment.toString(),
                relativeFlag);

    }

    String parseUsername() {
        StringBuilder buffer = new StringBuilder(input.length() * 2);
        startIdx = 0;
        endIdx = input.length();
        setIdx(0);
        while (!isEOF) {
            utf8PercentEncode(c, EncodeSet.USERNAME, buffer);
            incIdx();
        }
        return buffer.toString();
    }

    String parsePassword() {
        StringBuilder buffer = new StringBuilder(input.length() * 2);
        startIdx = 0;
        endIdx = input.length();
        setIdx(0);
        while (!isEOF) {
            utf8PercentEncode(c, EncodeSet.PASSWORD, buffer);
            incIdx();
        }
        return buffer.toString();
    }

    private static enum EncodeSet {
        SIMPLE,
        DEFAULT,
        PASSWORD,
        USERNAME
    }

    private static void utf8PercentEncode(final int c, final EncodeSet encodeSet, final StringBuilder buffer) {
        if (encodeSet != null) {
            switch (encodeSet) {
                case SIMPLE:
                    if (!isInSimpleEncodeSet(c)) {
                        buffer.appendCodePoint(c);
                        return;
                    }
                    break;
                case DEFAULT:
                    if (!isInDefaultEncodeSet(c)) {
                        buffer.appendCodePoint(c);
                        return;
                    }
                    break;
                case PASSWORD:
                    if (!isInPasswordEncodeSet(c)) {
                        buffer.appendCodePoint(c);
                        return;
                    }
                    break;
                case USERNAME:
                    if (!isInUsernameEncodeSet(c)) {
                        buffer.appendCodePoint(c);
                        return;
                    }
                    break;
            }
        }
        final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
        for (final byte b : bytes) {
            percentEncode(b, buffer);
        }
    }

    private static boolean isInSimpleEncodeSet(final int c) {
        return c < 0x0020 || c > 0x007E;
    }

    private static boolean isInDefaultEncodeSet(final int c) {
        return isInSimpleEncodeSet(c) || c == ' ' || c == '"' || c == '#' || c == '<' || c == '>' || c == '?' || c == '`';
    }

    private static boolean isInPasswordEncodeSet(final int c) {
        return isInDefaultEncodeSet(c) || c == '/' || c == '@' || c == '\\';
    }

    private static boolean isInUsernameEncodeSet(final int c) {
        return isInPasswordEncodeSet(c) || c == ':';
    }

}
