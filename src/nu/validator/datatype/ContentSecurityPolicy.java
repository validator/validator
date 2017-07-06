/*
 * Copyright (c) 2017 Mozilla Foundation
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

import org.relaxng.datatype.DatatypeException;

import com.shapesecurity.salvation.Parser;
import com.shapesecurity.salvation.data.Notice;

public class ContentSecurityPolicy extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final ContentSecurityPolicy THE_INSTANCE = new ContentSecurityPolicy();

    private static final String DIRECTIVE_NAME = " ("
            + "child-src|connect-src|default-src|font-src|img-src|manifest-src"
            + "|media-src|object-src|script-src|style-src|worker-src|frame-src"
            + "|allow|options|referrer|upgrade-insecure-requests"
            + "|block-all-mixed-content|report-to|report-uri) ";

    private static final String SANDBOX_KEYWORDS = "("
            + "allow-forms|allow-modals|allow-pointer-lock"
            + "|allow-popups-to-escape-sandbox|allow-popups|allow-same-origin"
            + "|allow-scripts|allow-top-navigation)";

    protected ContentSecurityPolicy() {
        super();
    }

    private final static boolean WARN = System.getProperty(
            "nu.validator.datatype.warn", "").equals("true");

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        List<Notice> notices = new ArrayList<>();
        StringBuilder errors = new StringBuilder();
        StringBuilder warnings = new StringBuilder();
        StringBuilder others = new StringBuilder();
        Parser.parse(literal.toString(), "http://example.org", notices);
        if (!notices.isEmpty()) {
            for (Notice notice : notices) {
                if (notice.show().contains("experimental directive")) {
                    continue;
                }
                String message = notice.show().replaceAll(DIRECTIVE_NAME,
                        " \u201c$1\u201d ").replaceAll(SANDBOX_KEYWORDS,
                                "\u201c$1\u201d")
                        + " ";
                if (notice.isError()) {
                    errors.append(message);
                } else if (notice.isWarning()) {
                    warnings.append(message);
                } else if (notice.isInfo()) {
                    others.append(message);
                }
            }
            if (errors.length() > 0) {
                throw newDatatypeException(errors.toString());
            } else if (warnings.length() > 0) {
                throw newDatatypeException(warnings.toString(), WARN);
            } else if (others.length() > 0) {
                throw newDatatypeException(others.toString(), WARN);
            }
        }
    }

    @Override
    public String getName() {
        return "content security policy";
    }

}
