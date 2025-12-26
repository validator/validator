/*
 * Copyright (c) 2015-2018 Mozilla Foundation
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

import com.shapesecurity.salvation2.Policy;

public class ContentSecurityPolicy extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final ContentSecurityPolicy THE_INSTANCE = new ContentSecurityPolicy();

    private static final String DIRECTIVE_NAME = " \\b("
            + "child-src|connect-src|default-src|font-src|frame-src|img-src"
            + "|manifest-src|media-src|object-src|prefetch-src|script-src"
            + "|style-src|worker-src|allow|options|referrer|report-uri"
            + "|upgrade-insecure-requests|block-all-mixed-content|report-to)";

    private static final String SANDBOX_KEYWORDS = "("
            + "allow-forms|allow-modals|allow-pointer-lock"
            + "|allow-downloads|allow-orientation-lock|allow-presentation"
            + "|allow-popups-to-escape-sandbox|allow-popups|allow-same-origin"
            + "|allow-scripts|allow-top-navigation"
            + "|allow-top-navigation-by-user-activation"
            + "|allow-top-navigation-to-custom-protocols)";

    protected ContentSecurityPolicy() {
        super();
    }

    private final static boolean WARN = System.getProperty(
            "nu.validator.datatype.warn", "").equals("true");

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        StringBuilder errors = new StringBuilder();
        StringBuilder warnings = new StringBuilder();
        StringBuilder others = new StringBuilder();
        String policyText = literal.toString()
                .replace("allow-downloads", "")
                .replace("allow-presentation", "");
        try {
            if (policyText.contains(",")) {
                Policy.parseSerializedCSPList(policyText,
                        (severity, message, policyIndex, directiveIndex,
                                valueIndex) -> {
                            if (message.contains("experimental directive")) {
                                return;
                            }
                            String formattedMessage = message
                                .replaceAll(SANDBOX_KEYWORDS, "\u201c$1\u201d")
                                .replaceAll(DIRECTIVE_NAME, " \u201c$1\u201d ")
                                    + " ";
                            switch (severity) {
                                case Error:
                                    errors.append(formattedMessage);
                                    break;
                                case Warning:
                                    warnings.append(formattedMessage);
                                    break;
                                case Info:
                                    others.append(formattedMessage);
                                    break;
                            }
                        });
            } else {
                Policy.parseSerializedCSP(policyText,
                        (severity, message, directiveIndex, valueIndex) -> {
                            if (message.contains("experimental directive")) {
                                return;
                            }
                            String formattedMessage = message
                                .replaceAll(SANDBOX_KEYWORDS, "\u201c$1\u201d")
                                .replaceAll(DIRECTIVE_NAME, " \u201c$1\u201d ")
                                    + " ";
                            switch (severity) {
                                case Error:
                                    errors.append(formattedMessage);
                                    break;
                                case Warning:
                                    warnings.append(formattedMessage);
                                    break;
                                case Info:
                                    others.append(formattedMessage);
                                    break;
                            }
                        });
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("string is not ascii")) {
                throw newDatatypeException("Content Security Policy must contain only ASCII characters.");
            }
            throw newDatatypeException(e.getMessage());
        }
        if (errors.length() > 0) {
            throw newDatatypeException(errors.toString());
        } else if (warnings.length() > 0) {
            throw newDatatypeException(warnings.toString(), WARN);
        } else if (others.length() > 0) {
            throw newDatatypeException(others.toString(), WARN);
        }
    }

    @Override
    public String getName() {
        return "content security policy";
    }

}
