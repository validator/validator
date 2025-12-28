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

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Checks for duplicate dt names within the same dl element.
 * 
 * According to the HTML specification:
 * "Within a single dl element, there should not be more than one dt element for each name."
 * 
 * @version $Id$
 * @author copilot
 */
public final class DuplicateDtChecker extends Checker {

    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    /**
     * Stack to track nested dl elements. Each entry contains a map of dt names to their first occurrence locators.
     */
    private Stack<Map<String, Locator>> dlStack = new Stack<>();

    /**
     * Stack to track nested dt elements. Each entry contains the text content and locator for that dt level.
     */
    private Stack<DtInfo> dtStack = new Stack<>();

    /**
     * Helper class to store dt element information
     */
    private static class DtInfo {
        StringBuilder textContent;
        Locator locator;

        DtInfo(Locator locator) {
            this.textContent = new StringBuilder();
            this.locator = locator;
        }
    }

    /**
     * Constructor.
     */
    public DuplicateDtChecker() {
        super();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (XHTML_NS.equals(uri)) {
            if ("dl".equals(localName)) {
                // Start tracking a new dl element
                dlStack.push(new HashMap<String, Locator>());
            } else if ("dt".equals(localName) && !dlStack.isEmpty()) {
                // Start collecting text content for this dt (push onto stack)
                dtStack.push(new DtInfo(new LocatorImpl(getDocumentLocator())));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (XHTML_NS.equals(uri)) {
            if ("dl".equals(localName) && !dlStack.isEmpty()) {
                // Finished processing this dl element
                dlStack.pop();
            } else if ("dt".equals(localName) && !dtStack.isEmpty()) {
                // Finished collecting text content for this dt
                DtInfo dtInfo = dtStack.pop();
                
                // Normalize the text content (trim and collapse whitespace)
                String dtName = dtInfo.textContent.toString().trim().replaceAll("\\s+", " ");
                
                // Only check for duplicates if the dt has non-empty text content
                if (!dtName.isEmpty() && !dlStack.isEmpty()) {
                    Map<String, Locator> dtNames = dlStack.peek();
                    Locator firstOccurrence = dtNames.get(dtName);
                    
                    if (firstOccurrence == null) {
                        // First occurrence of this dt name
                        dtNames.put(dtName, dtInfo.locator);
                    } else {
                        // Duplicate dt name found
                        String warningMessage = String.format(
                                "Duplicate \u201Cdt\u201D name \u201C%s\u201D in \u201Cdl\u201D element. "
                                + "Consider using unique \u201Cdt\u201D names. "
                                + "Within a single \u201Cdl\u201D element, there should not be more than one "
                                + "\u201Cdt\u201D element for each name.",
                                dtName);
                        warn(warningMessage, dtInfo.locator);
                        warn(String.format("The first occurrence of \u201Cdt\u201D \u201C%s\u201D was here.", dtName),
                                firstOccurrence);
                    }
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (!dtStack.isEmpty()) {
            // Accumulate text content for the current (innermost) dt element
            dtStack.peek().textContent.append(ch, start, length);
        }
    }

    @Override
    public void reset() {
        dlStack.clear();
        dtStack.clear();
    }
}
