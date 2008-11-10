/*
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.whattf.checker.AttributeUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.thaiopensource.validate.Validator;

public class CssDetector implements Validator, ContentHandler {

    public static boolean lowerCaseLiteralEqualsIgnoreAsciiCase(String lowerCaseLiteral,
            String string) {
        if (string == null) {
            return false;
        }
        if (lowerCaseLiteral.length() != string.length()) {
            return false;
        }
        for (int i = 0; i < lowerCaseLiteral.length(); i++) {
            char c0 = lowerCaseLiteral.charAt(i);
            char c1 = string.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }
    
    private static final Pattern TEXT_CSS = Pattern.compile("^[tT][eE][xX][tT]/[cC][sS][sS]\\s*(?:;.*)?$");
    
    private boolean sawCss = false;
    
    public ContentHandler getContentHandler() {
        return this;
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void reset() {
        sawCss = false;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        
    }

    public void endDocument() throws SAXException {
        
    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {
        
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // TODO Auto-generated method stub
        
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
        reset();
    }

    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("style" == localName) {
                checkType(atts);
                return;
            } else if ("link" == localName) {
                String rel = atts.getValue("", "rel");
                if (rel != null) {
                    String[] tokens = AttributeUtil.split(rel);
                    for (int i = 0; i < tokens.length; i++) {
                        String token = tokens[i];
                        if (lowerCaseLiteralEqualsIgnoreAsciiCase("stylesheet", token)) {
                            checkType(atts);
                            return;
                        }
                    }
                }
            } else {
                if (atts.getIndex("", "style") > -1) {
                    sawCss = true;
                }
            }
        }
    }

    private void checkType(Attributes atts) {
        String type = atts.getValue("", "type");
        if (type == null) {
            sawCss = true;
        } else {
            Matcher m = TEXT_CSS.matcher(type);
            if (m.matches()) {
                sawCss = true;                
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        
    }

}
