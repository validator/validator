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

package nu.validator.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class ForbiddenCharacterFilter extends ContentHandlerFilter {

    /**
     * Array version of U+FFFD.
     */
    private static final char[] REPLACEMENT_CHARACTER = { '\uFFFD' };

    /**
     * @param contentHandler
     */
    public ForbiddenCharacterFilter(ContentHandler contentHandler) {
        super(contentHandler, null);
    }

    /**
     * @see nu.validator.xml.ContentHandlerFilter#characters(char[], int, int)
     */
    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        int end = start + length;
        for (int i = start; i < end; i++) {
            char c = chars[i];
            if (!((c >= '\u0020' && c <= '\uFFFD') || c == '\t' || c == '\n' || c == '\r') || c == '\uFEFF' || (c >= '\u007F' && c <= '\u009F') || (c >= '\uFDD0' && c <= '\uFDDF')) {
                if (start < i) {
                    super.characters(chars, start, i - start);
                }
                super.characters(REPLACEMENT_CHARACTER, 0, 1);
                start = i + 1;
            }
        }
        if (start < end) {
            super.characters(chars, start, end - start);
        }
    }


}
