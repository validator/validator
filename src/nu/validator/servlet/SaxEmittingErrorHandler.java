/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
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

import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public abstract class SaxEmittingErrorHandler extends AbstractErrorHandler {

    protected XhtmlSaxEmitter emitter;
    protected ContentHandler contentHandler;

    public SaxEmittingErrorHandler(ContentHandler contentHandler) {
        this.emitter = new XhtmlSaxEmitter(contentHandler);
        this.contentHandler = contentHandler;
    }

    protected void emitMessage(String message) throws SAXException {
        if (message == null) {
            message = "";
        }
        message = scrub(message);
        int len = message.length();
        int start = 0;
        int startQuotes = 0;
        for (int i = 0; i < len; i++) {
            char c = message.charAt(i);
            if (c == '\u201C') {
                startQuotes++;
                if (startQuotes == 1) {
                    this.emitter.characters(scrub(message.substring(start, i)));
                    start = i + 1;
                    this.emitter.startElement("code");
                }
            } else if (c == '\u201D' && startQuotes > 0) {
                startQuotes--;
                if (startQuotes == 0) {
                    this.emitter.characters(scrub(message.substring(start, i)));
                    start = i + 1;
                    this.emitter.endElement("code");                    
                }
            }
        }
        if (start < len) {
            this.emitter.characters(scrub(message.substring(start, len)));            
        }
        if (startQuotes > 0) {
            this.emitter.endElement("code");                                
        }
    }

}