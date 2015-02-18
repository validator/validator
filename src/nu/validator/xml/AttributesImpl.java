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
public class AttributesImpl extends org.xml.sax.helpers.AttributesImpl {

    /**
     * 
     */
    public AttributesImpl() {
        super();
    }

    /**
     * @param atts
     */
    public AttributesImpl(Attributes atts) {
        super(atts);
    }

    /**
     * Adds an attribute that is not in a namespace. The infoset type 
     * of the attribute will be ID if the <code>localName</code> is 
     * "id" and CDATA otherwise.
     * 
     * @param localName the local name of the attribute
     * @param value the value of the attribute
     */
    public void addAttribute(String localName, String value) {
        if ("id".equals(localName)) {
            super.addAttribute("", localName, localName, "ID", value);
        } else {
            super.addAttribute("", localName, localName, "CDATA", value);
        }
    }
}
