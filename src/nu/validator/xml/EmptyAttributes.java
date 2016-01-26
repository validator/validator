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

package nu.validator.xml;

import org.xml.sax.Attributes;

/**
 * @version $Id$
 * @author hsivonen
 */
public final class EmptyAttributes implements Attributes {

    public final static EmptyAttributes EMPTY_ATTRIBUTES = new EmptyAttributes();
    
    private EmptyAttributes() {
        
    }
    
    /**
     * @see org.xml.sax.Attributes#getLength()
     */
    @Override
    public int getLength() {
        return 0;
    }

    /**
     * @see org.xml.sax.Attributes#getURI(int)
     */
    @Override
    public String getURI(int arg0) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getLocalName(int)
     */
    @Override
    public String getLocalName(int arg0) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getQName(int)
     */
    @Override
    public String getQName(int arg0) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getType(int)
     */
    @Override
    public String getType(int arg0) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
    public String getValue(int arg0) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
     */
    @Override
    public int getIndex(String arg0, String arg1) {
        return -1;
    }

    /**
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    @Override
    public int getIndex(String arg0) {
        return -1;
    }

    /**
     * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
     */
    @Override
    public String getType(String arg0, String arg1) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
    public String getType(String arg0) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getValue(String arg0, String arg1) {
        return null;
    }

    /**
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    @Override
    public String getValue(String arg0) {
        return null;
    }

}
