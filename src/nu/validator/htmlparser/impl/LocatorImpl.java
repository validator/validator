/*
 * Copyright (c) 2007 Henri Sivonen
 * Copyright (c) 2011 Mozilla Foundation
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

package nu.validator.htmlparser.impl;

import org.xml.sax.Locator;
import org.xml.sax.ext.Locator2;

public class LocatorImpl implements Locator, Locator2 {

    private final String systemId;

    private final String publicId;

    private final String encoding;

    private final int column;

    private final int line;

    /**
     * Create a new Locator with default values
     */
    public LocatorImpl() {
        this.systemId = this.publicId = this.encoding = null;
        this.column = this.line = 0;
    }

    public LocatorImpl(Locator locator) {
        this.systemId = locator.getSystemId();
        this.publicId = locator.getPublicId();
        this.column = locator.getColumnNumber();
        this.line = locator.getLineNumber();
        if (locator instanceof Locator2) {
            this.encoding = ((Locator2)locator).getEncoding();
        } else {
            this.encoding = null;
        }
    }

    public final int getColumnNumber() {
        return column;
    }

    public final int getLineNumber() {
        return line;
    }

    public final String getPublicId() {
        return publicId;
    }

    public final String getSystemId() {
        return systemId;
    }

    public final String getXMLVersion() {
        return "1.0";
    }

    public final String getEncoding() {
        return encoding;
    }
}
