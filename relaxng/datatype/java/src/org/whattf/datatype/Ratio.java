/*
 * Copyright (c) 2006 Henri Sivonen
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

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UnicodeSet;

public final class Ratio extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Ratio THE_INSTANCE = new Ratio();

    @SuppressWarnings("deprecation")
    private static final UnicodeSet ZS = (UnicodeSet) new UnicodeSet("[:Zs:]").freeze();

    private Ratio() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        int len = literal.length();
        if (len == 0) {
            throw newDatatypeException("Empty literal.");
        }
        int pos = 0;
        pos = findANumber(literal, pos);
        pos = skipZs(literal, pos);
        if (pos == len) {
            throw newDatatypeException("Premature end of literal\u2014neither a denominator nor a second number was found.");
        }
        if (isDenominator(literal.charAt(pos))) {
            while (pos < len) {
                char c = literal.charAt(pos);
                if (c >= '0' && c <= '9') {
                    throw newDatatypeException("Found digits after denominator.");
                }
                pos++;
            }
            return;
        } else {
            pos = findANumber(literal, pos);
            pos = skipZs(literal, pos); // REVISIT this step not in spec
            if (pos == len) {
                return;
            }
            if (isDenominator(literal.charAt(pos))) {
                throw newDatatypeException("Found a denominator after the second number.");
            }
            while (pos < len) {
                char c = literal.charAt(pos);
                if (c >= '0' && c <= '9') {
                    throw newDatatypeException("Found digits after the second number.");
                }
                pos++;
            }
            return;            
        }
    }

    private boolean isDenominator(char c) {
        switch (c) {
            case '\u0025':
            case '\u066A':
            case '\uFE6A':
            case '\uFF05':
            case '\u2030':
            case '\u2031':                
                return true;
            default:
                return false;
        }
    }

    private int skipZs(CharSequence literal, int pos) throws DatatypeException {
        // there are no astral Zs characters in Unicode 5.0.0, but let's be
        // forward-compatible
        int len = literal.length();
        char prev = '\u0000';
        while (pos < len) {
            char c = literal.charAt(pos);
            if (!UCharacter.isHighSurrogate(c)) {
                if (UCharacter.isLowSurrogate(c)) {
                    if (!UCharacter.isHighSurrogate(prev)) {
                        throw newDatatypeException("Bad UTF-16!");
                    }
                    if (!ZS.contains(UCharacter.getCodePoint(prev, c))) {
                        return pos;
                    }
                } else {
                    if (!ZS.contains(c)) {
                        return pos;
                    }
                }
            }
            prev = c;
            pos++;
        }
        return pos;
    }

    private int findANumber(CharSequence literal, int pos)
            throws DatatypeException {
        boolean pointSeen = false;
        boolean collectingNumber = false;
        boolean lastWasPoint = false;
        int len = literal.length();
        while (pos < len) {
            char c = literal.charAt(pos);
            if (c == '.') {
                if (pointSeen) {
                    throw newDatatypeException(
                            "More than one decimal point in a number.");
                }
                pointSeen = true;
                lastWasPoint = true;
                collectingNumber = true;
            } else if (c >= '0' && c <= '9') {
                collectingNumber = true;
                lastWasPoint = false;
            } else {
                if (collectingNumber) {
                    if (lastWasPoint) {
                        throw newDatatypeException(
                                "A decimal point was not followed by a digit.");
                    }
                    return pos;
                }
            }
            pos++;
        }
        if (!collectingNumber) {
            throw newDatatypeException(
                    "Expected a number but did not find one.");
        }
        if (lastWasPoint) {
            throw newDatatypeException(
                    "A decimal point was not followed by a digit.");
        }
        return pos;
    }

    @Override
    public String getName() {
        return "ratio";
    }

}
