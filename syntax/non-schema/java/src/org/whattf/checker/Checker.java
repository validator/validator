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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The abstract base class for SAX-based content checkers that listen to 
 * the <code>ContentHandler</code> events and emit errors and warnings to 
 * an <code>ErrorHandler</code>.
 * 
 * <p>Always delegates <code>ignorableWhitespace()</code> to 
 * <code>characters()</code>. The other <code>ContentHandler</code> 
 * methods here are stubs that do nothing. Subclasses, therefore, never 
 * need to call the superclass methods. 
 * 
 * @version $Id$
 * @author hsivonen
 */
public abstract class Checker implements ContentHandler {

    private ErrorHandler errorHandler;

    private Locator locator;

    /**
     * Constructor.
     */
    public Checker() {
        super();
    }

    /**
     * Emit a warning. The locator is used.
     * 
     * @param message the warning message
     * @throws SAXException if something goes wrong
     */
    public void warn(String message) throws SAXException {
        if (errorHandler != null) {
            SAXParseException spe = new SAXParseException(message, locator);
            errorHandler.warning(spe);
        }
    }

    /**
     * Emit a warning with specified locator.
     * 
     * @param message the warning message
     * @throws SAXException if something goes wrong
     */
    public void warn(String message, Locator overrideLocator) throws SAXException {
        if (errorHandler != null) {
            SAXParseException spe = new SAXParseException(message, overrideLocator);
            errorHandler.warning(spe);
        }
    }
    
    /**
     * Emit an error with specified locator.
     * 
     * @param message the error message
     * @throws SAXException if something goes wrong
     */
    public void err(String message, Locator overrideLocator) throws SAXException {
        if (errorHandler != null) {
            SAXParseException spe = new SAXParseException(message, overrideLocator);
            errorHandler.error(spe);
        }
    }
    
    /**
     * Emit an error. The locator is used.
     * 
     * @param message the error message
     * @throws SAXException if something goes wrong
     */
    public void err(String message) throws SAXException {
        if (errorHandler != null) {
            SAXParseException spe = new SAXParseException(message, locator);
            errorHandler.error(spe);
        }
    }

    /**
     * Does nothing. Subclasses are expected to override this method with 
     * an implementation that clears the state of the checker and releases 
     * objects the checker might hold references to.
     */
    public void reset() {
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
     * @param errorHandler
     *            the errorHandler to set
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
    
    /**
     * Returns the locator.
     * 
     * @return the locator
     */
    public Locator getDocumentLocator() {
        return this.locator;
    }
    
    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * Calls <code>reset()</code>.
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        reset();
    }

    /**
     * Calls <code>reset()</code>.
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        reset();
    }

    /**
     * Calls <code>characters()</code>.
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public final void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }
    
    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}
