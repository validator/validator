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
        List<CharSequence> urls = new ArrayList<CharSequence>();
        List<Integer> widths = new ArrayList<Integer>();
        List<Float> densities = new ArrayList<Float>();
        StringBuilder token = new StringBuilder();
        CharSequence extract;
        boolean eof = false;
        boolean waitingForCandidate = true;
        int position = 0;
        int start = 0;
        int ix = 0;
        State state = State.SPLITTING_LOOP;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            eof = i == literal.length() - 1;
            switch (state) {
                case SPLITTING_LOOP:
                    if (isWhitespace(c)) {
                        position = i + 1;
                        continue;
                    } else if (',' == c) {
                        if (urls.isEmpty()) {
                            err("Starts with empty image-candidate string.");
                        }
                        if (waitingForCandidate) {
                            errEmpty(literal.subSequence(start, i + 1));
                        }
                        commaHandler(literal.subSequence(start, i + 1));
                        waitingForCandidate = true;
                        position = i + 1;
                        continue;
                    }
                    start = position;
                    // fall through
                case URL:
                    waitingForCandidate = false;
                    if (eof || isWhitespace(c)) {
                        int end = isWhitespace(c) ? i : i + 1;
                        CharSequence url = literal.subSequence(position, end);
                        state = State.COLLECTING_DESCRIPTOR_TOKENS;
                        if (endsWithComma(url)) {
                            url = url.subSequence(0, url.length() - 1);
                            waitingForCandidate = true;
                            state = State.SPLITTING_LOOP;
                        }
                        if (eof || waitingForCandidate) {
                            widths.add(null);
                            if (densities.indexOf(DEFAULT_DENSITY) != -1) {
                                errSameDensity(
                                        url,
                                        urls.get(densities.indexOf(DEFAULT_DENSITY)));
                            }
                            densities.add(DEFAULT_DENSITY);
                            ix++;
                        }
                        IC_URL.checkValid(url);
                        urls.add(url);
                        token.setLength(0);
                        continue;
                    } else {
                        state = State.URL;
                        continue;
                    }
                case COLLECTING_DESCRIPTOR_TOKENS: // spec calls this "Start"
                    extract = literal.subSequence(position, i + 1);
                    if (isWhitespace(c)) {
                        checkToken(token, extract, urls, widths, densities, ix);
                        token.setLength(0);
                        state = State.AFTER_TOKEN;
                        continue;
                    } else if (',' == c) {
                        checkToken(token, extract, urls, widths, densities, ix);
                        if (widths.size() == ix) {
                            widths.add(null);
                        }
                        if (densities.size() == ix) {
                            if (densities.indexOf(DEFAULT_DENSITY) != -1) {
                                errSameDensity(
                                        urls.get(ix),
                                        urls.get(densities.indexOf(DEFAULT_DENSITY)));
                            }
                            densities.add(DEFAULT_DENSITY);
                        }
                        ix++;
                        waitingForCandidate = true;
                        state = State.SPLITTING_LOOP;
                        continue;
                    } else if (eof) {
                        token.append(c);
                        checkToken(token, extract, urls, widths, densities, ix);
                        if (widths.size() == ix) {
                            widths.add(null);
                        }
                        if (densities.size() == ix) {
                            if (densities.indexOf(DEFAULT_DENSITY) != -1) {
                                errSameDensity(
                                        urls.get(ix),
                                        urls.get(densities.indexOf(DEFAULT_DENSITY)));
                            }
                            densities.add(DEFAULT_DENSITY);
                        }
                        break;
                    } else {
                        token.append(c);
                        continue;
                    }
                case AFTER_TOKEN:
                    extract = literal.subSequence(position, i + 1);
                    if (isWhitespace(c)) {
                        if (eof) {
                            checkToken(token, extract, urls, widths, densities,
                                    ix);
                            if (widths.size() == ix) {
                                widths.add(null);
                            }
                            if (densities.size() == ix) {
                                if (densities.indexOf(DEFAULT_DENSITY) != -1) {
                                    errSameDensity(
                                            urls.get(ix),
                                            urls.get(densities.indexOf(DEFAULT_DENSITY)));
                                }
                                densities.add(DEFAULT_DENSITY);
                            }
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
            List<CharSequence> urls, List<Integer> widths,
            List<Float> densities, int ix) throws DatatypeException {
        if (token.length() > 0) {
            if (widths.size() > ix || densities.size() > ix) {
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
                    densities.add(null);
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
                    if (!densities.isEmpty() && densities.contains(density)) {
                        errSameDensity(urls.get(ix),
                                urls.get(densities.indexOf(density)));
                    }
                    densities.add(density);
                    widths.add(null);
                } catch (NumberFormatException e) {
                    err("Expected a floating-point number but found \u201c"
                            + number + "\u201d at \u201c" + extract(cs)
                            + "\u201d");
                }
            }
        }
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
