/*
 * Copyright (c) 2012 Mozilla Foundation
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RdfaLiteChecker extends Checker {

    private String guidance = " Consider checking against the HTML5 + RDFa 1.1 schema instead.";

    private void warnNonRDFaLite(String localName, String att)
            throws SAXException {
        warn("RDFa Core attribute \u201C" + att
                + "\u201D is only allowed on the \u201C" + localName
                + "\u201D element in HTML5 + RDFa 1.1 documents,"
                + " not in HTML5 + RDFa 1.1 Lite documents." + guidance);
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override public void startElement(String uri, String localName,
            String qName, Attributes atts) throws SAXException {

        if ("http://www.w3.org/1999/xhtml" != uri) {
            return;
        }

        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            String att = atts.getLocalName(i);
            if ("datatype" == att || "resource" == att || "inlist" == att) {
                warn("RDFa Core attribute \u201C"
                        + att
                        + "\u201D is only allowed in HTML + RDFa 1.1 documents,"
                        + " not in HTML + RDFa 1.1 Lite documents." + guidance);
            } else if ("content" == att && "meta" != localName) {
                warnNonRDFaLite(localName, att);
            } else if ("href" == att && "a" != localName && "area" != localName
                    && "link" != localName && "base" != localName) {
                warnNonRDFaLite(localName, att);
            } else if (("rel" == att || "rev" == att) && "a" != localName
                    && "area" != localName && "link" != localName) {
                warnNonRDFaLite(localName, att);
            } else if ("src" == att && "audio" != localName
                    && "embed" != localName && "iframe" != localName
                    && "img" != localName && "input" != localName
                    && "script" != localName && "source" != localName
                    && "track" != localName && "video" != localName) {
                warnNonRDFaLite(localName, att);
            }
        }
    }
}
