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

import org.xml.sax.SAXException;

import nu.validator.source.SourceHandler;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.XhtmlSaxEmitter;

public class XhtmlSourceHandler implements SourceHandler {

    private static final char[] SPACE = {'\u00A0'};

    private final AttributesImpl attrs = new AttributesImpl();
    
    private final XhtmlSaxEmitter emitter;
    
    private boolean listOpen;

    private boolean lineOpen;
    
    private String rangeOpen;
    
    private boolean charOpen;
    
    private int lineNumber;
    
    private boolean lineHadCharacters;
    
    /**
     * @param emitter
     */
    public XhtmlSourceHandler(final XhtmlSaxEmitter emitter) {
        this.emitter = emitter;
    }
    
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        maybeOpen();
        lineHadCharacters = true;
        emitter.characters(ch, start, length);
    }

    private void maybeOpen() throws SAXException {
        assert !(lineOpen && !listOpen);
        if (!listOpen) {
            emitter.startElementWithClass("ol", "source");
            listOpen = true;
        }
        if (!lineOpen) {
            lineNumber++;
            attrs.clear();
            attrs.addAttribute("id", "l" + lineNumber);
            emitter.startElement("li", attrs);
            emitter.startElement("code");
            lineOpen = true;
            lineHadCharacters = false;
            if (rangeOpen != null) {
                emitter.startElementWithClass("b", rangeOpen);
            }
        }
    }

    public void endCharHilite() throws SAXException {
        if (!charOpen) {
            return;
        }
        emitter.endElement("b");
        charOpen = false;
    }

    public void endRange() throws SAXException {
        assert rangeOpen != null;
        emitter.endElement("b");
        rangeOpen = null;
    }

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

    public void newLine() throws SAXException {
        maybeOpen();
        if (charOpen) {
            endCharHilite();
        }
        if (rangeOpen != null) {
            emitter.endElement("b");
        }
        emitter.endElement("code");
        emitter.characters(SPACE);
        emitter.endElement("li");
        lineOpen = false;
    }

    public void startCharHilite(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        maybeOpen();
        assert !charOpen;
        assert lineNumber == oneBasedLine;
        attrs.clear();
        attrs.addAttribute("id", "cl" + oneBasedLine + "c" + oneBasedColumn);
        emitter.startElement("b", attrs);
        charOpen = true;
    }

    public void startRange(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        maybeOpen();
        assert rangeOpen == null;
        rangeOpen = "l" + oneBasedLine + "c" + oneBasedColumn;
        attrs.clear();
        attrs.addAttribute("id", rangeOpen);
        attrs.addAttribute("class", rangeOpen);
        emitter.startElement("b", attrs);
    }

    public void startSource(String type, String encoding) throws SAXException {
        listOpen = false;
        lineOpen = false;
        rangeOpen = null;
        charOpen = false;
        lineNumber = 0;
    }

}
