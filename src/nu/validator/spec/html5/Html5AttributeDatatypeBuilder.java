/*
 * Copyright (c) 2007-2016 Mozilla Foundation
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

package nu.validator.spec.html5;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeBuilder;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.EmptyAttributes;

import org.relaxng.datatype.DatatypeException;
import nu.validator.datatype.AbstractDatatype;
import nu.validator.datatype.Html5DatatypeLibrary;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

@SuppressWarnings("rawtypes")
public final class Html5AttributeDatatypeBuilder implements ContentHandler {

    private static final String NS = "http://www.w3.org/1999/xhtml";

    private enum State {
        AWAITING_WIKI_BODY, AWAITING_H3, IN_HEADLINE, AWAITING_P, IN_P

    }

    private State state = State.AWAITING_WIKI_BODY;

    private Class currentClass = null;

    private int depth = 0;

    private StringBuilder stringBuilder = new StringBuilder();

    private Html5DatatypeLibrary datatypeLibrary = new Html5DatatypeLibrary();

    private TreeBuilder treeBuilder;

    private Map<Class, DocumentFragment> adviceByClass = new HashMap<>();

    public static Map<Class, DocumentFragment> parseSyntaxDescriptions() throws IOException, SAXException {
        HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        Html5AttributeDatatypeBuilder handler = new Html5AttributeDatatypeBuilder();
        parser.setContentHandler(handler);
        InputSource in = new InputSource(
                Html5AttributeDatatypeBuilder.class.getClassLoader().getResourceAsStream(
                        "nu/validator/localentities/files/syntax-descriptions"));
        parser.parse(in);
        return handler.getAdvice();
    }
    
    public static void main(String[] args) throws IOException, SAXException {
        parseSyntaxDescriptions();
    }
    
    private Html5AttributeDatatypeBuilder() {

    }
    
    @Override
    public void startDocument() throws SAXException {
        state = State.AWAITING_WIKI_BODY;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        switch (state) {
            case AWAITING_WIKI_BODY:
                if ("wiki-body".equals(atts.getValue("", "id"))) {
                    state = State.AWAITING_H3;
                }
                return;
            case AWAITING_H3:
                if ("h3" == localName && NS == uri) {
                    depth = 0;
                    state = State.IN_HEADLINE;
                }
                return;
            case IN_HEADLINE:
                stringBuilder.setLength(0);
                depth++;
                return;
            case AWAITING_P:
                if (NS == uri && "p" == localName) {
                    treeBuilder.startElement(NS, "dd", "dd",
                            EmptyAttributes.EMPTY_ATTRIBUTES);
                    state = State.IN_P;
                } else {
                    state = State.AWAITING_H3;
                }
                return;
            case IN_P:
                depth++;
                String href = null;
                if (NS == uri && "a" == localName
                        && ((href = atts.getValue("", "href")) != null)) {
                    AttributesImpl ai = new AttributesImpl();
                    ai.addAttribute("href", href);
                    treeBuilder.startElement(uri, localName, qName, ai);
                } else {
                    treeBuilder.startElement(uri, localName, qName,
                            EmptyAttributes.EMPTY_ATTRIBUTES);
                }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (state) {
            case AWAITING_WIKI_BODY:
            case AWAITING_H3:
                return;
            case IN_HEADLINE:
                if (depth == 0) {
                    try {
                        String name = stringBuilder.toString();
                        AbstractDatatype datatype = (AbstractDatatype) datatypeLibrary.createDatatype(name);
                        currentClass = datatype.getClass();
                        treeBuilder = new TreeBuilder(true, true);
                        treeBuilder.startElement(NS, "dt", "dt",
                                EmptyAttributes.EMPTY_ATTRIBUTES);
                        String dt = "Syntax of " + datatype.getName() + ":";
                        treeBuilder.characters(dt.toCharArray(), 0, dt.length());
                        treeBuilder.endElement(NS, "dt", "dt");
                        state = State.AWAITING_P;
                    } catch (DatatypeException e) {
                        System.out.println(String.format(
                                "Warning: No datatype class for \"%s\".",
                                stringBuilder.toString().trim()));
                        state = State.AWAITING_H3;
                    }
                } else {
                    depth--;
                }
                return;
            case AWAITING_P:
                // should be impossible
                state = State.AWAITING_H3;
                return;
            case IN_P:
                if (depth == 0) {
                    treeBuilder.endElement(NS, "dt", "dt");
                    adviceByClass.put(currentClass,
                            (DocumentFragment) treeBuilder.getRoot());
                    state = State.AWAITING_H3;
                } else {
                    depth--;
                    treeBuilder.endElement(uri, localName, qName);
                }
                return;
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        switch (state) {
            case IN_HEADLINE:
                stringBuilder.append(ch, start, length);
                return;
            case IN_P:
                treeBuilder.characters(ch, start, length);
                return;
            default:
                return;
        }
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    /**
     * Returns the adviceByClass.
     * 
     * @return the adviceByClass
     */
    private Map<Class, DocumentFragment> getAdvice() {
        return adviceByClass;
    }

}
