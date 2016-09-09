/*
 * Copyright (c) 2016 Mozilla Foundation
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

import java.util.HashSet;
import java.util.ArrayList;

import org.relaxng.datatype.DatatypeException;

abstract class AbstractAutocompleteDetails extends AbstractDatatype {

    AbstractAutocompleteDetails() {
        super();
    }

    private static final HashSet<String> contactTypes = new HashSet<>();

    private static final HashSet<String> allFieldnames = new HashSet<>();

    private static final HashSet<String> allowedFieldnames = new HashSet<>();

    private static final HashSet<String> allowedContactFieldnames = new HashSet<>();

    static {
        contactTypes.add("home");
        contactTypes.add("work");
        contactTypes.add("mobile");
        contactTypes.add("fax");
        contactTypes.add("pager");

        allFieldnames.add("name");
        allFieldnames.add("honorific-prefix");
        allFieldnames.add("given-name");
        allFieldnames.add("additional-name");
        allFieldnames.add("family-name");
        allFieldnames.add("honorific-suffix");
        allFieldnames.add("nickname");
        allFieldnames.add("organization-title");
        allFieldnames.add("username");
        allFieldnames.add("new-password");
        allFieldnames.add("current-password");
        allFieldnames.add("organization");
        allFieldnames.add("street-address");
        allFieldnames.add("address-line1");
        allFieldnames.add("address-line2");
        allFieldnames.add("address-line3");
        allFieldnames.add("address-level4");
        allFieldnames.add("address-level3");
        allFieldnames.add("address-level2");
        allFieldnames.add("address-level1");
        allFieldnames.add("country");
        allFieldnames.add("country-name");
        allFieldnames.add("postal-code");
        allFieldnames.add("cc-name");
        allFieldnames.add("cc-given-name");
        allFieldnames.add("cc-additional-name");
        allFieldnames.add("cc-family-name");
        allFieldnames.add("cc-number");
        allFieldnames.add("cc-exp");
        allFieldnames.add("cc-exp-month");
        allFieldnames.add("cc-exp-year");
        allFieldnames.add("cc-csc");
        allFieldnames.add("cc-type");
        allFieldnames.add("transaction-currency");
        allFieldnames.add("transaction-amount");
        allFieldnames.add("language");
        allFieldnames.add("bday");
        allFieldnames.add("bday-day");
        allFieldnames.add("bday-month");
        allFieldnames.add("bday-year");
        allFieldnames.add("sex");
        allFieldnames.add("url");
        allFieldnames.add("photo");
        allFieldnames.add("tel");
        allFieldnames.add("tel-country-code");
        allFieldnames.add("tel-national");
        allFieldnames.add("tel-area-code");
        allFieldnames.add("tel-local");
        allFieldnames.add("tel-local-prefix");
        allFieldnames.add("tel-local-suffix");
        allFieldnames.add("tel-extension");
        allFieldnames.add("email");
        allFieldnames.add("impp");
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            throw newDatatypeException("Must not be empty.");
        }
        StringBuilder builder = new StringBuilder();
        ArrayList<String> detailTokens = new ArrayList<>();
        int len = literal.length();
        for (int i = 0; i < len; i++) {
            char c = literal.charAt(i);
            if (isWhitespace(c) && builder.length() > 0) {
                detailTokens.add(builder.toString());
                builder.setLength(0);
            } else if (!isWhitespace(c)) {
                builder.append(toAsciiLowerCase(c));
            }
        }
        if (builder.length() > 0) {
            detailTokens.add(builder.toString());
        }
        if (detailTokens.size() > 0) {
            checkTokens(detailTokens);
        }
    }

    private void checkTokens(ArrayList<String> detailTokens)
            throws DatatypeException {
        boolean isContactDetails = false;
        String contactType = "";
        String firstRemainingToken = "";
        if (contactTypes.contains(detailTokens.get(0))) {
            isContactDetails = true;
            contactType = detailTokens.get(0);
            detailTokens.remove(0);
        }
        if (detailTokens.size() > 0
                && detailTokens.get(0).startsWith("section-")) {
            if (isContactDetails) {
                throw newDatatypeException(
                        "A \u201csection-*\u201d indicator is not allowed"
                                + " when the first token in a list"
                                + " of autofill detail tokens is" + " \u201c"
                                + contactType + "\u201d.");
            }
            detailTokens.remove(0);
        }
        if (detailTokens.size() < 1) {
            return;
        }
        firstRemainingToken = detailTokens.get(0);
        if (firstRemainingToken.equals("shipping")
                || firstRemainingToken.equals("billing")) {
            if (isContactDetails) {
                throw newDatatypeException(
                        "The token \u201c" + firstRemainingToken + "\u201d is"
                                + " not allowed when the first token in a list"
                                + " of autofill detail tokens is" + " \u201c"
                                + contactType + "\u201d.");
            }
            detailTokens.remove(0);
        }
        for (String token : detailTokens) {
            if (contactTypes.contains(token)) {
                throw newDatatypeException(
                        "The token \u201c" + token + "\u201d must only"
                                + " appear as the first token in a list"
                                + " of autofill detail tokens.");
            } else if (token.startsWith("section-")) {
                throw newDatatypeException(
                        "A \u201csection-*\u201d indicator must only"
                                + " appear as the first token in a list"
                                + " of autofill detail tokens.");
            } else if ("shipping".equals(token) || "billing".equals(token)) {
                throw newDatatypeException(
                        "The token \u201c" + token + "\u201d must only"
                                + " appear as either the first token in a list"
                                + " of autofill detail tokens, or, if the first"
                                + " token is a \u201csection-*\u201d indicator,"
                                + " as the second token.");
            }
            if (!allFieldnames.contains(token)) {
                throw newDatatypeException("The string \u201c" + token
                        + "\u201d is not a valid autofill field name.");
            }
            if (isContactDetails
                    && !getAllowedContactFieldnames().contains(token)) {
                throw newDatatypeException("The autofill field name \u201c"
                        + token + "\u201d is not allowed in this context.");
            }
            if (!getAllowedFieldnames().contains(token)) {
                throw newDatatypeException("The autofill field name \u201c"
                        + token + "\u201d is not allowed in this context.");
            }
        }
        if (detailTokens.size() > 1) {
            throw newDatatypeException("A list of autofill details tokens must"
                    + " not contain more than one autofill field name.");
        }
    }

    public HashSet<String> getAllowedFieldnames() {
        return allowedFieldnames;
    }

    public HashSet<String> getAllowedContactFieldnames() {
        return allowedContactFieldnames;
    }
}
