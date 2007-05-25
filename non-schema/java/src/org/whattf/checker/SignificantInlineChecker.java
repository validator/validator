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

package org.whattf.checker;

import java.util.Arrays;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UnicodeSet;

/**
 * Checks whether elements that require significant inline content have it.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class SignificantInlineChecker extends Checker {

    /**
     * A thread-safe set of insignificant chacarcters.
     */
    @SuppressWarnings("deprecation")
    private static final UnicodeSet INSIGNIFICANT_CHARACTERS = (UnicodeSet) new UnicodeSet(
            "[[:Zs:][:Zl:][:Zp:][:Cc:][:Cf:]]").freeze();

    /**
     * A lexicographically sorted array of names of XHTML elements that count as 
     * significant inline content.
     */
    private static final String[] SIGNIFICANT_ELEMENTS = { "button", "canvas",
            "embed", "iframe", "img", "input", "object", "output", "select",
            "textarea" };

    /**
     * A lexicographically sorted array of names of XHTML elements that require 
     * significant inline content.
     */
    private static final String[] REQUIRE_SIGNIFICANT_CONTENT = { "a",
            "caption", "h1", "h2", "h3", "h4", "h5", "h6", "p" };

    /**
     * The stack for keeping track of which elements have already had 
     * significant inline content. <em>Grows from the head of the list!</em>
     */
    private LinkedList<StackNode> stack = new LinkedList<StackNode>();

    /**
     * Indicates whether checking for significant inline content is necessary.
     */
    private boolean needToCheck = false;

    /**
     * A holder for the previous UTF-16 code unit for dealing with 
     * high surrogates.
     */
    private char prev = '\u0000';

    /**
     * Returns <code>true</code> if the argument names an XHTML element that 
     * counts as significant inline content.
     * @param localName name of an HTML element
     * @return <code>true</code> if the argument names an XHTML element that 
     * counts as significant inline content
     */
    private static boolean isSignificantElement(String localName) {
        return Arrays.binarySearch(SIGNIFICANT_ELEMENTS, localName) > -1;
    }

    /**
     * Returns <code>true</code> if the argument names an XHTML element that 
     * requires significant inline content.
     * @param localName name of an HTML element
     * @return <code>true</code> if the argument names an XHTML element that 
     * requires significant inline content
     */
    private static boolean requiresSignificantContent(String localName) {
        return Arrays.binarySearch(REQUIRE_SIGNIFICANT_CONTENT, localName) > -1;
    }

    /**
     * Returns <code>true</code> if teh argument is a significant character.
     * @param c a Unicode code point
     * @return <code>true</code> if teh argument is a significant character
     */
    private static boolean isSignificantCharacter(int c) {
        return !INSIGNIFICANT_CHARACTERS.contains(c);
    }

    /**
     * Constructor.
     */
    public SignificantInlineChecker() {
        super();
    }

    /**
     * @see org.whattf.checker.Checker#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!needToCheck) {
            return;
        }
        for (int i = start; i < (start + length); i++) {
            char c = ch[i];
            if (!UCharacter.isHighSurrogate(c)) {
                if (UCharacter.isLowSurrogate(c)) {
                    if (!UCharacter.isHighSurrogate(prev)) {
                        throw new SAXException("Malformed UTF-16!");
                    }
                    if (isSignificantCharacter(UCharacter.getCodePoint(prev, c))) {
                        prev = '\u0000';
                        markSignificant();
                        return;
                    }
                } else {
                    if (isSignificantCharacter(c)) {
                        prev = '\u0000';
                        markSignificant();
                        return;
                    }
                }
            }
            prev = c;
        }
    }

    /**
     * @see org.whattf.checker.Checker#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("http://www.w3.org/1999/xhtml".equals(uri)) {
            StackNode node = stack.removeFirst();
            if (!node.hasSignificantInline
                    && requiresSignificantContent(localName)) {
                err("Element \u201C"
                        + localName
                        + "\u201D from namespace \u201Chttp://www.w3.org/1999/xhtml\u201D requires significant inline content but did not have any.");
            }
        }
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml".equals(uri)) {
            if (needToCheck && isSignificantElement(localName)) {
                markSignificant();
            }
            stack.addFirst(new StackNode());
            if (!needToCheck) {
                needToCheck = requiresSignificantContent(localName);
            }
        }
    }

    /**
     * Marks the currently open elements as having significant inline content.
     */
    private void markSignificant() {
        needToCheck = false;
        for (StackNode node : stack) {
            if (node.hasSignificantInline) {
                break;
            } else {
                node.hasSignificantInline = true;
            }
        }
    }

    /**
     * @see org.whattf.checker.Checker#reset()
     */
    public void reset() {
        stack.clear();
        needToCheck = false;
        prev = '\u0000';
    }

    /**
     * Inner class for wrapping a mutable boolean in an object.
     */
    class StackNode {
        boolean hasSignificantInline = false;
    }

}
