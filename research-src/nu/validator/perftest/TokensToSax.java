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

package nu.validator.perftest;

import nu.validator.htmlparser.common.TokenHandler;
import nu.validator.htmlparser.impl.ElementName;
import nu.validator.htmlparser.impl.HtmlAttributes;
import nu.validator.htmlparser.impl.Tokenizer;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class TokensToSax implements TokenHandler {

    private static final int PHASE_START = 0;

    private static final int PHASE_CONTENT = 1;
    
    private static final int PHASE_TRAILING = 2;
    
    private final ContentHandler contentHandler;
    
    private final LexicalHandler lexicalHandler;
    
    private String[] stack;
    
    private int stackLen;
    
    private int phase;

    /**
     * @param contentHandler
     * @param lexicalHandler
     */
    public TokensToSax(ContentHandler contentHandler,
            LexicalHandler lexicalHandler) {
        if (contentHandler == null) {
            throw new IllegalArgumentException("ContentHandler must not be null.");
        }
        this.contentHandler = contentHandler;
        this.lexicalHandler = lexicalHandler;
    }

    /**
     * @param contentHandler
     * @param lexicalHandler
     */
    public TokensToSax(ContentHandler contentHandler) {
        if (contentHandler == null) {
            throw new IllegalArgumentException("ContentHandler must not be null.");
        }
        this.contentHandler = contentHandler;
        this.lexicalHandler = null;
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] buf, int start, int length)
            throws SAXException {
        if (phase == PHASE_CONTENT) {
            contentHandler.characters(buf, start, length);
        }
    }

    public void comment(char[] buf, int start, int length) throws SAXException {
        lexicalHandler.comment(buf, start, length);
    }

    public void doctype(String name, String publicIdentifier,
            String systemIdentifier, boolean forceQuirks) throws SAXException {
        if (phase == PHASE_START && lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicIdentifier, systemIdentifier);
            lexicalHandler.endDTD();
        }
    }

    public void endTag(ElementName eltName) throws SAXException {
        if (phase != PHASE_CONTENT) {
            return;
        }
        String name = eltName.name;
        if (stack[--stackLen] == name) {
            contentHandler.endElement("http://www.w3.org/1999/xhtml", name, null);
        } else {
            // OK, the end tag didn't match, let's see if there's an open tag on the stack
            for (int i = stackLen; i >= 0; i--) {
                if (stack[i] == name) {
                    // found a match
                    for (int j = stackLen; j >= i; j--) {
                        contentHandler.endElement("http://www.w3.org/1999/xhtml", stack[j], null);                        
                    }
                    stackLen = i;
                    return;
                }
            }
        }
        if (stackLen == 0) {
            phase = PHASE_TRAILING;
        }
    }

    public void endTokenization() throws SAXException {
        contentHandler.endDocument();
        stack = null;
    }

    public void eof() throws SAXException {
        
    }

    public boolean inForeign() throws SAXException {
        return true;
    }

    public void startTag(ElementName eltName, HtmlAttributes attributes,
            boolean selfClosing) throws SAXException {
        switch (phase) {
            case PHASE_START:
                phase = PHASE_CONTENT;
            case PHASE_CONTENT:
                String name = eltName.name;
                contentHandler.startElement("http://www.w3.org/1999/xhtml",
                        name, null, attributes);
                if (selfClosing) {
                    contentHandler.endElement("http://www.w3.org/1999/xhtml",
                            name, null);
                } else {
                    push(name);
                }
        }
    }

    private void push(String name) {
        if (stackLen == stack.length) {
            String[] newBuf = new String[stackLen + (stackLen >> 1)];
            System.arraycopy(stack, 0, newBuf, 0, stackLen);
            stack = newBuf;
        }
        stack[stackLen++] = name;
    }

    public void startTokenization(Tokenizer self) throws SAXException {
        stack = new String[16];
        phase = PHASE_START;
        stackLen = 0;
        contentHandler.setDocumentLocator(self);
        contentHandler.startDocument();
    }

    public boolean wantsComments() throws SAXException {
        return lexicalHandler != null;
    }
    
    
    
}
