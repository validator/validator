/*
 * Copyright (c) 2015 Mozilla Foundation
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

package nu.validator.datatype;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;

import com.shapesecurity.salvation.data.Base64Value;

public final class IntegrityMetadata extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final IntegrityMetadata THE_INSTANCE = new IntegrityMetadata();

    private static final Pattern THE_PATTERN = Pattern.compile(
            "^(?:sha256-|sha384-|sha512)(.+$)", Pattern.CASE_INSENSITIVE);

    private IntegrityMetadata() {
        super();
    }

    private final static boolean WARN = System.getProperty(
            "nu.validator.datatype.warn", "").equals("true") ? true : false;

    public void checkValid(CharSequence literal) throws DatatypeException {
        Matcher m = getPattern().matcher(literal);
        if (m.matches()) {
            try {
                new Base64Value(m.group(1));
            } catch (IllegalArgumentException e) {
                throw newDatatypeException(e.getMessage(), WARN);
            }
        } else {
            throw newDatatypeException("The value must start with"
                    + " \u201csha256-\u201d or \u201csha384-\u201d"
                    + " or \u201csha512-\u201d.");
        }
    }

    /**
     * Returns the regexp for this datatype.
     *
     * @return the base regexp for this datatype
     * @see nu.validator.datatype.AbstractDatetime#getPattern()
     */
    protected Pattern getPattern() {
        return THE_PATTERN;
    }

    @Override public String getName() {
        return "integrity-metadata";
    }
}
