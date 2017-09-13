/*
 * Copyright (c) 2016-2017 Mozilla Foundation
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
 *
 */

package nu.validator.xml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import org.xml.sax.XMLReader;

public final class UseCountingXMLReaderWrapper
        implements XMLReader, ContentHandler {

    private final XMLReader wrappedReader;

    private ContentHandler contentHandler;

    private ErrorHandler errorHandler;

    private HttpServletRequest request;

    private StringBuilder documentContent;

    private boolean inBody;

    private boolean loggedLinkWithCharset;

    private boolean loggedScriptWithCharset;

    private boolean loggedStyleInBody;

    private boolean loggedRelAlternate;

    private boolean loggedRelAuthor;

    private boolean loggedRelBookmark;

    private boolean loggedRelCanonical;

    private boolean loggedRelDnsPrefetch;

    private boolean loggedRelExternal;

    private boolean loggedRelHelp;

    private boolean loggedRelIcon;

    private boolean loggedRelLicense;

    private boolean loggedRelNext;

    private boolean loggedRelNofollow;

    private boolean loggedRelNoopener;

    private boolean loggedRelNoreferrer;

    private boolean loggedRelPingback;

    private boolean loggedRelPreconnect;

    private boolean loggedRelPrefetch;

    private boolean loggedRelPreload;

    private boolean loggedRelPrerender;

    private boolean loggedRelPrev;

    private boolean loggedRelSearch;

    private boolean loggedRelServiceworker;

    private boolean loggedRelStylesheet;

    private boolean loggedRelTag;

    public UseCountingXMLReaderWrapper(XMLReader wrappedReader,
            HttpServletRequest request) {
        this.wrappedReader = wrappedReader;
        this.contentHandler = wrappedReader.getContentHandler();
        this.request = request;
        this.inBody = false;
        this.loggedLinkWithCharset = false;
        this.loggedScriptWithCharset = false;
        this.loggedStyleInBody = false;
        this.loggedRelAlternate = false;
        this.loggedRelAuthor = false;
        this.loggedRelBookmark = false;
        this.loggedRelCanonical = false;
        this.loggedRelAlternate = false;
        this.loggedRelAuthor = false;
        this.loggedRelBookmark = false;
        this.loggedRelCanonical = false;
        this.loggedRelDnsPrefetch = false;
        this.loggedRelExternal = false;
        this.loggedRelHelp = false;
        this.loggedRelIcon = false;
        this.loggedRelLicense = false;
        this.loggedRelNext = false;
        this.loggedRelNofollow = false;
        this.loggedRelNoopener = false;
        this.loggedRelNoreferrer = false;
        this.loggedRelPingback = false;
        this.loggedRelPreconnect = false;
        this.loggedRelPrefetch = false;
        this.loggedRelPreload = false;
        this.loggedRelPrerender = false;
        this.loggedRelPrev = false;
        this.loggedRelSearch = false;
        this.loggedRelServiceworker = false;
        this.loggedRelStylesheet = false;
        this.loggedRelTag = false;
        this.documentContent = new StringBuilder();
        wrappedReader.setContentHandler(this);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.endElement(uri, localName, qName);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        if (contentHandler == null) {
            return;
        }
        documentContent.setLength(0);
        contentHandler.startDocument();
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if ("link".equals(localName)) {
            boolean hasAppleTouchIcon = false;
            boolean hasSizes = false;
            for (int i = 0; i < atts.getLength(); i++) {
                if ("rel".equals(atts.getLocalName(i))) {
                    if (atts.getValue(i).contains("apple-touch-icon")) {
                        hasAppleTouchIcon = true;
                    }
                } else if ("sizes".equals(atts.getLocalName(i))) {
                    hasSizes = true;
                } else if ("charset".equals(atts.getLocalName(i))
                        && !loggedLinkWithCharset) {
                    loggedLinkWithCharset = true;
                    if (request != null) {
                        request.setAttribute(
                                "http://validator.nu/properties/link-with-charset-found",
                                true);
                    }
                }
            }
            if (request != null && hasAppleTouchIcon && hasSizes) {
                request.setAttribute(
                        "http://validator.nu/properties/apple-touch-icon-with-sizes-found",
                        true);
            }
        } else if ("script".equals(localName) && !loggedScriptWithCharset) {
            for (int i = 0; i < atts.getLength(); i++) {
                if ("charset".equals(atts.getLocalName(i))) {
                    loggedScriptWithCharset = true;
                    if (request != null) {
                        request.setAttribute(
                                "http://validator.nu/properties/script-with-charset-found",
                                true);
                    }
                }
            }
        } else if (inBody && "style".equals(localName) && !loggedStyleInBody) {
            loggedStyleInBody = true;
            if (request != null) {
                request.setAttribute(
                        "http://validator.nu/properties/style-in-body-found",
                        true);
            }
        }
        if (atts.getIndex("", "rel") > -1
                && ("link".equals(localName) || "a".equals(localName))) {
            List<String> relValues = Arrays.asList(
                    atts.getValue("", "rel").trim().toLowerCase() //
                            .split("\\s+"));
            if (relValues.contains("alternate") && !loggedRelAlternate) {
                loggedRelAlternate = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-alternate-found",
                            true);
                }
            }
            if (relValues.contains("author") && !loggedRelAuthor) {
                loggedRelAuthor = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-author-found",
                            true);
                }
            }
            if (relValues.contains("bookmark") && !loggedRelBookmark) {
                loggedRelBookmark = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-bookmark-found",
                            true);
                }
            }
            if (relValues.contains("canonical") && !loggedRelCanonical) {
                loggedRelCanonical = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-canonical-found",
                            true);
                }
            }
            if (relValues.contains("dns-prefetch") && !loggedRelDnsPrefetch) {
                loggedRelDnsPrefetch = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-dns-prefetch-found",
                            true);
                }
            }
            if (relValues.contains("external") && !loggedRelExternal) {
                loggedRelExternal = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-external-found",
                            true);
                }
            }
            if (relValues.contains("help") && !loggedRelHelp) {
                loggedRelHelp = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-help-found",
                            true);
                }
            }
            if (relValues.contains("icon") && !loggedRelIcon) {
                loggedRelIcon = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-icon-found",
                            true);
                }
            }
            if (relValues.contains("license") && !loggedRelLicense) {
                loggedRelLicense = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-license-found",
                            true);
                }
            }
            if (relValues.contains("next") && !loggedRelNext) {
                loggedRelNext = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-next-found",
                            true);
                }
            }
            if (relValues.contains("nofollow") && !loggedRelNofollow) {
                loggedRelNofollow = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-nofollow-found",
                            true);
                }
            }
            if (relValues.contains("noopener") && !loggedRelNoopener) {
                loggedRelNoopener = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-noopener-found",
                            true);
                }
            }
            if (relValues.contains("noreferrer") && !loggedRelNoreferrer) {
                loggedRelNoreferrer = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-noreferrer-found",
                            true);
                }
            }
            if (relValues.contains("pingback") && !loggedRelPingback) {
                loggedRelPingback = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-pingback-found",
                            true);
                }
            }
            if (relValues.contains("preconnect") && !loggedRelPreconnect) {
                loggedRelPreconnect = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-preconnect-found",
                            true);
                }
            }
            if (relValues.contains("prefetch") && !loggedRelPrefetch) {
                loggedRelPrefetch = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-prefetch-found",
                            true);
                }
            }
            if (relValues.contains("preload") && !loggedRelPreload) {
                loggedRelPreload = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-preload-found",
                            true);
                }
            }
            if (relValues.contains("prerender") && !loggedRelPrerender) {
                loggedRelPrerender = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-prerender-found",
                            true);
                }
            }
            if (relValues.contains("prev") && !loggedRelPrev) {
                loggedRelPrev = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-prev-found",
                            true);
                }
            }
            if (relValues.contains("search") && !loggedRelSearch) {
                loggedRelSearch = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-search-found",
                            true);
                }
            }
            if (relValues.contains("serviceworker")
                    && !loggedRelServiceworker) {
                loggedRelServiceworker = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-serviceworker-found",
                            true);
                }
            }
            if (relValues.contains("stylesheet") && !loggedRelStylesheet) {
                loggedRelStylesheet = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-stylesheet-found",
                            true);
                }
            }
            if (relValues.contains("tag") && !loggedRelTag) {
                loggedRelTag = true;
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/rel-tag-found",
                            true);
                }
            }
        }
        contentHandler.startElement(uri, localName, qName, atts);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        if (contentHandler == null) {
            return;
        }
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
        if (contentHandler == null) {
            return;
        }
        contentHandler.endDocument();
    }

    /**
     * @param prefix
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        if (contentHandler == null) {
            return;
        }
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
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    /**
     * @param target
     * @param data
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.processingInstruction(target, data);
    }

    /**
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.skippedEntity(name);
    }

    /**
     * @param prefix
     * @param uri
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
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
        return errorHandler;
    }

    /**
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getFeature(java.lang.String)
     */
    @Override
    public boolean getFeature(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
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
    public Object getProperty(String name)
            throws SAXNotRecognizedException, SAXNotSupportedException {
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
    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setFeature(name, value);
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
        wrappedReader.setProperty(name, value);
    }
}
