/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
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

import nu.validator.messages.types.MessageType;
import nu.validator.source.SourceHandler;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @version $Id: XhtmlMessageEmitter.java 54 2007-09-20 15:37:38Z hsivonen $
 * @author hsivonen
 */
public class XhtmlMessageEmitter extends MessageEmitter {

    private static final char[] COLON_SPACE = { ':', ' ' };

    private static final char[] PERIOD = { '.' };

    private static final char[] ON_LINE = "On line ".toCharArray();
    
    private static final char[] AT_LINE = "At line ".toCharArray();

    private static final char[] FROM_LINE = "From line ".toCharArray();

    private static final char[] TO_LINE = "; to line ".toCharArray();

    private static final char[] COLUMN = ", column ".toCharArray();

    private static final char[] IN_RESOURCE = " in resource ".toCharArray();
        
    private boolean listOpen = false;

    private final ContentHandler contentHandler;
    
    private final XhtmlSaxEmitter emitter;

    private final XhtmlMessageTextHandler messageTextHandler;
    
    private final XhtmlExtractHandler extractHandler;
    
    private boolean textEmitted;

    private String systemId;

    private int oneBasedFirstLine;

    private int oneBasedFirstColumn;

    private int oneBasedLastLine;

    private int oneBasedLastColumn;

    private boolean exact;
    
    /**
     * @param contentHandler
     */
    public XhtmlMessageEmitter(ContentHandler contentHandler) {
        super();
        this.contentHandler = contentHandler;
        this.emitter = new XhtmlSaxEmitter(contentHandler);
        this.messageTextHandler = new XhtmlMessageTextHandler(emitter);
        this.extractHandler = new XhtmlExtractHandler(emitter);
    }

    private void maybeOpenList() throws SAXException {
        if (!this.listOpen) {
            this.emitter.startElement("ol");
            this.listOpen = true;
        }
    }
    
    private void emitErrorLevel(char[] level) throws SAXException {
        this.emitter.startElement("strong");
        this.emitter.characters(level);
        this.emitter.endElement("strong");
    }
    
    @Override
    public void endMessage() throws SAXException {
        maybeCloseTextPara();
        this.emitter.endElement("li");
    }

    private void maybeCloseTextPara() throws SAXException {
        if (!textEmitted) {
            this.emitter.characters(PERIOD);
            this.emitter.endElement("p");
            maybeEmitLocation();
        }
    }

    private void maybeEmitLocation() throws SAXException {
        if (oneBasedLastLine == -1 && systemId == null) {
            return;
        }
        this.emitter.startElementWithClass("p", "location");
        if (oneBasedLastLine == -1) {
            emitSystemId();            
        } else if (oneBasedLastColumn == -1) {
            emitLineLocation();
        } else if (oneBasedFirstLine == -1 || (oneBasedFirstLine == oneBasedLastLine && oneBasedFirstColumn == oneBasedLastColumn)) {
            emitSingleLocation();
        } else {
            emitRangeLocation();
        }
        this.emitter.endElement("p");
    }

    /**
     * @throws SAXException
     */
    private void maybeEmitInResource() throws SAXException {
        if (systemId != null) {
            this.emitter.characters(IN_RESOURCE);
            emitSystemId();
        }
    }

    /**
     * @throws SAXException
     */
    private void emitSystemId() throws SAXException {
        this.emitter.startElementWithClass("span", "url");
        this.emitter.characters(systemId);
        this.emitter.endElement("span");
    }

    private void emitRangeLocation() throws SAXException {
        this.emitter.characters(FROM_LINE);
        this.emitter.startElementWithClass("span", "first-line");
        this.emitter.characters(Integer.toString(oneBasedFirstLine));
        this.emitter.endElement("span");
        this.emitter.characters(COLUMN);
        this.emitter.startElementWithClass("span", "first-col");
        this.emitter.characters(Integer.toString(oneBasedFirstColumn));
        this.emitter.endElement("span");
        this.emitter.characters(TO_LINE);
        this.emitter.startElementWithClass("span", "last-line");
        this.emitter.characters(Integer.toString(oneBasedLastLine));
        this.emitter.endElement("span");
        this.emitter.characters(COLUMN);
        this.emitter.startElementWithClass("span", "last-col");
        this.emitter.characters(Integer.toString(oneBasedLastColumn));
        this.emitter.endElement("span");
        maybeEmitInResource();
    }

    private void emitSingleLocation() throws SAXException {
        this.emitter.characters(AT_LINE);
        this.emitter.startElementWithClass("span", "last-line");
        this.emitter.characters(Integer.toString(oneBasedLastLine));
        this.emitter.endElement("span");
        this.emitter.characters(COLUMN);
        this.emitter.startElementWithClass("span", "last-col");
        this.emitter.characters(Integer.toString(oneBasedLastColumn));
        this.emitter.endElement("span");
        maybeEmitInResource();
    }

    private void emitLineLocation() throws SAXException {
        this.emitter.characters(ON_LINE);
        this.emitter.startElementWithClass("span", "last-line");
        this.emitter.characters(Integer.toString(oneBasedLastLine));
        this.emitter.endElement("span");
        maybeEmitInResource();
    }

    @Override
    public void startMessage(MessageType type, String systemId, int oneBasedFirstLine, int oneBasedFirstColumn, int oneBasedLastLine, int oneBasedLastColumn, boolean exact) throws SAXException {
        this.systemId = systemId;
        this.oneBasedFirstLine = oneBasedFirstLine;
        this.oneBasedFirstColumn = oneBasedFirstColumn;
        this.oneBasedLastLine = oneBasedLastLine;
        this.oneBasedLastColumn = oneBasedLastColumn;
        this.exact = exact;
        
        this.maybeOpenList();
        this.emitter.startElementWithClass("li", type.getFlatType());
        this.emitter.startElement("p");
        emitErrorLevel(type.getPresentationName());
        this.textEmitted = false;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endMessages()
     */
    @Override
    public void endMessages() throws SAXException {
        maybeCloseList();
    }

    /**
     * @throws SAXException
     */
    private void maybeCloseList() throws SAXException {
        if (this.listOpen) {
            this.emitter.endElement("ol");
            this.listOpen = false;
        }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endText()
     */
    @Override
    public void endText() throws SAXException {
        this.emitter.endElement("span");
        this.emitter.endElement("p");
        this.textEmitted = true;
        maybeEmitLocation();
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String)
     */
    @Override
    public void startMessages(String documentUri) throws SAXException {
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startText()
     */
    @Override
    public MessageTextHandler startText() throws SAXException {
        this.emitter.characters(COLON_SPACE);
        this.emitter.startElement("span");
        return messageTextHandler;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endSource()
     */
    @Override
    public void endSource() throws SAXException {
        emitter.endElement("code");
        emitter.endElement("pre");
    }

    /**
     * @throws SAXException 
     * @see nu.validator.messages.MessageEmitter#startSource()
     */
    @Override
    public SourceHandler startSource() throws SAXException {
        maybeCloseTextPara();
        emitter.startElement("pre");
        emitter.startElement("code");
        return extractHandler;
    }

}