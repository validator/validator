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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Conceptually decodes bytes into ASCII and then decodes Base64 
 * into bytes. In practice, converting into a <code>Reader</code>
 * in between is optimized away.
 * 
 * @version $Id$
 * @author hsivonen
 */
public class Base64InputStream extends InputStream {

    private static final int[] DECODING_TABLE = new int[256];
    
    static {
        for (int i = 0; i < DECODING_TABLE.length; i++) {
            if (i >= 'A' && i <= 'Z') {
                DECODING_TABLE[i] = (i - 'A');
            } else if (i >= 'a' && i <= 'z') {
                DECODING_TABLE[i] = (i - 'a' + 26);
            } else if (i >= '0' && i <= '9') {
                DECODING_TABLE[i] = (i - '0' + 52);
            } else if (i == '+') {
                DECODING_TABLE[i] = 62;
            } else if (i == '/') {
                DECODING_TABLE[i] = 63;
            } else if (i == '=') {
                DECODING_TABLE[i] = -2;
            } else {
                DECODING_TABLE[i] = -1;
            }
        }
    }
    
    private final InputStream delegate;

    private int bytesLeftInBuffer = 0;
    
    private int buffer = 0;
    
    /**
     * @param delegate
     */
    public Base64InputStream(InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int read() throws IOException {
        if (bytesLeftInBuffer == 0) {
            bytesLeftInBuffer = 3;
            for (int i = 0; i < 4; i++) {
                int c = delegate.read();
                buffer <<= 6;
                if (c < 0) {
                    if (i == 0) {
                        bytesLeftInBuffer = 0;
                        return -1;
                    } else {
                        throw new EOFException();
                    }
                } else if (bytesLeftInBuffer == 2) {
                    if (c == '=') {
                        bytesLeftInBuffer = 1;
                    } else {
                        throw new IOException("Non-padding in Base64 stream after padding had started.");
                    }
                } else {
                    int b = DECODING_TABLE[c];
                    if (b == -2) {
                        if (i <= 1) {
                            throw new IOException(
                                    "Base 64 padding in a bad position.");
                        }
                        bytesLeftInBuffer = 2;
                    } else if (b == -1) {
                        throw new IOException("Non-Base64 input: \u201C0x"
                                + Integer.toHexString(c) + "\u201D.");
                    } else {
                        buffer |= b;
                    }
                }
            }
        }
        int rv = (buffer & 0xFF0000) >> 16;
        buffer <<= 8;
        bytesLeftInBuffer--;
        return rv;
    }

    /**
     * @throws IOException
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        delegate.close();
    }

}
