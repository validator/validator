/*
 * Copyright (c) 2007 Mozilla Foundation
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

package nu.validator.source;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

final class LocationRecorder implements ContentHandler, LexicalHandler {
    private static final Logger log4j = Logger.getLogger(LocationRecorder.class);

    private final SourceCode owner;
    
    private Locator locator;

    private String uri;

    /**
     * @param owner
     */
    LocationRecorder(final SourceCode owner) {
        this.owner = owner;
    }

    /**
     * 
     */
    private void addLocatorLocation() {
        if (locator != null) {
            String systemId = locator.getSystemId();
            log4j.debug(systemId);
            if (uri == systemId || (uri != null && uri.equals(systemId))) {
                owner.addLocatorLocation(locator.getLineNumber(), locator.getColumnNumber());
            }
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        addLocatorLocation();
    }


    @Override
    public void endDocument() throws SAXException {
        locator = null;
    }

    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        log4j.debug(locator);
    }

    @Override
    public void skippedEntity(String arg0) throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void startDocument() throws SAXException {
        uri = owner.getUri();
    }

    @Override
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
    }

    @Override
    public void comment(char[] arg0, int arg1, int arg2) throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void endCDATA() throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void endDTD() throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void endEntity(String arg0) throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void startCDATA() throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void startDTD(String arg0, String arg1, String arg2)
            throws SAXException {
        addLocatorLocation();
    }

    @Override
    public void startEntity(String arg0) throws SAXException {
        addLocatorLocation();
    }

}
