/*
 * Copyright (c) 2016-2018 Mozilla Foundation
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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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

    private static final Logger log4j = Logger.getLogger(
            UseCountingXMLReaderWrapper.class);

    private final XMLReader wrappedReader;

    private ContentHandler contentHandler;

    private ErrorHandler errorHandler;

    private HttpServletRequest request;

    private String systemId;

    private StringBuilder documentContent;

    private boolean hasVisibleMain;

    private int currentFigurePtr;

    private int currentHeadingPtr;

    private int currentSectioningElementPtr;

    private boolean inBody;

    private boolean loggedStyleInBody;

    private static final String[] SPECIAL_ANCESTORS = { "a", "address", "body",
            "button", "caption", "dfn", "dt", "figcaption", "figure", "footer",
            "form", "header", "label", "map", "noscript", "th", "time",
            "progress", "meter", "article", "section", "aside", "nav", "h1",
            "h2", "h3", "h4", "h5", "h6" };

    private static int specialAncestorNumber(String name) {
        for (int i = 0; i < SPECIAL_ANCESTORS.length; i++) {
            if (name == SPECIAL_ANCESTORS[i]) {
                return i;
            }
        }
        return -1;
    }

    private static final int FIGURE_MASK = (1 << specialAncestorNumber(
            "figure"));

    private boolean hasH1;

    private boolean hasTopLevelH1;

    private static final int H1_MASK = (1 << specialAncestorNumber("h1"));

    private static final int H2_MASK = (1 << specialAncestorNumber("h2"));

    private static final int H3_MASK = (1 << specialAncestorNumber("h3"));

    private static final int H4_MASK = (1 << specialAncestorNumber("h4"));

    private static final int H5_MASK = (1 << specialAncestorNumber("h5"));

    private static final int H6_MASK = (1 << specialAncestorNumber("h6"));

    private boolean secondLevelH1 = false;

    private class StackNode {

        private final String name;

        private final int ancestorMask;

        private boolean headingFound = false;

        private boolean imgFound = false;

        public StackNode(int ancestorMask, String name) {
            this.name = name;
            this.ancestorMask = ancestorMask;
        }

        public String getName() {
            return name;
        }

        public int getAncestorMask() {
            return ancestorMask;
        }

        public boolean hasHeading() {
            return headingFound;
        }

        public void setHeadingFound() {
            this.headingFound = true;
        }

        public boolean hasImg() {
            return imgFound;
        }

        public void setImgFound() {
            this.imgFound = true;
        }

    }

    private StackNode[] stack;

    private int currentPtr;

    private int currentSectioningDepth;

    private void push(StackNode node) {
        currentPtr++;
        if (currentPtr == stack.length) {
            StackNode[] newStack = new StackNode[stack.length + 64];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[currentPtr] = node;
    }

    private StackNode pop() {
        return stack[currentPtr--];
    }

    private StackNode peek() {
        return stack[currentPtr];
    }

    public UseCountingXMLReaderWrapper(XMLReader wrappedReader,
            HttpServletRequest request, String systemId) {
        this.wrappedReader = wrappedReader;
        this.contentHandler = wrappedReader.getContentHandler();
        this.request = request;
        this.systemId = systemId;
        this.inBody = false;
        this.loggedStyleInBody = false;
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
        StackNode node = pop();
        if ("section" == localName && !node.hasHeading()) {
            if (request != null) {
                request.setAttribute(
                        "http://validator.nu/properties/section-no-heading",
                        true);
            }
        } else if ("article" == localName && !node.hasHeading()) {
            if (request != null) {
                request.setAttribute(
                        "http://validator.nu/properties/article-no-heading",
                        true);
            }
        }
        if ("article" == localName || "aside" == localName || "nav" == localName
                || "section" == localName) {
            currentSectioningElementPtr = currentPtr - 1;
            currentSectioningDepth--;
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
        reset();
        stack = new StackNode[32];
        currentPtr = 0;
        currentHeadingPtr = -1;
        currentSectioningElementPtr = -1;
        currentSectioningDepth = 0;
        stack[0] = null;
        hasVisibleMain = false;
        hasH1 = false;
        hasTopLevelH1 = false;
        contentHandler.startDocument();
    }

    public void reset() {
        secondLevelH1 = false;
    }
    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        int ancestorMask = 0;
        StackNode parent = peek();
        String parentName = null;
        if (parent != null) {
            parentName = parent.getName();
            ancestorMask = parent.getAncestorMask();
        }
        if (contentHandler == null) {
            return;
        }
        if (atts.getIndex("", "style") > -1) {
            if (request != null) {
                request.setAttribute(
                        "http://validator.nu/properties/style-attribute-found",
                        true);
            }
        }
        if (inBody && "style".equals(localName) && !loggedStyleInBody) {
            loggedStyleInBody = true;
            if (request != null) {
                request.setAttribute(
                        "http://validator.nu/properties/style-in-body-found",
                        true);
            }
        } else if ("body".equals(localName)) {
            inBody = true;
        } else if ("hgroup".equals(localName)) {
            request.setAttribute("http://validator.nu/properties/hgroup-found",
                    true);
        } else if ("main".equals(localName)) {
            request.setAttribute("http://validator.nu/properties/main-found",
                    true);
            if (atts.getIndex("", "hidden") < 0) {
                if (hasVisibleMain) {
                    if (systemId != null) {
                        log4j.info("<main> multiple visible " + systemId);
                    }
                    request.setAttribute(
                            "http://validator.nu/properties/main-multiple-visible-found",
                            true);
                }
                hasVisibleMain = true;
            }
        } else if ("h1" == localName) {
            if (hasH1 && currentSectioningDepth > 1) {
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/h1-multiple", true);
                    if ("section".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-section-parent",
                                true);
                    } else if ("article".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-article-parent",
                                true);
                    } else if ("aside".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-aside-parent",
                                true);
                    } else if ("nav".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-nav-parent",
                                true);
                    } else if (systemId != null) {
                        log4j.info("<h1> multiple not nested in section/article/aside/nav " + systemId);
                    }
                }
            } else if (hasH1 && currentSectioningDepth == 1) {
                secondLevelH1 = true;
                if (request != null) {
                    if ("section".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-section-parent",
                                true);
                    } else if ("article".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-article-parent",
                                true);
                    } else if ("aside".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-aside-parent",
                                true);
                    } else if ("nav".equals(parentName)) {
                        request.setAttribute(
                                "http://validator.nu/properties/h1-multiple-with-nav-parent",
                                true);
                    } else if (systemId != null) {
                        log4j.info("<h1> multiple not nested in section/article/aside/nav " + systemId);
                    }
                }
            } else {
                hasTopLevelH1 = true;
            }
            hasH1 = true;
        }
        if ("article" == localName || "aside" == localName
                || "nav" == localName || "section" == localName) {
            currentSectioningElementPtr = currentPtr + 1;
            currentSectioningDepth++;
        }
        if ("h1" == localName || "h2" == localName || "h3" == localName
                || "h4" == localName || "h5" == localName
                || "h6" == localName) {
            currentHeadingPtr = currentPtr + 1;
            if (currentSectioningElementPtr > -1) {
                stack[currentSectioningElementPtr].setHeadingFound();
            }
        }
        if ((ancestorMask & FIGURE_MASK) != 0) {
            if ("img" == localName) {
                if (!stack[currentFigurePtr].hasImg()) {
                    stack[currentFigurePtr].setImgFound();
                }
            }
        }
        if (((ancestorMask & H1_MASK) != 0 || (ancestorMask & H2_MASK) != 0
                || (ancestorMask & H3_MASK) != 0
                || (ancestorMask & H4_MASK) != 0
                || (ancestorMask & H5_MASK) != 0
                || (ancestorMask & H6_MASK) != 0) && "img" == localName
                && atts.getIndex("", "alt") > -1
                && !"".equals(atts.getValue("", "alt"))) {
            stack[currentHeadingPtr].setImgFound();
        }
        StackNode child = new StackNode(ancestorMask, localName);
        push(child);
        if ("article" == localName || "aside" == localName || "nav" == localName
                || "section" == localName) {
            if (atts.getIndex("", "aria-label") > -1
                    && !"".equals(atts.getValue("", "aria-label"))) {
                child.setHeadingFound();
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

        if (hasTopLevelH1) {
            if (secondLevelH1) {
                if (request != null) {
                    request.setAttribute(
                            "http://validator.nu/properties/h1-multiple", true);
                }
            }
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
