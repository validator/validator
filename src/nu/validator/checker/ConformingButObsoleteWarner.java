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

package nu.validator.checker;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ConformingButObsoleteWarner extends Checker {

    /**
     * @see nu.validator.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String name,
            Attributes atts) throws SAXException {
        if ("http://www.w3.org/1999/xhtml" == uri) {
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attributeName = atts.getLocalName(i);
                if ("aria-dropeffect".equals(attributeName)
                        || "aria-grabbed".equals(attributeName)) {
                    warn("The \u201C" + attributeName + "\u201D attribute is"
                            + " deprecated and should not be used. Support for"
                            + " it is poor and is unlikely to improve.");
                        }
            }
            if ("img" == localName) {
                if (atts.getIndex("", "border") > -1) {
                    warn("The \u201Cborder\u201D attribute is obsolete."
                        + " Consider specifying \u201Cimg { border: 0; }\u201D"
                        + " in CSS instead.");
                }
            } else if ("script" == localName) {
                if (AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                        "javascript", atts.getValue("", "language"))) {
                    String type = atts.getValue("", "type");
                    if (type == null
                            || AttributeUtil.lowerCaseLiteralEqualsIgnoreAsciiCaseString(
                                    "text/javascript", type)) {
                        warn("The \u201Clanguage\u201D attribute on the"
                            + " \u201Cscript\u201D element is obsolete."
                            + " You can safely omit it.");
                    }
                }
            } else if ("a" == localName) {
                if (atts.getIndex("", "name") > -1) {
                    warn("The \u201Cname\u201D attribute is obsolete."
                        + " Consider putting an \u201Cid\u201D attribute"
                        + " on the nearest container instead.");
                }
            }
        }
    }
}
