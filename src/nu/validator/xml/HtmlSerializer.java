/*
 * Copyright (c) 2003, 2004 Henri Sivonen and Taavi Hupponen
 * Copyright (c) 2006 Henri Sivonen
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;

import nu.validator.io.NcrEscapingWindows1252OutputStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Serializes a sequence of SAX events representing an XHTML 1.0 Strict document
 * to an <code>OutputStream</code> as a UTF-8-encoded HTML 4.01 Strict
 * document. The SAX events must represent a valid XHTML 1.0 document, except
 * the namespace prefixes don't matter and there may be
 * <code>startElement</code> and <code>endElement</code> calls for elements
 * from other namespaces. The <code>startElement</code> and
 * <code>endElement</code> calls for non-XHTML elements are ignored. No
 * validity checking is performed. Hence, the emitter of the SAX events is
 * responsible for making sure the events represent a document that meets the
 * above requirements. The <code>OutputStream</code> is closed when the end of
 * the document is seen.
 * 
 * @version $Id$
 * @author hsivonen
 * @author taavi
 */
public class HtmlSerializer implements ContentHandler {

    public final static int NO_DOCTYPE = 0;

    public final static int DOCTYPE_HTML401_TRANSITIONAL = 1;

    public final static int DOCTYPE_HTML401_STRICT = 2;

    public final static int DOCTYPE_HTML5 = 3;

    /**
     * The XHTML namespace URI
     */
    private final static String XHTML_NS = "http://www.w3.org/1999/xhtml";

    /**
     * HTML 4.01 elements which don't have an end tag
     */
    private static final String[] emptyElements = { "area", "base", "basefont",
            "br", "col", "command", "frame", "hr", "img", "input", "isindex",
            "link", "meta", "param" };

    /**
     * Minimized "boolean" HTML attributes
     */
    private static final String[] booleanAttributes = { "active", "async",
            "autofocus", "autosubmit", "checked", "compact", "declare",
            "default", "defer", "disabled", "ismap", "multiple", "nohref",
            "noresize", "noshade", "nowrap", "readonly", "required", "selected" };

    /**
     * The writer used for output
     */
    protected Writer writer;

    private int doctype;

    private String encoding;

    private boolean emitMeta;

    /**
     * Creates a new instance of HtmlSerializer in the HTML 4.01 doctype mode
     * with the UTF-8 encoding and no charset meta.
     * 
     * @param out
     *            the stream to which the output is written
     */
    public HtmlSerializer(OutputStream out) {
        this(out, DOCTYPE_HTML401_STRICT, false, "UTF-8");
    }

    public HtmlSerializer(OutputStream out, int doctype, boolean emitMeta) {
        this(out, doctype, emitMeta, "UTF-8");
    }

    public HtmlSerializer(OutputStream out, int doctype, boolean emitMeta,
            String enc) {
        this.emitMeta = emitMeta;
        if (doctype < 0 || doctype > 3) {
            throw new IllegalArgumentException("Bad doctype constant.");
        }
        this.doctype = doctype;
        if ("UTF-8".equalsIgnoreCase(enc)) {
            try {
                this.encoding = "UTF-8";
                this.writer = new OutputStreamWriter(out, "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("UTF-8 not supported", uee);
            }
        } else if ("Windows-1252".equalsIgnoreCase(enc)) {
            this.encoding = "Windows-1252";
            this.writer = new NcrEscapingWindows1252OutputStreamWriter(out);
        } else {
            throw new IllegalArgumentException(
                    "Encoding must be UTF-8 or Windows-1252.");
        }
    }

    /**
     * Writes out characters.
     * 
     * @param ch
     *            the source array
     * @param start
     *            the index of the first character to be written
     * @param length
     *            the number of characters to write
     * 
     * @throws SAXException
     *             if there are IO problems
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            for (int j = 0; j < length; j++) {
                char c = ch[start + j];
                switch (c) {
                    case '<':
                        this.writer.write("&lt;");
                        break;
                    case '>':
                        this.writer.write("&gt;");
                        break;
                    case '&':
                        this.writer.write("&amp;");
                        break;
                    default:
                        this.writer.write(c);
                }
            }
        } catch (IOException ioe) {
            throw (SAXException)new SAXException(ioe).initCause(ioe);
        }
    }

    /**
     * Must be called in the end.
     * 
     * @throws SAXException
     *             if there are IO problems
     */
    @Override
    public void endDocument() throws SAXException {
        try {
            this.writer.close();
        } catch (IOException ioe) {
            throw (SAXException)new SAXException(ioe).initCause(ioe);
        }
    }

    /**
     * Writes an end tag if the element is an XHTML element and is not an empty
     * element in HTML 4.01 Strict.
     * 
     * @param namespaceURI
     *            the XML namespace
     * @param localName
     *            the element name in the namespace
     * @param qName
     *            ignored
     * 
     * @throws SAXException
     *             if there are IO problems
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        try {
            if (XHTML_NS.equals(namespaceURI)
                    && Arrays.binarySearch(emptyElements, localName) < 0) {
                this.writer.write("</");
                this.writer.write(localName);
                this.writer.write('>');
            }
        } catch (IOException ioe) {
            throw (SAXException)new SAXException(ioe).initCause(ioe);
        }
    }

    /**
     * Must be called first.
     */
    @Override
    public void startDocument() throws SAXException {
        try {
            switch (doctype) {
                case NO_DOCTYPE:
                    return;
                case DOCTYPE_HTML5:
                    writer.write("<!DOCTYPE html>\n");
                    return;
                case DOCTYPE_HTML401_STRICT:
                    writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
                    return;
                case DOCTYPE_HTML401_TRANSITIONAL:
                    writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
                    return;
            }
        } catch (IOException ioe) {
            throw (SAXException)new SAXException(ioe).initCause(ioe);
        }
    }

    /**
     * Writes a start tag if the element is an XHTML element.
     * 
     * @param namespaceURI
     *            the XML namespace
     * @param localName
     *            the element name in the namespace
     * @param qName
     *            ignored
     * @param atts
     *            the attribute list
     * 
     * @throws SAXException
     *             if there are IO problems
     */
    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        try {
            if (XHTML_NS.equals(namespaceURI)) {

                if ("meta".equals(localName)
                        && ((atts.getIndex("", "http-equiv") != -1) || (atts.getIndex(
                                "", "httpequiv") != -1))) {
                    return;
                }

                // start and element name
                this.writer.write('<');
                this.writer.write(localName);

                // attributes
                int length = atts.getLength();
                boolean langPrinted = false;
                for (int i = 0; i < length; i++) {
                    String ns = atts.getURI(i);
                    String name = null;
                    if ("".equals(ns)) {
                        name = atts.getLocalName(i);
                    } else if ("http://www.w3.org/XML/1998/namespace".equals(ns)
                            && "lang".equals(atts.getLocalName(i))) {
                        name = "lang";
                    }
                    if (name != null && !(langPrinted && "lang".equals(name))) {
                        this.writer.write(' ');
                        this.writer.write(name);
                        if ("lang".equals(name)) {
                            langPrinted = true;
                        }
                        if (Arrays.binarySearch(booleanAttributes, name) < 0) {
                            // write value, escape certain characters
                            this.writer.write("=\"");
                            String value = atts.getValue(i);
                            for (int j = 0; j < value.length(); j++) {
                                char c = value.charAt(j);
                                switch (c) {
                                    case '<':
                                        this.writer.write("&lt;");
                                        break;
                                    case '>':
                                        this.writer.write("&gt;");
                                        break;
                                    case '&':
                                        this.writer.write("&amp;");
                                        break;
                                    case '"':
                                        this.writer.write("&quot;");
                                        break;
                                    default:
                                        this.writer.write(c);
                                }
                            }

                            this.writer.write('"');
                        }
                    }
                }

                // close
                this.writer.write('>');
                if (emitMeta && "head".equals(localName)) {
                    this.writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=");
                    this.writer.write(encoding);
                    this.writer.write("\">");
                }
            }
        } catch (IOException ioe) {
            throw (SAXException)new SAXException(ioe).initCause(ioe);
        }
    }

    /**
     * Used for testing. Pass a file:// URL as the command line argument.
     */
    public static void main(String[] args) {
        try {
            javax.xml.parsers.SAXParserFactory fac = javax.xml.parsers.SAXParserFactory.newInstance();
            fac.setNamespaceAware(true);
            fac.setValidating(false);
            XMLReader parser = fac.newSAXParser().getXMLReader();
            parser.setContentHandler(new HtmlSerializer(System.out));
            parser.parse(args[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Does nothing. */
    @Override
    public void endPrefixMapping(String str) throws SAXException {
    }

    /** Does nothing. */
    @Override
    public void ignorableWhitespace(char[] values, int param, int param2)
            throws SAXException {
    }

    /** Does nothing. */
    @Override
    public void processingInstruction(String str, String str1)
            throws SAXException {
    }

    /** Does nothing. */
    @Override
    public void setDocumentLocator(Locator locator) {
    }

    /** Does nothing. */
    @Override
    public void skippedEntity(String str) throws SAXException {
    }

    /** Does nothing. */
    @Override
    public void startPrefixMapping(String str, String str1) throws SAXException {
    }
}