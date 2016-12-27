/*
 * Copyright (c) 2016 Mozilla Foundation
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

package nu.validator.datatype.tools;

import java.io.IOException;
import java.util.HashSet;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public final class RegisteredRelValuesBuilder implements ContentHandler {

    private static final String NS = "http://www.w3.org/1999/xhtml";

    private enum State {
        AWAITING_SECTION, //
        AWAITING_TR, //
        IN_TR, //
        AWAITING_KEYWORD, //
        AWAITING_LINK_EFFECT, //
        AWAITING_A_EFFECT, //
    }

    private State state = State.AWAITING_SECTION;

    private StringBuilder keyword = new StringBuilder();

    private StringBuilder linkEffect = new StringBuilder();

    private StringBuilder aEffect = new StringBuilder();

    private static HashSet<String> registeredLinkRelValues = new HashSet<>();

    private static HashSet<String> registeredARelValues = new HashSet<>();

    public static void parseRegistry() throws IOException, SAXException {
        HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        RegisteredRelValuesBuilder handler = new RegisteredRelValuesBuilder();
        parser.setContentHandler(handler);
        InputSource in = new InputSource(
                RegisteredRelValuesBuilder.class.getClassLoader().getResourceAsStream(
                        "nu/validator/localentities/files/existing-rel-values"));
        parser.parse(in);
    }

    public static HashSet<String> getLinkRelValues() throws SAXException {
        return registeredLinkRelValues;
    }

    public static HashSet<String> getARelValues() throws SAXException {
        return registeredARelValues;
    }

    private RegisteredRelValuesBuilder() {
    }

    @Override
    public void startDocument() throws SAXException {
        state = State.AWAITING_SECTION;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        switch (state) {
            case AWAITING_SECTION:
                if ("formats".equals(atts.getValue("", "name"))) {
                    state = State.AWAITING_TR;
                }
                if ("HTML5_link_type_extensions".equals(
                        atts.getValue("", "name"))) {
                    state = State.AWAITING_TR;
                }
                return;
            case AWAITING_TR:
                if ("tr" == localName && NS == uri) {
                    state = State.IN_TR;
                }
                return;
            case IN_TR:
                if ("td" == localName && NS == uri) {
                    state = State.AWAITING_KEYWORD;
                }
                return;
            case AWAITING_KEYWORD:
                keyword.setLength(0);
                return;
            case AWAITING_LINK_EFFECT:
                linkEffect.setLength(0);
                return;
            case AWAITING_A_EFFECT:
                aEffect.setLength(0);
                return;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (state) {
            case AWAITING_KEYWORD:
                if ("td" == localName && NS == uri) {
                    state = State.AWAITING_LINK_EFFECT;
                }
                return;
            case AWAITING_LINK_EFFECT:
                if ("td" == localName && NS == uri) {
                    state = State.AWAITING_A_EFFECT;
                }
                return;
            case AWAITING_A_EFFECT:
                if ("td" == localName && NS == uri) {
                    if (!"not allowed".equals(
                            linkEffect.toString().trim().toLowerCase())) {
                        registeredLinkRelValues.add(keyword.toString().trim() //
                                .toLowerCase());
                    }
                    if (!"not allowed".equals(
                            aEffect.toString().trim().toLowerCase())) {
                        registeredARelValues.add(keyword.toString().trim() //
                                .toLowerCase());
                    }
                    state = State.AWAITING_TR;
                }
                return;
            case AWAITING_TR:
                if ("table" == localName && NS == uri) {
                    state = State.AWAITING_SECTION;
                }
                return;
            case IN_TR:
            case AWAITING_SECTION:
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
            case AWAITING_KEYWORD:
                keyword.append(ch, start, length);
                return;
            case AWAITING_LINK_EFFECT:
                linkEffect.append(ch, start, length);
                return;
            case AWAITING_A_EFFECT:
                aEffect.append(ch, start, length);
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
}
