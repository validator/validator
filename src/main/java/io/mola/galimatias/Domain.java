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

import com.ibm.icu.text.IDNA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Domain extends Host {

    private static final long serialVersionUID = 2L;

    private final String domain;
    private final boolean unicode;

    private Domain(final String domain, final boolean unicode) {
        this.domain = domain;
        this.unicode = unicode;
    }

    public static Domain parseDomain(final String input) throws GalimatiasParseException {
        return parseDomain(input, false);
    }

    public List<String> labels() {
        return Arrays.asList(splitWorker(domain, "\u002E\u3002\uFF0E\uFF61", -1, true));
    }

    public static Domain parseDomain(final String input, final boolean unicode) throws GalimatiasParseException {
        return parseDomain(URLParsingSettings.create(), input, unicode);
    }

    public static Domain parseDomain(final URLParsingSettings settings, final String input, final boolean unicode) throws GalimatiasParseException {
        if (input == null) {
            throw new NullPointerException();
        }
        if (input.isEmpty()) {
            throw new GalimatiasParseException("input is empty");
        }

        final ErrorHandler errorHandler = settings.errorHandler();

        // WHATWG says: Let host be the result of running utf-8's decoder on the percent decoding of running utf-8 encode on input.
        String domain = URLUtils.percentDecode(input);

        String asciiDomain = URLUtils.domainToASCII(domain, errorHandler);

        for (int i = 0; i < asciiDomain.length(); i++) {
            char codePoint = asciiDomain.charAt(i);
            switch (codePoint) {
                case 0x0000:
                case 0x0009:
                case 0x000A:
                case 0x000D:
                case 0x0020:
                case '#':
                case '%':
                case '/':
                case ':':
                case '<':
                case '>':
                case '?':
                case '@':
                case '[':
                case '\\':
                case ']':
                case '^':
                case '|':
                    String message = "Illegal character in domain";
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
                    final GalimatiasParseException exception =
                            new GalimatiasParseException(message);
                    errorHandler.fatalError(exception);
                    throw exception;
            }
        }

        if (!unicode) {
            return new Domain(asciiDomain, unicode);
        }

        return new Domain(URLUtils.domainToUnicode(asciiDomain, errorHandler), unicode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return domain;
    }

    /**
     * Converts the domain to a Unicode representation suitable for human interpretation. It does IDNA conversion.
     *
     * @see Host#toHumanString()
     * @return unicode string
     */
    @Override
    public String toHumanString() {
        if (unicode) {
            return domain;
        }
        final IDNA.Info idnaInfo = new IDNA.Info();
        final StringBuilder idnaOutput = new StringBuilder();
        IDNA.getUTS46Instance(IDNA.DEFAULT).nameToUnicode(domain, idnaOutput, idnaInfo);
        return idnaOutput.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Domain domain1 = (Domain) o;

        //if (unicode != domain1.unicode) return false;
        if (domain != null ? !domain.equals(domain1.domain) : domain1.domain != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = domain != null ? domain.hashCode() : 0;
        //result = 31 * result + (unicode ? 1 : 0);
        return result;
    }

    /**
     * Imported from
     * https://github.com/apache/commons-lang/blob/690dc3c9c4cf8a1875d882ae09741c2e6342ad6b/src/main/java/org/apache/commons/lang3/StringUtils.java
     *
     * Performs the logic for the {@code split} and
     * {@code splitPreserveAllTokens} methods that return a maximum array
     * length.
     *
     * @param str  the String to parse, may be {@code null}
     * @param separatorChars the separate character
     * @param max  the maximum number of elements to include in the
     *  array. A zero or negative value implies no limit.
     * @param preserveAllTokens if {@code true}, adjacent separators are
     * treated as empty token separators; if {@code false}, adjacent
     * separators are treated as one separator.
     * @return an array of parsed Strings, {@code null} if null String input
     */
    private static String[] splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        //XXX: This never happens in Domain.parseDomain
        //if (str == null) {
        //    return null;
        //}
        final int len = str.length();
        //if (len == 0) {
        //    return EMPTY_STRING_ARRAY;
        //}

        final List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            final char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

}
