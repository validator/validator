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

import java.util.regex.Pattern;

public class RegexCanonicalizer implements URLCanonicalizer {

    public static enum Scope {
        HOST,
        PATH,
        QUERY,
        FRAGMENT,
        FULL
    }

    private final Scope scope;
    private final Pattern pattern;
    private final String substitution;

    public RegexCanonicalizer(final Scope scope, final Pattern pattern, final String substitution) {
        this.scope = scope;
        this.pattern = pattern;
        this.substitution = substitution;
    }

    @Override
    public URL canonicalize(final URL input) throws GalimatiasParseException {
        switch (scope) {
            case HOST:
                return input.withHost(pattern.matcher(input.host().toString()).replaceAll(substitution));
            case PATH:
                return input.withPath(pattern.matcher(input.path()).replaceAll(substitution));
            case QUERY:
                return input.withQuery(pattern.matcher(input.query()).replaceAll(substitution));
            case FRAGMENT:
                return input.withFragment(pattern.matcher(input.fragment()).replaceAll(substitution));
            case FULL:
                return URL.parse(pattern.matcher(input.toString()).replaceAll(substitution));
            default:
                return input;
        }
    }

}
