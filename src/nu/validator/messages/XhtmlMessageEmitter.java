/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
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
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeParser;
import nu.validator.servlet.imagereview.Image;
import nu.validator.source.SourceHandler;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * @version $Id: XhtmlMessageEmitter.java 54 2007-09-20 15:37:38Z hsivonen $
 * @author hsivonen
 */
public class XhtmlMessageEmitter extends MessageEmitter implements ImageReviewHandler {

    private static final int IMAGE_CLAMP = 180;
    
    private static final char[] COLON_SPACE = { ':', ' ' };

    private static final char[] PERIOD = { '.' };

    private static final char[] ON_LINE = "On line ".toCharArray();

    private static final char[] AT_LINE = "At line ".toCharArray();

    private static final char[] FROM_LINE = "From line ".toCharArray();

    private static final char[] TO_LINE = "; to line ".toCharArray();

    private static final char[] COLUMN = ", column ".toCharArray();

    private static final char[] IN_RESOURCE = " in resource ".toCharArray();

    private static final char[] NOT_RESOLVABLE = "Not resolvable".toCharArray();

    private static final char[] EMPTY_STRING_AS_ALT = "Omit image in non-graphical presentation".toCharArray();

    private static final char[] NO_ALT = "Not available".toCharArray();

    private static final char[] IMAGE = "Image".toCharArray();

    private static final char[] TEXTUAL_ALTERNATIVE = "Textual alternative".toCharArray();

    private static final char[] LOCATION = "Location".toCharArray();

    private static final char[] IMAGE_REPORT = "Image report".toCharArray();

    private static final char[] SOURCE_CODE = "Source".toCharArray();
    
    private final AttributesImpl attrs = new AttributesImpl();
    
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

    private boolean willShowSource;
    
    private final TreeParser treeParser;

    /**
     * @param contentHandler
     */
    public XhtmlMessageEmitter(ContentHandler contentHandler) {
        super();
        this.contentHandler = contentHandler;
        this.emitter = new XhtmlSaxEmitter(contentHandler);
        this.messageTextHandler = new XhtmlMessageTextHandler(emitter);
        this.extractHandler = new XhtmlExtractHandler(emitter);
        this.treeParser = new TreeParser(contentHandler, null);
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
            maybeEmitLocation(true);
        }
    }

    private void maybeEmitLocation(boolean withPara) throws SAXException {
        if (oneBasedLastLine == -1 && systemId == null) {
            return;
        }
        if (withPara) {
            this.emitter.startElementWithClass("p", "location");
        }
        if (oneBasedLastLine == -1) {
            emitSystemId();
        } else if (oneBasedLastColumn == -1) {
            emitLineLocation();
        } else if (oneBasedFirstLine == -1
                || (oneBasedFirstLine == oneBasedLastLine && oneBasedFirstColumn == oneBasedLastColumn)) {
            emitSingleLocation();
        } else {
            emitRangeLocation();
        }
        if (withPara) {
            this.emitter.endElement("p");
        }
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
        if (willShowSource && systemId == null) {
            attrs.clear();
            attrs.addAttribute("href", "#l" + oneBasedLastLine + "c" + oneBasedLastColumn);
            emitter.startElement("a", attrs);
        }
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
        if (willShowSource && systemId == null) {
            emitter.endElement("a");
        }
    }

    private void emitSingleLocation() throws SAXException {
        if (willShowSource && systemId == null) {
            attrs.clear();
            attrs.addAttribute("href", "#cl" + oneBasedLastLine + "c" + oneBasedLastColumn);
            emitter.startElement("a", attrs);
        }
        this.emitter.characters(AT_LINE);
        this.emitter.startElementWithClass("span", "last-line");
        this.emitter.characters(Integer.toString(oneBasedLastLine));
        this.emitter.endElement("span");
        this.emitter.characters(COLUMN);
        this.emitter.startElementWithClass("span", "last-col");
        this.emitter.characters(Integer.toString(oneBasedLastColumn));
        this.emitter.endElement("span");
        maybeEmitInResource();
        if (willShowSource && systemId == null) {
            emitter.endElement("a");
        }
    }

    private void emitLineLocation() throws SAXException {
        if (willShowSource && systemId == null) {
            attrs.clear();
            attrs.addAttribute("href", "#l" + oneBasedLastLine);
            emitter.startElement("a", attrs);
        }
        this.emitter.characters(ON_LINE);
        this.emitter.startElementWithClass("span", "last-line");
        this.emitter.characters(Integer.toString(oneBasedLastLine));
        this.emitter.endElement("span");
        maybeEmitInResource();
        if (willShowSource && systemId == null) {
            emitter.endElement("a");
        }
    }

    @Override
    public void startMessage(MessageType type, String aSystemId,
            int aOneBasedFirstLine, int aOneBasedFirstColumn,
            int aOneBasedLastLine, int aOneBasedLastColumn, boolean exact)
            throws SAXException {
        this.systemId = aSystemId;
        this.oneBasedFirstLine = aOneBasedFirstLine;
        this.oneBasedFirstColumn = aOneBasedFirstColumn;
        this.oneBasedLastLine = aOneBasedLastLine;
        this.oneBasedLastColumn = aOneBasedLastColumn;

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
    public void endMessages(String language) throws SAXException {
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
        maybeEmitLocation(true);
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startMessages(java.lang.String, boolean)
     */
    @Override
    public void startMessages(String documentUri, boolean willShowSource)
            throws SAXException {
        this.willShowSource = willShowSource;
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
        emitter.endElement("p");
    }

    /**
     * @throws SAXException 
     * @see nu.validator.messages.MessageEmitter#startSource()
     */
    @Override
    public SourceHandler startSource() throws SAXException {
        maybeCloseTextPara();
        emitter.startElementWithClass("p", "extract");
        emitter.startElement("code");
        return extractHandler;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endFullSource()
     */
    @Override
    public void endFullSource() throws SAXException {
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startFullSource(int)
     */
    @Override
    public SourceHandler startFullSource(int lineOffset) throws SAXException {
        maybeCloseList();
        
        attrs.clear();
        attrs.addAttribute("id", "source");
        this.emitter.startElement("h2", attrs);
        this.emitter.characters(SOURCE_CODE);
        this.emitter.endElement("h2");
        
        return new XhtmlSourceHandler(emitter, lineOffset);
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endResult()
     */
    @Override
    public void endResult() throws SAXException {
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startResult()
     */
    @Override
    public ResultHandler startResult() throws SAXException {
        maybeCloseList();
        return new XhtmlResultHandler(emitter);
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endElaboration()
     */
    @Override
    public void endElaboration() throws SAXException {
    }

    /**
     * @see nu.validator.messages.MessageEmitter#startElaboration()
     */
    @Override
    public ContentHandler startElaboration() throws SAXException {
        return contentHandler;
    }

    /**
     * @see nu.validator.messages.MessageEmitter#endImageReview()
     */
    @Override
    public void endImageReview() throws SAXException {

    }

    /**
     * @see nu.validator.messages.MessageEmitter#startImageReview(DocumentFragment, boolean)
     */
    @Override
    public ImageReviewHandler startImageReview(DocumentFragment instruction, boolean fatal) throws SAXException {
        attrs.clear();
        attrs.addAttribute("id", "imagereport");
        this.emitter.startElement("h2", attrs);
        this.emitter.characters(IMAGE_REPORT);
        this.emitter.endElement("h2");

        if (instruction != null) {
            treeParser.parse(instruction);
        }

        return this;
    }

    @Override
    public void endImageGroup() throws SAXException {
        this.emitter.endElement("tbody");
        this.emitter.endElement("table");
    }

    @Override
    public void image(Image image, boolean showAlt, String aSystemId,
            int aOneBasedFirstLine, int aOneBasedFirstColumn,
            int aOneBasedLastLine, int aOneBasedLastColumn) throws SAXException {
        this.systemId = null;
        this.oneBasedFirstLine = aOneBasedFirstLine;
        this.oneBasedFirstColumn = aOneBasedFirstColumn;
        this.oneBasedLastLine = aOneBasedLastLine;
        this.oneBasedLastColumn = aOneBasedLastColumn;

        this.emitter.startElement("tr");
        
        imageCell(image);
        if (showAlt) {
            altCell(image.getAlt(), image.getLang(), image.isRtl());
        }
        locationCell();
        
        this.emitter.endElement("tr");
    }

    private void locationCell() throws SAXException {
        this.emitter.startElementWithClass("td", "location");       
        maybeEmitLocation(false);
        this.emitter.endElement("td");
    }

    private void altCell(String alt, String lang, boolean rtl) throws SAXException {
        attrs.clear();
        attrs.addAttribute("class", "alt");
        if (rtl && !(alt == null || "".equals(alt))) {
            attrs.addAttribute("dir", "rtl");
        }        
        this.emitter.startElement("td", attrs);       
        if (alt == null) {
            this.emitter.startElement("i");       
            this.emitter.characters(NO_ALT);
            this.emitter.endElement("i");            
        } else if ("".equals(alt)) {
            this.emitter.startElement("i");       
            this.emitter.characters(EMPTY_STRING_AS_ALT);
            this.emitter.endElement("i");                        
        } else {
            attrs.clear();
            attrs.addAttribute("http://www.w3.org/XML/1998/namespace", "lang", "xml:lang", "CDATA", lang);
            this.emitter.startElement("span", attrs);       
            this.emitter.characters(alt);
            this.emitter.endElement("span");                                    
        }
        this.emitter.endElement("td");        
    }

    private void imageCell(Image image) throws SAXException {
        this.emitter.startElementWithClass("td", "img");       
        String src = image.getSrc();
        if (src == null) {
            this.emitter.startElement("i");       
            this.emitter.characters(NOT_RESOLVABLE);
            this.emitter.endElement("i");                        
        } else {
            int width = image.getWidth();
            int height = image.getHeight();
            if (width < 1 || height < 1) {
                width = height = -1;
            } else if (width > height) {
                if (width > IMAGE_CLAMP) {
                    height = (int) Math.ceil(height * (((double)IMAGE_CLAMP)/((double)width)));
                    width = IMAGE_CLAMP;
                }
            } else {
                if (height > IMAGE_CLAMP) {
                    width = (int) Math.ceil(width * (((double)IMAGE_CLAMP)/((double)height)));
                    height = IMAGE_CLAMP;                
                }
            }
            attrs.clear();
            attrs.addAttribute("src", src);
            if (width != -1) {
                attrs.addAttribute("width", Integer.toString(width));
                attrs.addAttribute("height", Integer.toString(height));
            }
            this.emitter.startElement("img", attrs);
            this.emitter.endElement("img");            
        }
        this.emitter.endElement("td");
    }

    @Override
    public void startImageGroup(char[] heading, DocumentFragment instruction,
            boolean hasAlt) throws SAXException {
        this.emitter.startElement("h3");               
        this.emitter.characters(heading);
        this.emitter.endElement("h3");               
        
        treeParser.parse(instruction);
        
        this.emitter.startElementWithClass("table", "imagereview");     
        this.emitter.startElement("colgroup"); 

        this.emitter.startElementWithClass("col", "img"); 
        this.emitter.endElement("col");               

        if (hasAlt) {
            this.emitter.startElementWithClass("col", "alt"); 
            this.emitter.endElement("col");               
        }
        
        this.emitter.startElementWithClass("col", "location"); 
        this.emitter.endElement("col");               
        
        this.emitter.endElement("colgroup");               
        
        this.emitter.startElement("thead");               
        this.emitter.startElement("tr");               

        this.emitter.startElementWithClass("th", "img");               
        this.emitter.characters(IMAGE);        
        this.emitter.endElement("th");               

        if (hasAlt) {
            this.emitter.startElementWithClass("th", "alt");               
            this.emitter.characters(TEXTUAL_ALTERNATIVE);        
            this.emitter.endElement("th");               
        }
        
        this.emitter.startElementWithClass("th", "location");               
        this.emitter.characters(LOCATION);        
        this.emitter.endElement("th");                       
        
        this.emitter.endElement("tr");               
        this.emitter.endElement("thead");               
        this.emitter.startElement("tbody");               
    }

}
