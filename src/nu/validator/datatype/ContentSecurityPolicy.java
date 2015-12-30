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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.relaxng.datatype.DatatypeException;

import com.shapesecurity.salvation.Parser;
import com.shapesecurity.salvation.Parser.ParseException;
import com.shapesecurity.salvation.Tokeniser.TokeniserException;
import com.shapesecurity.salvation.data.Warning;

public class ContentSecurityPolicy extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final ContentSecurityPolicy THE_INSTANCE = new ContentSecurityPolicy();

    private static final Pattern UNRECOGNISED_DIRECTIVE_NAME_PATTERN = Pattern.compile("^(Unrecognised directive name: )(.+)$");

    private static final Pattern BUT_FOUND_PATTERN = Pattern.compile("^(.+ but found )(.+)$");

    private static final Pattern BAD_CODE_POINT_PATTERN = Pattern.compile("^("
            + "expecting directive-value but found )(U\\+[^ ]+) \\((.)\\)\\."
            + " (Non-ASCII and non-printable characters must be percent-encoded)$");

    private static final String DIRECTIVE_NAME = " ("
            + "frame-src|child-src|default-src|allow|options|referrer"
            + "|upgrade-insecure-requests|block-all-mixed-content) ";

    protected ContentSecurityPolicy() {
        super();
    }

    private final static boolean WARN = System.getProperty(
            "nu.validator.datatype.warn", "").equals("true") ? true : false;

    public void checkValid(CharSequence literal) throws DatatypeException {
        try {
            List<Warning> warnings = new ArrayList<Warning>();
            Parser.parse(literal.toString(), "http://example.org", warnings);
            if (!warnings.isEmpty() && WARN) {
                StringBuilder sb = new StringBuilder();
                for (Warning w : warnings) {
                    sb.append(w.show().replaceAll(DIRECTIVE_NAME,
                            " \u201c$1\u201d ")
                            + " ");
                }
                throw newDatatypeException(sb.toString().trim(), WARN);
            }
        } catch (ParseException e) {
            String msg = e.getMessage().replaceAll(DIRECTIVE_NAME,
                    " \u201c$1\u201d ");
            Matcher m = BUT_FOUND_PATTERN.matcher(msg);
            if (m.matches()) {
                throw newDatatypeException(String.format("%s\u201c%s\u201d",
                        m.group(1), m.group(2)));
            } else {
                throw newDatatypeException(msg);
            }
        } catch (TokeniserException e) {
            String msg = e.getMessage();
            Matcher m = BAD_CODE_POINT_PATTERN.matcher(msg);
            if (m.matches()) {
                throw newDatatypeException(String.format(
                        "%s\u201c%s\u201d (%s). %s", m.group(1), m.group(3),
                        m.group(2), m.group(4)));
            } else {
                Matcher n = BUT_FOUND_PATTERN.matcher(msg);
                if (n.matches()) {
                    throw newDatatypeException(String.format(
                            "%s\u201c%s\u201d", n.group(1), n.group(2)));
                } else {
                    throw newDatatypeException(msg);
                }
            }
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            Matcher m = UNRECOGNISED_DIRECTIVE_NAME_PATTERN.matcher(msg);
            if (m.matches()) {
                throw newDatatypeException(String.format("%s\u201c%s\u201d",
                        m.group(1), m.group(2)));
            } else {
                throw newDatatypeException(msg);
            }
        }
    }

    @Override public String getName() {
        return "content security policy";
    }

}
