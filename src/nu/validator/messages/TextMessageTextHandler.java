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

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.SAXException;

public class TextMessageTextHandler implements MessageTextHandler {

    private final Writer writer;
    
    private final boolean asciiQuotes;
    
    /**
     * @param writer
     */
    public TextMessageTextHandler(final Writer writer, final boolean asciiQuotes) {
        this.writer = writer;
        this.asciiQuotes = asciiQuotes;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        try {
            int end = start + length;
            for (int i = start; i < end; i++) {
                char c = ch[i];
                switch (c) {
                    case '\n':
                    case '\r':
                        if (start < i) {
                            writer.write(ch, start, i - start);                
                        }                        
                        start = i + 1;
                        writer.write(0x21A9);
                        break;
                    case '\u2018':
                    case '\u2019':
                        if (asciiQuotes) {
                            if (start < i) {
                                writer.write(ch, start, i - start);                
                            }                        
                            start = i + 1;
                            writer.write('\'');                            
                        }
                        break;
                }
            }
            if (start < end) {
                writer.write(ch, start, end - start);                
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endCode() throws SAXException {
        try {
            if (asciiQuotes) {
                writer.write('\"');                            
            } else {
                writer.write('\u201D');
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void endLink() throws SAXException {
    }

    @Override
    public void startCode() throws SAXException {
        try {
            if (asciiQuotes) {
                writer.write('\"');                            
            } else {
                writer.write('\u201C');
            }
        } catch (IOException e) {
            throw new SAXException(e.getMessage(), e);
        }
    }

    @Override
    public void startLink(String href, String title) throws SAXException {
    }

}
