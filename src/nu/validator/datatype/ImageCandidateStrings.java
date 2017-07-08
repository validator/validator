/*
 * Copyright (c) 2014-2015 Mozilla Foundation
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
import java.util.Collections;
import java.util.List;

import org.relaxng.datatype.DatatypeException;

public class ImageCandidateStrings extends AbstractDatatype {

    private static enum State {
        SPLITTING_LOOP, URL, COLLECTING_DESCRIPTOR_TOKENS, IN_PARENS, AFTER_TOKEN
    }

    private static final int CLIP_LIMIT = 15;

    private static final int ELIDE_LIMIT = 50;

    private static final int NO_WIDTH = -1;

    private static final float NO_DENSITY = -1F;

    private static final float ONE = 1F;

    private static final ImageCandidateURL IC_URL = ImageCandidateURL.THE_INSTANCE;

    private static final FloatingPointExponentPositive FLOAT = FloatingPointExponentPositive.THE_INSTANCE;

    public static final ImageCandidateStrings THE_INSTANCE = new ImageCandidateStrings();

    protected ImageCandidateStrings() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            err("Must contain one or more image candidate strings.");
        }
        List<String> urls = new ArrayList<>();
        List<Integer> widths = new ArrayList<>();
        List<Float> denses = new ArrayList<>();
        StringBuilder url = new StringBuilder();
        StringBuilder tok = new StringBuilder();
        StringBuilder extract = new StringBuilder();
        boolean eof = false;
        boolean waitingForCandidate = true;
        int ix = 0;
        State state = State.SPLITTING_LOOP;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            extract.append(c);
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
                            errEmpty(extract);
                        }
                        commaHandler(extract);
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
                        tok.setLength(0);
                        if (eof || waitingForCandidate) {
                            adjustWidths(urls, widths, ix);
                            adjustDenses(urls, denses, ix);
                            ix++;
                        }
                        continue;
                    } else {
                        url.append(c);
                        state = State.URL;
                        continue;
                    }
                case COLLECTING_DESCRIPTOR_TOKENS: // spec labels this "Start"
                    if (isWhitespace(c)) {
                        checkToken(tok, extract, urls, widths, denses, ix);
                        tok.setLength(0);
                        state = State.AFTER_TOKEN;
                        continue;
                    } else if (',' == c) {
                        checkToken(tok, extract, urls, widths, denses, ix);
                        ix++;
                        waitingForCandidate = true;
                        state = State.SPLITTING_LOOP;
                        continue;
                    } else if ('(' == c) {
                        tok.append(c);
                        state = State.IN_PARENS;
                        continue;
                    } else if (eof) {
                        tok.append(c);
                        checkToken(tok, extract, urls, widths, denses, ix);
                        break;
                    } else {
                        tok.append(c);
                        continue;
                    }
                case IN_PARENS:
                    if (')' == c) {
                        tok.append(c);
                        if (eof) {
                            checkToken(tok, extract, urls, widths, denses, ix);
                            break;
                        }
                        state = State.COLLECTING_DESCRIPTOR_TOKENS;
                    } else if (eof) {
                        errNoRightParen(tok, extract);
                    } else {
                        tok.append(c);
                        continue;
                    }
                case AFTER_TOKEN:
                    if (isWhitespace(c)) {
                        if (eof) {
                            checkToken(tok, extract, urls, widths, denses, ix);
                            break;
                        }
                        continue;
                    } else {
                        i--;
                        extract.setLength(extract.length() - 1);
                        state = State.COLLECTING_DESCRIPTOR_TOKENS;
                    }
            }
        }
        checkWidths(widths, urls);
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

    private void checkToken(StringBuilder tok, CharSequence extract,
            List<String> urls, List<Integer> widths, List<Float> denses, int ix)
                    throws DatatypeException {
        if (tok.length() > 0) {
            if (widths.size() > ix || denses.size() > ix) {
                errExtraDescriptor(tok, extract);
            }
            char first = tok.charAt(0);
            char last = tok.charAt(tok.length() - 1);
            if (!('w' == last) && widthRequired()) {
                errNotWidthDescriptor(tok, extract);
            }
            if (!('w' == last || 'x' == last)) {
                errNotSupportedFormat(tok, extract);
            }
            String num = tok.subSequence(0, tok.length() - 1).toString();
            if ('-' == first) {
                errNotNumberGreaterThanZero(num, extract);
            }
            if ('+' == first) {
                errLeadingPlusSign(num, extract);
            }
            if ('w' == last) {
                // see nu.validator.checker.schematronequiv.Assertions
                System.setProperty(
                        "nu.validator.checker.imageCandidateString.hasWidth",
                        "1");
                try {
                    int width = Integer.parseInt(num, 10);
                    if (width <= 0) {
                        errNotNumberGreaterThanZero(num, extract);
                    }
                    if (!widths.isEmpty() && widths.contains(width)) {
                        errSameWidth(urls.get(ix),
                                urls.get(widths.indexOf(width)));
                    }
                    widths.add(width);
                    denses.add(NO_DENSITY);
                } catch (NumberFormatException e) {
                    errNotInteger(num, extract);
                }
            }
            if ('x' == last) {
                try {
                    try {
                        FLOAT.checkValid(num);
                    } catch (DatatypeException e) {
                        errFromOtherDatatype(e.getMessage(), extract);
                    }
                    float density = Float.parseFloat(num);
                    if (!denses.isEmpty() && denses.contains(density)) {
                        errSameDensity(urls.get(ix),
                                urls.get(denses.indexOf(density)));
                    }
                    denses.add(density);
                    widths.add(NO_WIDTH);
                } catch (NumberFormatException e) {
                    errNotFloatingPointNumber(num, extract);
                }
            }
        }
    }

    private void checkWidths(List<Integer> widths, List<String> urls)
            throws DatatypeException {
        if (widths.contains(NO_WIDTH)
                && Collections.frequency(widths, NO_WIDTH) != widths.size()) {
            errNoWidth(urls.get(widths.indexOf(NO_WIDTH)),
                    urls.get(widths.indexOf(Collections.max(widths))));
        }
    }

    private void adjustWidths(List<String> urls, List<Integer> widths, int ix)
            throws DatatypeException {
        if (widths.size() == ix || (widths.size() != 0 && widths.size() > ix
                && widths.get(ix) == NO_WIDTH)) {
            if (widthRequired()) {
                errNoWidth(urls.get(ix), null);
            } else if (widths.size() == ix) {
                widths.add(NO_WIDTH);
            }
        }
    }

    private void adjustDenses(List<String> urls, List<Float> denses, int ix)
            throws DatatypeException {
        if (denses.size() == ix) {
            if (denses.indexOf(ONE) != -1) {
                errSameDensity(urls.get(ix), urls.get(denses.indexOf(ONE)));
            }
            denses.add(ONE);
        }
    }

    private void err(String message) throws DatatypeException {
        throw newDatatypeException(message);
    }

    private void errFromOtherDatatype(CharSequence msg, CharSequence extract)
            throws DatatypeException {
        err(msg.subSequence(0, msg.length() - 1) + " at " + clip(extract)
                + ".");
    }

    private void errEmpty(CharSequence extract) throws DatatypeException {
        err("Empty image-candidate string at " + clip(extract) + ".");
    }

    private void errExtraDescriptor(CharSequence tok, CharSequence extract)
            throws DatatypeException {
        err("Expected single descriptor but found extraneous descriptor "
                + code(tok) + " at " + clip(extract) + ".");
    }

    private void errNotWidthDescriptor(CharSequence tok, CharSequence extract)
            throws DatatypeException {
        err("Expected width descriptor but found " + code(tok) + " at "
                + clip(extract) + "." + whenSizesIsPresent
                + allMustSpecifyWidth);
    }

    private void errNotSupportedFormat(CharSequence tok, CharSequence extract)
            throws DatatypeException {
        err("Expected number followed by " + code("w") + " or " + code("x")
                + " but found " + code(tok) + " at " + clip(extract) + ".");
    }

    private void errNotInteger(String num, CharSequence extract)
            throws DatatypeException {
        err("Expected integer but found " + code(num) + " at " + clip(extract)
                + ".");
    }

    private void errNotFloatingPointNumber(String num, CharSequence extract)
            throws DatatypeException {
        err("Expected floating-point number but found " + code(num) + " at "
                + clip(extract) + ".");
    }

    private void errNotNumberGreaterThanZero(CharSequence num,
            CharSequence extract) throws DatatypeException {
        err("Expected number greater than zero but found " + code(num) + " at "
                + clip(extract) + ".");
    }

    private void errNoRightParen(CharSequence tok, CharSequence extract)
            throws DatatypeException {
        err("Expected right parenthesis character but found " + code(tok)
                + " at " + clip(extract) + ".");
    }

    private void errLeadingPlusSign(CharSequence num, CharSequence extract)
            throws DatatypeException {
        err("Expected number without leading plus sign but found " + code(num)
                + " at " + clip(extract) + ".");
    }

    private void errSameWidth(CharSequence url1, CharSequence url2)
            throws DatatypeException {
        err("Width for image " + elide(url1)
                + " is identical to width for image " + elide(url2) + ".");
    }

    private void errSameDensity(CharSequence url1, CharSequence url2)
            throws DatatypeException {
        err("Density for image " + elide(url1)
                + " is identical to density for image " + elide(url2) + ".");
    }

    private void errNoWidth(CharSequence url1, CharSequence url2)
            throws DatatypeException {
        String msg = "No width specified for image " + elide(url1) + ".";
        if (url2 == null) {
            msg += whenSizesIsPresent;
        } else {
            msg += " (Because one or more image candidate strings specify a"
                    + " width (e.g., the string for image " + elide(url2) + ")";
        }
        msg += allMustSpecifyWidth;
        err(msg);
    }

    private String whenSizesIsPresent = " (When the " + code("sizes")
            + " attribute is present";

    private String allMustSpecifyWidth = ", all image candidate strings must"
            + " specify a width.)";

    private CharSequence clip(CharSequence cs) {
        int len = cs.length();
        if (len > CLIP_LIMIT) {
            cs = "\u2026" + cs.subSequence(len - CLIP_LIMIT, len);
        }
        return code(cs);
    }

    private CharSequence elide(CharSequence cs) {
        int len = cs.length();
        if (len < ELIDE_LIMIT) {
            return code(cs);
        } else {
            StringBuilder sb = new StringBuilder(ELIDE_LIMIT + 1);
            sb.append(cs, 0, ELIDE_LIMIT / 2);
            sb.append('\u2026');
            sb.append(cs, len - ELIDE_LIMIT / 2, len);
            return code(sb);
        }
    }

    private CharSequence code(CharSequence cs) {
        return "\u201c" + cs + "\u201d";
    }

    protected boolean widthRequired() {
        return false;
    }

    @Override
    public String getName() {
        return "image candidate strings";
    }

}
