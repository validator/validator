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

import static io.mola.galimatias.URLUtils.UTF_8;
import static io.mola.galimatias.URLUtils.isASCIIHexDigit;
import static io.mola.galimatias.URLUtils.percentEncode;

abstract class BaseURLCanonicalizer implements URLCanonicalizer {

  protected static String canonicalize(String input, CharacterPredicate unencodedPredicate) {
    StringBuilder result = new StringBuilder();
    final int length = input.length();
    for (int offset = 0; offset < length; ) {
        final int c = input.codePointAt(offset);

        if ((c == '%' && input.length() > offset + 2 &&
                isASCIIHexDigit(input.charAt(offset + 1)) && isASCIIHexDigit(input.charAt(offset + 2))) ||
            unencodedPredicate.test(c)) {
            result.append((char) c);
        } else {
            final byte[] bytes = new String(Character.toChars(c)).getBytes(UTF_8);
            for (final byte b : bytes) {
                percentEncode(b, result);
            }
        }

        offset += Character.charCount(c);
    }
    return result.toString();
  }

}
