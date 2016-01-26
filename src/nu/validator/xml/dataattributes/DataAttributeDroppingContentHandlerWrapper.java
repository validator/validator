/*
 * Copyright (c) 2008-2011 Mozilla Foundation
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

package nu.validator.xml.dataattributes;

import nu.validator.htmlparser.impl.NCName;
import nu.validator.xml.AttributesImpl;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This wrapper removes <code>data-*</code> attributes from the pipeline and
 * emits errors if the <code>data-*</code> attributes contain prohibited
 * characters.
 */
public class DataAttributeDroppingContentHandlerWrapper implements
        ContentHandler {

    private final ContentHandler delegate;

    private final ErrorHandler errorHandler;

    private Locator locator = null;

    /**
     * @param delegate
     */
    public DataAttributeDroppingContentHandlerWrapper(ContentHandler delegate,
            ErrorHandler errorHandler) {
        this.delegate = delegate;
        this.errorHandler = errorHandler;
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        delegate.characters(arg0, arg1, arg2);
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        delegate.endDocument();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        delegate.endElement(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String arg0) throws SAXException {
        delegate.endPrefixMapping(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
            throws SAXException {
        delegate.ignorableWhitespace(arg0, arg1, arg2);
    }

    /**
     * @param arg0
     * @param arg1
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void processingInstruction(String arg0, String arg1)
            throws SAXException {
        delegate.processingInstruction(arg0, arg1);
    }

    /**
     * @param arg0
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator arg0) {
        locator = arg0;
        delegate.setDocumentLocator(arg0);
    }

    /**
     * @param arg0
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String arg0) throws SAXException {
        delegate.skippedEntity(arg0);
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        delegate.startDocument();
    }

    /**
     * @param ns
     * @param arg1
     * @param arg2
     * @param attributes
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String ns, String arg1, String arg2,
            Attributes attributes) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == ns
                || "http://www.w3.org/2000/svg" == ns
                || "http://www.w3.org/1998/Math/MathML" == ns) {
            delegate.startElement(ns, arg1, arg2, filterAttributes(attributes));
        } else {
            delegate.startElement(ns, arg1, arg2, attributes);
        }
    }

    private Attributes filterAttributes(Attributes attributes)
            throws SAXException {
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            String local = attributes.getLocalName(i);
            if (local.length() > 5 && local.startsWith("data-")
                    && attributes.getURI(i) == "") {
                if (errorHandler != null) {
                    checkDataName(local);
                }
                AttributesImpl attributesImpl = new AttributesImpl();
                for (int j = 0; j < i; j++) {
                    attributesImpl.addAttribute(attributes.getURI(j),
                            attributes.getLocalName(j), attributes.getQName(j),
                            attributes.getType(j), attributes.getValue(j));
                }
                for (int k = i + 1; k < len; k++) {
                    String uri = attributes.getURI(k);
                    local = attributes.getLocalName(k);
                    if (local.length() > 5 && local.startsWith("data-")
                            && "" == uri) {
                        checkDataName(local);
                    } else {
                        attributesImpl.addAttribute(uri, local,
                                attributes.getQName(k), attributes.getType(k),
                                attributes.getValue(k));
                    }
                }
                return attributesImpl;
            }
        }
        return attributes;
    }

    private void checkDataName(String local) throws SAXException {
        for (int i = 5; i < local.length(); i++) {
            char c = local.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                errorHandler.error(new SAXParseException(
                        "\u201Cdata-*\u201D attributes must not have"
                        + " characters from the range"
                        + " \u201CA\u201D\u2026\u201CZ\u201D in the name.",
                        locator));
            } else if (!NCName.isNCNameTrail(c)) {
                errorHandler.error(new SAXParseException(
                        "\u201Cdata-*\u201D attribute names must be"
                        + " XML 1.0 4th ed. plus Namespaces NCNames.",
                        locator));
            }
        }
    }

    /**
     * @param arg0
     * @param arg1
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        delegate.startPrefixMapping(arg0, arg1);
    }

}
