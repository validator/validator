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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.relaxng.datatype.DatatypeException;
import org.whattf.io.Utf8PercentDecodingReader;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIException;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;

public class IriRef extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final IriRef THE_INSTANCE = new IriRef();

    private static final Pattern JAVASCRIPT = Pattern.compile("^[jJ][aA][vV][aA][sS][cC][rR][iI][pP][tT]:.*$");
    
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
        //fac.setQueryCharacterRestrictions(false);
        IRI iri;
        try {
            Matcher m = JAVASCRIPT.matcher(literal);
            if (m.matches()) {
                StringBuilder sb = new StringBuilder(2 + literal.length());
                sb.append("x-").append(literal);
                String xStr = sb.toString();
                iri = fac.construct(xStr);
                Reader reader = new BufferedReader(
                        new Utf8PercentDecodingReader(new StringReader(
                                xStr.substring(13))));
                reader.mark(1);
                int c = reader.read();
                if (c != 0xFEFF) {
                    reader.reset();
                }
                try {
                    Context context = Context.enter();
                    context.setOptimizationLevel(0);
                    context.setLanguageVersion(Context.VERSION_1_6);
                    context.compileReader(reader, null, 1, null);
                } finally {
                    Context.exit();
                }
            } else {
                iri = fac.construct(literal.toString());
            }
        } catch (IRIException e) {
            Violation v = e.getViolation();
            throw newDatatypeException(v.codeName() + " in " + v.component() + ".");
        } catch (IOException e) {
            throw newDatatypeException(e.getMessage());            
        } catch (RhinoException e) {
            throw newDatatypeException(e.getMessage());
        }
        if (isAbsolute()) {
            if (!iri.isAbsolute()) {
                throw newDatatypeException("Not an absolute IRI.");
            }
        }
        try {
            iri.toASCIIString();
        } catch (MalformedURLException e) {
            throw newDatatypeException(e.getMessage());
        }
    }

    protected boolean isAbsolute() {
        return false;
    }

    @Override
    public String getName() {
        return "IRI reference";
    }
}
