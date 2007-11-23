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
import java.io.Reader;
import java.net.MalformedURLException;

public final class Utf8PercentDecodingReader extends Reader {

    private final Reader delegate;
    
    private char pending = '\u0000';
    
    /**
     * @param delegate
     */
    public Utf8PercentDecodingReader(final Reader delegate) {
        this.delegate = delegate;
    }

    /**
     * @see java.io.Reader#read()
     */
    @Override
    public int read() throws IOException {
        if (pending != '\u0000') {
            char rv = pending;
            pending = '\u0000';
            return rv;
        }
        int byteVal = 0;
        int codePoint = 0;
        int c = delegate.read();
        int trailBytes = 0;
        if (c == -1) {
            return -1;
        } if (c == '%') {
            byteVal = readHexByte();
            if (byteVal < 0x80) {
                return byteVal;
            } else if ((0xE0 & byteVal) == 0xC0) {
                trailBytes = 1;
                codePoint = byteVal & 0x1F;
            } else if ((0xF0 & byteVal) == 0xE0) {
                trailBytes = 2;                
                codePoint = byteVal & 0x0F;
            } else if ((0xF8 & byteVal) == 0xF0) {
                trailBytes = 3;
                codePoint = byteVal & 0x07;
            } else {
                throw new MalformedURLException("Percent escape decodes to a byte that is not a valid UTF-8 lead byte.");                
            }
            for (int i = 0; i < trailBytes; i++) {
                byteVal = readPercentHexByte();
                if ((0xC0 & byteVal) == 0x80) {
                    codePoint = (codePoint << 6) | (byteVal & 0x3F);
                } else {
                    throw new MalformedURLException("Percent escape decodes to a byte that is not a valid UTF-8 trail byte.");                                    
                }
            }
            switch (trailBytes) {
                case 3:
                    if (codePoint <= 0xFFFF) {
                        throw new MalformedURLException("Non-shortest form UTF-8 percent escape sequence.");                                                            
                    } else {
                        int rv = (0xD7C0 + (codePoint >> 10));
                        pending = (char) (0xDC00 + (codePoint & 0x3FF));
                        return rv;
                    }
                case 2:
                    if (codePoint <= 0x07FF) {
                        throw new MalformedURLException("Non-shortest form UTF-8 percent escape sequence.");                                                                                    
                    } else {
                        return codePoint;
                    }
                default:
                    if (codePoint <= 0x007F) {
                        throw new MalformedURLException("Non-shortest form UTF-8 percent escape sequence.");                                                                                    
                    } else {
                        return codePoint;
                    }                    
            }
        } else {
            return c;
        }
    }

    private int readPercentHexByte() throws IOException {
        int c = delegate.read();
        if (c != '%') {
            throw new MalformedURLException("Percent-encoded trail byte missing.");                            
        }
        return readHexByte();
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
        pending = '\u0000';
        delegate.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int i = 0;
        while (i < len) {
            int c = read();
            if (c == -1) {
                if (i == 0) {
                    return -1;
                } else {
                    return i;
                }
            }
            cbuf[off] = (char) c;
            off++;
            i++;
        }
        return i;
    }

}
