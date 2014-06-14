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

import org.relaxng.datatype.DatatypeException;

public class ImageCandidateStrings extends AbstractDatatype {

    private enum State {
        SPLITTING_LOOP, URL, DESCRIPTOR
    }

    /**
     * The singleton instance.
     */
    public static final ImageCandidateStrings THE_INSTANCE = new ImageCandidateStrings();

    protected ImageCandidateStrings() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        if (literal.length() == 0) {
            err("Must contain one or more image candidate strings.");
        }
        boolean lacksURL = true;
        int position = 0;
        State state = State.SPLITTING_LOOP;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            switch (state) {
                case SPLITTING_LOOP:
                    if (isWhitespace(c)) {
                        position = i + 1;
                        continue;
                    } else if (',' == c) {
                        if (lacksURL) {
                            err("Found empty image candidate string.");
                        }
                        commaHandler(literal.subSequence(position, i + 1));
                        lacksURL = true;
                        position = i + 1;
                        continue;
                    }
                    // fall through
                case URL:
                    lacksURL = false;
                    if (i == literal.length() - 1 || isWhitespace(c)) {
                        int end = isWhitespace(c) ? i : i + 1;
                        CharSequence url = literal.subSequence(position, end);
                        position = end;
                        state = State.DESCRIPTOR;
                        if (endsWithComma(url)) {
                            lacksURL = true;
                            position = i + 1;
                            state = State.SPLITTING_LOOP;
                        }
                        continue;
                    } else {
                        state = State.URL;
                        continue;
                    }
                case DESCRIPTOR:
                    position = i + 1;
                    if (',' == c) {
                        lacksURL = true;
                        state = State.SPLITTING_LOOP;
                        continue;
                    } else {
                        continue;
                    }
            }
        }
        if (lacksURL) {
            err("Found empty image candidate string.");
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
            err("Found empty image candidate string.");
        }
        return cs.length() - 1;
    }

    private boolean endsWithComma(CharSequence cs) throws DatatypeException {
        int end = commaHandler(cs);
        Html5DatatypeLibrary dl = new Html5DatatypeLibrary();
        IriRef url = (IriRef) dl.createDatatype("iri-ref");
        System.out.println(cs.subSequence(0, end));
        url.checkValid(cs.subSequence(0, end));
        return end == cs.length() - 1;
    }

    private void err(String message) throws DatatypeException {
        throw newDatatypeException(message);
    }

    protected boolean widthRequired() {
        return false;
    }

    @Override public String getName() {
        return "image candidate strings";
    }

}
