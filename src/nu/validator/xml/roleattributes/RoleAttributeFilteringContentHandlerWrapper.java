/*
 * Copyright (c) 2013-2017 Mozilla Foundation
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

package nu.validator.xml.roleattributes;

import nu.validator.checker.InfoAwareErrorHandler;
import nu.validator.xml.AttributesImpl;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoleAttributeFilteringContentHandlerWrapper
        implements ContentHandler {

    private final ContentHandler delegate;

    private final ErrorHandler errorHandler;

    private Locator locator = null;

    /**
     * @param delegate the underlying ContentHandler to which events will be delegated
     * @param errorHandler the ErrorHandler to which validation errors will be reported,
     *            or null if error reporting is not needed
     */
    public RoleAttributeFilteringContentHandlerWrapper(ContentHandler delegate,
            ErrorHandler errorHandler) {
        this.delegate = delegate;
        this.errorHandler = errorHandler;
    }

    /**
     * @param chars
     * @param start
     * @param length
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] chars, int start, int length)
            throws SAXException {
        delegate.characters(chars, start, length);
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
     * @param namespaceURI
     * @param localName
     * @param qName
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        delegate.endElement(namespaceURI, localName, qName);
    }

    /**
     * @param prefix
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        delegate.endPrefixMapping(prefix);
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
        delegate.ignorableWhitespace(ch, start, length);
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
        delegate.processingInstruction(target, data);
    }

    /**
     * @param locator
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        delegate.setDocumentLocator(locator);
    }

    /**
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        delegate.skippedEntity(name);
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
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String ns, String localName, String qName,
            Attributes attributes) throws SAXException {
        if ("http://www.w3.org/1999/xhtml".equals(ns)
                || "http://www.w3.org/2000/svg".equals(ns)) {
            delegate.startElement(ns, localName, qName, filterAttributes(attributes));
        } else {
            delegate.startElement(ns, localName, qName, attributes);
        }
    }

    private static final Set<String> NON_ABSTRACT_ARIA_ROLES = new HashSet<>(Arrays.asList(
            "alert", //
            "alertdialog", //
            "application", //
            "article", //
            "banner", //
            "button", //
            "cell", //
            "checkbox", //
            "columnheader", //
            "combobox", //
            "complementary", //
            "contentinfo", //
            "definition", //
            "dialog", //
            "directory", //
            "doc-abstract", //
            "doc-acknowledgments", //
            "doc-afterword", //
            "doc-appendix", //
            "doc-backlink", //
            "doc-biblioentry", //
            "doc-bibliography", //
            "doc-biblioref", //
            "doc-chapter", //
            "doc-colophon", //
            "doc-conclusion", //
            "doc-cover", //
            "doc-credit", //
            "doc-credits", //
            "doc-dedication", //
            "doc-endnote", //
            "doc-endnotes", //
            "doc-epigraph", //
            "doc-epilogue", //
            "doc-errata", //
            "doc-example", //
            "doc-footnote", //
            "doc-foreword", //
            "doc-glossary", //
            "doc-glossref", //
            "doc-index", //
            "doc-introduction", //
            "doc-noteref", //
            "doc-notice", //
            "doc-pagebreak", //
            "doc-pagelist", //
            "doc-part", //
            "doc-preface", //
            "doc-prologue", //
            "doc-pullquote", //
            "doc-qna", //
            "doc-subtitle", //
            "doc-tip", //
            "doc-toc", //
            "document", //
            "feed", //
            "figure", //
            "form", //
            "graphics-document", //
            "graphics-object", //
            "graphics-symbol", //
            "grid", //
            "gridcell", //
            "group", //
            "heading", //
            "img", //
            "link", //
            "list", //
            "listbox", //
            "listitem", //
            "log", //
            "main", //
            "marquee", //
            "math", //
            "menu", //
            "menubar", //
            "menuitem", //
            "menuitemcheckbox", //
            "menuitemradio", //
            "navigation", //
            "none", //
            "note", //
            "option", //
            "presentation", //
            "progressbar", //
            "radio", //
            "radiogroup", //
            "region", //
            "row", //
            "rowgroup", //
            "rowheader", //
            "scrollbar", //
            "search", //
            "searchbox", //
            "separator", //
            "slider", //
            "spinbutton", //
            "status", //
            "switch", //
            "tab", //
            "table", //
            "tablist", //
            "tabpanel", //
            "term", //
            "textbox", //
            "timer", //
            "toolbar", //
            "tooltip", //
            "tree", //
            "treegrid", //
            "treeitem" //
    ));

    private Attributes filterAttributes(Attributes attributes)
            throws SAXException {
        AttributesImpl attributesImpl = new AttributesImpl();
        for (int i = 0; i < attributes.getLength(); i++) {
            if ("role".equals(attributes.getLocalName(i))
                    && "".equals(attributes.getURI(i))) {
                attributesImpl.addAttribute(attributes.getURI(i),
                        attributes.getLocalName(i), attributes.getQName(i),
                        attributes.getType(i),
                        getFirstMatchingAriaRoleFromTokenList(
                                attributes.getValue(i).trim()));
            } else {
                attributesImpl.addAttribute(attributes.getURI(i),
                        attributes.getLocalName(i), attributes.getQName(i),
                        attributes.getType(i), attributes.getValue(i));
            }
        }
        return attributesImpl;
    }

    private String getFirstMatchingAriaRoleFromTokenList(String tokenList)
            throws SAXException {
        if ("".equals(tokenList)) {
            return "";
        }
        int len = tokenList.length();
        List<String> tokens = new ArrayList<>();
        boolean collectingSpace = true;
        int start = 0;
        for (int i = 0; i < len; i++) {
            char c = tokenList.charAt(i);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                if (!collectingSpace) {
                    tokens.add(tokenList.substring(start, i));
                    collectingSpace = true;
                }
            } else {
                if (collectingSpace) {
                    start = i;
                    collectingSpace = false;
                }
            }
        }
        if (start < len) {
            tokens.add(tokenList.substring(start, len));
        }
        String roleValue = null;
        List<String> unrecognizedTokens = new ArrayList<>();
        List<String> superfluousTokens = new ArrayList<>();
        for (String token : tokens) {
            if (!NON_ABSTRACT_ARIA_ROLES.contains(token)) {
                unrecognizedTokens.add(token);
            } else if (roleValue == null) {
                roleValue = token;
            } else {
                if (!"presentation".equals(token)) {
                    superfluousTokens.add(token);
                }
            }
        }
        if (errorHandler != null && roleValue != null
                && unrecognizedTokens.size() > 0) {
            errorHandler.error(new SAXParseException("Discarding unrecognized"
                    + renderTokenList(unrecognizedTokens)
                    + " from value of attribute"
                    + " “role”. Browsers ignore any"
                    + " token that is not a defined ARIA"
                    + " non-abstract role.", locator));

        }
        if (errorHandler instanceof InfoAwareErrorHandler
                && roleValue != null
                && superfluousTokens.size() > 0) {
            ((InfoAwareErrorHandler) errorHandler).info(
                    new SAXParseException("Discarding superfluous"
                    + renderTokenList(superfluousTokens)
                    + " from value of attribute"
                    + " “role”. Browsers only process"
                    + " the first token found that is a defined"
                    + " ARIA non-abstract role.", locator));

        }
        return roleValue != null ? roleValue : tokenList;
    }

    private CharSequence renderTokenList(List<String> tokens) {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        if (tokens.size() > 1) {
            sb.append(" tokens ");
        } else {
            sb.append(" token ");
        }
        for (String token : tokens) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append("“");
            sb.append(token);
            sb.append('”');
        }
        return sb;
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
        delegate.startPrefixMapping(prefix, uri);
    }

}
