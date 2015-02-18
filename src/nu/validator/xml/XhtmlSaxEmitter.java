/*
 * Copyright (c) 2005 Henri Sivonen
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

package nu.validator.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class XhtmlSaxEmitter extends SaxEmitter {
    
    private final AttributesImpl attrs = new AttributesImpl();
    
    /**
     * @param contentHandler
     */
    public XhtmlSaxEmitter(ContentHandler contentHandler) {
        super(contentHandler);
    }

    public static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    public void startElement(String name, Attributes attrs) throws SAXException {
        this.contentHandler.startElement(XHTML_NS, name, name, attrs);
    }
    
    public void startElement(String name) throws SAXException {
        this.contentHandler.startElement(XHTML_NS, name, name, EmptyAttributes.EMPTY_ATTRIBUTES);
    }
    
    public void endElement(String name) throws SAXException {
        this.contentHandler.endElement(XHTML_NS, name, name);
    }

    public void startElementWithClass(String name, String clazz) throws SAXException {
        attrs.clear();
        attrs.addAttribute("class", clazz);
        this.contentHandler.startElement(XHTML_NS, name, name, attrs);
    }
    
    public void option(String label, String value, boolean selected) throws SAXException {
        attrs.clear();
        attrs.addAttribute("value", value);
        if(selected) {
            attrs.addAttribute("selected", "selected");            
        }
        startElement("option", attrs);
        characters(label);
        endElement("option");
    }

    public void option(char[] label, String value, boolean selected) throws SAXException {
        attrs.clear();
        attrs.addAttribute("value", value);
        if(selected) {
            attrs.addAttribute("selected", "selected");            
        }
        startElement("option", attrs);
        characters(label);
        endElement("option");
    }
    
    public void checkbox(String name, String value, boolean checked) throws SAXException {
        attrs.clear();
        attrs.addAttribute("type", "checkbox");
        attrs.addAttribute("name", name);
        attrs.addAttribute("id", name);
        attrs.addAttribute("value", value);
        if(checked) {
            attrs.addAttribute("checked", "checked");            
        }
        startElement("input", attrs);
        endElement("input");
    }
    
}
