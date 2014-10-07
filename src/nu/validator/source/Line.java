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

package nu.validator.source;

final class Line {

    private char[] buffer;
    
    private int offset = 0;
    
    private int bufferLength = 0;
    
    /**
     * @param buffer
     * @param offset
     */
    Line(char[] buffer, int offset) {
        this.buffer = buffer;
        this.offset = offset;
    }
    
    /**
     * Returns the buffer.
     * 
     * @return the buffer
     */
    char[] getBuffer() {
        return buffer;
    }

    /**
     * Returns the bufferLength.
     * 
     * @return the bufferLength
     */
    int getBufferLength() {
        return bufferLength;
    }

    /**
     * Returns the offset.
     * 
     * @return the offset
     */
    int getOffset() {
        return offset;
    }

    /**
     * @see java.lang.StringBuffer#append(char[], int, int)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    void characters(char[] ch, int start, int length) {
        int newBufferLength = bufferLength + length;
        if (offset + newBufferLength > buffer.length) {
            char[] newBuf = new char[((newBufferLength >> 11) + 1) << 11];
            System.arraycopy(buffer, offset, newBuf, 0, bufferLength);
            buffer = newBuf;
            offset = 0;
        }
        System.arraycopy(ch, start, buffer, offset + bufferLength, length);
        bufferLength = newBufferLength;
    }

}
