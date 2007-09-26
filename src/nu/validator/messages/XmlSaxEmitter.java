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

package nu.validator.messages;

import nu.validator.xml.EmptyAttributes;
import nu.validator.xml.SaxEmitter;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @version $Id: XhtmlSaxEmitter.java 9 2007-08-11 08:40:38Z hsivonen $
 * @author hsivonen
 */
public class XmlSaxEmitter extends SaxEmitter {
    
    /**
     * @param contentHandler
     */
    public XmlSaxEmitter(ContentHandler contentHandler) {
        super(contentHandler);
    }

    public static final String NAMESPACE = "http://n.validator.nu/messages/";

    public void startElement(String name, Attributes attrs) throws SAXException {
        this.contentHandler.startElement(NAMESPACE, name, name, attrs);
    }
    
    public void startElement(String name) throws SAXException {
        this.contentHandler.startElement(NAMESPACE, name, name, EmptyAttributes.EMPTY_ATTRIBUTES);
    }
    
    public void endElement(String name) throws SAXException {
        this.contentHandler.endElement(NAMESPACE, name, name);
    }
    
}
