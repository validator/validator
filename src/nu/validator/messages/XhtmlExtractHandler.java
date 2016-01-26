/*
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

import java.util.SortedSet;

import nu.validator.source.SourceHandler;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.XhtmlSaxEmitter;

import org.xml.sax.SAXException;

public class XhtmlExtractHandler implements SourceHandler {

    private static final char[] NEWLINE_SUBSTITUTE = { '\u21A9' };

    private static final char[] ELLIPSIS = { '\u2026' };

    private final XhtmlSaxEmitter emitter;

    private static final AttributesImpl LINE_BREAK_ATTRS = new AttributesImpl();

    static {
        LINE_BREAK_ATTRS.addAttribute("class", "lf");
        LINE_BREAK_ATTRS.addAttribute("title", "Line break");
    }

    /**
     * @param emitter
     */
    public XhtmlExtractHandler(final XhtmlSaxEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (length < 200) {
            emitter.characters(ch, start, length);
        } else {
            emitter.characters(ch, start, 100);
            emitter.startElementWithClass("span", "snip");
            emitter.characters(ELLIPSIS);
            emitter.endElement("span");
            emitter.characters(ch, start + length - 100, 100);
        }
    }

    @Override
    public void endSource() throws SAXException {
    }

    @Override
    public void endCharHilite() throws SAXException {
        emitter.endElement("b");
    }

    @Override
    public void endRange() throws SAXException {
        emitter.endElement("b");
    }

    @Override
    public void newLine() throws SAXException {
        emitter.startElement("span", LINE_BREAK_ATTRS);
        emitter.characters(NEWLINE_SUBSTITUTE);
        emitter.endElement("span");
    }

    @Override
    public void startSource(String type, String encoding) throws SAXException {
    }

    @Override
    public void startCharHilite(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        emitter.startElement("b");
    }

    @Override
    public void startRange(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        emitter.startElement("b");
    }

    @Override
    public void setLineErrors(SortedSet<Integer> oneBasedLineErrors) throws SAXException {
        
    }

}
