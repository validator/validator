/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2007-2010 Mozilla Foundation
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

    /**
     * The "known" Jena IRI violation codes we catch and handle specifically.
     * Note that this enum intentionally does not hold a complete list of all
     * the violation codes that Jena can return, and "ZZZ_DUMMY_DEFAULT" is not
     * an actual Jena IRI violation code (it's instead added here for our own
     * internal purposes).
     */
    private enum KnownViolationCode {
        COMPATIBILITY_CHARACTER, CONTROL_CHARACTER, DNS_LABEL_DASH_START_OR_END, DOUBLE_WHITESPACE, EMPTY_SCHEME, HAS_PASSWORD, ILLEGAL_CHARACTER, ILLEGAL_PERCENT_ENCODING, IP_V4_HAS_FOUR_COMPONENTS, IP_V4_OCTET_RANGE, IP_V6_OR_FUTURE_ADDRESS_SYNTAX, NON_INITIAL_DOT_SEGMENT, NOT_DNS_NAME, PORT_SHOULD_NOT_BE_WELL_KNOWN, REQUIRED_COMPONENT_MISSING, SCHEME_MUST_START_WITH_LETTER, UNDEFINED_UNICODE_CHARACTER, UNICODE_WHITESPACE, UNREGISTERED_NONIETF_SCHEME_TREE, WHITESPACE, ZZZ_DUMMY_DEFAULT
    }

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
            /*
             * Violation codes that are not "known" codes get assign the
             * dummy value so that handling of them will fall through to
             * the default case.
             */
            KnownViolationCode vc = KnownViolationCode.valueOf("ZZZ_DUMMY_DEFAULT");
            try {
                /*
                 * If this violation code is one of the "known" Jena IRI
                 * violation codes we want to handle specifically, then use it
                 * as-is.
                 */
                vc = KnownViolationCode.valueOf(v.codeName());
            } catch (Exception ex) { }
            switch (vc) {
                case HAS_PASSWORD:
                    if (WARN) {
                        throw newDatatypeException(
                                underbarStringToSentence(v.component())
                                        + " component contains a password.",
                                WARN);
                    } else {
                        return;
                    }
                case NON_INITIAL_DOT_SEGMENT:
                    if (WARN) {
                        throw newDatatypeException(
                                "Path component contains a segment \u201C/../\u201D not at the beginning of a relative reference, or it contains a \u201C/./\u201D. These should be removed.",
                                WARN);
                    } else {
                        return;
                    }
                case PORT_SHOULD_NOT_BE_WELL_KNOWN:
                    if (WARN) {
                        throw newDatatypeException(
                                "Ports under 1024 should be accessed using the appropriate scheme name.",
                                WARN);
                    } else {
                        return;
                    }
                case COMPATIBILITY_CHARACTER:
                    if (WARN) {
                        throw newDatatypeException(
                                underbarStringToSentence(v.codeName()) + " in "
                                        + toAsciiLowerCase(v.component())
                                        + " component.", WARN);
                    } else {
                        return;
                    }
                case DNS_LABEL_DASH_START_OR_END:
                    throw newDatatypeException("Host component contains a DNS name with a \u201C-\u201D (dash) character at the beginning or end.");
                case DOUBLE_WHITESPACE:
                case WHITESPACE:
                    throw newDatatypeException("Whitespace in "
                            + toAsciiLowerCase(v.component()) + " component. "
                            + "Use \u201C%20\u201D in place of spaces.");
                case EMPTY_SCHEME:
                    throw newDatatypeException("Scheme component is empty.");
                case ILLEGAL_PERCENT_ENCODING:
                    throw newDatatypeException(underbarStringToSentence(v.component())
                            + " component contains a percent sign that is not followed by two hexadecimal digits.");
                case IP_V4_HAS_FOUR_COMPONENTS:
                    throw newDatatypeException("Host component is entirely numeric but does not have four components like an IPv4 address.");
                case IP_V4_OCTET_RANGE:
                    throw newDatatypeException("Host component contains a number not in the range 0-255, or a number with a leading zero.");
                case IP_V6_OR_FUTURE_ADDRESS_SYNTAX:
                    throw newDatatypeException("Host component contains an IPv6 (or IPvFuture) syntax violation.");
                case NOT_DNS_NAME:
                    throw newDatatypeException("Host component did not meet the restrictions on DNS names.");
                case REQUIRED_COMPONENT_MISSING:
                    throw newDatatypeException("A component that is required by the scheme is missing.");
                case SCHEME_MUST_START_WITH_LETTER:
                    throw newDatatypeException("Scheme component must start with a letter.");
                case UNREGISTERED_NONIETF_SCHEME_TREE:
                    throw newDatatypeException("Scheme component has a \u201C-\u201D (dash) character, but does not start with \u201Cx-\u201D, and the prefix is not known as the prefix of an alternative tree for URI schemes.");
                case CONTROL_CHARACTER:
                case ILLEGAL_CHARACTER:
                case UNDEFINED_UNICODE_CHARACTER:
                case UNICODE_WHITESPACE:
                    throw newDatatypeException(underbarStringToSentence(v.codeName())
                            + " in "
                            + toAsciiLowerCase(v.component())
                            + " component.");
                default:
                    throw newDatatypeException(v.codeName() + " in "
                            + toAsciiLowerCase(v.component()) + " component.");
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
        if (iri != null) {
            if (data) {
                try {
                    DataUri dataUri = new DataUri(iri);
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

    /**
     * Turn "FOO_BAR_BAZ" into "Foo bar baz".
     */
    protected static final String underbarStringToSentence(String str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        // preserve case of first character
        buf[0] = str.charAt(0);
        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            } else if (c == 0x5f) {
                // convert underbar to space
                c = 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
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
