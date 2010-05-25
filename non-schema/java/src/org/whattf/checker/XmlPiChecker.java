/*
 * Copyright (c) 2010 Mozilla Foundation
 * Portions of comments Copyright 2004-2010 Apple Computer, Inc., Mozilla 
 * Foundation, and Opera Software ASA.
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
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import org.xml.sax.helpers.AttributesImpl;

import org.relaxng.datatype.DatatypeException;

import org.whattf.datatype.Html5DatatypeLibrary;
import org.whattf.datatype.Html5DatatypeException;
import org.whattf.datatype.Charset;
import org.whattf.datatype.IriRef;
import org.whattf.datatype.MediaQuery;
import org.whattf.datatype.MimeType;

public class XmlPiChecker extends Checker implements LexicalHandler {

    private static final char[][] NAMES = { "amp;".toCharArray(),
            "lt;".toCharArray(), "gt;".toCharArray(), "quot;".toCharArray(),
            "apos;".toCharArray(), };

    private static final char[][] VALUES = { { '\u0026' }, { '\u003c' },
            { '\u003e' }, { '\u0022' }, { '\'' }, };

    private static final int DATA_AND_RCDATA_MASK = ~1;

    private static final int BEFORE_ATTRIBUTE_NAME = 0;

    private static final int ATTRIBUTE_NAME = 1;

    private static final int AFTER_ATTRIBUTE_NAME = 2;

    private static final int BEFORE_ATTRIBUTE_VALUE = 3;

    private static final int ATTRIBUTE_VALUE_DOUBLE_QUOTED = 4;

    private static final int ATTRIBUTE_VALUE_SINGLE_QUOTED = 5;

    private static final int ATTRIBUTE_VALUE_UNQUOTED = 6;

    private static final int AFTER_ATTRIBUTE_VALUE_QUOTED = 7;

    private static final int CONSUME_CHARACTER_REFERENCE = 8;

    private static final int CONSUME_NCR = 9;

    private static final int CHARACTER_REFERENCE_LOOP = 10;

    private static final int HEX_NCR_LOOP = 11;

    private static final int DECIMAL_NRC_LOOP = 12;

    private static final int HANDLE_NCR_VALUE = 13;

    private static final int BUFFER_GROW_BY = 1024;

    private static final char[] REPLACEMENT_CHARACTER = { '\uFFFD' };

    private static final int LEAD_OFFSET = (0xD800 - (0x10000 >> 10));

    private char[] strBuf = new char[64];

    private int strBufLen;

    private char[] longStrBuf = new char[1024];

    private int longStrBufLen;

    private final char[] bmpChar = new char[1];

    private final char[] astralChar = new char[2];

    private int entCol;

    private int lo;

    private int hi;

    private int candidate;

    private int strBufMark;

    private int prevValue;

    private int value;

    private boolean seenDigits;

    private char additional;

    private boolean alreadyWarnedAboutPrivateUseCharacters;

    private AttributesImpl attributes;

    private String attributeName;

    private boolean inDoctype;

    private boolean alreadyHasElement;

    private String piTarget = null;

    private boolean hasXsltPi;

    private enum PseudoAttrName {
        HREF, TYPE, TITLE, MEDIA, CHARSET, ALTERNATE, INVALID;
        private static PseudoAttrName toCaps(String str) {
            try {
                if (!str.toLowerCase().equals(str)) {
                    return INVALID;
                }
                return valueOf(newAsciiUpperCaseStringFromString(str));
            } catch (Exception ex) {
                return INVALID;
            }
        }
    }

    public XmlPiChecker() {
        super();
        inDoctype = false;
        hasXsltPi = false;
        alreadyHasElement = false;
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        inDoctype = true;
    }

    public void endDTD() throws SAXException {
        inDoctype = false;
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void comment(char[] ch, int start, int len) throws SAXException {
    }

    @Override public void startDocument() throws SAXException {
        inDoctype = false;
        hasXsltPi = false;
        alreadyHasElement = false;
    }

    @Override public void startElement(String uri, String localName,
            String qName, Attributes atts) throws SAXException {
        alreadyHasElement = true;
    }

    @Override public void processingInstruction(String target, String data)
            throws SAXException {
        piTarget = target;
        if ("xml-stylesheet".equals(piTarget)) {
            checkXmlStylesheetPiData(data);
        }
    }

    private void errBadPseudoAttrDatatype(DatatypeException e,
            Class<?> datatypeClass, String attrName, String attrValue)
            throws SAXException, ClassNotFoundException {
        if (getErrorHandler() != null) {
            Html5DatatypeException ex5 = (Html5DatatypeException) e;
            boolean warning = ex5.isWarning() ? true : false;
            DatatypeMismatchException bpe = new DatatypeMismatchException(
                    "Bad value \u201c" + attrValue + "\u201d for \u201c"
                            + piTarget + "\u201d pseudo-attribute \u201c"
                            + attrName + "\u201d. "
                            + e.getMessage(),
                    getDocumentLocator(), datatypeClass, warning);
            getErrorHandler().error(bpe);
        }
    }

    private void errAttributeWithNoValue() throws SAXException {
        err("Found \u201c" + piTarget + "\u201d pseudo-attribute \u201c"
                + attributeName
                + "\u201d without a value. All pseudo-attributes in \u201c"
                + piTarget + "\u201d instructions must have values.");
    }

    private void errAttributeValueContainsLt() throws SAXException {
        err("Found \u201c"
                + piTarget
                + "\u201d pseudo-attribute \u201c"
                + attributeName
                + "\u201d with the character \u201c<\u201d in its value. All pseudo-attribute values in \u201c"
                + piTarget
                + "\u201d instructions must not contain the character \u201c<\u201d.");
    }

    private void errUpperCaseXinHexNcr() throws SAXException {
        err("In XML documents, a hexadecimal character reference must begin with "
                + "\u201c&#x\u201d (lowercase \u201cx\u201d), not \u201c&#X\u201d (uppercase \u201cX\u201d).");
    }

    private void checkXmlStylesheetPiData(String data) throws SAXException {
        boolean hasHref = false;
        boolean hasTitle = false;
        boolean hasMedia = false;
        boolean hasCharset = false;
        boolean hasAlternate = false;
        boolean hasNonEmptyTitle = false;
        boolean alternateIsYes = false;
        boolean badDatatype = false;
        if (inDoctype) {
            warn("An \u201cxml-stylesheet\u201d instruction should not be used within a \u201cDOCTYPE\u201d declaration.");
        }
        if (alreadyHasElement) {
            err("Any \u201cxml-stylesheet\u201d instruction in a document must occur before any elements in the document. "
                    + "Suppressing any further errors for this \u201cxml-stylesheet\u201d instruction.");
            return;
        }
        if (!"".equals(data)) {
            Html5DatatypeLibrary dl = new Html5DatatypeLibrary();
            AttributesImpl patts = getPseudoAttributesFromPiData(data);
            String attrName;
            String attrValue;
            for (int i = 0; i < patts.getLength(); i++) {
                attrName = patts.getQName(i);
                attrValue = patts.getValue(i);
                switch (PseudoAttrName.toCaps(attrName)) {
                    case HREF:
                        hasHref = true;
                        if (attrValue == null) {
                            break;
                        }
                        try {
                            IriRef ir = (IriRef) dl.createDatatype("iri-ref");
                            ir.checkValid(attrValue);
                        } catch (DatatypeException e) {
                            try {
                                errBadPseudoAttrDatatype(e, IriRef.class,
                                        "href", attrValue);
                            } catch (ClassNotFoundException ce) {
                            }
                        }
                        break;
                    case TYPE:
                        if (attrValue == null) {
                            break;
                        }
                        try {
                            MimeType mt = (MimeType) dl.createDatatype("mime-type");
                            mt.checkValid(attrValue);
                            attrValue = newAsciiLowerCaseStringFromString(attrValue);
                        } catch (DatatypeException e) {
                            badDatatype = true;
                            try {
                                errBadPseudoAttrDatatype(e, MimeType.class,
                                        "type", attrValue);
                            } catch (ClassNotFoundException ce) {
                            }
                        }
                        if (!badDatatype) {
                            if (attrValue.matches("application/xml(;.*)?")
                                    || attrValue.matches("text/xml(;.*)?")
                                    || attrValue.matches("application/xslt+xml(;.*)?")
                                    || attrValue.matches("text/xsl(;.*)?")
                                    || attrValue.matches("text/xslt(;.*)?")) {
                                if (!attrValue.matches("text/xsl(;.*)?")) {
                                    warn("For indicating XSLT, \u201ctext/xsl\u201d is the only MIME type for the "
                                            + "\u201cxml-stylesheet\u201d pseudo-attribute \u201ctype\u201d that is supported across browsers.");
                                }
                                if (hasXsltPi) {
                                    warn("Browsers do not support multiple \u201cxml-stylesheet\u201d instructions with a "
                                            + "\u201ctype\u201d value that indicates XSLT.");
                                }
                                hasXsltPi = true;
                            } else if (!attrValue.matches("^text/css(;.*)?$")) {
                                warn("\u201ctext/css\u201d and \u201ctext/xsl\u201d are the only MIME types for the "
                                        + "\u201cxml-stylesheet\u201d pseudo-attribute \u201ctype\u201d that are supported across browsers.");
                            }
                        }
                        break;
                    case TITLE:
                        hasTitle = true;
                        if (attrValue == null) {
                            break;
                        }
                        if (!"".equals(attrValue)) {
                            hasNonEmptyTitle = true;
                        }
                        break;
                    case MEDIA:
                        hasMedia = true;
                        if (attrValue == null) {
                            break;
                        }
                        try {
                            MediaQuery mq = (MediaQuery) dl.createDatatype("media-query");
                            mq.checkValid(attrValue);
                        } catch (DatatypeException e) {
                            try {
                                errBadPseudoAttrDatatype(e, MediaQuery.class,
                                        "media", attrValue);
                            } catch (ClassNotFoundException ce) {
                            }
                        }
                        break;
                    case CHARSET:
                        hasCharset = true;
                        if (attrValue == null) {
                            break;
                        }
                        try {
                            Charset c = (Charset) dl.createDatatype("charset");
                            c.checkValid(attrValue);
                        } catch (DatatypeException e) {
                            try {
                                errBadPseudoAttrDatatype(e, Charset.class,
                                        "charset", attrValue);
                            } catch (ClassNotFoundException ce) {
                            }
                        }
                        break;
                    case ALTERNATE:
                        hasAlternate = true;
                        if (attrValue == null) {
                            break;
                        }
                        if ("yes".equals(attrValue)) {
                            alternateIsYes = true;
                        } else if (!"no".equals(attrValue)) {
                            err("The value of the \u201cxml-stylesheet\u201d pseudo-attribute \u201calternate\u201d "
                                    + "must be either \u201cyes\u201d or \u201cno\u201d.");
                        }
                        break;
                    default:
                        err("Pseudo-attribute \u201c"
                                + attrName
                                + "\u201D not allowed in \u201cxml-stylesheet\u201d instruction.");
                        break;
                }
            }
            if (alternateIsYes && !hasNonEmptyTitle) {
                err("An \u201cxml-stylesheet\u201d instruction with an \u201calternate\u201d pseudo-attribute "
                        + "whose value is \u201cyes\u201d must also have a \u201ctitle\u201d pseudo-attribute with a non-empty value.");
            }
        }
        if (!hasHref) {
            err("\u201cxml-stylesheet\u201d instruction lacks \u201chref\u201d pseudo-attribute. "
                    + "The \u201chref\u201d pseudo-attribute is required in all \u201cxml-stylesheet\u201d instructions.");
        }
        if (hasXsltPi && (hasTitle || hasMedia || hasCharset || hasAlternate)) {
            warn("When processing \u201cxml-stylesheet\u201d instructions, browsers ignore the pseudo-attributes "
                    + "\u201ctitle\u201d, \u201cmedia\u201d, \u201ccharset\u201d, and \u201calternate\u201d.");
        } else if (hasCharset) {
            warn("Some browsers ignore the value of the \u201cxml-stylesheet\u201d pseudo-attribute \u201ccharset\u201d.");
        }
    }

    /**
     * Collect a set of attribues and values from the data part of a PI.
     * 
     * <p>
     * The bulk of this method and associated methods that follow it here are
     * copied from the nu.validator.htmlparser.impl.Tokenizer class, with
     * appropriate modifications.
     * </p>
     * 
     * @see nu.validator.htmlparser.impl.Tokenizer
     * @see nu.validator.htmlparser.impl.ErrorReportingTokenizer
     * 
     */
    private AttributesImpl getPseudoAttributesFromPiData(String buf)
            throws SAXException {

        int state = BEFORE_ATTRIBUTE_NAME;
        int returnState = BEFORE_ATTRIBUTE_NAME;
        char c = '\u0000';
        int pos = -1;
        int endPos = buf.length();
        boolean reconsume = false;
        attributes = null;
        attributeName = null;
        stateloop: for (;;) {
            switch (state) {
                case BEFORE_ATTRIBUTE_NAME:
                    beforeattributenameloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = buf.charAt(pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\n':
                            case ' ':
                            case '\t':
                                continue;
                            case '/':
                            case '>':
                            case '\"':
                            case '\'':
                            case '<':
                            case '=':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') U+003C LESS-THAN SIGN (<) U+003D EQUALS
                                 * SIGN (=) Parse error.
                                 */
                                errBadCharBeforeAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                /*
                                 * Anything else Start a new attribute in the
                                 * current tag token.
                                 */
                                /*
                                 * Set that attribute's name to the current
                                 * input character,
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                /*
                                 * and its value to the empty string.
                                 */
                                // Will do later.
                                /*
                                 * Switch to the attribute name state.
                                 */
                                state = ATTRIBUTE_NAME;
                                break beforeattributenameloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case ATTRIBUTE_NAME:
                    attributenameloop: for (;;) {
                        if (++pos == endPos) {
                            attributeNameComplete();
                            addAttributeWithoutValue();
                            break stateloop;
                        }
                        c = buf.charAt(pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\n':
                            case ' ':
                            case '\t':
                                attributeNameComplete();
                                state = AFTER_ATTRIBUTE_NAME;
                                continue stateloop;
                            case '=':
                                /*
                                 * U+003D EQUALS SIGN (=) Switch to the before
                                 * attribute value state.
                                 */
                                attributeNameComplete();
                                state = BEFORE_ATTRIBUTE_VALUE;
                                break attributenameloop;
                            // continue stateloop;
                            case '\"':
                            case '\'':
                            case '<':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') U+003C LESS-THAN SIGN (<) Parse error.
                                 */
                                errQuoteOrLtInAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's name.
                                 */
                                appendStrBuf(c);
                                /*
                                 * Stay in the attribute name state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_ATTRIBUTE_VALUE:
                    beforeattributevalueloop: for (;;) {
                        if (++pos == endPos) {
                            addAttributeWithoutValue();
                            break stateloop;
                        }
                        c = buf.charAt(pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\n':
                            case ' ':
                            case '\t':
                                continue;
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Switch to the
                                 * attribute value (double-quoted) state.
                                 */
                                clearLongStrBufForNextState();
                                state = ATTRIBUTE_VALUE_DOUBLE_QUOTED;
                                break beforeattributevalueloop;
                            // continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the attribute
                                 * value (unquoted) state and reconsume this
                                 * input character.
                                 */
                                clearLongStrBuf();
                                state = ATTRIBUTE_VALUE_UNQUOTED;
                                reconsume = true;
                                continue stateloop;
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Switch to the attribute
                                 * value (single-quoted) state.
                                 */
                                clearLongStrBufForNextState();
                                state = ATTRIBUTE_VALUE_SINGLE_QUOTED;
                                continue stateloop;
                            case '<':
                            case '=':
                            case '`':
                                /*
                                 * U+003C LESS-THAN SIGN (<) U+003D EQUALS SIGN
                                 * (=) U+0060 GRAVE ACCENT (`)
                                 */
                                errLtOrEqualsOrGraveInUnquotedAttributeOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                clearLongStrBufAndAppendCurrentC(c);
                                /*
                                 * Switch to the attribute value (unquoted)
                                 * state.
                                 */

                                state = ATTRIBUTE_VALUE_UNQUOTED;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
                    attributevaluedoublequotedloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                addAttributeWithoutValue();
                                break stateloop;
                            }
                            c = buf.charAt(pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Switch to the after
                                 * attribute value (quoted) state.
                                 */
                                addAttributeWithValue();

                                state = AFTER_ATTRIBUTE_VALUE_QUOTED;
                                break attributevaluedoublequotedloop;
                            // continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in attribute value state, with the
                                 * additional allowed character being U+0022
                                 * QUOTATION MARK (").
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                returnState = state;
                                state = CONSUME_CHARACTER_REFERENCE;
                                continue stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the attribute value (double-quoted)
                                 * state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case AFTER_ATTRIBUTE_VALUE_QUOTED:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = buf.charAt(pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\n':
                            case ' ':
                            case '\t':
                                state = BEFORE_ATTRIBUTE_NAME;
                                continue stateloop;
                            default:
                                /*
                                 * Anything else Parse error.
                                 */
                                errNoSpaceBetweenAttributes();
                                /*
                                 * Reconsume the character in the before
                                 * attribute name state.
                                 */
                                state = BEFORE_ATTRIBUTE_NAME;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case ATTRIBUTE_VALUE_UNQUOTED:
                    errUnquotedAttributeValOrNull();
                    for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                addAttributeWithValue();
                                break stateloop;
                            }
                            c = buf.charAt(pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\n':
                            case ' ':
                            case '\t':
                                addAttributeWithValue();
                                state = BEFORE_ATTRIBUTE_NAME;
                                continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in attribute value state, with the
                                 * additional allowed character being U+003E
                                 * GREATER-THAN SIGN (>)
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                returnState = state;
                                state = CONSUME_CHARACTER_REFERENCE;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                // addAttributeWithValue();
                                // state = emitCurrentTagToken(false, pos);
                                // if (shouldSuspend) {
                                // break stateloop;
                                // }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '<':
                            case '\"':
                            case '\'':
                            case '=':
                            case '`':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') U+003C LESS-THAN SIGN (<) U+003D EQUALS
                                 * SIGN (=) U+0060 GRAVE ACCENT (`) Parse error.
                                 */
                                // errUnquotedAttributeValOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                                // fall through
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the attribute value (unquoted) state.
                                 */
                                continue;
                        }
                    }
                    // XXX reorder point
                case AFTER_ATTRIBUTE_NAME:
                    for (;;) {
                        if (++pos == endPos) {
                            addAttributeWithoutValue();
                            break stateloop;
                        }
                        c = buf.charAt(pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\n':
                            case ' ':
                            case '\t':
                                continue;
                            case '=':
                                /*
                                 * U+003D EQUALS SIGN (=) Switch to the before
                                 * attribute value state.
                                 */
                                state = BEFORE_ATTRIBUTE_VALUE;
                                continue stateloop;
                            case '\"':
                            case '\'':
                            case '<':
                                errQuoteOrLtInAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                addAttributeWithoutValue();
                                /*
                                 * Anything else Start a new attribute in the
                                 * current tag token.
                                 */
                                /*
                                 * Set that attribute's name to the current
                                 * input character,
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                /*
                                 * and its value to the empty string.
                                 */
                                // Will do later.
                                /*
                                 * Switch to the attribute name state.
                                 */
                                state = ATTRIBUTE_NAME;
                                continue stateloop;
                        }
                    }
                    // XXX reorder point
                case ATTRIBUTE_VALUE_SINGLE_QUOTED:
                    attributevaluesinglequotedloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                addAttributeWithoutValue();
                                break stateloop;
                            }
                            c = buf.charAt(pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Switch to the after
                                 * attribute value (quoted) state.
                                 */
                                addAttributeWithValue();
                                state = AFTER_ATTRIBUTE_VALUE_QUOTED;
                                continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in attribute value state, with the
                                 * + additional allowed character being U+0027
                                 * APOSTROPHE (').
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                returnState = state;
                                state = CONSUME_CHARACTER_REFERENCE;
                                break attributevaluesinglequotedloop;
                            // continue stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the attribute value (double-quoted)
                                 * state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case CONSUME_CHARACTER_REFERENCE:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = buf.charAt(pos);
                    /*
                     * Unlike the definition is the spec, this state does not
                     * return a value and never requires the caller to
                     * backtrack. This state takes care of emitting characters
                     * or appending to the current attribute value. It also
                     * takes care of that in the case when consuming the
                     * character reference fails.
                     */
                    /*
                     * This section defines how to consume a character
                     * reference. This definition is used when parsing character
                     * references in text and in attributes.
                     * 
                     * The behavior depends on the identity of the next
                     * character (the one immediately after the U+0026 AMPERSAND
                     * character):
                     */
                    switch (c) {
                        case '#':
                            /*
                             * U+0023 NUMBER SIGN (#) Consume the U+0023 NUMBER
                             * SIGN.
                             */
                            appendStrBuf('#');
                            state = CONSUME_NCR;
                            continue stateloop;
                        default:
                            if (c == additional) {
                                emitOrAppendStrBuf(returnState);
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            }
                            entCol = -1;
                            lo = 0;
                            hi = (NAMES.length - 1);
                            candidate = -1;
                            strBufMark = 0;
                            state = CHARACTER_REFERENCE_LOOP;
                            reconsume = true;
                            // FALL THROUGH continue stateloop;
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case CHARACTER_REFERENCE_LOOP:
                    outer: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = buf.charAt(pos);
                        }
                        entCol++;
                        /*
                         * Consume the maximum number of characters possible,
                         * with the consumed characters matching one of the
                         * identifiers in the first column of the named
                         * character references table (in a case-sensitive
                         * manner).
                         */
                        hiloop: for (;;) {
                            if (hi == -1) {
                                break hiloop;
                            }
                            if (entCol == NAMES[hi].length) {
                                break hiloop;
                            }
                            if (entCol > NAMES[hi].length) {
                                break outer;
                            } else if (c < NAMES[hi][entCol]) {
                                hi--;
                            } else {
                                break hiloop;
                            }
                        }

                        loloop: for (;;) {
                            if (hi < lo) {
                                break outer;
                            }
                            if (entCol == NAMES[lo].length) {
                                candidate = lo;
                                strBufMark = strBufLen;
                                lo++;
                            } else if (entCol > NAMES[lo].length) {
                                break outer;
                            } else if (c > NAMES[lo][entCol]) {
                                lo++;
                            } else {
                                break loloop;
                            }
                        }
                        if (hi < lo) {
                            break outer;
                        }
                        appendStrBuf(c);
                        continue;
                    }

                    if (candidate == -1) {
                        /*
                         * If no match can be made, then this is a parse error.
                         */
                        errNoNamedCharacterMatch();
                        emitOrAppendStrBuf(returnState);
                        state = returnState;
                        reconsume = true;
                        continue stateloop;
                    } else {
                        char[] candidateArr = NAMES[candidate];
                        if (candidateArr[candidateArr.length - 1] != ';') {
                            /*
                             * If the last character matched is not a U+003B
                             * SEMICOLON (;), there is a parse error.
                             */
                            if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
                                /*
                                 * If the entity is being consumed as part of an
                                 * attribute, and the last character matched is
                                 * not a U+003B SEMICOLON (;),
                                 */
                                char ch;
                                if (strBufMark == strBufLen) {
                                    ch = c;
                                } else {
                                    // if (strBufOffset != -1) {
                                    // ch = buf[strBufOffset + strBufMark];
                                    // } else {
                                    ch = strBuf[strBufMark];
                                    // }
                                }
                                if ((ch >= '0' && ch <= '9')
                                        || (ch >= 'A' && ch <= 'Z')
                                        || (ch >= 'a' && ch <= 'z')) {
                                    /*
                                     * and the next character is in the range
                                     * U+0030 DIGIT ZERO to U+0039 DIGIT NINE,
                                     * U+0041 LATIN CAPITAL LETTER A to U+005A
                                     * LATIN CAPITAL LETTER Z, or U+0061 LATIN
                                     * SMALL LETTER A to U+007A LATIN SMALL
                                     * LETTER Z, then, for historical reasons,
                                     * all the characters that were matched
                                     * after the U+0026 AMPERSAND (&) must be
                                     * unconsumed, and nothing is returned.
                                     */
                                    errNoNamedCharacterMatch();
                                    appendStrBufToLongStrBuf();
                                    state = returnState;
                                    reconsume = true;
                                    continue stateloop;
                                }
                            }
                            if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
                                errUnescapedAmpersandInterpretedAsCharacterReference();
                            }
                        }

                        /*
                         * Otherwise, return a character token for the character
                         * corresponding to the entity name (as given by the
                         * second column of the named character references
                         * table).
                         */
                        char[] val = VALUES[candidate];
                        emitOrAppend(val, returnState);
                        // this is so complicated!
                        if (strBufMark < strBufLen) {
                            // if (strBufOffset != -1) {
                            // if ((returnState & (~1)) != 0) {
                            // for (int i = strBufMark; i < strBufLen; i++) {
                            // appendLongStrBuf(buf[strBufOffset + i]);
                            // }
                            // } else {
                            // tokenHandler.characters(buf, strBufOffset
                            // + strBufMark, strBufLen
                            // - strBufMark);
                            // }
                            // } else {
                            if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
                                for (int i = strBufMark; i < strBufLen; i++) {
                                    appendLongStrBuf(strBuf[i]);
                                }
                            }
                            // }
                        }
                        state = returnState;
                        reconsume = true;
                        continue stateloop;
                        /*
                         * If the markup contains I'm &notit; I tell you, the
                         * entity is parsed as "not", as in, I'm ¬it; I tell
                         * you. But if the markup was I'm &notin; I tell you,
                         * the entity would be parsed as "notin;", resulting in
                         * I'm ∉ I tell you.
                         */
                    }
                    // XXX reorder point
                case CONSUME_NCR:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = buf.charAt(pos);
                    prevValue = -1;
                    value = 0;
                    seenDigits = false;
                    /*
                     * The behavior further depends on the character after the
                     * U+0023 NUMBER SIGN:
                     */
                    switch (c) {
                        case 'x':
                            /*
                             * U+0078 LATIN SMALL LETTER X U+0058 LATIN CAPITAL
                             * LETTER X Consume the X.
                             * 
                             * Follow the steps below, but using the range of
                             * characters U+0030 DIGIT ZERO through to U+0039
                             * DIGIT NINE, U+0061 LATIN SMALL LETTER A through
                             * to U+0066 LATIN SMALL LETTER F, and U+0041 LATIN
                             * CAPITAL LETTER A, through to U+0046 LATIN CAPITAL
                             * LETTER F (in other words, 0-9, A-F, a-f).
                             * 
                             * When it comes to interpreting the number,
                             * interpret it as a hexadecimal number.
                             */
                            appendStrBuf(c);
                            state = HEX_NCR_LOOP;
                            continue stateloop;
                        case 'X':
                            /*
                             * XML requires a lowercase 'x' for hex character
                             * refs
                             */
                            errUpperCaseXinHexNcr();
                            appendStrBuf(c);
                            state = HEX_NCR_LOOP;
                            continue stateloop;
                        default:
                            /*
                             * Anything else Follow the steps below, but using
                             * the range of characters U+0030 DIGIT ZERO through
                             * to U+0039 DIGIT NINE (i.e. just 0-9).
                             * 
                             * When it comes to interpreting the number,
                             * interpret it as a decimal number.
                             */
                            state = DECIMAL_NRC_LOOP;
                            reconsume = true;
                            // FALL THROUGH continue stateloop;
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case DECIMAL_NRC_LOOP:
                    decimalloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = buf.charAt(pos);
                        }
                        // Deal with overflow gracefully
                        if (value < prevValue) {
                            value = 0x110000; // Value above Unicode range but
                            // within int
                            // range
                        }
                        prevValue = value;
                        /*
                         * Consume as many characters as match the range of
                         * characters given above.
                         */
                        if (c >= '0' && c <= '9') {
                            seenDigits = true;
                            value *= 10;
                            value += c - '0';
                            continue;
                        } else if (c == ';') {
                            if (seenDigits) {
                                state = HANDLE_NCR_VALUE;
                                // FALL THROUGH continue stateloop;
                                break decimalloop;
                            } else {
                                errNoDigitsInNCR();
                                appendStrBuf(';');
                                emitOrAppendStrBuf(returnState);
                                state = returnState;
                                continue stateloop;
                            }
                        } else {
                            /*
                             * If no characters match the range, then don't
                             * consume any characters (and unconsume the U+0023
                             * NUMBER SIGN character and, if appropriate, the X
                             * character). This is a parse error; nothing is
                             * returned.
                             * 
                             * Otherwise, if the next character is a U+003B
                             * SEMICOLON, consume that too. If it isn't, there
                             * is a parse error.
                             */
                            if (!seenDigits) {
                                errNoDigitsInNCR();
                                emitOrAppendStrBuf(returnState);
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            } else {
                                errCharRefLacksSemicolon();
                                state = HANDLE_NCR_VALUE;
                                reconsume = true;
                                // FALL THROUGH continue stateloop;
                                break decimalloop;
                            }
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case HANDLE_NCR_VALUE:
                    // WARNING previous state sets reconsume
                    handleNcrValue(returnState);
                    state = returnState;
                    continue stateloop;
                    // XXX reorder point
                case HEX_NCR_LOOP:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = buf.charAt(pos);
                        // Deal with overflow gracefully
                        if (value < prevValue) {
                            value = 0x110000; // Value above Unicode range but
                            // within int
                            // range
                        }
                        prevValue = value;
                        /*
                         * Consume as many characters as match the range of
                         * characters given above.
                         */
                        if (c >= '0' && c <= '9') {
                            seenDigits = true;
                            value *= 16;
                            value += c - '0';
                            continue;
                        } else if (c >= 'A' && c <= 'F') {
                            seenDigits = true;
                            value *= 16;
                            value += c - 'A' + 10;
                            continue;
                        } else if (c >= 'a' && c <= 'f') {
                            seenDigits = true;
                            value *= 16;
                            value += c - 'a' + 10;
                            continue;
                        } else if (c == ';') {
                            if (seenDigits) {
                                state = HANDLE_NCR_VALUE;
                                continue stateloop;
                            } else {
                                errNoDigitsInNCR();
                                appendStrBuf(';');
                                emitOrAppendStrBuf(returnState);
                                state = returnState;
                                continue stateloop;
                            }
                        } else {
                            /*
                             * If no characters match the range, then don't
                             * consume any characters (and unconsume the U+0023
                             * NUMBER SIGN character and, if appropriate, the X
                             * character). This is a parse error; nothing is
                             * returned.
                             * 
                             * Otherwise, if the next character is a U+003B
                             * SEMICOLON, consume that too. If it isn't, there
                             * is a parse error.
                             */
                            if (!seenDigits) {
                                errNoDigitsInNCR();
                                emitOrAppendStrBuf(returnState);
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            } else {
                                errCharRefLacksSemicolon();
                                state = HANDLE_NCR_VALUE;
                                reconsume = true;
                                continue stateloop;
                            }
                        }
                    }
            }
        }
        return attributes;
    }

    private void appendStrBufToLongStrBuf() {
        appendLongStrBuf(strBuf, 0, strBufLen);
    }

    private void appendLongStrBuf(char[] buffer, int offset, int length) {
        int reqLen = longStrBufLen + length;
        if (longStrBuf.length < reqLen) {
            char[] newBuf = new char[reqLen + (reqLen >> 1)];
            System.arraycopy(longStrBuf, 0, newBuf, 0, longStrBuf.length);
            longStrBuf = newBuf;
        }
        System.arraycopy(buffer, offset, longStrBuf, longStrBufLen, length);
        longStrBufLen = reqLen;
    }

    private void appendLongStrBuf(char[] arr) {
        appendLongStrBuf(arr, 0, arr.length);
    }

    private void appendLongStrBuf(char c) {
        if (longStrBufLen == longStrBuf.length) {
            char[] newBuf = new char[longStrBufLen + (longStrBufLen >> 1)];
            System.arraycopy(longStrBuf, 0, newBuf, 0, longStrBuf.length);
            longStrBuf = newBuf;
        }
        longStrBuf[longStrBufLen++] = c;
    }

    private void appendLongStrBufLineFeed() {
        appendLongStrBuf('\n');
    }

    private void appendStrBuf(char c) {
        if (strBufLen == strBuf.length) {
            char[] newBuf = new char[strBuf.length + BUFFER_GROW_BY];
            System.arraycopy(strBuf, 0, newBuf, 0, strBuf.length);
            strBuf = newBuf;
        }
        strBuf[strBufLen++] = c;
    }

    private void clearLongStrBufForNextState() {
        longStrBufLen = 0;
    }

    private void clearLongStrBuf() {
        longStrBufLen = 0;
    }

    private void clearLongStrBufAndAppendCurrentC(char c) {
        longStrBuf[0] = c;
        longStrBufLen = 1;
        // longStrBufOffset = pos;
    }

    private void clearStrBufAndAppendCurrentC(char c) {
        strBuf[0] = c;
        strBufLen = 1;
    }

    private void emitOrAppend(char[] val, int returnState) throws SAXException {
        if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
            appendLongStrBuf(val);
        }
    }

    private void emitOrAppendOne(char[] val, int returnState)
            throws SAXException {
        if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
            appendLongStrBuf(val[0]);
        }
    }

    private void emitOrAppendTwo(char[] val, int returnState)
            throws SAXException {
        if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
            appendLongStrBuf(val[0]);
            appendLongStrBuf(val[1]);
        }
    }

    private void emitOrAppendStrBuf(int returnState) throws SAXException {
        if ((returnState & DATA_AND_RCDATA_MASK) != 0) {
            appendStrBufToLongStrBuf();
        }
    }

    private String longStrBufToString() {
        return new String(longStrBuf, 0, longStrBufLen);
    }

    private void attributeNameComplete() throws SAXException {
        attributeName = new String(strBuf, 0, strBufLen).intern();

        if (attributes == null) {
            attributes = new AttributesImpl();
        }

        /*
         * When the user agent leaves the attribute name state (and before
         * emitting the tag token, if appropriate), the complete attribute's
         * name must be compared to the other attributes on the same token; if
         * there is already an attribute on the token with the exact same name,
         * then this is a parse error and the new attribute must be dropped,
         * along with the value that gets associated with it (if any).
         */
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getQName(i).equals(attributeName)) {
                errDuplicateAttribute();
                attributeName = null;
                return;
            }
        }
    }

    private void addAttributeWithValue() throws SAXException {
        if (attributeName != null) {
            String value = longStrBufToString();
            if (value.indexOf("<") != -1) {
                errAttributeValueContainsLt();
                return;
            }
            if (badCharInCandidateAttributeName()) {
                return;
            }
            attributes.addAttribute("", "", attributeName, "", value);
            attributeName = null;
        }
    }

    private void addAttributeWithoutValue() throws SAXException {
        if (attributeName != null) {
            if (badCharInCandidateAttributeName()) {
                return;
            }
            attributes.addAttribute("", "", attributeName, "", null);
            errAttributeWithNoValue();
            attributeName = null;
        }
    }

    private boolean badCharInCandidateAttributeName() {
        return attributeName.indexOf("/") != -1
                || attributeName.indexOf(">") != -1
                || attributeName.indexOf("\"") != -1
                || attributeName.indexOf("\'") != -1
                || attributeName.indexOf("<") != -1
                || attributeName.indexOf("=") != -1;
    }

    private void handleNcrValue(int returnState) throws SAXException {
        if (!isLegalXmlCharValue(value)) {
            errNcrIllegalValueForXml();
        } else {
            /*
             * If one or more characters match the range, then take them all and
             * interpret the string of characters as a number (either
             * hexadecimal or decimal as appropriate).
             */
            if (value <= 0xFFFF) {
                /*
                 * about ((value <= 0x0008) || (value == 0x000B) || (value >=
                 * 0x000E && value <= 0x001F)) -- we already check for
                 * XML-illegal control characters in isLegalXmlCharValue
                 */
                if ((value & 0xF800) == 0xD800) {
                    errNcrSurrogate();
                    emitOrAppendOne(REPLACEMENT_CHARACTER, returnState);
                } else {
                    /*
                     * Otherwise, return a character token for the Unicode
                     * character whose code point is that number.
                     */
                    char ch = (char) value;
                    if (value >= 0xFDD0 && value <= 0xFDEF) {
                        errNcrUnassigned();
                    } else if ((value & 0xFFFE) == 0xFFFE) {
                        ch = errNcrNonCharacter(ch);
                    } else if (value >= 0x007F && value <= 0x009F) {
                        errNcrControlChar();
                    } else {
                        maybeWarnPrivateUse(ch);
                    }
                    bmpChar[0] = ch;
                    emitOrAppendOne(bmpChar, returnState);
                }
            } else if (value <= 0x10FFFF) {
                maybeWarnPrivateUseAstral();
                if ((value & 0xFFFE) == 0xFFFE) {
                    errAstralNonCharacter(value);
                }
                astralChar[0] = (char) (LEAD_OFFSET + (value >> 10));
                astralChar[1] = (char) (0xDC00 + (value & 0x3FF));
                emitOrAppendTwo(astralChar, returnState);
            } else {
                errNcrOutOfRange();
                emitOrAppendOne(REPLACEMENT_CHARACTER, returnState);
    }




            if ((value & 0xF800) == 0xD800) {
                errNcrSurrogate();
                emitOrAppendOne(REPLACEMENT_CHARACTER, returnState);
            } else if (value <= 0xFFFF) {
                /*
                 * Otherwise, return a character token for the Unicode character
                 * whose code point is that number.
                 */
                char ch = (char) value;
                /*
                 * if ((value <= 0x0008) || (value == 0x000B) || (value >=
                 * 0x000E && value <= 0x001F)) { // we already check for
                 * XML-illegal control // characters in isLegalXmlCharValue ch =
                 * errNcrControlChar(ch); }
                 */
                if (value >= 0xFDD0 && value <= 0xFDEF) {
                    errNcrUnassigned();
                } else if ((value & 0xFFFE) == 0xFFFE) {
                    ch = errNcrNonCharacter(ch);
                } else if (value >= 0x007F && value <= 0x009F) {
                    errNcrControlChar();
                } else {
                    maybeWarnPrivateUse(ch);
                }
                bmpChar[0] = ch;
                emitOrAppendOne(bmpChar, returnState);
            } else if (value <= 0x10FFFF) {
                maybeWarnPrivateUseAstral();
                astralChar[0] = (char) (LEAD_OFFSET + (value >> 10));
                astralChar[1] = (char) (0xDC00 + (value & 0x3FF));
                emitOrAppend(astralChar, returnState);
            } else {
                errNcrOutOfRange();
                emitOrAppendOne(REPLACEMENT_CHARACTER, returnState);
            }





        }
    }

    private String toUPlusString(char c) {
        String hexString = Integer.toHexString(c);
        switch (hexString.length()) {
            case 1:
                return "U+000" + hexString;
            case 2:
                return "U+00" + hexString;
            case 3:
                return "U+0" + hexString;
            case 4:
                return "U+" + hexString;
            default:
                throw new RuntimeException("Unreachable.");
        }
    }

    private boolean isLegalXmlCharValue(int charval) {
        return charval == 0x0009 || charval == 0x000A || charval == 0x000D
                || (charval >= 0x0020 && charval <= 0xD7FF)
                || (charval >= 0xE000 && charval <= 0xFFFD)
                || (charval >= 0x10000 && charval <= 0x10FFFF);
    }

    private boolean isPrivateUse(char c) {
        return c >= '\uE000' && c <= '\uF8FF';
    }

    private boolean isAstralPrivateUse(int c) {
        return (c >= 0xF0000 && c <= 0xFFFFD)
                || (c >= 0x100000 && c <= 0x10FFFD);
    }

    private void warnAboutPrivateUseChar() throws SAXException {
        if (!alreadyWarnedAboutPrivateUseCharacters) {
            warn("Document uses the Unicode Private Use Area(s), which should not be used in publicly exchanged documents. (Charmod C073)");
            alreadyWarnedAboutPrivateUseCharacters = true;
        }
    }

    private void errBadCharBeforeAttributeNameOrNull(char c)
            throws SAXException {
        if (c == '=') {
            errEqualsSignBeforeAttributeName();
        } else if (c != '\uFFFD') {
            errQuoteBeforeAttributeName(c);
        } else {
            err("The character \u201C" + c + "\u201D is not allowed in \u201C"
                    + piTarget + "\u201D pseudo-attribute names.");
        }
    }

    private void errCharRefLacksSemicolon() throws SAXException {
        err("Character reference was not terminated by a semicolon.");
    }

    private void errDuplicateAttribute() throws SAXException {
        err("Duplicate \u201C" + piTarget + "\u201D pseudo-attribute \u201C"
                + attributeName + "\u201D.");
    }

    private void errEqualsSignBeforeAttributeName() throws SAXException {
        err("Saw \u201C=\u201D when expecting \u201C"
                + piTarget
                + "\u201D pseudo-attribute name. Probable cause: Pseudo-attribute name missing.");
    }

    private void errLtOrEqualsOrGraveInUnquotedAttributeOrNull(char c)
            throws SAXException {
        switch (c) {
            case '=':
                err("\u201C=\u201D at the start of an unquoted \u201C"
                        + piTarget
                        + "\u201D pseudo-attribute value. Probable cause: Stray duplicate equals sign.");
                return;
            case '<':
                /*
                 * we deal with this case in the attribute-value error- checking
                 * code in the getPseudoAttributesFromPiData method
                 */
                // err("\u201C<\u201D at the start of an unquoted attribute value. Probable cause: Missing \u201C>\u201D immediately before.");
                return;
            case '`':
                err("\u201C`\u201D at the start of an unquoted \u201C"
                        + piTarget
                        + "\u201D pseudo-attribute value. Probable cause: Using the wrong character as a quote.");
                return;
        }
    }

    private void errNoSpaceBetweenAttributes() throws SAXException {
        err("Space is required between \u201C" + piTarget
                + "\u201D pseudo-attributes.");
    }

    private void errQuoteBeforeAttributeName(char c) throws SAXException {
        err("Saw \u201C"
                + c
                + "\u201D when expecting a pseudo-attribute name. Probable cause: \u201C=\u201D missing immediately before.");
    }

    private void errQuoteOrLtInAttributeNameOrNull(char c) throws SAXException {
        if (c == '<') {
            // err("\u201C<\u201D in attribute name. Probable cause: \u201C>\u201D missing immediately before.");
        } else if (c != '\uFFFD') {
            err("Quote \u201C"
                    + c
                    + "\u201D in pseudo-attribute name. Probable cause: Matching quote missing somewhere earlier.");
        }
    }

    private void errUnquotedAttributeValOrNull() throws SAXException {
        err("Found unquoted value for \u201c" + piTarget
                + "\u201d pseudo-attribute \u201c" + attributeName
                + "\u201d. The value of all pseudo-attributes in \u201c"
                + piTarget + "\u201d instructions must be quoted.");
    }

    private void errNoNamedCharacterMatch() throws SAXException {
        if (getErrorHandler() == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(
                "\u201C&\u201D did not start a character reference. (\u201C&\u201D probably should have been escaped as \u201C&amp;\u201D.)",
                getDocumentLocator());
        getErrorHandler().error(spe);
    }

    private void errNcrControlChar() throws SAXException {
        /*
         * warn instead of error because these control characters are legal in
         * XML
         */
        warn("Character reference expands to a control character ("
                + toUPlusString((char) value) + ").");
    }

    private void errNcrIllegalValueForXml() throws SAXException {
        err("Character reference expands to a character that is not legal in XML ("
                + toUPlusString((char) value) + ").");
    }

    private void errNcrSurrogate() throws SAXException {
        err("Character reference expands to a surrogate.");
    }

    private void errNcrUnassigned() throws SAXException {
        err("Character reference expands to a permanently unassigned code point.");
    }

    private char errNcrNonCharacter(char ch) throws SAXException {
        err("Character reference expands to a non-character ("
                + toUPlusString((char) value) + ").");
        return ch;
    }

    private void errNcrOutOfRange() throws SAXException {
        err("Character reference outside the permissible Unicode range.");
    }

    private void errNoDigitsInNCR() throws SAXException {
        err("No digits after \u201C" + new String(strBuf, 0, strBufLen)
                + "\u201D.");
    }

    private void errUnescapedAmpersandInterpretedAsCharacterReference()
            throws SAXException {
        if (getErrorHandler() == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(
                "The string following \u201C&\u201D was interpreted as a character reference. (\u201C&\u201D probably should have been escaped as \u201C&amp;\u201D.)",
                getDocumentLocator());
        getErrorHandler().error(spe);
    }

    private void maybeWarnPrivateUse(char ch) throws SAXException {
        if (getErrorHandler() != null && isPrivateUse(ch)) {
            warnAboutPrivateUseChar();
        }
    }

    private void maybeWarnPrivateUseAstral() throws SAXException {
        if (getErrorHandler() != null && isAstralPrivateUse(value)) {
            warnAboutPrivateUseChar();
        }
    }

    private  void errAstralNonCharacter(int ch) throws SAXException {
        err("Character reference expands to an astral non-character ("
                + toUPlusString((char) value) + ").");
    }

    private static String newAsciiLowerCaseStringFromString(String str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

    private static String newAsciiUpperCaseStringFromString(String str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                c -= 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

}
