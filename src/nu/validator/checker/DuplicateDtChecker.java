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
import java.util.LinkedList;
import java.util.Map;

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
public class DuplicateDtChecker extends Checker {

    private static final String HTML_NS = "http://www.w3.org/1999/xhtml";

    private final LinkedList<Map<String, Locator>> dlStack = new LinkedList<>();
    
    private final LinkedList<StringBuilder> dtTextStack = new LinkedList<>();
    
    private boolean inDt = false;

    private Locator locator = null;

    public DuplicateDtChecker() {
        super();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (HTML_NS.equals(uri)) {
            if ("dl".equals(localName)) {
                dlStack.addLast(new HashMap<>());
            } else if ("dt".equals(localName) && !dlStack.isEmpty()) {
                inDt = true;
                dtTextStack.addLast(new StringBuilder());
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (HTML_NS.equals(uri)) {
            if ("dl".equals(localName) && !dlStack.isEmpty()) {
                dlStack.removeLast();
            } else if ("dt".equals(localName) && inDt) {
                inDt = false;
                if (!dtTextStack.isEmpty()) {
                    String dtText = dtTextStack.removeLast().toString().trim();
                    if (!dtText.isEmpty() && !dlStack.isEmpty()) {
                        Map<String, Locator> dtNames = dlStack.getLast();
                        Locator firstOccurrence = dtNames.get(dtText);
                        if (firstOccurrence == null) {
                            dtNames.put(dtText, new LocatorImpl(locator));
                        } else {
                            warn("Duplicate \u201Cdt\u201D name \u201C" + dtText 
                                    + "\u201D in \u201Cdl\u201D element. "
                                    + "Within a single \u201Cdl\u201D element, "
                                    + "there should not be more than one "
                                    + "\u201Cdt\u201D element for each name.");
                            warn("The first occurrence of \u201Cdt\u201D name \u201C" 
                                    + dtText + "\u201D was here.", firstOccurrence);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (inDt && !dtTextStack.isEmpty()) {
            dtTextStack.getLast().append(ch, start, length);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
        dlStack.clear();
        dtTextStack.clear();
        inDt = false;
    }

    @Override
    public void endDocument() throws SAXException {
        dlStack.clear();
        dtTextStack.clear();
        inDt = false;
    }

    @Override
    public void reset() {
        dlStack.clear();
        dtTextStack.clear();
        inDt = false;
    }
}
