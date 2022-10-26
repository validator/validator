/*
 * Copyright (c) 2016-2020 Mozilla Foundation
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

    private static final HashSet<String> CONTACT_TYPES = new HashSet<>();

    private static final HashSet<String> ALL_FIELD_NAMES = new HashSet<>();

    private static final HashSet<String> allowedFieldnames = new HashSet<>();

    private static final HashSet<String> allowedContactFieldnames = new HashSet<>();

    static {
        CONTACT_TYPES.add("home");
        CONTACT_TYPES.add("work");
        CONTACT_TYPES.add("mobile");
        CONTACT_TYPES.add("fax");
        CONTACT_TYPES.add("pager");

        ALL_FIELD_NAMES.add("name");
        ALL_FIELD_NAMES.add("honorific-prefix");
        ALL_FIELD_NAMES.add("given-name");
        ALL_FIELD_NAMES.add("additional-name");
        ALL_FIELD_NAMES.add("family-name");
        ALL_FIELD_NAMES.add("honorific-suffix");
        ALL_FIELD_NAMES.add("nickname");
        ALL_FIELD_NAMES.add("organization-title");
        ALL_FIELD_NAMES.add("username");
        ALL_FIELD_NAMES.add("new-password");
        ALL_FIELD_NAMES.add("current-password");
        ALL_FIELD_NAMES.add("one-time-code");
        ALL_FIELD_NAMES.add("organization");
        ALL_FIELD_NAMES.add("street-address");
        ALL_FIELD_NAMES.add("address-line1");
        ALL_FIELD_NAMES.add("address-line2");
        ALL_FIELD_NAMES.add("address-line3");
        ALL_FIELD_NAMES.add("address-level4");
        ALL_FIELD_NAMES.add("address-level3");
        ALL_FIELD_NAMES.add("address-level2");
        ALL_FIELD_NAMES.add("address-level1");
        ALL_FIELD_NAMES.add("country");
        ALL_FIELD_NAMES.add("country-name");
        ALL_FIELD_NAMES.add("postal-code");
        ALL_FIELD_NAMES.add("cc-name");
        ALL_FIELD_NAMES.add("cc-given-name");
        ALL_FIELD_NAMES.add("cc-additional-name");
        ALL_FIELD_NAMES.add("cc-family-name");
        ALL_FIELD_NAMES.add("cc-number");
        ALL_FIELD_NAMES.add("cc-exp");
        ALL_FIELD_NAMES.add("cc-exp-month");
        ALL_FIELD_NAMES.add("cc-exp-year");
        ALL_FIELD_NAMES.add("cc-csc");
        ALL_FIELD_NAMES.add("cc-type");
        ALL_FIELD_NAMES.add("transaction-currency");
        ALL_FIELD_NAMES.add("transaction-amount");
        ALL_FIELD_NAMES.add("language");
        ALL_FIELD_NAMES.add("bday");
        ALL_FIELD_NAMES.add("bday-day");
        ALL_FIELD_NAMES.add("bday-month");
        ALL_FIELD_NAMES.add("bday-year");
        ALL_FIELD_NAMES.add("sex");
        ALL_FIELD_NAMES.add("url");
        ALL_FIELD_NAMES.add("photo");
        ALL_FIELD_NAMES.add("tel");
        ALL_FIELD_NAMES.add("tel-country-code");
        ALL_FIELD_NAMES.add("tel-national");
        ALL_FIELD_NAMES.add("tel-area-code");
        ALL_FIELD_NAMES.add("tel-local");
        ALL_FIELD_NAMES.add("tel-local-prefix");
        ALL_FIELD_NAMES.add("tel-local-suffix");
        ALL_FIELD_NAMES.add("tel-extension");
        ALL_FIELD_NAMES.add("email");
        ALL_FIELD_NAMES.add("impp");
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        String trimmed = trimWhitespace(literal.toString());
        if (trimmed.length() == 0) {
            throw newDatatypeException("Must not be empty.");
        }
        StringBuilder builder = new StringBuilder();
        ArrayList<String> detailTokens = new ArrayList<>();
        int len = trimmed.length();
        for (int i = 0; i < len; i++) {
            char c = trimmed.charAt(i);
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
        if (detailTokens.size() < 1) {
            return;
        }
        if (CONTACT_TYPES.contains(detailTokens.get(0))) {
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
        if (detailTokens.size() > 0
                && CONTACT_TYPES.contains(detailTokens.get(0))) {
            isContactDetails = true;
            contactType = detailTokens.get(0);
            detailTokens.remove(0);
        }
        for (String token : detailTokens) {
            if (CONTACT_TYPES.contains(token)) {
                throw newDatatypeException(
                        "The token \u201c" + token + "\u201d must only"
                                + " appear before any autofill field names.");
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
            if (!ALL_FIELD_NAMES.contains(token)) {
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
