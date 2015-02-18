/*
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

package org.whattf.checker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Dumps the parse events as warnings.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class DebugChecker extends Checker {

    /**
     * Contructor.
     */
    public DebugChecker() {
        super();
    }

    /**
     * @see org.whattf.checker.Checker#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        StringBuilder buf = new StringBuilder();
        buf.append("Characters: \u201C");
        buf.append(ch, start, length);
        buf.append("\u201D.");
        warn(buf.toString());
    }

    /**
     * @see org.whattf.checker.Checker#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
    }

    /**
     * @see org.whattf.checker.Checker#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        warn("EndElement: \u201C" + localName + "\u201D from namespace \u201C" + uri + "\u201D.");
    }

    /**
     * @see org.whattf.checker.Checker#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        warn("EndPrefixMapping: \u201C" + prefix + "\u201D.");        
    }

    /**
     * @see org.whattf.checker.Checker#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        warn("ProcessingInstruction: \u201C" + target + "\u201D, \u201C" + data + "\u201D.");
    }

    /**
     * @see org.whattf.checker.Checker#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        warn("SkippedEntity: \u201C" + name + "\u201D.");
    }

    /**
     * @see org.whattf.checker.Checker#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        warn("StartElement: \u201C" + localName + "\u201D from namespace \u201C" + uri + "\u201D.");
        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            warn("Attribute: \u201C" + atts.getLocalName(i) + "\u201D" + ("".equals(atts.getURI(i)) ? "" : "from namespace \u201C" + atts.getURI(i) + "\u201D") + " has value: \u201C" + atts.getValue(i) + "\u201D.");                       
        }
    }

    /**
     * @see org.whattf.checker.Checker#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        warn("StartPrefixMapping: \u201C" + prefix + "\u201D, \u201C" + uri + "\u201D.");
    }

}
