/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This does not extend XMLFilterImpl, because XMLFilterImpl constructor overwrites 
 * handlers on the wrapped XMLReader.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class NamespaceDroppingXMLReaderWrapper implements XMLReader, ContentHandler {

    private final static String[] ARRAY_TYPE = new String[0];

    private static String[] toInternedArray(Set<String> set) {
        String[] rv = set.toArray(ARRAY_TYPE);
        for (int i = 0; i < rv.length; i++) {
            rv[i] = rv[i].intern();
        }
        return rv;
    }

    private final XMLReader wrappedReader;
    
    private final String[] namespacesToRemove;

    private ContentHandler contentHandler;
    
    private int depth;

    private boolean alreadyWarnedAboutForeign;

    private boolean alreadyWarnedAboutFiltering;

    private boolean rootSeen;

    private Locator locator = null;

    public NamespaceDroppingXMLReaderWrapper(XMLReader wrappedReader,
            Set<String> namespacesToRemove) {
        this.wrappedReader = wrappedReader;
        this.namespacesToRemove = toInternedArray(namespacesToRemove);
        this.contentHandler = wrappedReader.getContentHandler();
        wrappedReader.setContentHandler(this);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (depth == 0) {
            contentHandler.characters(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (depth == 0) {
            contentHandler.endElement(uri, localName, qName);
        } else {
            depth--;
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        depth = 0;
        alreadyWarnedAboutForeign = false;
        alreadyWarnedAboutFiltering = false;
        rootSeen = false;
        contentHandler.startDocument();
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (depth == 0) {
            if (isInNamespacesToRemove(uri)) {
                if (rootSeen) {
                    depth = 1;
                    if (!alreadyWarnedAboutFiltering) {
                        warning(new SAXParseException(
                                "Content is being hidden from the validator based on namespace filtering.",
                                locator));
                        alreadyWarnedAboutFiltering = true;
                    }
                } else {
                    warning(new SAXParseException(
                            "Cannot filter out the root element.", locator));
                    contentHandler.startElement(uri, localName, qName,
                            filterAttributes(atts));
                }
            } else {
                contentHandler.startElement(uri, localName, qName,
                        filterAttributes(atts));
            }
        } else {
            if (!alreadyWarnedAboutForeign && !isInNamespacesToRemove(uri)) {
                warning(new SAXParseException(
                        "Filtering out selected namespaces causes descendants in other namespaces to be dropped as well.",
                        locator));
                alreadyWarnedAboutForeign = true;
            }
            depth++;
        }
        rootSeen = true;
    }

    private void warning(SAXParseException exception) throws SAXException {
        wrappedReader.getErrorHandler().warning(exception);
    }

    private Attributes filterAttributes(Attributes atts) throws SAXException {
        int length = atts.getLength();
        int i = 0;
        while (i < length) {
            if (isInNamespacesToRemove(atts.getURI(i))) {
                if (!alreadyWarnedAboutFiltering) {
                    warning(new SAXParseException(
                            "Content is being hidden from the validator based on namespace filtering.",
                            locator));
                    alreadyWarnedAboutFiltering = true;
                }
                AttributesImpl rv = new AttributesImpl();
                for (int j = 0; j < i; j++) {
                    rv.addAttribute(atts.getURI(j), atts.getLocalName(j),
                            atts.getQName(j), atts.getType(j), atts.getValue(j));
                }
                i++;
                while (i < length) {
                    String uri = atts.getURI(i);
                    if (!isInNamespacesToRemove(uri)) {
                        rv.addAttribute(uri, atts.getLocalName(i),
                                atts.getQName(i), atts.getType(i),
                                atts.getValue(i));
                    }
                    i++;
                }
                return rv;
            }
            i++;
        }
        return atts;
    }

    private boolean isInNamespacesToRemove(String uri) {
        for (String namespace : namespacesToRemove) {
            if (uri == namespace) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        contentHandler.setDocumentLocator(locator);
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    /**
     * @param prefix
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        contentHandler.endPrefixMapping(prefix);
    }

    /**
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    /**
     * @param target
     * @param data
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        contentHandler.processingInstruction(target, data);
    }

    /**
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        contentHandler.skippedEntity(name);
    }

    /**
     * @param prefix
     * @param uri
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        contentHandler.startPrefixMapping(prefix, uri);
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
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return wrappedReader.getFeature(name);
    }

    /**
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return wrappedReader.getProperty(name);
    }

    /**
     * @param input
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    @Override
    public void parse(InputSource input) throws IOException, SAXException {
        wrappedReader.parse(input);
    }

    /**
     * @param systemId
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    @Override
    public void parse(String systemId) throws IOException, SAXException {
        wrappedReader.parse(systemId);
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    @Override
    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    @Override
    public void setDTDHandler(DTDHandler handler) {
        wrappedReader.setDTDHandler(handler);
    }

    /**
     * @param resolver
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    @Override
    public void setEntityResolver(EntityResolver resolver) {
        wrappedReader.setEntityResolver(resolver);
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    @Override
    public void setErrorHandler(ErrorHandler handler) {
        wrappedReader.setErrorHandler(handler);
    }

    /**
     * @param name
     * @param value
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#setFeature(java.lang.String, boolean)
     */
    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setFeature(name, value);
    }

    /**
     * @param name
     * @param value
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#setProperty(java.lang.String, java.lang.Object)
     */
    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setProperty(name, value);
    }


}
