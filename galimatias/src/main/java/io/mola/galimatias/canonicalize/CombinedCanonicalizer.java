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

import java.util.ArrayList;
import java.util.List;

public class CombinedCanonicalizer implements URLCanonicalizer {

    private final List<URLCanonicalizer> canonicalizers;

    public CombinedCanonicalizer(final URLCanonicalizer ... canons) {
        canonicalizers = new ArrayList<URLCanonicalizer>();
        for (final URLCanonicalizer canon : canons) {
            if (canon instanceof CombinedCanonicalizer) {
                canonicalizers.addAll(((CombinedCanonicalizer) canon).canonicalizers);
            } else {
                canonicalizers.add(canon);
            }
        }
    }

    @Override
    public URL canonicalize(final URL input) throws GalimatiasParseException {
        URL result = input;
        for (final URLCanonicalizer canon : canonicalizers) {
            result = canon.canonicalize(result);
        }
        return result;
    }

}
