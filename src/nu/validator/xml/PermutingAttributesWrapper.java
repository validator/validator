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

package nu.validator.xml;

import org.xml.sax.Attributes;

public final class PermutingAttributesWrapper implements Attributes {

    private final Attributes delegate;
    
    private final int[] permutation;
    
    public PermutingAttributesWrapper(Attributes delegate) {
        this.delegate = delegate;
        this.permutation = new int[delegate.getLength()];
        for (int i = 0; i < permutation.length; i++) {
            permutation[i] = i;
        }
    }

    private int findIndex(int index) {
        if (index < 0) {
            return -1;
        }
        for (int i = 0; i < permutation.length; i++) {
            if (permutation[i] == index) {
                return i;
            }
        }
        throw new IllegalArgumentException("Index not in range.");
    }
    
    public void pullUp(String uri, String localName) {
        int index = getIndex(uri, localName);
        if (index <= 0) {
            return;
        }
        int temp = permutation[index];
        System.arraycopy(permutation, 0, permutation, 1, index);
        permutation[0] = temp;
    }
    
    public void pushDown(String uri, String localName) {
        int index = getIndex(uri, localName);
        if (index < 0 || index == permutation.length - 1) {
            return;
        }
        int temp = permutation[index];
        System.arraycopy(permutation, index + 1, permutation, index, permutation.length - 1 - index);
        permutation[permutation.length - 1] = temp;
    }
    
    /**
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
     */
    @Override
    public int getIndex(String uri, String localName) {
        return findIndex(delegate.getIndex(uri, localName));
    }

    /**
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    @Override
    public int getIndex(String qName) {
        return findIndex(delegate.getIndex(qName));
    }

    /**
     * @return
     * @see org.xml.sax.Attributes#getLength()
     */
    @Override
    public int getLength() {
        return permutation.length;
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getLocalName(int)
     */
    @Override
    public String getLocalName(int index) {
        if (index < 0 && index >= permutation.length) {
            return null;
        }
        return delegate.getLocalName(permutation[index]);
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getQName(int)
     */
    @Override
    public String getQName(int index) {
        if (index < 0 && index >= permutation.length) {
            return null;
        }
        return delegate.getQName(permutation[index]);
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getType(int)
     */
    @Override
    public String getType(int index) {
        if (index < 0 && index >= permutation.length) {
            return null;
        }
        return delegate.getType(permutation[index]);
    }

    /**
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
     */
    @Override
    public String getType(String uri, String localName) {
        return delegate.getType(uri, localName);
    }

    /**
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
    public String getType(String qName) {
        return delegate.getType(qName);
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getURI(int)
     */
    @Override
    public String getURI(int index) {
        if (index < 0 && index >= permutation.length) {
            return null;
        }
        return delegate.getURI(permutation[index]);
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
    public String getValue(int index) {
        if (index < 0 && index >= permutation.length) {
            return null;
        }
        return delegate.getValue(permutation[index]);
    }

    /**
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getValue(String uri, String localName) {
        return delegate.getValue(uri, localName);
    }

    /**
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    @Override
    public String getValue(String qName) {
        return delegate.getValue(qName);
    }
    
}
