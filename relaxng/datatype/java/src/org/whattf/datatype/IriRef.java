/*
 * Copyright (c) 2006 Henri Sivonen
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

package org.whattf.datatype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.relaxng.datatype.DatatypeException;
import org.whattf.io.DataUri;
import org.whattf.io.DataUriException;
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

    protected IriRef() {
        super();
    }

    private final static boolean WARN = System.getProperty("org.whattf.datatype.warn","").equals("true") ? true : false;

    private final CharSequencePair splitScheme(CharSequence iri) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iri.length(); i++) {
            char c = toAsciiLowerCase(iri.charAt(i));
            if (i == 0) {
                if ('a' <= c && 'z' >= c) {
                    sb.append(c);
                } else {
                    return null;
                }
            } else {
                if (('a' <= c && 'z' >= c) || ('0' <= c && '9' >= c)
                        || c == '+' || c == '.') {
                    sb.append(c);
                    continue;
                } else if (c == ':') {
                    return new CharSequencePair(sb, iri.subSequence(i + 1,
                            iri.length()));
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    public void checkValid(CharSequence literal) throws DatatypeException {
        // TODO Find out if it is safe to put this in a field
        IRIFactory fac = new IRIFactory();
        fac.shouldViolation(true, false);
        fac.securityViolation(true, false);
        fac.dnsViolation(true, false);
        fac.mintingViolation(false, false);
        fac.useSpecificationIRI(true);
        fac.useSchemeSpecificRules("http", true);
        fac.useSchemeSpecificRules("https", true);
        fac.useSchemeSpecificRules("ftp", true);
        fac.useSchemeSpecificRules("mailto", true); // XXX broken
        fac.useSchemeSpecificRules("file", true);
        fac.useSchemeSpecificRules("data", true); // XXX broken
        // XXX javascript?
        // fac.setQueryCharacterRestrictions(false);
        IRI iri;
        boolean data = false;
        try {
            CharSequencePair pair = splitScheme(literal);
            if (pair == null) {
                // no scheme or scheme is private
                iri = fac.construct(literal.toString());
            } else {
                CharSequence scheme = pair.getHead();
                CharSequence tail = pair.getTail();
                if (isWellKnown(scheme)) {
                    iri = fac.construct(literal.toString());
                } else if ("javascript".contentEquals(scheme)) {
                    // StringBuilder sb = new StringBuilder(2 +
                    // literal.length());
                    // sb.append("x-").append(literal);
                    // iri = fac.construct(sb.toString());
                    iri = null; // Don't bother user with generic IRI syntax
                    Reader reader = new BufferedReader(
                            new Utf8PercentDecodingReader(new StringReader(
                                    tail.toString()))); // XXX
                                                        // CharSequenceReader
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
                } else if ("data".contentEquals(scheme)) {
                    data = true;
                    iri = fac.construct(literal.toString());
                } else if (isHttpAlias(scheme)) {
                    StringBuilder sb = new StringBuilder(5 + tail.length());
                    sb.append("http:").append(tail);
                    iri = fac.construct(sb.toString());
                } else {
                    StringBuilder sb = new StringBuilder(2 + literal.length());
                    sb.append("x-").append(literal);
                    iri = fac.construct(sb.toString());
                }
            }
        } catch (IRIException e) {
            Violation v = e.getViolation();
            if (v.codeName() == "COMPATIBILITY_CHARACTER") {
              if (WARN) {
                throw newDatatypeException(v.codeName() + " in " + v.component() + ".", WARN);
              } else {
                return;
              }
            } else {
              throw newDatatypeException(v.codeName() + " in " + v.component() + ".");
            }
        } catch (IOException e) {
            throw newDatatypeException(e.getMessage());
        } catch (RhinoException e) {
            throw newDatatypeException(e.getMessage());
        }
        if (isAbsolute()) {
            if (iri != null && !iri.isAbsolute()) {
                throw newDatatypeException("Not an absolute IRI.");
            }
        }
        try {
            if (iri != null) {
                String ascii = iri.toASCIIString();
                if (data) {
                    try {
                        DataUri dataUri = new DataUri(ascii);
                        InputStream is = dataUri.getInputStream();
                        while (is.read() >= 0) {
                            // spin
                        }
                    } catch (DataUriException e) {
                        throw newDatatypeException(e.getIndex(), e.getHead(), e.getLiteral(), e.getTail());
                    } catch (IOException e) {
                        throw newDatatypeException(e.getMessage());
                    }                    
                }
            }
        } catch (MalformedURLException e) {
            throw newDatatypeException(e.getMessage());
        }
    }

    private final boolean isHttpAlias(CharSequence scheme) {
        return "feed".contentEquals(scheme) || "webcal".contentEquals(scheme);
    }

    private final boolean isWellKnown(CharSequence scheme) {
        return "http".contentEquals(scheme) || "https".contentEquals(scheme)
                || "ftp".contentEquals(scheme)
                || "mailto".contentEquals(scheme)
                || "file".contentEquals(scheme);
    }

    protected boolean isAbsolute() {
        return false;
    }

    @Override
    public String getName() {
        return "IRI reference";
    }

    private class CharSequencePair {
        private final CharSequence head;

        private final CharSequence tail;

        /**
         * @param head
         * @param tail
         */
        public CharSequencePair(final CharSequence head, final CharSequence tail) {
            this.head = head;
            this.tail = tail;
        }

        /**
         * Returns the head.
         * 
         * @return the head
         */
        public CharSequence getHead() {
            return head;
        }

        /**
         * Returns the tail.
         * 
         * @return the tail
         */
        public CharSequence getTail() {
            return tail;
        }
    }
}
