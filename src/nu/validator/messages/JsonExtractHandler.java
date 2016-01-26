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

import nu.validator.json.JsonHandler;
import nu.validator.source.SourceHandler;

import org.xml.sax.SAXException;

public class JsonExtractHandler implements SourceHandler {

    private static final char[] NEWLINE = {'\n'};
    
    private enum HiliteState {
        BEFORE,
        INSIDE,
        AFTER
    }
    
    private final JsonHandler handler;
    
    private int hiliteStart;
    
    private int hiliteLength;
    
    private HiliteState hiliteState;
    
    public JsonExtractHandler(JsonHandler handler) {
        this.handler = handler;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        handler.characters(ch, start, length);
        switch (hiliteState) {
            case BEFORE:
                hiliteStart += length;
                break;
            case INSIDE:
                hiliteLength += length;
            case AFTER:
                break;
        }
    }

    @Override
    public void endCharHilite() throws SAXException {
        assert hiliteState == HiliteState.INSIDE;
        hiliteState = HiliteState.AFTER;
    }

    @Override
    public void endRange() throws SAXException {
        assert hiliteState == HiliteState.INSIDE;
        hiliteState = HiliteState.AFTER;
    }

    @Override
    public void endSource() throws SAXException {
        handler.endString();
    }

    @Override
    public void newLine() throws SAXException {
        handler.characters(NEWLINE, 0, 1);
        switch (hiliteState) {
            case BEFORE:
                hiliteStart++;
                break;
            case INSIDE:
                hiliteLength++;
            case AFTER:
                break;
        }
    }

    @Override
    public void startCharHilite(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        assert hiliteState == HiliteState.BEFORE;
        hiliteState = HiliteState.INSIDE;
    }

    @Override
    public void startRange(int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        assert hiliteState == HiliteState.BEFORE;
        hiliteState = HiliteState.INSIDE;
    }

    @Override
    public void startSource(String type, String encoding) throws SAXException {
        hiliteStart = 0;
        hiliteLength = 0;
        hiliteState = HiliteState.BEFORE;
        handler.startString();
    }

    /**
     * Returns the hiliteLength.
     * 
     * @return the hiliteLength
     */
    public int getHiliteLength() {
        return hiliteLength;
    }

    /**
     * Returns the hiliteStart.
     * 
     * @return the hiliteStart
     */
    public int getHiliteStart() {
        return hiliteStart;
    }

    @Override
    public void setLineErrors(SortedSet<Integer> oneBasedLineErrors) throws SAXException {
        
    }

}
