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

package org.whattf.checker;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class UsemapChecker extends Checker {

    private final Map<String, Locator> usemapLocationsByName = new LinkedHashMap<String, Locator>();
    
    private final Set<String> mapNames = new HashSet<String>();
    
    private Locator locator = null;
    
    public UsemapChecker() {
    }

     /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("map" == localName) {
                String name = atts.getValue("", "name");
                if (name != null && !"".equals(name)) {
                    mapNames.add(name);
                }
            } else if ("img" == localName || "object" == localName) {
                String usemap = atts.getValue("", "usemap");
                if (usemap != null) {
                    int hashIndex = usemap.indexOf('#');
                    if (hashIndex > -1) {
                        // XXX not complaining about bad values here as 
                        // the schema takes care of that.
                        if (hashIndex < usemap.length() - 1) {
                            String ref = usemap.substring(hashIndex + 1);
                            usemapLocationsByName.put(ref, new LocatorImpl(locator));
                        }
                    }
                }
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endDocument()
     */
    @Override
    public void endDocument() throws SAXException {
        for (Map.Entry<String, Locator> entry : usemapLocationsByName.entrySet()) {
            if (!mapNames.contains(entry.getKey())) {
                err("The hash-name reference in attribute \u201Cusemap\u201D referred to \u201C" + entry.getKey() + "\u201D, but there is no \u201Cmap\u201D element with a \u201Cname\u201D attribute with that value.", entry.getValue());
            }
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        usemapLocationsByName.clear();
        mapNames.clear();
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * @see org.whattf.checker.Checker#reset()
     */
    @Override public void reset() {
        usemapLocationsByName.clear();
        mapNames.clear();
    }

    
}
