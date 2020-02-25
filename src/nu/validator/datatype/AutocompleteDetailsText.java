/*
 * Copyright (c) 2016-2019 Mozilla Foundation
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

public final class AutocompleteDetailsText extends AbstractAutocompleteDetails {

    /**
     * The singleton instance.
     */
    public static final AutocompleteDetailsText THE_INSTANCE = new AutocompleteDetailsText();

    private AutocompleteDetailsText() {
        super();
    }

    private static final HashSet<String> allowedFieldnames = new HashSet<>();

    private static final HashSet<String> allowedContactFieldnames = new HashSet<>();

    static {
        allowedFieldnames.add("name");
        allowedFieldnames.add("honorific-prefix");
        allowedFieldnames.add("given-name");
        allowedFieldnames.add("additional-name");
        allowedFieldnames.add("family-name");
        allowedFieldnames.add("honorific-suffix");
        allowedFieldnames.add("nickname");
        allowedFieldnames.add("organization-title");
        allowedFieldnames.add("username");
        allowedFieldnames.add("new-password");
        allowedFieldnames.add("current-password");
        allowedFieldnames.add("one-time-code");
        allowedFieldnames.add("organization");
        allowedFieldnames.add("address-line1");
        allowedFieldnames.add("address-line2");
        allowedFieldnames.add("address-line3");
        allowedFieldnames.add("address-level4");
        allowedFieldnames.add("address-level3");
        allowedFieldnames.add("address-level2");
        allowedFieldnames.add("address-level1");
        allowedFieldnames.add("country");
        allowedFieldnames.add("country-name");
        allowedFieldnames.add("postal-code");
        allowedFieldnames.add("cc-name");
        allowedFieldnames.add("cc-given-name");
        allowedFieldnames.add("cc-additional-name");
        allowedFieldnames.add("cc-family-name");
        allowedFieldnames.add("cc-number");
        allowedFieldnames.add("cc-exp");
        allowedFieldnames.add("cc-exp-month");
        allowedFieldnames.add("cc-exp-year");
        allowedFieldnames.add("cc-csc");
        allowedFieldnames.add("cc-type");
        allowedFieldnames.add("transaction-currency");
        allowedFieldnames.add("transaction-amount");
        allowedFieldnames.add("language");
        allowedFieldnames.add("bday");
        allowedFieldnames.add("bday-day");
        allowedFieldnames.add("bday-month");
        allowedFieldnames.add("bday-year");
        allowedFieldnames.add("sex");
        allowedFieldnames.add("url");
        allowedFieldnames.add("photo");
        allowedFieldnames.add("tel");
        allowedFieldnames.add("tel-country-code");
        allowedFieldnames.add("tel-national");
        allowedFieldnames.add("tel-area-code");
        allowedFieldnames.add("tel-local");
        allowedFieldnames.add("tel-local-prefix");
        allowedFieldnames.add("tel-local-suffix");
        allowedFieldnames.add("tel-extension");
        allowedFieldnames.add("email");
        allowedFieldnames.add("impp");

        allowedContactFieldnames.add("tel");
        allowedContactFieldnames.add("tel-country-code");
        allowedContactFieldnames.add("tel-national");
        allowedContactFieldnames.add("tel-area-code");
        allowedContactFieldnames.add("tel-local");
        allowedContactFieldnames.add("tel-local-prefix");
        allowedContactFieldnames.add("tel-local-suffix");
        allowedContactFieldnames.add("tel-extension");
        allowedContactFieldnames.add("email");
        allowedContactFieldnames.add("impp");
    }

    @Override
    public HashSet<String> getAllowedFieldnames() {
        return allowedFieldnames;
    }

    @Override
    public HashSet<String> getAllowedContactFieldnames() {
        return allowedContactFieldnames;
    }

    @Override
    public String getName() {
        return "autocomplete detail tokens (text)";
    }
}
