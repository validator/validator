/*
 * Copyright (c) 2007-2018 Mozilla Foundation
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

import java.util.SortedSet;

import nu.validator.source.SourceHandler;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.SAXException;

public class XhtmlSourceHandler implements SourceHandler {

    private static final char[] NEWLINE_SUBSTITUTE = {'\u21A9'};

    private static final AttributesImpl LINE_BREAK_ATTRS = new AttributesImpl();

    static {
        LINE_BREAK_ATTRS.addAttribute("class", "lf");
        LINE_BREAK_ATTRS.addAttribute("title", "Line break");
    }
    
    private final AttributesImpl attrs = new AttributesImpl();
    
    private final XhtmlSaxEmitter emitter;
    
    private final int lineOffset;
    
    private boolean listOpen;

    private boolean lineOpen;
    
    private String rangeOpen;
    
    private boolean charOpen;
    
    private int lineNumber;

    private SortedSet<Integer> oneBasedLineErrors;
    
    /**
     * @param emitter
     */
    public XhtmlSourceHandler(XhtmlSaxEmitter emitter, int lineOffset) {
        this.emitter = emitter;
        this.lineOffset = lineOffset;
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        maybeOpen();
        emitter.characters(ch, start, length);
    }

    private void maybeOpen() throws SAXException {
        assert !(lineOpen && !listOpen);
        if (!listOpen) {
            attrs.clear();
            attrs.addAttribute("class", "source");
            if (lineOffset > 0) {
                attrs.addAttribute("start", Integer.toString(1 + lineOffset));                
            }
            emitter.startElement("ol", attrs);
            listOpen = true;
        }
        if (!lineOpen) {
            lineNumber++;
            attrs.clear();
            attrs.addAttribute("id", "l" + (lineNumber + lineOffset));
            if (oneBasedLineErrors != null && oneBasedLineErrors.contains(lineNumber)) {
                attrs.addAttribute("class", "b");
            }
            emitter.startElement("li", attrs);
            emitter.startElement("code");
            lineOpen = true;
            if (rangeOpen != null) {
                emitter.startElementWithClass("b", rangeOpen);
            }
        }
    }

    @Override
    public void endCharHilite() throws SAXException {
        if (!charOpen) {
            return;
        }
        emitter.endElement("b");
        charOpen = false;
    }

    @Override
    public void endRange() throws SAXException {
        assert rangeOpen != null;
        emitter.endElement("b");
        rangeOpen = null;
    }

    @Override
    public void endSource() throws SAXException {
        if (charOpen) {
            endCharHilite();
        }
        assert rangeOpen == null;
        if (lineOpen) {
            emitter.endElement("code");
            emitter.endElement("li");
        }
        if (listOpen) {
            emitter.endElement("ol");
        }
    }

    @Override
    public void newLine() throws SAXException {
        maybeOpen();
        if (charOpen) {
            endCharHilite();
        }
        if (rangeOpen != null) {
            emitter.endElement("b");
        }
        emitter.endElement("code");
        emitter.startElement("code", LINE_BREAK_ATTRS);
        emitter.characters(NEWLINE_SUBSTITUTE);
        emitter.endElement("code");
        emitter.endElement("li");
        lineOpen = false;
    }

    @Override
    public void startCharHilite(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        maybeOpen();
        assert !charOpen;
        assert lineNumber == oneBasedLine;
        attrs.clear();
        attrs.addAttribute("id", "cl" + (oneBasedLine + lineOffset) + "c" + oneBasedColumn);
        emitter.startElement("b", attrs);
        charOpen = true;
    }

    @Override
    public void startRange(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        maybeOpen();
        assert rangeOpen == null;
        rangeOpen = "l" + (oneBasedLine + lineOffset) + "c" + oneBasedColumn;
        attrs.clear();
        attrs.addAttribute("id", rangeOpen);
        attrs.addAttribute("class", rangeOpen);
        emitter.startElement("b", attrs);
    }

    @Override
    public void startSource(String type, String encoding) throws SAXException {
        listOpen = false;
        lineOpen = false;
        rangeOpen = null;
        charOpen = false;
        lineNumber = 0;
        oneBasedLineErrors = null;
    }

    @Override
    public void setLineErrors(SortedSet<Integer> oneBasedLineErrors) throws SAXException {
        this.oneBasedLineErrors = oneBasedLineErrors;
    }

}
