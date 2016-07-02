/*
 * Copyright (c) 2008-2016 Mozilla Foundation
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

import io.mola.galimatias.URL;
import io.mola.galimatias.GalimatiasParseException;

public class BaseUriTracker implements ContentHandler, UriLangContext {

    private enum Direction {
        LTR, RTL, INHERIT
    }

    private class Node {
        public URL currentAbsolute; // not null

        public String originalRelative; // null if no xml:base

        public String lang; // null if no xml:lang

        private boolean langSpeficied;

        private boolean rtl;

        /**
         * @param currentAbsolute
         * @param originalRelative
         */
        public Node(URL currentAbsolute, String originalRelative, String lang,
                boolean langSpecified, boolean rtl) {
            this.currentAbsolute = currentAbsolute;
            this.originalRelative = originalRelative;
            this.lang = lang;
            this.langSpeficied = langSpecified;
            this.rtl = rtl;
        }
    }

    private LinkedList<Node> stack = new LinkedList<>();

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

        URL url = null;
        try {
            url = URL.parse(systemId);
        } catch (Exception e) {
            url = null;
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
        stack.add(new Node(url, null, lang, langSpecified, false));
        stack.add(new Node(url, null, lang, false, false)); // base/content-language placeholder
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
        URL base = curr.currentAbsolute;
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
            URL newBase;
            String ascii = null;
            try {
                if (base != null) {
                    try {
                        newBase = base.resolve(relative);
                    } catch (GalimatiasParseException e) {
                        newBase = base;
                    }
                } else {
                    try {
                        newBase = URL.parse((new URI(ascii)).toString());
                    } catch (GalimatiasParseException e) {
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
        stack = new LinkedList<>();
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
    @Override
    public String currentLanguage() {
        return stack.getLast().lang;
    }

    /**
     * @see nu.validator.xml.UriLangContext#isCurrentRtl()
     */
    @Override
    public boolean isCurrentRtl() {
        return stack.getLast().rtl;
    }
    
    /**
     * @see nu.validator.xml.UriLangContext#toAbsoluteUriWithCurrentBase(java.lang.String)
     */
    @Override
    public String toAbsoluteUriWithCurrentBase(String uri) {
        try {
            URL base = stack.getLast().currentAbsolute;
            return URL.parse(base, uri).toString();
        } catch (GalimatiasParseException e) {
            return null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
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
    public void startDocument() throws SAXException {
        inHeadDepth = -1;
        inCruftDepth = 0;
    }

    @Override
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

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}
