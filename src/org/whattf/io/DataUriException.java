/*
 * Copyright (c) 2008 Mozilla Foundation
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

package org.whattf.io;

import java.io.IOException;

public class DataUriException extends IOException {

    private final int index;
    private final String head;
    private final char literal;
    private final String tail;
    
    /**
     * @param index
     * @param head
     * @param literal
     * @param tail
     */
    public DataUriException(int index, String head, char literal, String tail) {
        super(head + '\u201C' + literal + '\u201D' + tail);
        this.index = index;
        this.head = head;
        this.literal = literal;
        this.tail = tail;
    }
    
    /**
     * Returns the index.
     * 
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the head.
     * 
     * @return the head
     */
    public String getHead() {
        return head;
    }

    /**
     * Returns the literal.
     * 
     * @return the literal
     */
    public char getLiteral() {
        return literal;
    }

    /**
     * Returns the tail.
     * 
     * @return the tail
     */
    public String getTail() {
        return tail;
    }
    
    
}
