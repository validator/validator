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

package org.whattf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;

public final class PercentDecodingReaderInputStream extends InputStream {

    private final Reader delegate;
    
    /**
     * @param delegate
     */
    public PercentDecodingReaderInputStream(final Reader delegate) {
        this.delegate = delegate;
    }

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        int c = delegate.read();
        if (c == -1) {
            return -1;
        } if (c == '%') {
            return readHexByte();
        } else if (c < 0x80) {
            return c;
        } else {
            throw new MalformedURLException("Unescaped non-ASCII character.");            
        }
    }

    private int readHexByte() throws IOException {
        int c = delegate.read();
        if (isHexDigit(c)) {
            int hi = Character.getNumericValue(c) << 4;
            c = delegate.read();
            if (isHexDigit(c)) {
                return hi | Character.getNumericValue(c);
            } else {
                throw new MalformedURLException("Malformed percent escape.");
            }
        } else {
            throw new MalformedURLException("Malformed percent escape.");
        }
    }

    private boolean isHexDigit(int c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
