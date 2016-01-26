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

package nu.validator.xml;

import org.xml.sax.Attributes;

public final class IdnessChangingAttributesWrapper implements Attributes {

    private Attributes delegate;

    private int xmlIdIndex;
    
    private int idIndex;
    
    private String xmlIdValue;

    /**
     * @param delegate
     * @param xmlIdIndex
     * @param idIndex
     */
    public IdnessChangingAttributesWrapper(Attributes delegate, int xmlIdIndex, int idIndex, String xmlIdValue) {
        this.delegate = delegate;
        this.xmlIdIndex = xmlIdIndex;
        this.idIndex = idIndex;
        this.xmlIdValue = xmlIdValue;
    }
    
    public IdnessChangingAttributesWrapper() {
        this.delegate = null;
        this.xmlIdIndex = -1;
        this.idIndex = -1;
        this.xmlIdValue = null;
    }
    
    public void setFields(Attributes delegate, int xmlIdIndex, int idIndex, String xmlIdValue) {
        this.delegate = delegate;
        this.xmlIdIndex = xmlIdIndex;
        this.idIndex = idIndex;
        this.xmlIdValue = xmlIdValue;
    }
    
    /**
     * @param arg0
     * @param arg1
     * @return
     * @see org.xml.sax.Attributes#getIndex(java.lang.String, java.lang.String)
     */
    @Override
    public int getIndex(String arg0, String arg1) {
        return delegate.getIndex(arg0, arg1);
    }

    /**
     * @param arg0
     * @return
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    @Override
    public int getIndex(String arg0) {
        return delegate.getIndex(arg0);
    }

    /**
     * @return
     * @see org.xml.sax.Attributes#getLength()
     */
    @Override
    public int getLength() {
        return delegate.getLength();
    }

    /**
     * @param arg0
     * @return
     * @see org.xml.sax.Attributes#getLocalName(int)
     */
    @Override
    public String getLocalName(int arg0) {
        return delegate.getLocalName(arg0);
    }

    /**
     * @param arg0
     * @return
     * @see org.xml.sax.Attributes#getQName(int)
     */
    @Override
    public String getQName(int arg0) {
        return delegate.getQName(arg0);
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getType(int)
     */
    @Override
    public String getType(int index) {
        if (idIndex == index || xmlIdIndex == index) {
            return "ID";
        } else {
            return delegate.getType(index);
        }
    }

    /**
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getType(java.lang.String, java.lang.String)
     */
    @Override
    public String getType(String uri, String localName) {
        int index = getIndex(uri, localName);
        if (index >= 0) {
            return getType(index);
        } else {
            return null;
        }
    }

    /**
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getType(java.lang.String)
     */
    @Override
    public String getType(String qName) {
        int index = getIndex(qName);
        if (index >= 0) {
            return getType(index);
        } else {
            return null;
        }
    }

    /**
     * @param arg0
     * @return
     * @see org.xml.sax.Attributes#getURI(int)
     */
    @Override
    public String getURI(int arg0) {
        return delegate.getURI(arg0);
    }

    /**
     * @param index
     * @return
     * @see org.xml.sax.Attributes#getValue(int)
     */
    @Override
    public String getValue(int index) {
        if (xmlIdValue == null) {
            return delegate.getValue(index);            
        } else {
            if (xmlIdIndex == index) {
                return xmlIdValue;
            } else {
                return delegate.getValue(index);
            }
        }
    }

    /**
     * @param uri
     * @param localName
     * @return
     * @see org.xml.sax.Attributes#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getValue(String uri, String localName) {
        if (xmlIdValue == null) {
            return delegate.getValue(uri, localName);            
        } else {
            int index = getIndex(uri, localName);
            if (index < 0) {
                return null;
            } else if (xmlIdIndex == index) {
                return xmlIdValue;
            } else {
                return delegate.getValue(index);
            }
        }
    }

    /**
     * @param qName
     * @return
     * @see org.xml.sax.Attributes#getValue(java.lang.String)
     */
    @Override
    public String getValue(String qName) {
        if (xmlIdValue == null) {
            return delegate.getValue(qName);            
        } else {
            int index = getIndex(qName);
            if (index < 0) {
                return null;
            } else if (xmlIdIndex == index) {
                return xmlIdValue;
            } else {
                return delegate.getValue(index);
            }
        }
    }
    
}
