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

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class CombineLexicalHandler implements LexicalHandler {

    private final LexicalHandler first;
    private final LexicalHandler second;

    /**
     * @param first
     * @param second
     */
    public CombineLexicalHandler(final LexicalHandler first, final LexicalHandler second) {
        this.first = first;
        this.second = second;
    }

    /**
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        first.comment(ch, start, length);
        second.comment(ch, start, length);
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    @Override
    public void endCDATA() throws SAXException {
        first.endCDATA();
        second.endCDATA();
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    @Override
    public void endDTD() throws SAXException {
        first.endDTD();
        second.endDTD();
    }

    /**
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    @Override
    public void endEntity(String name) throws SAXException {
        first.endEntity(name);
        second.endEntity(name);
    }

    /**
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    @Override
    public void startCDATA() throws SAXException {
        first.startCDATA();
        second.startCDATA();
    }

    /**
     * @param name
     * @param publicId
     * @param systemId
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        first.startDTD(name, publicId, systemId);
        second.startDTD(name, publicId, systemId);
    }

    /**
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    @Override
    public void startEntity(String name) throws SAXException {
        first.startEntity(name);
        second.startEntity(name);
    }
    
}
