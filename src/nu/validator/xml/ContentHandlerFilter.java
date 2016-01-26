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

package nu.validator.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 * @author hsivonen
 */
public abstract class ContentHandlerFilter implements ContentHandler {

    private ContentHandler contentHandler;
    
    private ErrorHandler errorHandler;
    
    private Locator locator;
    
    /**
     * @param contentHandler
     * @param errorHandler
     */
    protected ContentHandlerFilter(ContentHandler contentHandler, ErrorHandler errorHandler) {
        this.contentHandler = contentHandler;
        this.errorHandler = errorHandler;
    }
    /**
     * @param chars
     * @param start
     * @param length
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        contentHandler.characters(chars, start, length);
    }
    /**
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }
    /**
     * @param uri
     * @param local
     * @param qName
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void endElement(String uri, String local, String qName)
            throws SAXException {
        contentHandler.endElement(uri, local, qName);
    }
    /**
     * @param arg0
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
        contentHandler.endPrefixMapping(arg0);
    }
    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        contentHandler.ignorableWhitespace(arg0, arg1, arg2);
    }
    /**
     * @param arg0
     * @param arg1
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        contentHandler.processingInstruction(arg0, arg1);
    }
    /**
     * @param locator
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        contentHandler.setDocumentLocator(locator);
    }
    /**
     * @param arg0
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void skippedEntity(String arg0) throws SAXException {
        contentHandler.skippedEntity(arg0);
    }
    /**
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startDocument() throws SAXException {
        contentHandler.startDocument();
    }
    /**
     * @param uri
     * @param local
     * @param qName
     * @param attrs
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startElement(String uri, String local, String qName,
            Attributes attrs) throws SAXException {
        contentHandler.startElement(uri, local, qName, attrs);
    }
    /**
     * @param arg0
     * @param arg1
     * @throws org.xml.sax.SAXException
     */
    @Override
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        contentHandler.startPrefixMapping(arg0, arg1);
    }
    protected void fatal(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, locator);
        errorHandler.fatalError(spe);
        throw spe;
    }
    protected void err(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, locator);
        errorHandler.error(spe);
    }
    /**
     * Returns the contentHandler.
     * 
     * @return the contentHandler
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }
    /**
     * Sets the contentHandler.
     * 
     * @param contentHandler the contentHandler to set
     */
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }
    /**
     * Returns the errorHandler.
     * 
     * @return the errorHandler
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    /**
     * Sets the errorHandler.
     * 
     * @param errorHandler the errorHandler to set
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
