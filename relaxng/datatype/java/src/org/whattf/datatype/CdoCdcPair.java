/*
 * Copyright (c) 2010 Mozilla Foundation
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

public final class CdoCdcPair extends AbstractDatatype {

    private enum State {
        DATA, LESS_THAN_SIGN, LESS_THAN_SIGN_BANG, LESS_THAN_SIGN_BANG_HYPHEN, HAS_CDO, HAS_CDO_AND_HYPHEN, HAS_CDO_AND_DOUBLE_HYPHEN
    }

    /**
     * The singleton instance.
     */
    public static final CdoCdcPair THE_INSTANCE = new CdoCdcPair();

    private CdoCdcPair() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        State state = State.DATA;
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            switch (state) {
                case DATA:
                    if ('<' == c) {
                        state = State.LESS_THAN_SIGN;
                        continue;
                    } else {
                        continue;
                    }
                case LESS_THAN_SIGN:
                    if ('!' == c) {
                        state = State.LESS_THAN_SIGN_BANG;
                        continue;
                    } else {
                        state = State.DATA;
                        continue;
                    }
                case LESS_THAN_SIGN_BANG:
                    if ('-' == c) {
                        state = State.LESS_THAN_SIGN_BANG_HYPHEN;
                        continue;
                    } else {
                        state = State.DATA;
                        continue;
                    }
                case LESS_THAN_SIGN_BANG_HYPHEN:
                    if ('-' == c) {
                        state = State.HAS_CDO;
                        continue;
                    } else {
                        state = State.DATA;
                        continue;
                    }
                case HAS_CDO:
                    if ('-' == c) {
                        state = State.HAS_CDO_AND_HYPHEN;
                        continue;
                    } else {
                        continue;
                    }
                case HAS_CDO_AND_HYPHEN:
                    if ('-' == c) {
                        state = State.HAS_CDO_AND_DOUBLE_HYPHEN;
                        continue;
                    } else {
                        state = State.HAS_CDO;
                        continue;
                    }
                case HAS_CDO_AND_DOUBLE_HYPHEN:
                    if ('>' == c) {
                        state = State.DATA;
                        continue;
                    } else if ('-' == c) {
                        continue;
                    } else {
                        state = State.HAS_CDO;
                        continue;
                    }
                default:
                    assert false : state;
            }
        }
        if (state == State.HAS_CDO) {
            throw newDatatypeException("Content contains the character sequence \u201c<!--\u201d without "
                    + " a later occurrence of the character sequence \u201c-->\u201d.");
        }
    }

    @Override public String getName() {
        return "text content with CDO-CDC pair";
    }

}
