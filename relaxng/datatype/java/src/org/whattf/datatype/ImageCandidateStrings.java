/*
 * Copyright (c) 2014 Mozilla Foundation
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

package org.whattf.datatype;

import java.util.ArrayList;
import java.util.List;

import org.relaxng.datatype.DatatypeException;

public class ImageCandidateStrings extends AbstractDatatype {

    private static enum State {
        SPLITTING_LOOP, URL, COLLECTING_DESCRIPTOR_TOKENS, AFTER_TOKEN
    }

    private static final int EXTRACT_LIMIT = 15;

    private static final float DEFAULT_DENSITY = (float) 1;

    private static final ImageCandidateURL IC_URL = ImageCandidateURL.THE_INSTANCE;

    public static final ImageCandidateStrings THE_INSTANCE = new ImageCandidateStrings();

    protected ImageCandidateStrings() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        if (literal.length() == 0) {
            err("Must contain one or more image candidate strings.");
        }
        List<String> urls = new ArrayList<String>();
        List<Integer> widths = new ArrayList<Integer>();
        List<Float> denses = new ArrayList<Float>();
        StringBuilder url = new StringBuilder();
        StringBuilder token = new StringBuilder();
        CharSequence extract;
        boolean eof = false;
        boolean waitingForCandidate = true;
        int ix = 0;
        State state = State.SPLITTING_LOOP;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            eof = i == literal.length() - 1;
            switch (state) {
                case SPLITTING_LOOP:
                    if (isWhitespace(c)) {
                        continue;
                    } else if (',' == c) {
                        if (urls.isEmpty()) {
                            err("Starts with empty image-candidate string.");
                        }
                        if (waitingForCandidate) {
                            errEmpty(literal.subSequence(0, i + 1));
                        }
                        commaHandler(literal.subSequence(0, i + 1));
                        waitingForCandidate = true;
                        url.setLength(0);
                        continue;
                    }
                    // fall through
                case URL:
                    waitingForCandidate = false;
                    if (eof || isWhitespace(c)) {
                        if (!isWhitespace(c)) {
                            url.append(c);
                        }
                        state = State.COLLECTING_DESCRIPTOR_TOKENS;
                        if (endsWithComma(url)) {
                            url.deleteCharAt(url.length() - 1);
                            waitingForCandidate = true;
                            state = State.SPLITTING_LOOP;
                        }
                        IC_URL.checkValid(url);
                        urls.add(url.toString());
                        url.setLength(0);
                        token.setLength(0);
                        if (eof || waitingForCandidate) {
                            widths = adjustWidths(widths, ix);
                            denses = adjustDenses(urls, denses, ix);
                            ix++;
                        }
                        continue;
                    } else {
                        url.append(c);
                        state = State.URL;
                        continue;
                    }
                case COLLECTING_DESCRIPTOR_TOKENS: // spec labels this "Start"
                    extract = literal.subSequence(0, i + 1);
                    if (isWhitespace(c)) {
                        checkToken(token, extract, urls, widths, denses, ix);
                        token.setLength(0);
                        state = State.AFTER_TOKEN;
                        continue;
                    } else if (',' == c) {
                        checkToken(token, extract, urls, widths, denses, ix);
                        widths = adjustWidths(widths, ix);
                        denses = adjustDenses(urls, denses, ix);
                        ix++;
                        waitingForCandidate = true;
                        state = State.SPLITTING_LOOP;
                        continue;
                    } else if (eof) {
                        token.append(c);
                        checkToken(token, extract, urls, widths, denses, ix);
                        widths = adjustWidths(widths, ix);
                        denses = adjustDenses(urls, denses, ix);
                        break;
                    } else {
                        token.append(c);
                        continue;
                    }
                case AFTER_TOKEN:
                    extract = literal.subSequence(0, i + 1);
                    if (isWhitespace(c)) {
                        if (eof) {
                            checkToken(token, extract, urls, widths, denses, ix);
                            widths = adjustWidths(widths, ix);
                            denses = adjustDenses(urls, denses, ix);
                            break;
                        }
                        continue;
                    } else {
                        i--;
                        state = State.COLLECTING_DESCRIPTOR_TOKENS;
                    }
            }
        }
        if (waitingForCandidate) {
            err("Ends with empty image-candidate string.");
        }
    }

    private int commaHandler(CharSequence cs) throws DatatypeException {
        if (',' != cs.charAt(cs.length() - 1)) {
            return cs.length();
        }
        for (int i = cs.length() - 2; i > 0; i--) {
            if (',' != cs.charAt(i)) {
                break;
            }
            errEmpty(cs);
        }
        return cs.length() - 1;
    }

    private boolean endsWithComma(CharSequence cs) throws DatatypeException {
        int end = commaHandler(cs);
        return end == cs.length() - 1;
    }

    private void checkToken(StringBuilder token, CharSequence cs,
            List<String> urls, List<Integer> widths, List<Float> denses, int ix)
            throws DatatypeException {
        if (token.length() > 0) {
            if (widths.size() > ix || denses.size() > ix) {
                errExtraDescriptor(token, cs);
            }
            char last = token.charAt(token.length() - 1);
            if (!('w' == last || 'x' == last)) {
                err("Expected a number followed by \u201cw\u201d or"
                        + " \u201cx\u201d but found \u201c" + token
                        + "\u201d at \u201c" + extract(cs) + "\u201d.");
            }
            String number = token.subSequence(0, token.length() - 1).toString();
            if ('-' == token.charAt(0)) {
                err("Expected a positive number but found \u201c" + number
                        + "\u201d at \u201c" + extract(cs) + "\u201d.");
            }
            if ('w' == last) {
                try {
                    int width = Integer.parseInt(number, 10);
                    if (width < 0) {
                        errNegativeNumber(number, cs);
                    }
                    if (!widths.isEmpty() && widths.contains(width)) {
                        errSameWidth(urls.get(ix),
                                urls.get(widths.indexOf(width)));
                    }
                    widths.add(width);
                    denses.add(null);
                } catch (NumberFormatException e) {
                    err("Expected an integer but found \u201c" + number
                            + "\u201d at \u201c" + extract(cs) + "\u201d");
                }
            }
            if ('x' == last) {
                try {
                    float density = Float.parseFloat(number);
                    if (density < 0) {
                        errNegativeNumber(number, cs);
                    }
                    if (!denses.isEmpty() && denses.contains(density)) {
                        errSameDensity(urls.get(ix),
                                urls.get(denses.indexOf(density)));
                    }
                    denses.add(density);
                    widths.add(null);
                } catch (NumberFormatException e) {
                    err("Expected a floating-point number but found \u201c"
                            + number + "\u201d at \u201c" + extract(cs)
                            + "\u201d");
                }
            }
        }
    }

    private List<Integer> adjustWidths(List<Integer> widths, int ix)
            throws DatatypeException {
        if (widths.size() == ix) {
            widths.add(null);
        }
        return widths;
    }

    private List<Float> adjustDenses(List<String> urls, List<Float> denses,
            int ix) throws DatatypeException {
        if (denses.size() == ix) {
            if (denses.indexOf(DEFAULT_DENSITY) != -1) {
                errSameDensity(urls.get(ix),
                        urls.get(denses.indexOf(DEFAULT_DENSITY)));
            }
            denses.add(DEFAULT_DENSITY);
        }
        return denses;
    }

    private void err(String message) throws DatatypeException {
        throw newDatatypeException(message);
    }

    private void errEmpty(CharSequence cs) throws DatatypeException {
        err("Empty image-candidate string at \u201c" + extract(cs) + "\u201d.");
    }

    private void errSameWidth(CharSequence url1, CharSequence url2)
            throws DatatypeException {
        err("Width for image \u201c" + extract(url1)
                + "\u201d is identical to width for image \u201c"
                + extract(url2) + "\u201d.");
    }

    private void errSameDensity(CharSequence url1, CharSequence url2)
            throws DatatypeException {
        err("Density for image \u201c" + extract(url1)
                + "\u201d is identical to density for image \u201c"
                + extract(url2) + "\u201d.");
    }

    private void errExtraDescriptor(StringBuilder token, CharSequence cs)
            throws DatatypeException {
        err("Image candidate string has extraneous descriptor \u201c" + token
                + "\u201d at \u201c" + extract(cs) + "\u201d");
    }

    private void errNegativeNumber(String number, CharSequence cs)
            throws DatatypeException {
        err("Negative number \u201c" + number
                + "\u201d in descriptor at \u201c" + extract(cs) + "\u201d");
    }

    private CharSequence extract(CharSequence extract) {
        int len = extract.length();
        if (len > EXTRACT_LIMIT) {
            extract = "\u2026" + extract.subSequence(len - EXTRACT_LIMIT, len);
        }
        return extract;
    }

    protected boolean widthRequired() {
        return false;
    }

    @Override public String getName() {
        return "image candidate strings";
    }

}
