/*
 * Copyright (c) 2025 Mozilla Foundation
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

package nu.validator.checker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Checks heading hierarchy according to the WHATWG HTML specification.
 *
 * Per the spec:
 * - Each heading following another heading must have a level that is less than,
 *   equal to, or 1 greater than the previous heading's level (MUST - error)
 * - If a document has headings, at least one should have a computed heading
 *   level of 1 (SHOULD - warning)
 *
 * This checker accounts for the headingoffset and headingreset attributes
 * when computing heading levels.
 *
 * @see <a href="https://html.spec.whatwg.org/multipage/sections.html#headings-and-outlines-2">
 *      HTML Standard: Headings and outlines</a>
 */
public class HeadingHierarchyChecker extends Checker {

    private static final String HTML_NS = "http://www.w3.org/1999/xhtml";

    /**
     * Stores information about an ancestor element relevant to heading offset
     * computation.
     */
    private static class AncestorInfo {
        final int headingOffset;
        final boolean headingReset;

        AncestorInfo(int headingOffset, boolean headingReset) {
            this.headingOffset = headingOffset;
            this.headingReset = headingReset;
        }
    }

    /**
     * Stores information about a heading encountered in the document.
     */
    private static class HeadingInfo {
        final int computedLevel;
        final String elementName;
        final Locator locator;

        HeadingInfo(int computedLevel, String elementName, Locator locator) {
            this.computedLevel = computedLevel;
            this.elementName = elementName;
            this.locator = locator;
        }
    }

    private Locator locator;
    private Deque<AncestorInfo> ancestorStack;
    private List<HeadingInfo> headings;

    public HeadingHierarchyChecker() {
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        ancestorStack = new ArrayDeque<>();
        headings = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (HTML_NS != uri) {
            // Push a neutral entry for non-HTML elements
            ancestorStack.push(new AncestorInfo(0, false));
            return;
        }

        // Parse headingoffset attribute
        int headingOffset = 0;
        String offsetValue = atts.getValue("", "headingoffset");
        if (offsetValue != null) {
            headingOffset = parseNonNegativeInteger(offsetValue);
        }

        // Check for headingreset attribute
        boolean headingReset = atts.getIndex("", "headingreset") >= 0;

        ancestorStack.push(new AncestorInfo(headingOffset, headingReset));

        // Check if this is a heading element
        if (isHeadingElement(localName)) {
            int baseLevel = localName.charAt(1) - '0';
            int computedLevel = computeHeadingLevel(baseLevel);
            headings.add(new HeadingInfo(computedLevel, localName,
                    new LocatorImpl(locator)));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!ancestorStack.isEmpty()) {
            ancestorStack.pop();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if (headings.isEmpty()) {
            return;
        }

        HeadingInfo previousHeading = null;

        for (HeadingInfo heading : headings) {
            // Check for skipped levels
            if (previousHeading != null) {
                int prevLevel = previousHeading.computedLevel;
                int currLevel = heading.computedLevel;

                // Valid transitions:
                // - currLevel <= prevLevel (going up or staying same)
                // - currLevel == prevLevel + 1 (going down exactly one level)
                // Invalid: currLevel > prevLevel + 1 (skipping levels)
                if (currLevel > prevLevel + 1) {
                    err("The heading \u201c" + heading.elementName
                            + "\u201d (with computed level " + currLevel
                            + ") follows the heading \u201c"
                            + previousHeading.elementName
                            + "\u201d (with computed level " + prevLevel
                            + "), skipping " + (currLevel - prevLevel - 1)
                            + " heading level"
                            + (currLevel - prevLevel - 1 > 1 ? "s" : "") + ".",
                            heading.locator);
                }
            }

            previousHeading = heading;
        }

        // Note: The spec says documents SHOULD have at least one heading with
        // computed level 1, but this produces too many warnings for existing
        // documents. If needed in the future, add a warning here.
    }

    @Override
    public void reset() {
        ancestorStack = null;
        headings = null;
    }

    /**
     * Checks if the given local name is a heading element (h1-h6).
     */
    private static boolean isHeadingElement(String localName) {
        return "h1" == localName || "h2" == localName || "h3" == localName
                || "h4" == localName || "h5" == localName || "h6" == localName;
    }

    /**
     * Parses a non-negative integer from a string.
     * Returns 0 if the value is not a valid non-negative integer.
     */
    private static int parseNonNegativeInteger(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            int result = Integer.parseInt(value.trim());
            return result >= 0 ? result : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Computes the heading offset by walking up the ancestor stack.
     * Accumulates headingoffset values until a headingreset is encountered.
     */
    private int computeHeadingOffset() {
        int offset = 0;
        for (AncestorInfo ancestor : ancestorStack) {
            offset += ancestor.headingOffset;
            if (ancestor.headingReset) {
                break;
            }
        }
        return offset;
    }

    /**
     * Computes the effective heading level for a heading element.
     * The level is capped at 9 per the spec.
     */
    private int computeHeadingLevel(int baseLevel) {
        int level = baseLevel + computeHeadingOffset();
        return level > 9 ? 9 : level;
    }
}
