/*
 * Copyright (c) 2008-2015 Mozilla Foundation
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

import java.net.URI;
import java.util.LinkedList;

import org.relaxng.datatype.DatatypeException;
import nu.validator.datatype.Language;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;

public class BaseUriTracker implements ContentHandler, UriLangContext {
    
    private enum Direction {
        LTR, RTL, INHERIT
    }

    private class Node {
        public URI currentAbsolute; // not null

        public String originalRelative; // null if no xml:base

        public String lang; // null if no xml:lang

        private boolean langSpeficied;

        private boolean rtl;

        /**
         * @param currentAbsolute
         * @param originalRelative
         */
        public Node(URI currentAbsolute, String originalRelative, String lang,
                boolean langSpecified, boolean rtl) {
            this.currentAbsolute = currentAbsolute;
            this.originalRelative = originalRelative;
            this.lang = lang;
            this.langSpeficied = langSpecified;
            this.rtl = rtl;
        }
    }

    private final IRIFactory iriFactory;
    
    private LinkedList<Node> stack = new LinkedList<Node>();

    private boolean baseSeen = false;

    private boolean contentLanguageSeen = false;

    /**
     * -1 at start
     * 0 in html
     * >=1 in head
     * -2 situation over
     */
    private int inHeadDepth = -1;

    private int inCruftDepth = 0;

    private boolean equalsIgnoreAsciiCase(CharSequence one, CharSequence other) {
        if (other == null && one == null) {
            return true;
        }
        if (other == null || one == null) {
            return false;
        }
        if (one.length() != other.length()) {
            return false;
        }
        for (int i = 0; i < other.length(); i++) {
            char c0 = one.charAt(i);
            if (c0 >= 'A' && c0 <= 'Z') {
                c0 += 0x20;
            }
            char c1 = other.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }

    public BaseUriTracker(String systemId, String contentLanguage) {

        this.iriFactory = new IRIFactory();
        this.iriFactory.shouldViolation(false, false);
        this.iriFactory.securityViolation(false, false);
        this.iriFactory.dnsViolation(false, false);
        this.iriFactory.mintingViolation(false, false);
        this.iriFactory.useSpecificationIRI(false);
        this.iriFactory.useSchemeSpecificRules("http", false);
        this.iriFactory.useSchemeSpecificRules("https", false);
        this.iriFactory.useSchemeSpecificRules("ftp", false);
        this.iriFactory.useSchemeSpecificRules("data", false);

        URI uri = null;
        try {
            IRI iri = iriFactory.construct(systemId);
            uri = new URI(iri.toASCIIString());
            if (!uri.isAbsolute()) {
                uri = null;
            }
        } catch (Exception e) {
            uri = null;
        }

        String lang = "";
        boolean langSpecified = false;
        if (contentLanguage != null) {
            try {
                if (!"".equals(contentLanguage)) {
                    Language.THE_INSTANCE.checkValid(contentLanguage);
                }
                lang = contentLanguage;
                langSpecified = true;
            } catch (DatatypeException e) {
            }
        }
        stack.add(new Node(uri, null, lang, langSpecified, false));
        stack.add(new Node(uri, null, lang, false, false)); // base/content-language placeholder
    }

    private Node peek() {
        return stack.getLast();
    }

    private void pop() {
        stack.removeLast();
    }

    private void push(String relative, String language, Direction dir) {
        String lang = "";
        boolean langSpecified = false;
        if (language != null) {
            try {
                if (!"".equals(language)) {
                    Language.THE_INSTANCE.checkValid(language);
                }
                lang = language;
                langSpecified = true;
            } catch (DatatypeException e) {
            }
        }

        Node curr = peek();
        URI base = curr.currentAbsolute;
        if (!langSpecified) {
            lang = curr.lang;
        }
        boolean rtl;
        switch (dir) {
            case RTL:
                rtl = true;
                break;
            case LTR:
                rtl = false;
                break;
            default:
                rtl = curr.rtl;
                break;
        }

        if (relative == null) {
            stack.addLast(new Node(base, null, lang, langSpecified, rtl));
        } else {
            URI newBase;
            String ascii = null;
            try {
                IRI relIri = iriFactory.construct(relative);
                ascii = relIri.toASCIIString();
                if (base != null) {
                    newBase = base.resolve(ascii);
                    if (!newBase.isAbsolute()) {
                        newBase = base;
                    }
                } else {
                    newBase = new URI(ascii);
                    if (!newBase.isAbsolute()) {
                        newBase = null;
                    }
                }
            } catch (Exception e) {
                newBase = base;
            }
            stack.addLast(new Node(newBase, ascii, lang, langSpecified, rtl));
        }
    }

    private void installBase(String href) {
        if (baseSeen) {
            return;
        }
        baseSeen = true;

        LinkedList<Node> oldStack = stack;
        stack = new LinkedList<Node>();
        int i = 0;
        for (Node node : oldStack) {
            if (i == 0) {
                stack.addLast(node); // root
            } else if (i == 1) {
                push(href, node.langSpeficied ? node.lang : null,
                        node.rtl ? Direction.RTL : Direction.LTR);
            } else {
                push(node.originalRelative, node.langSpeficied ? node.lang
                        : null, node.rtl ? Direction.RTL : Direction.LTR);
            }
            i++;
        }
    }

    private void installContentLanguage(String language) {
        if (contentLanguageSeen) {
            return;
        }
        contentLanguageSeen = true;

        String lang = "";
        boolean langSpecified = false;
        if (language != null) {
            try {
                if (!"".equals(language)) {
                    Language.THE_INSTANCE.checkValid(language);
                }
                lang = language;
                langSpecified = true;
            } catch (DatatypeException e) {
            }
        }
        if (!langSpecified) {
            return;
        }

        int i = 0;
        for (Node node : stack) {
            if (i == 0) {
                // nop
            } else if (i == 1) {
                node.lang = lang;
                node.langSpeficied = true; // probably unnecessary...
            } else {
                if (node.langSpeficied) {
                    return;
                } else {
                    node.lang = lang;
                }
            }
            i++;
        }
    }

    /**
     * @see nu.validator.xml.UriLangContext#currentLanguage()
     */
    public String currentLanguage() {
        return stack.getLast().lang;
    }

    /**
     * @see nu.validator.xml.UriLangContext#isCurrentRtl()
     */
    public boolean isCurrentRtl() {
        return stack.getLast().rtl;
    }
    
    /**
     * @see nu.validator.xml.UriLangContext#toAbsoluteUriWithCurrentBase(java.lang.String)
     */
    public String toAbsoluteUriWithCurrentBase(String uri) {
        try {
            IRI relIri = iriFactory.construct(uri);
            String ascii;
            ascii = relIri.toASCIIString();

            URI base = stack.getLast().currentAbsolute;
            URI rv;
            if (base == null) {
                rv = new URI(ascii);
            } else {
                rv = base.resolve(ascii);

            }
            if (rv.isAbsolute()) {
                return rv.toASCIIString();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if (inHeadDepth > 0) {
            inHeadDepth--;
            if (inHeadDepth == 0) {
                inHeadDepth = -2;
            }
        }
        if (inCruftDepth > 0) {
            inCruftDepth--;
        }
        pop();
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

    public void startDocument() throws SAXException {
        inHeadDepth = -1;
        inCruftDepth = 0;
    }

    public void startElement(String uri, String localName, String n,
            Attributes atts) throws SAXException {
        if (inHeadDepth >= 1) {
            inHeadDepth++;
            if ("http://www.w3.org/1999/xhtml" == uri) {
                if ("base" == localName) {
                    String href = atts.getValue("", "href");
                    if (href != null) {
                        installBase(href);
                    }
                } else if ("meta" == localName) {
                    String httpEquiv = atts.getValue("", "http-equiv");
                    if (equalsIgnoreAsciiCase("content-language", httpEquiv)) {
                        String content = atts.getValue("", "content");
                        if (content == null) {
                            content = "";
                        }
                        installContentLanguage(content);
                    }
                }
            }
        } else if (inHeadDepth == -1) {
            if ("html" == localName && "http://www.w3.org/1999/xhtml" == uri) {
                inHeadDepth = 0;
            } else {
                inHeadDepth = -2;
            }
        } else if (inHeadDepth == 0 && inCruftDepth == 0) {
            if ("head" == localName && "http://www.w3.org/1999/xhtml" == uri) {
                inHeadDepth = 1;
            } else {
                inCruftDepth = 1;
            }
        }

        String base = null;
        String lang = null;
        Direction dir = Direction.INHERIT;
        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            String ns = atts.getURI(i);
            if ("http://www.w3.org/XML/1998/namespace" == ns) {
                String name = atts.getLocalName(i);
                if ("lang" == name) {
                    lang = atts.getValue(i);
                } else if ("base" == name) {
                    base = atts.getValue(i);
                }
            } else if ("" == ns) {
                String name = atts.getLocalName(i);
                if (("dir" == name && "http://www.w3.org/1999/xhtml" == uri)
                        || ("direction" == name && "http://www.w3.org/2000/svg" == uri)) {
                    String value = atts.getValue(i);
                    if (equalsIgnoreAsciiCase("ltr", value)) {
                        dir = Direction.LTR;
                    } else if (equalsIgnoreAsciiCase("rtl", value)) {
                        dir = Direction.RTL;
                    }
                }
            }
        }
        push(base, lang, dir);
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}
