/*
 * Copyright (c) 2005 Henri Sivonen
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

package nu.validator.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * @version $Id$
 * @author hsivonen
 */
public final class BoundedInputStream extends InputStream {

    private final InputStream delegate;
    
    private final String systemId;

    private long counter = 0;

    private long limit;

    /**
     * @param delegate
     * @param limit
     */
    public BoundedInputStream(InputStream delegate, long limit, String systemId) {
        this.delegate = delegate;
        this.limit = limit;
        this.systemId = systemId;
    }

    private void checkLimit() throws IOException {
        if (counter > limit) {
            throw new StreamBoundException("Stream length exceeds limit.", systemId);
        }
    }

    /**
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    /**
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public void mark(int arg0) {
        delegate.mark(arg0);
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        this.checkLimit();
        this.counter++;
        return delegate.read();
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] arg0) throws IOException {
        this.checkLimit();
        int c = delegate.read(arg0);
        this.counter += c;
        return c;
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        this.checkLimit();
        int c = delegate.read(arg0, arg1, arg2);
        this.counter += c;
        return c;
    }

    /**
     * @see java.io.InputStream#reset()
     */
    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip(long arg0) throws IOException {
        return delegate.skip(arg0);
    }
}