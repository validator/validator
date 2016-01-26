/*
 * Copyright (c) 2005 Henri Sivonen
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

package nu.validator.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class NonBufferingAsciiInputStreamReader extends Reader {
    private InputStream stream;
    
    /**
     * @param stream
     */
    public NonBufferingAsciiInputStreamReader(InputStream stream) {
        this.stream = stream;
    }
    
    /**
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {
        stream.close();
    }
    /**
     * @see java.io.Reader#read()
     */
    @Override
    public int read() throws IOException {
        int rv = stream.read();
        if (rv < 0x80) {
            return rv;
        } else {
            throw new CharacterCodingException();
        }
    }

    /**
     * @see java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(char[] buf, int off, int len) throws IOException {
        int val = read();
        if(val == -1) {
            return -1;
        } else {
            buf[off] = (char) val;
            return 1;
        }
    }

    
    
}
