/*
 * Copyright (c) 2008-2010 Mozilla Foundation
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

import nu.validator.vendor.relaxng.datatype.DatatypeException;

public class MetaCharset extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MetaCharset THE_INSTANCE = new MetaCharset();

    public MetaCharset() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        String lower = toAsciiLowerCase(literal);
        if (!lower.startsWith("text/html;")) {
            throw newDatatypeException(
                    "The legacy encoding declaration did not start with ",
                    "text/html;", ".");
        }
        if (lower.length() == 10) {
            throw newDatatypeException(
                    "The legacy encoding declaration ended prematurely.");
        }
        // Extract charset using the WHATWG algorithm which loops to find
        // a valid "charset=" pattern. See:
        // https://html.spec.whatwg.org/#algorithm-for-extracting-a-character-encoding-from-a-meta-element
        String encodingName = extractCharset(lower, 10);
        if (encodingName == null) {
            throw newDatatypeException(
                    "The legacy encoding declaration did not contain ",
                    "charset=", " followed by a valid encoding name.");
        }
        if (!"utf-8".equals(encodingName)) {
            throw newDatatypeException(
                    "“charset=” must be followed by"
                            + " “utf-8”.");
        }
    }

    /**
     * Extracts charset from a content attribute value using the WHATWG
     * algorithm. This properly handles cases like "charset charset=utf-8"
     * where the first "charset" is not followed by "=".
     *
     * @param lower the lowercased content attribute value
     * @param start the position to start searching from
     * @return the extracted encoding name, or null if not found
     */
    private String extractCharset(String lower, int start)
            throws DatatypeException {
        int pos = start;
        int len = lower.length();

        // Loop: search for "charset"
        while (pos < len) {
            int charsetPos = lower.indexOf("charset", pos);
            if (charsetPos == -1) {
                return null;
            }
            pos = charsetPos + 7; // Move past "charset"

            // Skip ASCII whitespace
            while (pos < len && isAsciiWhitespace(lower.charAt(pos))) {
                pos++;
            }

            // Check for '='
            if (pos >= len) {
                return null;
            }
            if (lower.charAt(pos) != '=') {
                // Not followed by '=', continue searching for another "charset"
                continue;
            }
            pos++; // Move past '='

            // Skip ASCII whitespace after '='
            while (pos < len && isAsciiWhitespace(lower.charAt(pos))) {
                pos++;
            }

            if (pos >= len) {
                throw newDatatypeException(
                        "The empty string is not a valid character encoding name.");
            }

            // Extract the encoding value
            char c = lower.charAt(pos);
            int valueStart;
            int valueEnd;

            if (c == '"') {
                // Double-quoted value
                valueStart = pos + 1;
                valueEnd = lower.indexOf('"', valueStart);
                if (valueEnd == -1) {
                    return null;
                }
            } else if (c == '\'') {
                // Single-quoted value
                valueStart = pos + 1;
                valueEnd = lower.indexOf('\'', valueStart);
                if (valueEnd == -1) {
                    return null;
                }
            } else {
                // Unquoted value: read until whitespace or semicolon
                valueStart = pos;
                valueEnd = pos;
                while (valueEnd < len) {
                    char vc = lower.charAt(valueEnd);
                    if (isAsciiWhitespace(vc) || vc == ';') {
                        break;
                    }
                    valueEnd++;
                }
            }

            if (valueStart == valueEnd) {
                throw newDatatypeException(
                        "The empty string is not a valid character encoding name.");
            }

            String encodingName = lower.substring(valueStart, valueEnd);
            // Validate encoding name characters
            for (int i = 0; i < encodingName.length(); i++) {
                char ec = encodingName.charAt(i);
                if (!((ec >= '0' && ec <= '9') || (ec >= 'a' && ec <= 'z')
                        || ec == '-' || ec == '!' || ec == '#' || ec == '$'
                        || ec == '%' || ec == '&' || ec == '\'' || ec == '+'
                        || ec == '_' || ec == '`' || ec == '{' || ec == '}'
                        || ec == '~' || ec == '^')) {
                    throw newDatatypeException(
                            "The legacy encoding contained ", ec,
                            ", which is not a valid character"
                                    + " in an encoding name.");
                }
            }
            return encodingName;
        }
        return null;
    }

    private static boolean isAsciiWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\u000C' || c == '\r';
    }

    @Override
    public String getName() {
        return "legacy character encoding declaration";
    }

}
