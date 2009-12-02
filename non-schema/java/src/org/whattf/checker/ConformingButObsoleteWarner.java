/*
 * Copyright (c) 2009 Mozilla Foundation
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

public class ConformingButObsoleteWarner extends Checker {

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override public void startElement(String uri, String localName,
            String name, Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == uri) {
            if ("meta" == localName) {
                if (lowerCaseLiteralEqualsIgnoreAsciiCaseString("content-language", atts.getValue("", "http-equiv"))) {
                    warn("The \u201CContent-Language\u201D state is obsolete. Consider specifying the language on the root element instead.");
                }
            } else if ("img" == localName) {
                if (atts.getIndex("", "border") > -1) {
                    warn("The \u201Cborder\u201D attribute is obsolete. Consider specifying \u201Cimg { border: 0; }\u201D in CSS instead.");
                }
            } else if ("script" == localName) {
                if (lowerCaseLiteralEqualsIgnoreAsciiCaseString("javascript", atts.getValue("", "language"))) {
                    String type = atts.getValue("", "type");
                    if (type == null || lowerCaseLiteralEqualsIgnoreAsciiCaseString("text/javascript", type)) {
                        warn("The \u201Clanguage\u201D attribute on the \u201Cscript\u201D element is obsolete. You can safely omit it.");
                    }
                }
            } else if ("a" == localName) {
                if (atts.getIndex("", "name") > -1) {
                    warn("The \u201Cname\u201D attribute is obsolete. Consider putting an \u201Cid\u201D attribute on the nearest container instead.");
                }
            } else if ("table" == localName) {
                if (atts.getIndex("", "summary") > -1) {
                    warn("The \u201Csummary\u201D attribute is obsolete. Consider describing the structure of complex tables in \u201C<caption>\u201D or in a paragraph and pointing to the paragraph using the \u201Caria-describedby\u201D attribute.");
                }
            }
        }
    }

    private static boolean lowerCaseLiteralEqualsIgnoreAsciiCaseString(String lowerCaseLiteral,
            String string) {
        if (string == null) {
            return false;
        }
        if (lowerCaseLiteral.length() != string.length()) {
            return false;
        }
        for (int i = 0; i < lowerCaseLiteral.length(); i++) {
            char c0 = lowerCaseLiteral.charAt(i);
            char c1 = string.charAt(i);
            if (c1 >= 'A' && c1 <= 'Z') {
                c1 += 0x20;
            }
            if (c0 != c1) {
                return false;
            }
        }
        return true;
    }
    
}
