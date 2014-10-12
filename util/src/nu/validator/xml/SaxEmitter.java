/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2007 Mozilla Foundation
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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class SaxEmitter {
    protected ContentHandler contentHandler;

    /**
     * @param contentHandler
     */
    public SaxEmitter(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }
    public void startElement(String ns, String name, Attributes attrs) throws SAXException {
        this.contentHandler.startElement(ns, name, name, attrs);
    }
    
    public void startElement(String ns, String name) throws SAXException {
        this.contentHandler.startElement(ns, name, name, EmptyAttributes.EMPTY_ATTRIBUTES);
    }
    
    public void endElement(String ns, String name) throws SAXException {
        this.contentHandler.endElement(ns, name, name);
    }

    public void characters(String content) throws SAXException {
        this.contentHandler.characters(content.toCharArray(), 0, content.length());
    }

    public void characters(char[] content) throws SAXException {
        this.contentHandler.characters(content, 0, content.length);
    }

    public void characters(char[] buffer, int offset, int length) throws SAXException {
        this.contentHandler.characters(buffer, offset, length);
    }
}
