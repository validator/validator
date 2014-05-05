/*
 * Copyright (c) 2012 Vadim Zaslawski, Ontos AG
 * Copyright (c) 2012-2014 Mozilla Foundation
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
 * This code contains comments that are verbatim quotations from the
 * document "HTML: The Living Standard", which has the following copyright
 * and permission notice:
 *
 *   Copyright 2004-2011 Apple Computer, Inc., Mozilla Foundation, and
 *   Opera Software ASA. You are granted a license to use, reproduce and
 *   create derivative works of this document.
 */

package nu.validator.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;

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

public final class OutlineBuildingXMLReaderWrapper implements XMLReader,
        ContentHandler {

    private final XMLReader wrappedReader;

    private final HttpServletRequest request;

    private ContentHandler contentHandler;

    public OutlineBuildingXMLReaderWrapper(XMLReader wrappedReader,
            HttpServletRequest request) {
        this.request = request;
        this.wrappedReader = wrappedReader;
        this.contentHandler = wrappedReader.getContentHandler();
        wrappedReader.setContentHandler(this);
    }

    private static final int MAX_EXCERPT = 500;

    private static final String[] SECTIONING_CONTENT_ELEMENTS = { "article",
            "aside", "nav", "section" };

    private static final String[] SECTIONING_ROOT_ELEMENTS = { "blockquote",
            "body", "details", "fieldset", "figure", "td" };

    private static final String[] HEADING_CONTENT_ELEMENTS = { "h1", "h2",
            "h3", "h4", "h5", "h6", "hgroup", };

    private Deque<Section> outline;

    public Deque<Section> getOutline() {
        return this.outline;
    }

    protected void setOutline(Deque<Section> outline) {
        this.outline = outline;
    }

    // an outlinee, a heading content element, or an element with a hidden
    // attribute;
    // during a walk over the nodes of a DOM tree, nodes are identified with
    // their depth and local name
    private class Element {
        // the depth of element in the DOM
        final private int depth;

        // the local name of element
        final private String name;

        // the outline for a sectioning content element or a sectioning root
        // element consists of a list of one or more potentially nested sections
        final private Deque<Section> outline = new LinkedList<Section>();

        public Element(int depth, String name) {
            this.depth = depth;
            this.name = name;
        }

        public boolean equals(int depth, String name) {
            return this.depth == depth && this.name.equals(name);
        }

        /**
         * @return the outline
         */
        public Deque<Section> getOutline() {
            return outline;
        }

        /**
         * @return 1-6 for a heading content element MAX_VALUE for an implied
         *         heading -1 for no section
         */
        public int getLastSectionHeadingRank() {
            Section section = outline.peekLast();
            return section != null ? section.getHeadingRank() : -1;
        }
    }

    // a section is a container that corresponds to some nodes in the original
    // DOM tree
    public class Section {
        // the section that contains this section
        private Section parent;

        // an outlinee or a heading content element
        final String elementName;

        // each section can have one heading associated with it
        final private StringBuilder headingTextBuilder = new StringBuilder();

        // we generate an "implied heading" for some sections that lack headings
        private boolean hasImpliedHeading;

        // MAX_VALUE for an implied heading, 1-6 for a heading content element
        private int headingRank = Integer.MAX_VALUE;

        // each section can contain any number of further nested sections
        final public Deque<Section> sections = new LinkedList<Section>();

        public Section(String elementName) {
            this.elementName = elementName;
        }

        /**
         * @return the parent section
         */
        public Section getParent() {
            return parent;
        }

        /**
         * @return the lement name
         */
        public String getElementName() {
            return elementName;
        }

        /**
         * @param parent
         *            the parent section to set
         */
        public void setParent(Section parent) {
            this.parent = parent;
        }

        /**
         * @return the heading text builder
         */
        public StringBuilder getHeadingTextBuilder() {
            return headingTextBuilder;
        }

        /**
         * @return the heading rank
         */
        public int getHeadingRank() {
            return headingRank;
        }

        /**
         * @return the sections
         */
        public Deque<Section> getSections() {
            return sections;
        }

        public void setHeadingRank(int headingRank) {
            this.headingRank = headingRank;
        }

        public boolean hasHeading() {
            return headingRank < 7 || hasImpliedHeading;
        }

        public void createImpliedHeading() {
            hasImpliedHeading = true;
        }
    }

    // for ContentHandler.characters()
    private enum AssociatedElement {
        NONE, HEADING_CONTENT_ELEMENT, NODE
    }

    private AssociatedElement inElement = AssociatedElement.NONE;

    // tracks the depth of walk through the DOM
    private int currentWalkDepth;

    // holds the element whose outline is being created;
    // a sectioning content element or a sectioning root element
    private Element currentOutlinee;

    // the top of the stack is either a heading content element or an element
    // with a hidden attribute
    private boolean isHeadingContentOrHiddenElement;

    private boolean needHeading;

    private boolean skipHeading;

    // a stack to hold elements, which is used to handle nesting
    private Deque<Element> stack = new LinkedList<Element>();

    // holds a pointer to a section, so that elements in the DOM can all be
    // associated with a section
    private Section currentSection;

    private boolean isWalkOver;

    private static final Pattern excerptPattern = Pattern.compile("\\W*\\S*$");

    private static final Pattern whitespacePattern = Pattern.compile("\\s+");

    /*
     * Returns the string excerpt.
     */
    private String excerpt(String str, int maxLength) {
        return str.length() > maxLength ? excerptPattern.matcher(
                str.substring(0, maxLength)).replaceFirst("&hellip;") : str;
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if (isWalkOver) {
            contentHandler.characters(ch, start, length);
            return;
        }

        switch (inElement) {
            case HEADING_CONTENT_ELEMENT:
                currentSection.getHeadingTextBuilder().append(ch, start, length);
                break;
            case NODE:
        }
        contentHandler.characters(ch, start, length);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if ("hgroup".equals(localName)) {
            needHeading = false;
            skipHeading = false;
        }
        if (isWalkOver) {
            contentHandler.endElement(uri, localName, qName);
            return;
        }

        int depth = currentWalkDepth--;

        if (isHeadingContentOrHiddenElement) {
            // When exiting an element, if that element is the element at the
            // top of the stack
            // Note: The element being exited is a heading content element or an
            // element with a hidden attribute.
            Element topElement = stack.peek();
            assert topElement != null;
            if (topElement.equals(depth, localName)) {
                // Pop that element from the stack.
                stack.pop();
                isHeadingContentOrHiddenElement = false;

                inElement = AssociatedElement.NONE;

                StringBuilder headingTextBuilder = currentSection.getHeadingTextBuilder();
                String heading = excerpt(whitespacePattern.matcher(
                        headingTextBuilder).replaceAll(" ").trim(), MAX_EXCERPT);
                headingTextBuilder.setLength(0);
                headingTextBuilder.append(heading);
            }

            // If the top of the stack is a heading content element or an
            // element with a hidden attribute
            // Do nothing.
            contentHandler.endElement(uri, localName, qName);
            return;
        }

        if (Arrays.binarySearch(SECTIONING_CONTENT_ELEMENTS, localName) > -1) {
            // When exiting a sectioning content element, if the stack is not
            // empty
            if (!stack.isEmpty()) {
                // If the current section has no heading,
                if (currentSection != null && !currentSection.hasHeading()) {
                    // create an implied heading and let that be the heading for
                    // the current section.
                    currentSection.createImpliedHeading();
                }
                Element exitedSectioningContentElement = currentOutlinee;
                assert exitedSectioningContentElement != null;

                // Pop the top element from the stack, and let the current
                // outlinee be that element.
                currentOutlinee = stack.pop();

                // Let current section be the last section in the outline of the
                // current outlinee element.
                currentSection = currentOutlinee.getOutline().peekLast();
                assert currentSection != null;

                // Append the outline of the sectioning content element being
                // exited to the current section.
                // (This does not change which section is the last section in
                // the outline.)
                for (Section section : exitedSectioningContentElement.outline) {
                    section.setParent(currentSection);
                    currentSection.sections.add(section);
                }
            }
        } else if (Arrays.binarySearch(SECTIONING_ROOT_ELEMENTS, localName) > -1) {
            // When exiting a sectioning root element, if the stack is not empty
            if (!stack.isEmpty()) {
                // Run these steps:

                // If the current section has no heading,
                if (currentSection != null && !currentSection.hasHeading()) {
                    // create an implied heading and let that be the heading for
                    // the current section.
                    currentSection.createImpliedHeading();
                }

                // Pop the top element from the stack, and let the current
                // outlinee be that element.
                currentOutlinee = stack.pop();

                // Let current section be the last section in the outline of the
                // current outlinee element.
                currentSection = currentOutlinee.getOutline().peekLast();

                // Finding the deepest child:
                // If current section has no child sections, stop these steps.
                while (!currentSection.sections.isEmpty())
                    // Let current section be the last child section of the
                    // current current section.
                    currentSection = currentSection.sections.peekLast();
                // Go back to the substep labeled finding the deepest child.
            }
        } else {
            // neither a sectioning content element nor a sectioning root
            // element
            contentHandler.endElement(uri, localName, qName);
            return;
        }

        // When exiting a sectioning content element or a sectioning root
        // element
        // Note: The current outlinee is the element being exited, and
        // it is the sectioning content element or a sectioning root element at
        // the root of the subtree for which an outline is being generated.

        // If the current section has no heading,
        if (currentSection != null && !currentSection.hasHeading()) {
            // create an implied heading and let that be the heading for the
            // current section.
            currentSection.createImpliedHeading();
        }

        // Skip to the next step in the overall set of steps.
        // (The walk is over.)
        // / isWalkOver = true;

        contentHandler.endElement(uri, localName, qName);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    public void startDocument() throws SAXException {
        if (contentHandler == null) {
            return;
        }
        contentHandler.startDocument();
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if (isWalkOver) {
            contentHandler.startElement(uri, localName, qName, atts);
            return;
        }

        ++currentWalkDepth;

        // If the top of the stack is a heading content element or an element
        // with a hidden attribute
        if (isHeadingContentOrHiddenElement) {
            if ("img".equals(localName) && atts.getIndex("", "alt") >= 0) {
                currentSection.getHeadingTextBuilder().append(
                        atts.getValue("", "alt"));
            }
            // Do nothing.
            contentHandler.startElement(uri, localName, qName, atts);
            return;
        }

        // When entering an element with a hidden attribute
        if (atts.getIndex("", "hidden") >= 0) {
            // Push the element being entered onto the stack. (This causes the
            // algorithm to skip that element and any descendants of the
            // element.)
            stack.push(new Element(currentWalkDepth, localName));
            isHeadingContentOrHiddenElement = true;
            contentHandler.startElement(uri, localName, qName, atts);
            return;
        }

        // When entering a sectioning content element or a sectioning root
        // element
        if (Arrays.binarySearch(SECTIONING_CONTENT_ELEMENTS, localName) > -1
                || Arrays.binarySearch(SECTIONING_ROOT_ELEMENTS, localName) > -1) {
            if (currentOutlinee != null) {
                // If current outlinee is not null, and the current section has
                // no heading,
                // create an implied heading and let that be the heading for the
                // current section.
                if (currentSection != null && !currentSection.hasHeading()) {
                    currentSection.createImpliedHeading();
                }
                // If current outlinee is not null, push current outlinee onto
                // the stack.
                stack.push(currentOutlinee);
            }

            // Let current outlinee be the element that is being entered.
            currentOutlinee = new Element(currentWalkDepth, localName);

            // Let current section be a newly created section for the current
            // outlinee element.
            // Associate current outlinee with current section.
            currentSection = new Section(localName);

            // Let there be a new outline for the new current outlinee,
            // initialized with just the new current section as the only section
            // in the outline.
            currentOutlinee.getOutline().add(currentSection);
            contentHandler.startElement(uri, localName, qName, atts);
            return;
        }

        // When entering a heading content element
        // Note: Recall that h1 has the highest rank, and h6 has the lowest
        // rank.
        if (Arrays.binarySearch(HEADING_CONTENT_ELEMENTS, localName) > -1
                && currentOutlinee != null) {
            if ("hgroup".equals(localName)) {
                needHeading = true;
                skipHeading = false;
                contentHandler.startElement(uri, localName, qName, atts);
                return;
            } else if (skipHeading) {
                contentHandler.startElement(uri, localName, qName, atts);
                return;
            } else if (needHeading) {
                skipHeading = true;
                needHeading = false;
            }
            int rank = localName.charAt(1) - '0';

            // If the current section has no heading,
            // let the element being entered be the heading for the current
            // section.
            if (currentSection != null && !currentSection.hasHeading()) {
                currentSection.setHeadingRank(rank);
                inElement = AssociatedElement.HEADING_CONTENT_ELEMENT;
            }
            // Otherwise, if the element being entered has a rank equal to
            // or higher than the heading of the last section of the
            // outline of the current outlinee, or if the heading of the
            // last section of the outline of the current outlinee is an
            // implied heading,
            else if (rank <= currentOutlinee.getLastSectionHeadingRank()) {
                // then create a new section and append it to the outline
                // of the current outlinee element, so that this new
                // section is the new last section of that outline.
                // Let current section be that new section.
                currentSection = new Section(localName);
                currentOutlinee.getOutline().add(currentSection);

                // Let the element being entered be the new heading for the
                // current section.
                currentSection.setHeadingRank(rank);
                inElement = AssociatedElement.HEADING_CONTENT_ELEMENT;
            }
            // Otherwise, run these substeps:
            else {
                // Let candidate section be current section.
                Section candidateSection = currentSection;

                // Heading loop:
                while (candidateSection != null) {
                    // If the element being entered has a rank lower than the
                    // rank of the heading of the candidate section,
                    if (rank > candidateSection.getHeadingRank()) {
                        // then create a new section, and append it to candidate
                        // section.
                        // (This does not change which section is the last
                        // section in the outline.)
                        // Let current section be this new section.
                        currentSection = new Section(localName);
                        currentSection.setParent(candidateSection);
                        candidateSection.getSections().add(currentSection);

                        // Let the element being entered be the new heading for
                        // the current section.
                        currentSection.setHeadingRank(rank);
                        inElement = AssociatedElement.HEADING_CONTENT_ELEMENT;

                        // Abort these substeps.
                        break;
                    }

                    // Let new candidate section be the section that contains
                    // candidate section in the outline of current outlinee.
                    // Let candidate section be new candidate section.
                    candidateSection = candidateSection.getParent();

                    // Return to the step labeled heading loop.
                }
            }

            // Push the element being entered onto the stack.
            // (This causes the algorithm to skip any descendants of the
            // element.)
            stack.push(new Element(currentWalkDepth, localName));
            isHeadingContentOrHiddenElement = true;
        }
        contentHandler.startElement(uri, localName, qName, atts);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        if (contentHandler == null) {
            return;
        }
        contentHandler.setDocumentLocator(locator);
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        if (contentHandler == null) {
            return;
        }
        if (currentOutlinee != null) {
            request.setAttribute(
                    "http://validator.nu/properties/document-outline",
                    (Deque<Section>) currentOutlinee.outline);
            setOutline(currentOutlinee.outline);
        }
        contentHandler.endDocument();
    }

    /**
     * @param prefix
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
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
    public DTDHandler getDTDHandler() {
        return wrappedReader.getDTDHandler();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getEntityResolver()
     */
    public EntityResolver getEntityResolver() {
        return wrappedReader.getEntityResolver();
    }

    /**
     * @return
     * @see org.xml.sax.XMLReader#getErrorHandler()
     */
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
    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return wrappedReader.getFeature(name);
    }

    /**
     * @param name
     * @return
     * @throws SAXNotRecognizedException
     * @throws SAXNotSupportedException
     * @see org.xml.sax.XMLReader#getProperty(java.lang.String)
     */
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return wrappedReader.getProperty(name);
    }

    /**
     * @param input
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
     */
    public void parse(InputSource input) throws IOException, SAXException {
        wrappedReader.parse(input);
    }

    /**
     * @param systemId
     * @throws IOException
     * @throws SAXException
     * @see org.xml.sax.XMLReader#parse(java.lang.String)
     */
    public void parse(String systemId) throws IOException, SAXException {
        wrappedReader.parse(systemId);
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setContentHandler(org.xml.sax.ContentHandler)
     */
    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setDTDHandler(org.xml.sax.DTDHandler)
     */
    public void setDTDHandler(DTDHandler handler) {
        wrappedReader.setDTDHandler(handler);
    }

    /**
     * @param resolver
     * @see org.xml.sax.XMLReader#setEntityResolver(org.xml.sax.EntityResolver)
     */
    public void setEntityResolver(EntityResolver resolver) {
        wrappedReader.setEntityResolver(resolver);
    }

    /**
     * @param handler
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
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
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        wrappedReader.setProperty(name, value);
    }

}
