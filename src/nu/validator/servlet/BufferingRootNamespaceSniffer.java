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

package nu.validator.servlet;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class BufferingRootNamespaceSniffer implements ContentHandler {

    private ContentHandler ch = null;

    private Locator locator = null;
    
    private List<String[]> namespaces = new LinkedList<>();
    
    private VerifierServletTransaction vst;
    
    public BufferingRootNamespaceSniffer(VerifierServletTransaction vst) {
        super();
        this.vst = vst;
    }

    public void setContentHandler(ContentHandler contentHandler) throws SAXException {
        this.ch = contentHandler;
        if (locator != null) {
            ch.setDocumentLocator(locator);
        }
        ch.startDocument();
        for (String[] element : namespaces) {
            ch.startPrefixMapping(element[0], element[1]);
        }
    }
    
    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (ch != null) {
            ch.characters(arg0, arg1, arg2);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        if (ch != null) {
            ch.endDocument();
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        if (ch != null) {
            ch.endElement(arg0, arg1, arg2);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
        if (ch != null) {
            ch.endPrefixMapping(arg0);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        if (ch != null) {
            ch.ignorableWhitespace(arg0, arg1, arg2);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        if (ch != null) {
            ch.processingInstruction(arg0, arg1);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator arg0) {
        locator = arg0;
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String arg0) throws SAXException {
        if (ch != null) {
            ch.skippedEntity(arg0);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {

    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
        if (ch != null) {
            ch.startElement(arg0, arg1, arg2, arg3);
        } else {
            vst.rootNamespace(arg0, locator);
            ch.startElement(arg0, arg1, arg2, arg3);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        if (ch != null) {
            ch.startPrefixMapping(arg0, arg1);
        } else {
            String[] arr = new String[2];
            arr[0] = arg0;
            arr[1] = arg1;
            namespaces.add(arr);
        }
    }

}
