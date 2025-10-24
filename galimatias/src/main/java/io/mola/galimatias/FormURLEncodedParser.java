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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses query strings with application/x-www-form-urlencoded rules.
 *
 * This class supersedes @{link java.net.URLEncoder}.
 *
 * @see <a href="https://url.spec.whatwg.org/#application/x-www-form-urlencoded-0">https://url.spec.whatwg.org/#application/x-www-form-urlencoded-0</a>
 */
public final class FormURLEncodedParser {

    private FormURLEncodedParser() {

    }

    private static final FormURLEncodedParser INSTANCE = new FormURLEncodedParser();

    @Deprecated
    public static FormURLEncodedParser getInstance() {
        return INSTANCE;
    }

    /**
     * Charset defaults to UTF-8.
     *
     * @see {@link #parse(String, java.nio.charset.Charset)}
     * @see <a href="https://url.spec.whatwg.org/#concept-urlencoded-parser">application/x-www-form-urlencoded parser</a>
     *
     * @param input
     */
    public static List<NameValue> parse(final String input) {
        return parse(input, Charset.forName("UTF-8"));
    }

    /**
     *
     * @see <a href="https://url.spec.whatwg.org/#concept-urlencoded-parser">application/x-www-form-urlencoded parser</a>
     *
     * @param input
     * @param charset Any charset. Note that the
     *                <a href="https://encoding.spec.whatwg.org/#concept-encoding-get">WHATWG Encoding Standard</a>
     *                imposes restrictions on what encodings can and cannot be used. This method does not enforce
     *                such restrictions.
     */
    public static List<NameValue> parse(final String input, final Charset charset) {
        final boolean isIndex = false;

        if (input == null) {
            throw new NullPointerException("input");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }

        //TODO: Encoding stuff is not here because we get Strings instead of bytes

        // 3. Let sequences be the result of splitting input on `&`.
        final String[] sequences = input.split("&");

        // 4. If the isindex flag is set and the first byte sequence in sequences does not contain
        //    a `=`, prepend `=` to the first byte sequence in sequences.
        if (isIndex) {
            if (sequences[0].isEmpty() || sequences[0].charAt(0) != '=') {
                sequences[0] = "=" + sequences[0];
            }
        }

        // 5. Let pairs be an empty list of name-value pairs where both name and value hold a byte sequence.
        final List<NameValue> pairs = new ArrayList<NameValue>();

        // 6. For each byte sequence bytes in sequences, run these substeps:
        for (final String bytes : sequences) {

            // 1. If bytes is the empty byte sequence, run these substeps for the next byte sequence.
            if (bytes.isEmpty()) {
                continue;
            }

            String name;
            String value;

            // 2. If bytes contains a `=`, then let name be the bytes from the start of bytes up to but excluding its
            //    first `=`, and let value be the bytes, if any, after the first `=` up to the end of bytes. If `=`
            //    is the first byte, then name will be the empty byte sequence. If it is the last, then value will be
            //    the empty byte sequence.
            final int equalsIndex = bytes.indexOf("=");
            if (equalsIndex != -1) {
                if (equalsIndex == 0) {
                    name = "";
                } else {
                    name = bytes.substring(0, equalsIndex);
                }
                if (equalsIndex == bytes.length() - 1) {
                    value = "";
                } else {
                    value = bytes.substring(equalsIndex + 1);
                }
            }

            // 3. Otherwise, let name have the value of bytes and let value be the empty byte sequence.
            else {
                name = bytes;
                value = "";
            }

            // 4. Replace any `+` in name and value with 0x20.
            name = name.replace('+', ' ');
            value = value.replace('+', ' ');

            // 5. If use _charset_ flag is set, name is `_charset_`, run these substeps:
            // TODO

            // 6. Add a pair consisting of name and value to pairs.
            pairs.add(new NameValue(name, value));

        }

        // 7. Let output be an empty list of name-value pairs where both name and value hold a string.
        // 8. For each name-value pair in pairs, append a name-value pair to output where the new name
        //    and value appended to output are the result of running encoding override's decoder on the percent
        //    decoding of the name and value from pairs, respectively.
        // TODO

        // 9. Return output.
        return pairs;
    }

    private static String serialize(ByteBuffer bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        return serialize(bytes.array(), bytes.arrayOffset(), bytes.limit());
    }

    /**
     * Implements the application/x-www-form-urlencoded byte serializer.
     */
    private static String serialize(final byte[] bytes, final int offset, final int length) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        final StringBuilder sb = new StringBuilder(length);
        for (int i = offset; i < offset + length; i++) {
            final byte b = bytes[i];
            if (0x20 == b) {
                sb.append((char)0x20);
            } else if (0x2A == b || 0x2D == b || 0x2E == b || (0x30 <= b && b <= 0x39)
                    || (0x41 <= b && b <= 0x5A) || 0x5F == b || (0x61 <= b && b <= 0x7A)) {
                sb.appendCodePoint(b);
            } else {
                URLUtils.percentEncode(b, sb);
            }
        }
        return sb.toString();
    }

    /**
     * Replacement for {@link java.net.URLEncoder#encode(String, String)}.
     *
     * Uses UTF-8 as default charset.
     *
     * @see {@link #encode(java.util.List, java.nio.charset.Charset)}
     *
     * @param input
     * @return
     */
    public static String encode(final String input) {
        // 1. If encoding override is not given, set it to utf-8.
        return encode(input, Charset.forName("UTF-8"));
    }

    /**
     * Replacement for {@link java.net.URLEncoder#encode(String, String)}.
     *
     * @see {@link #encode(java.util.List, java.nio.charset.Charset)}
     *
     * @param input
     * @param charset
     * @return
     */
    public static String encode(final String input, final Charset charset) {
        return encode(parse(input), charset);
    }

    /**
     * Implements the application/x-www-form-urlencoded serializer.
     *
     * Uses UTF-8 as default charset.
     *
     * @see {@link #encode(java.util.List, java.nio.charset.Charset)}
     *
     * @param input
     * @return
     */
    public static String encode(final List<NameValue> input) {
        // 1. If encoding override is not given, set it to utf-8.
        return encode(input, Charset.forName("UTF-8"));
    }

    /**
     * Implements the application/x-www-form-urlencoded serializer.
     *
     * @param input
     * @param charset
     * @return
     */
    public static String encode(final List<NameValue> input, final Charset charset) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        if (charset == null) {
            throw new NullPointerException("charset");
        }

        // 2. Let output be the empty string.
        final StringBuilder sb = new StringBuilder();

        // 3. For each pair in pairs, run these substeps:
        for (int i = 0; i < input.size(); i++) {
            final NameValue pair = input.get(i);

            // 1. Let outputPair be a copy of pair.
            // N/A

            // 2. Replace outputPair's name and value with the result of running encode on them using
            //    encoding override, respectively.
            // 3. Replace outputPair's name and value with their serialization.
            final String outputName = serialize(charset.encode(pair.name()));
            final String outputValue = serialize(charset.encode(pair.value()));

            // 4. If pair is not the first pair in pairs, append "&" to output.
            if (i != 0) {
                sb.append('&');
            }

            // 5. Append outputPair's name, followed by "=", followed by outputPair's value to output.
            sb.append(outputName).append('=').append(outputValue);
        }

        // 4. Return output.
        return sb.toString();
    }

}