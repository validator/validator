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
        SPLITTING_LOOP, URL, DESCRIPTORS
    }

    private static final int EXTRACT_LIMIT = 15;

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
        List<Integer> densities = new ArrayList<Integer>();
        boolean eof = false;
        boolean waitingForCandidate = true;
        int position = 0;
        int start = 0;
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
                        position = end;
                        state = State.DESCRIPTORS;
                        if (endsWithComma(url)) {
                            url = url.subSequence(0, url.length() - 1);
                            position = i + 1;
                            waitingForCandidate = true;
                            state = State.SPLITTING_LOOP;
                        }
                        if (eof || waitingForCandidate) {
                            if (densities.indexOf(1) != -1) {
                                errSameDensity(url,
                                        urls.get(densities.indexOf(1)));
                            }
                            widths.add(null);
                            densities.add(1);
                        }
                        IC_URL.checkValid(url);
                        urls.add(url);
                        continue;
                    } else {
                        state = State.URL;
                        continue;
                    }
                case DESCRIPTORS:
                    position = i + 1;
                    if (i == literal.length() || ',' == c) {
                      // add width
                      // add density
                    }
                    if (',' == c) {
                        waitingForCandidate = true;
                        state = State.SPLITTING_LOOP;
                        continue;
                    } else {
                        continue;
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

    private void err(String message) throws DatatypeException {
        throw newDatatypeException(message);
    }

    private void errEmpty(CharSequence extract) throws DatatypeException {
        int len = extract.length();
        if (len > EXTRACT_LIMIT) {
            extract = "\u2026" + extract.subSequence(len - EXTRACT_LIMIT, len);
        }
        err("Empty image-candidate string at \u201c" + extract + "\u201d.");
    }

    private void errSameDensity(CharSequence url1, CharSequence url2)
            throws DatatypeException {
        err("Density for image \u201c" + url1
                + "\u201d is identical to density for image \u201c" + url2
                + "\u201d.");
    }

    protected boolean widthRequired() {
        return false;
    }

    @Override public String getName() {
        return "image candidate strings";
    }

}
