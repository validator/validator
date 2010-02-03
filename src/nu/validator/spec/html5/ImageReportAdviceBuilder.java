/*
 * Copyright (c) 2007-2010 Mozilla Foundation
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeBuilder;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.EmptyAttributes;

import org.relaxng.datatype.DatatypeException;
import org.whattf.datatype.AbstractDatatype;
import org.whattf.datatype.Html5DatatypeLibrary;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ImageReportAdviceBuilder implements ContentHandler {

    private static final String NS = "http://www.w3.org/1999/xhtml";

    private enum State {
        AWAITING_H2, AWAITING_HEADLINE, IN_HEADLINE, AWAITING_H2_END, IN_PROSE

    }

    private State state = State.AWAITING_H2;

    private int depth = 0;

    private TreeBuilder treeBuilder;

    private final List<DocumentFragment> fragments = new LinkedList<DocumentFragment>();

    public static List<DocumentFragment> parseAltAdvice(InputSource in)
            throws IOException, SAXException {
        HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        ImageReportAdviceBuilder handler = new ImageReportAdviceBuilder();
        parser.setContentHandler(handler);
        parser.parse(in);
        return handler.getFragments();
    }

    public static void main(String[] args) throws IOException, SAXException {
        parseAltAdvice(new InputSource(System.getProperty(
                "nu.validator.spec.alt-advice",
                "http://wiki.whatwg.org/wiki/Validator.nu_alt_advice")));
    }

    private ImageReportAdviceBuilder() {

    }

    public void startDocument() throws SAXException {
        state = State.AWAITING_H2;
    }

    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        switch (state) {
            case AWAITING_H2:
                if ("h2" == localName && NS == uri) {
                    depth = 0;
                    state = State.AWAITING_HEADLINE;
                }
                return;
            case AWAITING_HEADLINE:
                if (depth == 0 && "span" == localName && NS == uri
                        && "mw-headline".equals(atts.getValue("", "class"))) {
                    state = State.IN_HEADLINE;
                } else {
                    depth++;
                }
                return;
            case IN_HEADLINE:
                depth++;
                return;
            case AWAITING_H2_END:
                depth++;
                return;
            case IN_PROSE:
                String href = null;
                if (NS == uri && "a" == localName) {
                    if ((href = atts.getValue("", "href")) != null) {
                        AttributesImpl ai = new AttributesImpl();
                        ai.addAttribute("href", href);
                        depth++;
                        treeBuilder.startElement(uri, localName, qName, ai);
                    }
                } else if (depth == 0 && NS == uri && "div" == localName
                        && ("printfooter".equals(atts.getValue("", "class")))) {
                    fragments.add((DocumentFragment) treeBuilder.getRoot());
                    treeBuilder = null;
                    state = State.AWAITING_H2;
                } else if (depth == 0 && "h2" == localName && NS == uri) {
                    fragments.add((DocumentFragment) treeBuilder.getRoot());
                    treeBuilder = null;
                    depth = 0;
                    state = State.AWAITING_HEADLINE;
                } else {
                    depth++;
                    treeBuilder.startElement(uri, localName, qName,
                            EmptyAttributes.EMPTY_ATTRIBUTES);
                }
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (state) {
            case AWAITING_H2:
                return;
            case AWAITING_HEADLINE:
                if (depth == 0) {
                    state = State.AWAITING_H2;
                } else {
                    depth--;
                }
                return;
            case IN_HEADLINE:
                if (depth == 0) {
                    state = State.AWAITING_H2_END;
                } else {
                    depth--;
                }
                return;
            case AWAITING_H2_END:
                if (depth == 0) {
                    treeBuilder = new TreeBuilder(true, true);
                    state = State.IN_PROSE;
                } else {
                    depth--;
                }
                return;
            case IN_PROSE:
                depth--;
                treeBuilder.endElement(uri, localName, qName);
                return;
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        switch (state) {
            case IN_PROSE:
                treeBuilder.characters(ch, start, length);
                return;
            default:
                return;
        }
    }

    public void endDocument() throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    /**
     * Returns the fragments.
     * 
     * @return the fragments
     */
    private List<DocumentFragment> getFragments() {
        return fragments;
    }

}
