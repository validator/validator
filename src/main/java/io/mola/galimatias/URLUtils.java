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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Utils for parsing and serializing URLs.
 *
 * Not to be confused with the URLUtils from the WHATWG URL spec.
 *
 */
public final class URLUtils {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final IDNA idna = IDNA.getUTS46Instance(IDNA.DEFAULT);

    private URLUtils() {

    }

    /**
     * Percent-decodes a string.
     *
     * Percent-encoded bytes are assumed to represent UTF-8 characters.
     *
     * @see <a href="http://url.spec.whatwg.org/#percent-encoded-bytes">WHATWG URL Standard: Percent-encoded bytes</a>
     *
     * @param input
     * @return
     */
    public static String percentDecode(final String input) {
        if (input.isEmpty()) {
            return input;
        }
        try {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            int idx = 0;
            while (idx < input.length()) {

                boolean isEOF = idx >= input.length();
                int c = (isEOF)? 0x00 : input.codePointAt(idx);

                while (!isEOF && c != '%') {
                    if (c <= 0x7F) { // String.getBytes is slow, so do not perform encoding
                                     // if not needed
                        bytes.write((byte) c);
                        idx++;
                    } else {
                        bytes.write(new String(Character.toChars(c)).getBytes(UTF_8));
                        idx += Character.charCount(c);
                    }
                    isEOF = idx >= input.length();
                    c = (isEOF)? 0x00 : input.codePointAt(idx);
                }

                if (c == '%' && (input.length() <= idx + 2 ||
                        !isASCIIHexDigit(input.charAt(idx + 1)) ||
                        !isASCIIHexDigit(input.charAt(idx + 2)))) {
                    if (c <= 0x7F) { // String.getBytes is slow, so do not perform encoding
                        // if not needed
                        bytes.write((byte) c);
                        idx++;
                    } else {
                        bytes.write(new String(Character.toChars(c)).getBytes(UTF_8));
                        idx += Character.charCount(c);
                    }
                } else {
                    while (c == '%' && input.length() > idx + 2 &&
                            isASCIIHexDigit(input.charAt(idx + 1)) &&
                            isASCIIHexDigit(input.charAt(idx + 2))) {
                        bytes.write(hexToInt(input.charAt(idx + 1), input.charAt(idx + 2)));
                        idx += 3;
                        c = (input.length() <= idx)? 0x00 : input.codePointAt(idx);
                    }
                }
            }
            return new String(bytes.toByteArray(), UTF_8);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Converts a domain to its ASCII representation. Uses the the <strong>name to ASCII</strong>
     * as specified in the IDNA standard.
     *
     * @see <a href="http://url.spec.whatwg.org/#idna">WHATWG URL Standard - IDNA Section</a>
     *
     * @param domain
     * @return
     */
    public static String domainToASCII(final String domain) throws GalimatiasParseException {
        return domainToASCII(domain, DefaultErrorHandler.getInstance());
    }

    static String domainToASCII(final String domain, final ErrorHandler errorHandler) throws GalimatiasParseException {
        final IDNA.Info idnaInfo = new IDNA.Info();
        final StringBuilder idnaOutput = new StringBuilder();
        idna.nameToASCII(domain, idnaOutput, idnaInfo);
        processIdnaInfo(errorHandler, idnaInfo, false);
        return idnaOutput.toString();
    }

    /**
     * Converts a domain to its Unicode representation. Uses the the <strong>name to Unicode</strong>
     * as specified in the IDNA standard.
     *
     * @see <a href="http://url.spec.whatwg.org/#idna">WHATWG URL Standard - IDNA Section</a>
     *
     * @param asciiDomain
     * @return
     */
    public static String domainToUnicode(final String asciiDomain) throws GalimatiasParseException {
        return domainToUnicode(asciiDomain, DefaultErrorHandler.getInstance());
    }

    static String domainToUnicode(final String asciiDomain, final ErrorHandler errorHandler) throws GalimatiasParseException {
        final IDNA.Info unicodeIdnaInfo = new IDNA.Info();
        final StringBuilder unicodeIdnaOutput = new StringBuilder();
        idna.nameToUnicode(asciiDomain, unicodeIdnaOutput, unicodeIdnaInfo);
        processIdnaInfo(errorHandler, unicodeIdnaInfo, false);
        return unicodeIdnaOutput.toString();
    }

    private static void processIdnaInfo(final ErrorHandler errorHandler,
            final IDNA.Info idnaInfo, final boolean checkHyphens)
            throws GalimatiasParseException {
        for (IDNA.Error error : idnaInfo.getErrors()) {
            String msg;
            switch (error) {
                case BIDI:
                    msg = "A label does not meet the IDNA BiDi requirements (for right-to-left characters).";
                    break;
                case CONTEXTJ:
                    msg = "A label does not meet the IDNA CONTEXTJ requirements.";
                    break;
                case CONTEXTO_DIGITS:
                    msg = "A label does not meet the IDNA CONTEXTO requirements for digits.";
                    break;
                case CONTEXTO_PUNCTUATION:
                    msg = "A label does not meet the IDNA CONTEXTO requirements for punctuation characters.";
                    break;
                case DISALLOWED:
                    msg = "A label or domain name contains disallowed characters.";
                    break;
                case DOMAIN_NAME_TOO_LONG:
                    msg = "A domain name is longer than 255 bytes in its storage form.";
                    break;
                case EMPTY_LABEL:
                    msg = "A non-final domain name label (or the whole domain name) is empty.";
                    break;
                case HYPHEN_3_4:
                    if (!checkHyphens) {
                        return;
                    }
                    msg = "A label contains hyphen-minus ('-') in the third and fourth positions.";
                    break;
                case INVALID_ACE_LABEL:
                    msg = "An ACE label does not contain a valid label string.";
                    break;
                case LABEL_HAS_DOT:
                    msg = "A label contains a dot=full stop.";
                    break;
                case LABEL_TOO_LONG:
                    msg = "A domain name label is longer than 63 bytes.";
                    break;
                case LEADING_COMBINING_MARK:
                    msg = "A label starts with a combining mark.";
                    break;
                case LEADING_HYPHEN:
                    if (!checkHyphens) {
                        return;
                    }
                    msg = "A label starts with a hyphen-minus ('-').";
                    break;
                case PUNYCODE:
                    msg = "A label starts with \"xn--\" but does not contain valid Punycode.";
                    break;
                case TRAILING_HYPHEN:
                    if (!checkHyphens) {
                        return;
                    }
                    msg = "A label ends with a hyphen-minus ('-').";
                    break;
                default:
                    msg = "IDNA error.";
                    break;
            }
            final GalimatiasParseException exception = new GalimatiasParseException(msg);
            errorHandler.fatalError(exception);
            throw exception;
        }
    }

    public static boolean isASCIIHexDigit(final int c) {
        return (c >= 0x0041 && c <= 0x0046) || (c >= 0x0061 && c <= 0x0066) || isASCIIDigit(c);
    }

    public static boolean isASCIIDigit(final int c) {
        return c >= 0x0030 && c <= 0x0039;
    }

    public static boolean isASCIIAlphaUppercase(final int c) {
        return c >= 0x0061 && c <= 0x007A;
    }

    public static boolean isASCIIAlphaLowercase(final int c) {
        return c >= 0x0041 && c <= 0x005A;
    }

    public static boolean isASCIIAlpha(final int c) {
        return isASCIIAlphaLowercase(c) || isASCIIAlphaUppercase(c);
    }

    public static boolean isASCIIAlphanumeric(final int c) {
        return isASCIIAlpha(c) || isASCIIDigit(c);
    }

    public static boolean isURLCodePoint(final int c) {
        return
                isASCIIAlphanumeric(c) ||
                        c == '!' ||
                        c == '$' ||
                        c == '&' ||
                        c == '\'' ||
                        c == '(' ||
                        c == ')' ||
                        c == '*' ||
                        c == '+' ||
                        c == ',' ||
                        c == '-' ||
                        c == '.' ||
                        c == '/' ||
                        c == ':' ||
                        c == ';' ||
                        c == '=' ||
                        c == '?' ||
                        c == '@' ||
                        c == '_' ||
                        c == '~' ||
                        (c >= 0x00A0 && c <= 0xD7FF) ||
                        (c >= 0xE000 && c <= 0xFDCF) ||
                        (c >= 0xFDF0 && c <= 0xFFEF) ||
                        (c >= 0x10000 && c <= 0x1FFFD) ||
                        (c >= 0x20000 && c <= 0x2FFFD) ||
                        (c >= 0x30000 && c <= 0x3FFFD) ||
                        (c >= 0x40000 && c <= 0x4FFFD) ||
                        (c >= 0x50000 && c <= 0x5FFFD) ||
                        (c >= 0x60000 && c <= 0x6FFFD) ||
                        (c >= 0x70000 && c <= 0x7FFFD) ||
                        (c >= 0x80000 && c <= 0x8FFFD) ||
                        (c >= 0x90000 && c <= 0x9FFFD) ||
                        (c >= 0xA0000 && c <= 0xAFFFD) ||
                        (c >= 0xB0000 && c <= 0xBFFFD) ||
                        (c >= 0xC0000 && c <= 0xCFFFD) ||
                        (c >= 0xD0000 && c <= 0xDFFFD) ||
                        (c >= 0xE0000 && c <= 0xEFFFD) ||
                        (c >= 0xF0000 && c <= 0xFFFFD) ||
                        (c >= 0x100000 && c <= 0x10FFFD);
    }

    private static final char[] _hex = "0123456789ABCDEF".toCharArray();
    static void byteToHex(final byte b, StringBuilder buffer) {
        int i = b & 0xFF;
        buffer.append(_hex[i >>> 4]);
        buffer.append(_hex[i & 0x0F]);
    }

    public static int hexToInt(final char c1, final char c2) {
        //TODO: Some micro-optimization here?
        return Integer.parseInt(new String(new char[]{c1, c2}), 16);
    }

    public static void percentEncode(final byte b, StringBuilder buffer) {
        buffer.append('%');
        byteToHex(b, buffer);
    }

    private static final List<String> RELATIVE_SCHEMES = Arrays.asList(
            "ftp", "file", "gopher", "http", "https", "ws", "wss"
    );

    /**
     * Returns true if the schema is a known relative schema
     * (ftp, file, gopher, http, https, ws, wss).
     *
     * @param scheme
     * @return
     */
    public static boolean isRelativeScheme(final String scheme) {
        return RELATIVE_SCHEMES.contains(scheme);
    }

    /**
     * Gets the default port for a given schema. That is:
     *
     * <ol>
     *     <li>ftp - 21</li>
     *     <li>file - null</li>
     *     <li>gopher - 70</li>
     *     <li>http - 80</li>
     *     <li>https - 443</li>
     *     <li>ws - 80</li>
     *     <li>wss - 433</li>
     * </ol>
     *
     * @param scheme
     * @return
     */
    public static String getDefaultPortForScheme(final String scheme) {
        if ("ftp".equals(scheme)) {
            return "21";
        }
        if ("file".equals(scheme)) {
            return null;
        }
        if ("gopher".equals(scheme)) {
            return "70";
        }
        if ("http".equals(scheme)) {
            return "80";
        }
        if ("https".equals(scheme)) {
            return "443";
        }
        if ("ws".equals(scheme)) {
            return "80";
        }
        if ("wss".equals(scheme)) {
            return "443";
        }
        return null;
    }

}
