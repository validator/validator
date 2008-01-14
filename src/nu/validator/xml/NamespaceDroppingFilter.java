/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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

import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

public final class NamespaceDroppingFilter extends XMLFilterImpl {

    private final static String[] ARRAY_TYPE = new String[0];
  
    private static String[] toInternedArray(Set<String> set) {
        String[] rv = set.toArray(ARRAY_TYPE);
        for (int i = 0; i < rv.length; i++) {
            rv[i] = rv[i].intern();
        }
        return rv;
    }
    
    private final String[] namespacesToRemove;
    
    private int depth;
    
    private boolean alreadyWarned;
    
    private boolean rootSeen;

    private Locator locator = null;
    
    public NamespaceDroppingFilter(XMLReader parent, Set<String> namespacesToRemove) {
        super(parent);
        this.namespacesToRemove = toInternedArray(namespacesToRemove);
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (depth == 0) {
            super.characters(ch, start, length);
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (depth == 0) {
            super.endElement(uri, localName, qName);
        } else {
            depth--;
        }
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        depth = 0;
        alreadyWarned = false;
        rootSeen = false;
        super.startDocument();
    }

    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (depth == 0) {
            if (isInNamespacesToRemove(uri)) {
                if (rootSeen) {
                    depth = 1;                    
                } else {
                    warning(new SAXParseException("Cannot filter out the root element.", locator));
                    super.startElement(uri, localName, qName, filterAttributes(atts));                    
                }
            } else {
                super.startElement(uri, localName, qName, filterAttributes(atts));
            }
        } else {
            if (!alreadyWarned && !isInNamespacesToRemove(uri)) {
                warning(new SAXParseException("Filtering out selected namespaces causes descendants in other namespaces to be dropped as well.", locator));
                alreadyWarned = true;
            }
            depth++;
        }
        rootSeen = true;
    }

    private final Attributes filterAttributes(Attributes atts) {
        int length = atts.getLength();
        int i = 0;
        while (i < length) {
            if (isInNamespacesToRemove(atts.getURI(i))) {
                AttributesImpl rv = new AttributesImpl();
                for (int j = 0; j < i; j++) {
                    rv.addAttribute(atts.getURI(j), atts.getLocalName(j), atts.getQName(j), atts.getType(j), atts.getValue(j));
                }
                i++;
                while (i < length) {
                    String uri = atts.getURI(i);
                    if (!isInNamespacesToRemove(uri)) {
                        rv.addAttribute(uri, atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i));                        
                    }
                    i++;
                }
                return rv;
            }
            i++;
        }
        return atts;
    }

    private final boolean isInNamespacesToRemove(String uri) {
        for (int i = 0; i < namespacesToRemove.length; i++) {
            if (uri == namespacesToRemove[i]) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @see org.xml.sax.helpers.XMLFilterImpl#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        super.setDocumentLocator(locator);
    }
}
