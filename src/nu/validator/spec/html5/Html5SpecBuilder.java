/*
 * Copyright (c) 2007-2012 Mozilla Foundation
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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeBuilder;
import nu.validator.spec.Spec;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.EmptyAttributes;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.xml.util.Name;

public final class Html5SpecBuilder implements ContentHandler {

    private static final String NS = "http://www.w3.org/1999/xhtml";

    private static final String SPEC_LINK_URI = System.getProperty(
            "nu.validator.spec.html5-link",
            "https://html.spec.whatwg.org/multipage/");

    private static final Pattern THE = Pattern.compile("^.*The.*$", Pattern.DOTALL);

    private static final Pattern ELEMENT = Pattern.compile("^.*The.*element\\s*$", Pattern.DOTALL);

    private static final Pattern CATEGORIES = Pattern.compile("^\\s*Categories\\s*");

    private static final Pattern CONTEXT = Pattern.compile("^\\s*Contexts\\s+in\\s+which\\s+th(is|ese)\\s+element[s]?\\s+can\\s+be\\s+used:?\\s*");

    private static final Pattern CONTENT_MODEL = Pattern.compile("^\\s*Content\\s+model:?\\s*$");

    private static final Pattern TAG_OMISSION = Pattern.compile("^\\s*Tag omission\\s+in\\s+text/html:?\\s*$");

    private static final Pattern ATTRIBUTES = Pattern.compile("^\\s*Content\\s+attributes:?\\s*$");

    private enum State {
        AWAITING_HEADING, IN_H4, IN_CODE_IN_H4, AWAITING_ELEMENT_DL, IN_ELEMENT_DL_START, IN_CATEGORIES_DT, CAPTURING_CATEGORIES_DDS, IN_CONTEXT_DT, CAPTURING_CONTEXT_DDS, IN_CONTENT_MODEL_DT, CAPTURING_CONTENT_MODEL_DDS, IN_TAG_OMISSION_DT, CAPTURING_TAG_OMISSION_DDS, IN_ATTRIBUTES_DT, CAPTURING_ATTRIBUTES_DDS
    }

    private Locator locator;
    
    private State state = State.AWAITING_HEADING;

    private int captureDepth = 0;

    private String currentId;

    private StringBuilder nameText = new StringBuilder();

    private StringBuilder referenceText = new StringBuilder();

    private TreeBuilder fragmentBuilder;

    private Name currentName;

    private Map<Name, String> urisByElement = new HashMap<>();

    private Map<Name, DocumentFragment> categoriesByElement = new HashMap<>();

    private Map<Name, DocumentFragment> contextsByElement = new HashMap<>();

    private Map<Name, DocumentFragment> contentModelsByElement = new HashMap<>();

    private Map<Name, DocumentFragment> attributesByElement = new HashMap<>();

    private boolean ignoreTextNodes = false;

    private static Spec parseSpec(InputSource in) throws IOException, SAXException {
        HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
        Html5SpecBuilder handler = new Html5SpecBuilder();
        parser.setContentHandler(handler);
        parser.parse(in);
        return handler.buildSpec();
    }

   
    public static void main(String[] args) throws IOException, SAXException {
        if (args == null || args.length < 1)  {
            System.err.printf("Usage: java -cp ~/vnu.jar nu.validator.spec.html5.Html5SpecBuilder URL_OF_HTML_SPEC\n");
            System.exit(1);
        }
        
        InputSource is;
        final String url = args[0];

        System.err.println(url);
        if ("-".equals(url)) {
            is = new InputSource(System.in);
        } else {
            is = new InputSource(url);
        }
        
        try {
            parseSpec(is);
        } catch (SAXParseException e) {
            System.err.printf("Line: %d Col: %d\n", e.getLineNumber(), e.getColumnNumber());
            e.printStackTrace();
        }
    }
    
    public static Spec parseSpec(InputStream html5SpecAsStream) throws IOException, SAXException {
        return parseSpec(new InputSource(html5SpecAsStream));
    }

    private Spec buildSpec() {
        return new Spec(urisByElement, contextsByElement,
                contentModelsByElement, attributesByElement);
    }

    /**
     * 
     */
    private Html5SpecBuilder() {
        super();
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        switch (state) {
            case AWAITING_HEADING:
                break;
            case IN_H4:
                referenceText.append(ch, start, length);
                if (nameText.length() != 0) {
                  Matcher m = THE.matcher(referenceText);
                  if (m.matches()) {
                    String ln = nameText.toString().intern();
                    if ("" == ln) {
                      throw new SAXParseException(
                          "Malformed spec: no element "+currentName, locator);
                    }
                    currentName = new Name(NS, ln);
                    if (!urisByElement.containsKey(currentName)) {
                      if (currentId == null) {
                        state = State.AWAITING_HEADING;
                        //                                throw new SAXParseException(
                        //                                        "Malformed spec: no element id.", locator);
                      }
                      urisByElement.put(currentName, SPEC_LINK_URI + "#"
                          + currentId);
                    }
                  }
                }
                break;
            case IN_CODE_IN_H4:
                nameText.append(ch, start, length);
                break;
            case AWAITING_ELEMENT_DL:
                break;
            case IN_ELEMENT_DL_START:
                break;
            case IN_CATEGORIES_DT:
            case IN_CONTEXT_DT:
            case IN_CONTENT_MODEL_DT:
            case IN_TAG_OMISSION_DT:
            case IN_ATTRIBUTES_DT:
                referenceText.append(ch, start, length);
                break;
            case CAPTURING_CATEGORIES_DDS:
            case CAPTURING_CONTEXT_DDS:
            case CAPTURING_CONTENT_MODEL_DDS:
            case CAPTURING_TAG_OMISSION_DDS:
            case CAPTURING_ATTRIBUTES_DDS:
                if (ignoreTextNodes) {
                    ignoreTextNodes = false;
                } else {
                    fragmentBuilder.characters(ch, start, length);
                }
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        switch (state) {
            case AWAITING_ELEMENT_DL:
            case AWAITING_HEADING:
                // XXX finish
                break;
            case IN_H4:
            case IN_CODE_IN_H4:
            case IN_ELEMENT_DL_START:
            case IN_CATEGORIES_DT:
            case IN_CONTEXT_DT:
            case IN_CONTENT_MODEL_DT:
            case IN_TAG_OMISSION_DT:
            case IN_ATTRIBUTES_DT:
            case CAPTURING_CATEGORIES_DDS:
            case CAPTURING_CONTEXT_DDS:
            case CAPTURING_CONTENT_MODEL_DDS:
            case CAPTURING_TAG_OMISSION_DDS:
            case CAPTURING_ATTRIBUTES_DDS:
                throw new SAXException(
                        "Malformed spec: Wrong state for document end.");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("p" == localName && NS == uri) {
            return;
        }
        switch (state) {
            case AWAITING_HEADING:
                break;
            case IN_H4:
                if ("h4" == localName && NS == uri) {
                    Matcher m = ELEMENT.matcher(referenceText);
                    if (m.matches()) {
                        String ln = nameText.toString().intern();
                        if ("" == ln) {
                            throw new SAXParseException(
                                    "Malformed spec: no element"+currentName, locator);
                        }
                            if (currentId == null) {
                                state = State.AWAITING_HEADING;
//                                throw new SAXParseException(
//                                        "Malformed spec: no element id.", locator);
                            }
                            state = State.AWAITING_ELEMENT_DL;
                    } else {
                        currentId = null;
                        nameText.setLength(0);
                        state = State.AWAITING_HEADING;
                    }
                }
                break;
            case IN_CODE_IN_H4:
                if ("code" == localName && NS == uri) {
                    state = State.IN_H4;
                }
                break;
            case AWAITING_ELEMENT_DL:
                break;
            case IN_ELEMENT_DL_START:
                throw new SAXParseException(
                        "Malformed spec: no children in element dl.", locator);
            case IN_CATEGORIES_DT:
                if ("a" == localName && NS == uri) {
                    Matcher m = CATEGORIES.matcher(referenceText);
                    if (m.matches()) {
                        state = State.CAPTURING_CATEGORIES_DDS;
                        captureDepth = 0;
                        fragmentBuilder = new TreeBuilder(true, true);
                    } else {
                        throw new SAXParseException(
                                "Malformed spec: Expected dt to be categories dt but it was not.", locator);
                    }
                }
                break;
            case IN_CONTEXT_DT:
                if ("a" == localName && NS == uri) {
                    Matcher m = CONTEXT.matcher(referenceText);
                    if (m.matches()) {
                        state = State.CAPTURING_CONTEXT_DDS;
                        captureDepth = 0;
                        fragmentBuilder = new TreeBuilder(true, true);
                    } else {
                      System.err.printf("Line: %d Col: %d\n", locator.getLineNumber(), locator.getColumnNumber());
                        throw new SAXParseException(
                                "Malformed spec at element " + currentName.getLocalName() + " (" + currentId + "): Expected dt to be context dt but it was not.", locator);
                    }
                }
                break;
            case IN_CONTENT_MODEL_DT:
                if ("a" == localName && NS == uri) {
                    Matcher m = CONTENT_MODEL.matcher(referenceText);
                    if (m.matches()) {
                        state = State.CAPTURING_CONTENT_MODEL_DDS;
                        captureDepth = 0;
                        fragmentBuilder = new TreeBuilder(true, true);
                    } else {
                        throw new SAXParseException(
                                "Malformed spec: Expected dt to be content-model dt but it was not.", locator);
                    }
                }
                break;
            case IN_TAG_OMISSION_DT:
                if ("a" == localName && NS == uri) {
                    Matcher m = TAG_OMISSION.matcher(referenceText);
                    if (m.matches()) {
                        state = State.CAPTURING_TAG_OMISSION_DDS;
                        captureDepth = 0;
                        fragmentBuilder = new TreeBuilder(true, true);
                    } else {
                        throw new SAXParseException(
                                "Malformed spec: Expected dt to be tag-omission dt but it was not.", locator);
                    }
                }
                break;
            case IN_ATTRIBUTES_DT:
                if ("a" == localName && NS == uri) {
                    Matcher m = ATTRIBUTES.matcher(referenceText);
                    if (m.matches()) {
                        state = State.CAPTURING_ATTRIBUTES_DDS;
                        captureDepth = 0;
                        fragmentBuilder = new TreeBuilder(true, true);
                    } else {
                        throw new SAXParseException(
                                "Malformed spec: Expected dt to be content-attributes dt but it was not.", locator);
                    }
                }
                break;
            case CAPTURING_CATEGORIES_DDS:
            case CAPTURING_CONTEXT_DDS:
            case CAPTURING_CONTENT_MODEL_DDS:
            case CAPTURING_TAG_OMISSION_DDS:
            case CAPTURING_ATTRIBUTES_DDS:
                if ("dt" == localName) {
                    break;
                }
                if (captureDepth == 0) {
                    throw new SAXParseException(
                            "Malformed spec: Did not see following dt when capturing dds.", locator);
                }
                captureDepth--;
                fragmentBuilder.endElement(uri, localName, qName);
                break;
        }
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
        this.locator = locator;
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if ("p" == localName && NS == uri) {
            return;
        }
        switch (state) {
            case AWAITING_HEADING:
                if ("h4" == localName && NS == uri) {
                    referenceText.setLength(0);
                    currentId = atts.getValue("", "id");
                    currentName = null;
                    state = State.IN_H4;
                }
                break;
            case IN_H4:
                if ("code" == localName && NS == uri) {
                    nameText.setLength(0);
                    state = State.IN_CODE_IN_H4;
                }
                break;
            case IN_CODE_IN_H4:
                break;
            case AWAITING_ELEMENT_DL:
                if ("dl" == localName && NS == uri
                        && "element".equals(atts.getValue("", "class"))) {
                    state = State.IN_ELEMENT_DL_START;
                }
                break;
            case IN_ELEMENT_DL_START:
                if ("dt" == localName && NS == uri) {
                    referenceText.setLength(0);
                    state = State.IN_CATEGORIES_DT;
                } else {
                    throw new SAXParseException("Malformed spec: Expected dt in dl.", locator);
                }
                break;
            case IN_CATEGORIES_DT:
                if ("a" == localName && NS == uri) {
                    state = State.IN_CATEGORIES_DT;
                    break;
                }
            case IN_CONTEXT_DT:
                if ("a" == localName && NS == uri) {
                    state = State.IN_CONTEXT_DT;
                    break;
                }
            case IN_CONTENT_MODEL_DT:
                if ("a" == localName && NS == uri) {
                    state = State.IN_CONTENT_MODEL_DT;
                    break;
                }
            case IN_TAG_OMISSION_DT:
                if ("a" == localName && NS == uri) {
                    state = State.IN_TAG_OMISSION_DT;
                    break;
                }
            case IN_ATTRIBUTES_DT:
                if ("a" == localName && NS == uri) {
                    state = State.IN_ATTRIBUTES_DT;
                    break;
                }
                throw new SAXParseException(
                        "Malformed spec: Not expecting children in dts.", locator);
            case CAPTURING_CATEGORIES_DDS:
            case CAPTURING_CONTEXT_DDS:
            case CAPTURING_CONTENT_MODEL_DDS:
            case CAPTURING_TAG_OMISSION_DDS:
            case CAPTURING_ATTRIBUTES_DDS:
                if ("dt" == localName && NS == uri && captureDepth == 0) {
                    ignoreTextNodes = true;
                    DocumentFragment fragment = (DocumentFragment) fragmentBuilder.getRoot();
                    fragmentBuilder = null;
                    referenceText.setLength(0);
                    if (state == State.CAPTURING_CATEGORIES_DDS) {
                        categoriesByElement.put(currentName, fragment);
                        state = State.IN_CONTEXT_DT;
                    } else if (state == State.CAPTURING_CONTEXT_DDS) {
                        contextsByElement.put(currentName, fragment);
                        state = State.IN_CONTENT_MODEL_DT;
                    } else if (state == State.CAPTURING_CONTENT_MODEL_DDS) {
                        contentModelsByElement.put(currentName, fragment);
                        state = State.IN_TAG_OMISSION_DT;
                    } else if (state == State.CAPTURING_TAG_OMISSION_DDS) {
                        state = State.IN_ATTRIBUTES_DT;
                    } else {
                        attributesByElement.put(currentName, fragment);
                        state = State.AWAITING_HEADING;
                    }
                } else {
                    captureDepth++;
                    String href = null;
                    if ("a" == localName && NS == uri
                            && (href = atts.getValue("", "href")) != null) {
                        if (href.startsWith("#")) {
                            href = SPEC_LINK_URI + href;
                        }
                        AttributesImpl attributesImpl = new AttributesImpl();
                        attributesImpl.addAttribute("href", href);
                        fragmentBuilder.startElement(uri, localName, qName,
                                attributesImpl);
                    } else {
                        fragmentBuilder.startElement(uri, localName, qName,
                                EmptyAttributes.EMPTY_ATTRIBUTES);
                    }
                }
                break;
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

}
