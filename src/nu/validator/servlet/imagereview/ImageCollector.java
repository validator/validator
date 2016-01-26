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

package nu.validator.servlet.imagereview;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import nu.validator.source.SourceCode;
import nu.validator.xml.UriLangContext;

import nu.validator.checker.AttributeUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.thaiopensource.validate.Validator;

public class ImageCollector implements Validator, ContentHandler, Iterable<Image> {

    private final SourceCode sourceCode;
    
    private final List<Image> images = new LinkedList<>();
    
    private UriLangContext context = null;
    
    private int depthInLink = 0;
    
    private Locator locator = null;
    
    /**
     * @param sourceCode
     */
    public ImageCollector(SourceCode sourceCode) {
        this.sourceCode = sourceCode;
    }
    
    public void initializeContext(UriLangContext c) {
        this.context = c;
    }
    
    @Override
    public ContentHandler getContentHandler() {
        return this;
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void reset() {
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
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
        if (depthInLink > 0) {
            depthInLink--;
        }
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
        this.locator = locator;
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if (depthInLink > 0) {
            depthInLink++;
        } else if ("http://www.w3.org/1999/xhtml" == uri && "a" == name) {
            depthInLink = 1;
            return;
        }
        if ("http://www.w3.org/1999/xhtml" == uri && "img" == name) {
            String alt = null;
            String src = null;
            int width = -1;
            int height = -1;
            
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                if ("" == atts.getURI(i)) {
                    String n = atts.getLocalName(i);
                    if ("src" == n) {
                        src = context.toAbsoluteUriWithCurrentBase(atts.getValue(i));
                    } else if ("alt" == n) {
                        alt = atts.getValue(i);
                    } else if ("width" == n) {
                        // XXX deal with percentages
                        width = AttributeUtil.parsePositiveInteger(atts.getValue(i));
                    } else if ("height" == n) {
                        // XXX deal with percentages
                        height = AttributeUtil.parsePositiveInteger(atts.getValue(i));
                    }
                }
            }
            Image image = new Image(src, alt, context.currentLanguage(), context.isCurrentRtl(), width, height, depthInLink > 0, locator);
            sourceCode.registerRandeEnd(image);
            images.add(image);
        }
    }
    

    @Override
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    @Override
    public Iterator<Image> iterator() {
        return images.iterator();
    }

    /**
     * @return
     * @see java.util.List#isEmpty()
     */
    public boolean isEmpty() {
        return images.isEmpty();
    }


}
