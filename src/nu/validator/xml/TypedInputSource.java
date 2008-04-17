/*
 * Copyright (c) 2005 Henri Sivonen
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

import java.io.InputStream;
import java.io.Reader;

import org.xml.sax.InputSource;

/**
 * @version $Id$
 * @author hsivonen
 */
public class TypedInputSource extends InputSource {

    private String type;
    
    private String language = "";

    private int length = -1;

    /**
     * 
     */
    public TypedInputSource() {
        super();
    }

    /**
     * @param arg0
     */
    public TypedInputSource(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public TypedInputSource(InputStream arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     */
    public TypedInputSource(Reader arg0) {
        super(arg0);
    }

    
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Returns the length.
     * 
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the length.
     * 
     * @param length the length to set
     */
    public void setLength(int length) {
        if (length < -1) {
            throw new IllegalArgumentException("Length must be -1 or greater.");
        }
        this.length = length;
    }

    /**
     * Returns the language.
     * 
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     * 
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }
}
