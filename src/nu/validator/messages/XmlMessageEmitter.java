/*
 * Copyright (c) 2007-2016 Mozilla Foundation
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
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.CharacterUtil;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class XmlMessageEmitter extends MessageEmitter {

    private final AttributesImpl attrs = new AttributesImpl();

    private final ContentHandler contentHandler;

    private final XmlSaxEmitter emitter;

    private final XhtmlMessageTextHandler messageTextHandler;

    private final XmlExtractHandler extractHandler;

    private String openMessage;

    /**
     * @param contentHandler
     */
    public XmlMessageEmitter(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        this.emitter = new XmlSaxEmitter(contentHandler);
        this.messageTextHandler = new XhtmlMessageTextHandler(
                new XhtmlSaxEmitter(contentHandler));
        this.extractHandler = new XmlExtractHandler(emitter);
    }

    @Override
    public void endMessage() throws SAXException {
        assert openMessage != null;
        emitter.characters("\n");
        emitter.endElement(openMessage);
        openMessage = null;
    }

    @Override
    public void startMessage(MessageType type, String systemId,
            int oneBasedFirstLine, int oneBasedFirstColumn,
            int oneBasedLastLine, int oneBasedLastColumn, boolean exact)
                    throws SAXException {
        assert openMessage == null;
        openMessage = type.getSuperType();
        attrs.clear();
        if (systemId != null) {
            attrs.addAttribute("url",
                    CharacterUtil.prudentlyScrubCharacterData(systemId));
        }
        if (oneBasedLastLine != -1) {
            attrs.addAttribute("last-line", Integer.toString(oneBasedLastLine));
            if (oneBasedFirstLine != oneBasedLastLine) {
                attrs.addAttribute("first-line",
                        Integer.toString(oneBasedFirstLine));
            }
            if (oneBasedLastColumn != -1) {
                attrs.addAttribute("last-column",
                        Integer.toString(oneBasedLastColumn));
                if (oneBasedFirstColumn != oneBasedLastColumn) {
                    attrs.addAttribute("first-column",
                            Integer.toString(oneBasedFirstColumn));
                }
            }
        }
        String subType = type.getSubType();
        if (subType != null) {
            attrs.addAttribute("type", subType);
        }
        emitter.characters("\n");
        emitter.startElement(openMessage, attrs);
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
        emitter.characters("\n");
        if (!"".equals(language) && language != null) {
            emitter.startElement("language");
            emitter.characters(language);
            emitter.endElement("language");
        }
        emitter.characters("\n");
        emitter.endElement("messages");
        emitter.characters("\n");
        contentHandler.endPrefixMapping("");
        contentHandler.endPrefixMapping("h");
        contentHandler.endDocument();
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endSource()
     */
    @Override
    public void endSource() throws SAXException {
        emitter.endElement("extract");
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endText()
     */
    @Override
    public void endText() throws SAXException {
        emitter.endElement("message");
        emitter.characters("\n");
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startFullSource(int)
     */
    @Override
    public SourceHandler startFullSource(int lineOffset) throws SAXException {
        return new XmlSourceHandler(emitter);
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String,
     *      boolean)
     */
    @Override
    public void startMessages(String documentUri, boolean willShowSource)
            throws SAXException {
        contentHandler.startDocument();
        attrs.clear();
        if (documentUri != null) {
            attrs.addAttribute("url",
                    CharacterUtil.prudentlyScrubCharacterData(documentUri));
        }
        emitter.startElement("messages", attrs);
        openMessage = null;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startSource()
     */
    @Override
    public SourceHandler startSource() throws SAXException {
        emitter.startElement("extract");
        return extractHandler;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startText()
     */
    @Override
    public MessageTextHandler startText() throws SAXException {
        emitter.characters("\n");
        emitter.startElement("message");
        return messageTextHandler;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endElaboration()
     */
    @Override
    public void endElaboration() throws SAXException {
        emitter.endElement("elaboration");
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startElaboration()
     */
    @Override
    public ContentHandler startElaboration() throws SAXException {
        emitter.startElement("elaboration");
        return contentHandler;
    }

}
