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

import org.xml.sax.SAXException;

public class JsonMessageTextHandler implements MessageTextHandler {

    private char[] LEFT;

    private char[] RIGHT;
    
    private final JsonHandler handler;
    

    /**
     * @param handler
     */
    public JsonMessageTextHandler(final JsonHandler handler,
            boolean asciiQuotes) {
        this.handler = handler;
        if (asciiQuotes) {
            LEFT = new char[]{'\''};
            RIGHT = new char[]{'\''};
        } else {
            LEFT = new char[]{'\u201C'};
            RIGHT = new char[]{'\u201D'};
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        handler.characters(ch, start, length);
    }

    @Override
    public void endCode() throws SAXException {
        handler.characters(RIGHT, 0, 1);
    }

    @Override
    public void endLink() throws SAXException {
    }

    @Override
    public void startCode() throws SAXException {
        handler.characters(LEFT, 0, 1);
    }

    @Override
    public void startLink(String href, String title) throws SAXException {
    }

}
