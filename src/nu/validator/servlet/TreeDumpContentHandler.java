/*
 * Copyright (c) 2007 Henri Sivonen
 * Copyright (c) 2008 Mozilla Foundation
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

package nu.validator.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class TreeDumpContentHandler implements ContentHandler, LexicalHandler {

    private final Writer writer;

    private int level = 0;

    private boolean inCharacters = false;

    private boolean close;

    /**
     * @param writer
     */
    public TreeDumpContentHandler(final Writer writer, boolean close) {
        this.writer = writer;
        this.close = close;
    }

    public TreeDumpContentHandler(final Writer writer) {
        this(writer, true);
    }

    private void printLead() throws IOException {
        if (inCharacters) {
            writer.write("\"\n");
            inCharacters = false;
        }
        writer.write("| ");
        for (int i = 0; i < level; i++) {
            writer.write("  ");
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            if (!inCharacters) {
                printLead();
                writer.write('"');
                inCharacters = true;
            }
            writer.write(ch, start, length);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        try {
            if (inCharacters) {
                writer.write("\"\n");
                inCharacters = false;
            }
            level--;
            if ("http://www.w3.org/1999/xhtml" == uri
                    && "template".equals(localName)) {
                level--;
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        try {
            printLead();
            writer.write('<');
            if ("http://www.w3.org/1998/Math/MathML" == uri) {
                writer.write("math ");                
            } else if ("http://www.w3.org/2000/svg" == uri) {
                writer.write("svg ");                                
            } else if ("http://www.w3.org/1999/xhtml" != uri) {
                writer.write("otherns ");                                
            }
            writer.write(localName);
            writer.write(">\n");
            level++;
            if ("http://www.w3.org/1999/xhtml" == uri
                    && "template".equals(localName)) {
                printLead();
                writer.write("content\n");
                level++;
            }

            TreeMap<String, String> map = new TreeMap<>();
            for (int i = 0; i < atts.getLength(); i++) {
                String ns = atts.getURI(i);
                String name;
                if ("http://www.w3.org/1999/xlink" == ns) {
                    name = "xlink " + atts.getLocalName(i);
                } else if ("http://www.w3.org/XML/1998/namespace" == ns) {
                    name = "xml " + atts.getLocalName(i);                    
                } else if ("http://www.w3.org/2000/xmlns/" == ns) {
                    name = "xmlns " + atts.getLocalName(i);                    
                } else if ("" != uri) {
                    name = atts.getLocalName(i);                    
                } else {
                    name = "otherns " + atts.getLocalName(i);                    
                }
                map.put(name, atts.getValue(i));
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                printLead();
                writer.write(entry.getKey());
                writer.write("=\"");
                writer.write(entry.getValue());
                writer.write("\"\n");
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void comment(char[] ch, int offset, int len) throws SAXException {
        try {
            printLead();
            writer.write("<!-- ");
            writer.write(ch, offset, len);
            writer.write(" -->\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startDTD(String name, String publicIdentifier,
            String systemIdentifier) throws SAXException {
        try {
            printLead();
            writer.write("<!DOCTYPE ");
            writer.write(name);
            if (publicIdentifier.length() > 0 || systemIdentifier.length() > 0) {
                writer.write(' ');
                writer.write('\"');
                writer.write(publicIdentifier);
                writer.write('\"');
                writer.write(' ');
                writer.write('\"');
                writer.write(systemIdentifier);
                writer.write('\"');
            }
            writer.write(">\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            if (inCharacters) {
                writer.write("\"\n");
                inCharacters = false;
            }
            if (close) {
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    @Override
    public void startEntity(String arg0) throws SAXException {
    }

    @Override
    public void endCDATA() throws SAXException {
    }

    @Override
    public void endDTD() throws SAXException {
    }

    @Override
    public void endEntity(String arg0) throws SAXException {
    }

    @Override
    public void startCDATA() throws SAXException {
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
    }

}
