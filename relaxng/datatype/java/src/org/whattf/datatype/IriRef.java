/*
 * Copyright (c) 2006 Henri Sivonen
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

package org.whattf.datatype;

import java.net.MalformedURLException;

import org.relaxng.datatype.DatatypeException;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;

public class IriRef extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final IriRef THE_INSTANCE = new IriRef();

    protected IriRef() {
        super();
    }

    public void checkValid(CharSequence literal)
            throws DatatypeException {
        // TODO Find out if it is safe to put this in a field
        IRIFactory fac = new IRIFactory();
        fac.shouldViolation(true, false);
        fac.securityViolation(true, false);
        fac.dnsViolation(true, false);
        fac.mintingViolation(false, false); // XXX do we want these?
        fac.useSpecificationIRI(true);
        fac.useSchemeSpecificRules("http", true);
        fac.useSchemeSpecificRules("https", true);
        fac.useSchemeSpecificRules("ftp", true);
        fac.useSchemeSpecificRules("mailto", true); // XXX broken
        fac.useSchemeSpecificRules("file", true);
        fac.useSchemeSpecificRules("data", true); // XXX broken
        // XXX javascript?
        fac.setQueryCharacterRestrictions(false);
        IRI iri;
        try {
            iri = fac.construct(literal.toString());
        } catch (IRIException e) {
            throw new DatatypeException("Bad IRI: " + e.getMessage());
        }
        if (isAbsolute()) {
            if (!iri.isAbsolute()) {
                throw new DatatypeException("Not an absolute IRI.");
            }
        }
        try {
            iri.toASCIIString();
        } catch (MalformedURLException e) {
            throw new DatatypeException("Bad IRI: " + e.getMessage());
        }
    }

    protected boolean isAbsolute() {
        return false;
    }
}
