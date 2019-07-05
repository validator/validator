/*
 * Copyright (c) 2007-2019 Mozilla Foundation
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

import nu.validator.json.JsonHandler;
import nu.validator.messages.types.MessageType;
import nu.validator.source.SourceHandler;

import org.xml.sax.SAXException;

public class JsonMessageEmitter extends MessageEmitter {

    private final JsonHandler handler;
    
    private final String callback;
    
    private final JsonExtractHandler extractHandler;

    private final JsonMessageTextHandler messageTextHandler;
    
    private boolean arrayOpen;
    
    /**
     * @param handler
     */
    public JsonMessageEmitter(final JsonHandler handler, final String callback,
            boolean asciiQuotes) {
        this.handler = handler;
        this.callback = callback;
        this.extractHandler = new JsonExtractHandler(handler);
        this.messageTextHandler = new JsonMessageTextHandler(handler,
                asciiQuotes);
    }

    @Override
    public void endMessage() throws SAXException {
        handler.endObject();
    }

    @Override
    public void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
            throws SAXException {
        assert arrayOpen;
        handler.startObject();
        handler.key("type");
        handler.string(type.getSuperType());
        if (systemId != null) {
            handler.key("url");
            handler.string(systemId);
        }
        if (oneBasedLastLine != -1) {
            handler.key("lastLine");
            handler.number(oneBasedLastLine);
            if (oneBasedFirstLine != oneBasedLastLine) {
                handler.key("firstLine");
                handler.number(oneBasedFirstLine);                
            }
            if (oneBasedLastColumn != -1) {
                handler.key("lastColumn");
                handler.number(oneBasedLastColumn);                                
                if (oneBasedFirstColumn != oneBasedLastColumn) {
                    handler.key("firstColumn");
                    handler.number(oneBasedFirstColumn);                
                }        
            }        
        }
        String subType = type.getSubType();
        if (subType != null) {
            handler.key("subType");
            handler.string(subType);
        }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endFullSource()
     */
    @Override
    public void endFullSource() throws SAXException {
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endMessages()
     */
    @Override
    public void endMessages(String language) throws SAXException {
        maybeCloseArray();
        if (!"".equals(language) && language != null) {
            handler.key("language");
            handler.string(language);
        }
        handler.endObject();
        handler.endDocument();
    }

    /**
     * @throws SAXException
     */
    private void maybeCloseArray() throws SAXException {
        if (arrayOpen) {
            handler.endArray();
            arrayOpen = false;
        }
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endSource()
     */
    @Override
    public void endSource() throws SAXException {
        handler.key("hiliteStart");
        handler.number(extractHandler.getHiliteStart());
        handler.key("hiliteLength");
        handler.number(extractHandler.getHiliteLength());
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endText()
     */
    @Override
    public void endText() throws SAXException {
        handler.endString();
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startFullSource(int)
     */
    @Override
    public SourceHandler startFullSource(int lineOffset) throws SAXException {
        maybeCloseArray();
        handler.key("source");
        return new JsonSourceHandler(handler);
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String, boolean)
     */
    @Override
    public void startMessages(String documentUri, boolean willShowSource) throws SAXException {
        handler.startDocument(callback);
        handler.startObject();
        if (documentUri != null) {
            handler.key("url");
            handler.string(documentUri);
        }
        handler.key("messages");
        handler.startArray();
        arrayOpen = true;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startSource()
     */
    @Override
    public SourceHandler startSource() throws SAXException {
        handler.key("extract");
        return extractHandler;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startText()
     */
    @Override
    public MessageTextHandler startText() throws SAXException {
        handler.key("message");
        handler.startString();
        return messageTextHandler;
    }

}
