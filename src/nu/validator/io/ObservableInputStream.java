/*
 * Copyright (c) 2005 Henri Sivonen
 * Copyright (c) 2008-2017 Mozilla Foundation
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
public class ObservableInputStream extends InputStream {

    private StreamObserver observer;

    private InputStream delegate;

    /**
     *  
     */
    public ObservableInputStream(InputStream delegate, StreamObserver obs) {
        this.delegate = delegate;
        this.observer = obs;
    }

    /**
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException {
        try {
            return delegate.available();
        } catch (IOException | RuntimeException e) {
            observer.exceptionOccurred(e);
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        try {
            observer.closeCalled();
            delegate.close();
        } catch (IOException | RuntimeException e) {
            observer.exceptionOccurred(e);
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    @Override
    public void mark(int arg0) {
        try {
            delegate.mark(arg0);
        } catch (RuntimeException e) {
            try {
                observer.exceptionOccurred(e);
            } catch (IOException e1) {
                throw new RuntimeException("The observer threw against the  API contract.", e1);
            }
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    @Override
    public boolean markSupported() {
        try {
            return delegate.markSupported();
        } catch (RuntimeException e) {
            try {
                observer.exceptionOccurred(e);
            } catch (IOException e1) {
                throw new RuntimeException("The observer threw against the  API contract.", e1);
            }
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    @Override
    public int read() throws IOException {
        try {
            return delegate.read();
        } catch (IOException | RuntimeException e) {
            observer.exceptionOccurred(e);
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(byte[] arg0) throws IOException {
        try {
            return delegate.read(arg0);
        } catch (IOException | RuntimeException e) {
            observer.exceptionOccurred(e);
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        try {
            return delegate.read(arg0, arg1, arg2);
        } catch (IOException | RuntimeException e) {
            if ("Corrupt GZIP trailer".equals(e.getMessage())) {
                return -1;
            } else {
                observer.exceptionOccurred(e);
                throw new RuntimeException("The observer failed to throw per API contract.");
            }
        }
    }

    /**
     * @see java.io.InputStream#reset()
     */
    @Override
    public void reset() throws IOException {
        try {
            delegate.reset();
        } catch (IOException | RuntimeException e) {
            observer.exceptionOccurred(e);
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    @Override
    public long skip(long arg0) throws IOException {
        try {
            return delegate.skip(arg0);
        } catch (IOException | RuntimeException e) {
            observer.exceptionOccurred(e);
            throw new RuntimeException("The observer failed to throw per API contract.");
        }
    }
    
    
    /**
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        observer.finalizerCalled();
        super.finalize();
    }
}