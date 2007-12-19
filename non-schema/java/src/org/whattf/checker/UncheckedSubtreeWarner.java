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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UncheckedSubtreeWarner extends Checker {

    private boolean alreadyWarnedAboutRdf;

    private boolean alreadyWarnedAboutOpenMath;

    private boolean alreadyWarnedAboutInkscape;
    
    private boolean alreadyWarnedAboutSvgVersion;

    public UncheckedSubtreeWarner() {
        alreadyWarnedAboutRdf = false;
        alreadyWarnedAboutOpenMath = false;
        alreadyWarnedAboutInkscape = false;
        alreadyWarnedAboutSvgVersion = false;
    }

    /**
     * @see org.whattf.checker.Checker#startDocument()
     */
    @Override
    public void startDocument() throws SAXException {
        alreadyWarnedAboutRdf = false;
        alreadyWarnedAboutOpenMath = false;
        alreadyWarnedAboutInkscape = false;
        alreadyWarnedAboutSvgVersion = false;
    }

    /**
     * @see org.whattf.checker.Checker#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (!alreadyWarnedAboutRdf
                && "http://www.w3.org/1999/02/22-rdf-syntax-ns#" == uri) {
            warn("This validator does not validate RDF. RDF subtrees go unchecked.");
            alreadyWarnedAboutRdf = true;
        }
        if (!alreadyWarnedAboutOpenMath
                && "http://www.openmath.org/OpenMath" == uri) {
            warn("This validator does not validate OpenMath. OpenMath subtrees go unchecked.");
            alreadyWarnedAboutOpenMath = true;
        }
        if (!alreadyWarnedAboutInkscape
                && (("http://www.w3.org/2000/svg" == uri && attrsContainInkscape(atts))
                        || "http://www.inkscape.org/namespaces/inkscape" == uri || "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" == uri)) {
            warn("This validator does not validate Inkscape extensions properly. Inkscape-specific errors may go unnoticed.");
            alreadyWarnedAboutInkscape = true;
        }
        if (!alreadyWarnedAboutSvgVersion && "http://www.w3.org/2000/svg" == uri && hasUnsupportedVersion(atts)) {
            warn("Unsupported SVG version specified. This validator only supports SVG 1.1. The recommended way to suppress this warning is to remove the \u201Cversion\u201D attribute altogether.");
            alreadyWarnedAboutSvgVersion = true;
        }
    }

    private boolean hasUnsupportedVersion(Attributes atts) {
        String version = atts.getValue("", "version");
        return "1.0".equals(version) || "1.2".equals(version);
    }

    private boolean attrsContainInkscape(Attributes atts) {
        int length = atts.getLength();
        for (int i = 0; i < length; i++) {
            String uri = atts.getURI(i);
            if ("http://www.inkscape.org/namespaces/inkscape" == uri
                    || "http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd" == uri) {
                return true;
            }
        }
        return false;
    }
}
