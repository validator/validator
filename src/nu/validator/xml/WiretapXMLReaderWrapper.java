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

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

public class WiretapXMLReaderWrapper implements XMLReader {

    private final XMLReader wrappedReader;

    private ContentHandler contentHandler;

    private ContentHandler wiretapContentHander;

    private LexicalHandler lexicalHandler;

    private LexicalHandler wiretapLexicalHandler;

    /**
     * @param wrappedReader
     */
    public WiretapXMLReaderWrapper(final XMLReader wrappedReader) {
        this.wrappedReader = wrappedReader;
        contentHandler = wrappedReader.getContentHandler();
        try {
            lexicalHandler = (LexicalHandler) wrappedReader.getProperty("http://xml.org/sax/properties/lexical-handler");
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
        }
    }

    /**
     * Sets the wiretapContentHander.
     * 
     * @param wiretapContentHander
     *            the wiretapContentHander to set
     */
    public void setWiretapContentHander(ContentHandler wiretapContentHander) {
        this.wiretapContentHander = wiretapContentHander;
        updateWiretap();
    }

    /**
     * Sets the wiretapLexicalHandler.
     * 
     * @param wiretapLexicalHandler
     *            the wiretapLexicalHandler to set
     */
    public void setWiretapLexicalHandler(LexicalHandler wiretapLexicalHandler) {
        this.wiretapLexicalHandler = wiretapLexicalHandler;
        updateWiretap();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getContentHandler()
     */
    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getDTDHandler()
     */
    @Override
    public DTDHandler getDTDHandler() {
        return wrappedReader.getDTDHandler();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    @Override
    public EntityResolver getEntityResolver() {
        return wrappedReader.getEntityResolver();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
    @Override
    public ErrorHandler getErrorHandler() {
        return wrappedReader.getErrorHandler();
    }

    /**
     * @param arg0
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(String arg0) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return wrappedReader.getFeature(arg0);
    }

    /**
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            return lexicalHandler;
        } else {
            return wrappedReader.getProperty(name);
        }
    }

    /**
     * @param arg0
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    @Override
    public void parse(InputSource arg0) throws IOException, SAXException {
        wrappedReader.parse(arg0);
    }

    /**
     * @param arg0
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    @Override
    public void parse(String arg0) throws IOException, SAXException {
        wrappedReader.parse(arg0);
    }

    /**
     * @param contentHandler
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    @Override
    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        updateWiretap();
    }

    /**
     * @param arg0
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    @Override
    public void setDTDHandler(DTDHandler arg0) {
        wrappedReader.setDTDHandler(arg0);
    }

    /**
     * @param arg0
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(EntityResolver arg0) {
        wrappedReader.setEntityResolver(arg0);
    }

    /**
     * @param arg0
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler arg0) {
        wrappedReader.setErrorHandler(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    @Override
    public void setFeature(String arg0, boolean arg1)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setFeature(arg0, arg1);
    }

    /**
     * @param name
     * @param value
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            lexicalHandler = (LexicalHandler) value;
            updateWiretap();
        } else {
            wrappedReader.setProperty(name, value);
        }
    }

    private void updateWiretap() {
        if (contentHandler != null) {
            if (wiretapContentHander != null) {
                wrappedReader.setContentHandler(new CombineContentHandler(
                        wiretapContentHander, contentHandler));
            } else {
                wrappedReader.setContentHandler(contentHandler);
            }
        } else {
            wrappedReader.setContentHandler(wiretapContentHander);
        }

        try {
            if (lexicalHandler != null) {
                if (wiretapLexicalHandler != null) {
                    wrappedReader.setProperty(
                            "http://xml.org/sax/properties/lexical-handler",
                            new CombineLexicalHandler(wiretapLexicalHandler,
                                    lexicalHandler));
                } else {
                    wrappedReader.setProperty(
                            "http://xml.org/sax/properties/lexical-handler",
                            lexicalHandler);
                }
            } else {
                wrappedReader.setProperty(
                        "http://xml.org/sax/properties/lexical-handler",
                        wiretapLexicalHandler);
            }
        } catch (SAXNotRecognizedException | SAXNotSupportedException e) {
        }
    }

}
