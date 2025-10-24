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
package io.mola.galimatias.canonicalize;

import io.mola.galimatias.GalimatiasParseException;
import io.mola.galimatias.URL;
import io.mola.galimatias.URLUtils;

public class DecodeUnreservedCanonicalizer implements URLCanonicalizer {

    @Override
    public URL canonicalize(final URL input) throws GalimatiasParseException {
        if (input == null) {
            return input;
        }
        URL output = input;
        if (output.isHierarchical()) {
            output = output
                    .withUsername(decodeUnreserved(output.username()))
                    .withPassword(decodeUnreserved(output.password()))
                    .withPath(decodeUnreserved(output.path()));
        }
        return output
                .withQuery(decodeUnreserved(output.query()))
                .withFragment(decodeUnreserved(output.fragment()));
    }

    private static String decodeUnreserved(final String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        final StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c == '%' && input.length() > i + 2 &&
                    URLUtils.isASCIIHexDigit(input.charAt(i + 1)) &&
                    URLUtils.isASCIIHexDigit(input.charAt(i + 2))) {
                final int d = URLUtils.hexToInt(input.charAt(i + 1), input.charAt(i + 2));
                if (URLUtils.isASCIIAlphanumeric(d) || d == 0x2D || d == 0x2E || d == 0x5F || d == 0x7E) {
                    output.appendCodePoint(d);
                } else {
                    output.append(input.substring(i, i + 3));
                }
                i += 2;
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

}
