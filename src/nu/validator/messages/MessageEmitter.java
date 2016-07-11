/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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

import nu.validator.messages.types.MessageType;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.source.SourceHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class MessageEmitter {

    public MessageEmitter() {
    }

    public void startMessages(String documentUri, boolean willShowSource)
            throws SAXException {

    }

    public void endMessages(String language) throws SAXException {

    }

    public abstract void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
            throws SAXException;

    public abstract void endMessage() throws SAXException;

    public MessageTextHandler startText() throws SAXException {
        return null;
    }

    public void endText() throws SAXException {

    }

    public SourceHandler startSource() throws SAXException {
        return null;
    }

    public void endSource() throws SAXException {

    }

    public ContentHandler startElaboration() throws SAXException {
        return null;
    }

    public void endElaboration() throws SAXException {

    }

    public SourceHandler startFullSource(int lineOffset) throws SAXException {
        return null;
    }

    public void endFullSource() throws SAXException {

    }

    public ResultHandler startResult() throws SAXException {
        return null;        
    }
    
    public void endResult() throws SAXException {
        
    }

    public ImageReviewHandler startImageReview(DocumentFragment instruction, boolean fatal) throws SAXException {
        return null;
    }

    public void endImageReview() throws SAXException {

    }
}
