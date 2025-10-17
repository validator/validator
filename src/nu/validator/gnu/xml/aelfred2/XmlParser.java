/* XmlParser.java -- 
 Copyright (C) 1999,2000,2001 Free Software Foundation, Inc.
 Portions Copyright 2006-2007 Henri Sivonen
 Portions Copyright 2007-2008 Mozilla Foundation

 This file is part of GNU JAXP.

 GNU JAXP is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU JAXP is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU JAXP; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 02111-1307 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.

 Partly derived from code which carried the following notice:

 Copyright (c) 1997, 1998 by Microstar Software Ltd.

 AElfred is free for both commercial and non-commercial use and
 redistribution, provided that Microstar's copyright and disclaimer are
 retained intact.  You are free to modify AElfred for your own use and
 to redistribute AElfred with your modifications, provided that the
 modifications are clearly documented.

 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 merchantability or fitness for a particular purpose.  Please use it AT
 YOUR OWN RISK.
 */

package nu.validator.gnu.xml.aelfred2;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import nu.validator.htmlparser.common.CharacterHandler;
import nu.validator.htmlparser.extra.NormalizationChecker;
import nu.validator.htmlparser.impl.NCName;
import nu.validator.htmlparser.io.Encoding;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// Organized imports -- 2005-08-20 hsivonen
// Removed unused imports -- 2015-03-03 rwhogg
// removed unnecessary final -- 2015-03-12 rwhogg

/**
 * Parse XML documents and return parse events through call-backs. Use the
 * <code>SAXDriver</code> class as your entry point, as all internal parser
 * interfaces are subject to change.
 * 
 * @author Written by David Megginson &lt;dmeggins@microstar.com&gt; (version
 *         1.2a with bugfixes)
 * @author Updated by David Brownell &lt;dbrownell@users.sourceforge.net&gt;
 * @author Modified by Henri Sivonen &lt;hsivonen@iki.fi&gt;
 * @see SAXDriver
 */
final class XmlParser {

    // //////////////////////////////////////////////////////////////////////
    // Constants.
    // //////////////////////////////////////////////////////////////////////

    private static final int SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;

    //private static final char[] NEW_LINE_ARR = {'\n'};

    //
    // Constants for element content type.
    //

    /**
     * Constant: an element has not been declared.
     * 
     * @see #getElementContentType
     */
    public final static int CONTENT_UNDECLARED = 0;

    /**
     * Constant: the element has a content model of ANY.
     * 
     * @see #getElementContentType
     */
    public final static int CONTENT_ANY = 1;

    /**
     * Constant: the element has declared content of EMPTY.
     * 
     * @see #getElementContentType
     */
    public final static int CONTENT_EMPTY = 2;

    /**
     * Constant: the element has mixed content.
     * 
     * @see #getElementContentType
     */
    public final static int CONTENT_MIXED = 3;

    /**
     * Constant: the element has element content.
     * 
     * @see #getElementContentType
     */
    public final static int CONTENT_ELEMENTS = 4;

    //
    // Constants for the entity type.
    //

    /**
     * Constant: the entity has not been declared.
     * 
     * @see #getEntityType
     */
    public final static int ENTITY_UNDECLARED = 0;

    /**
     * Constant: the entity is internal.
     * 
     * @see #getEntityType
     */
    public final static int ENTITY_INTERNAL = 1;

    /**
     * Constant: the entity is external, non-parsable data.
     * 
     * @see #getEntityType
     */
    public final static int ENTITY_NDATA = 2;

    /**
     * Constant: the entity is external XML data.
     * 
     * @see #getEntityType
     */
    public final static int ENTITY_TEXT = 3;

    //
    // Attribute type constants are interned literal strings.
    //

    //
    // Constants for attribute default value.
    //

    /**
     * Constant: the attribute is not declared.
     * 
     * @see #getAttributeDefaultValueType
     */
    public final static int ATTRIBUTE_DEFAULT_UNDECLARED = 30;

    /**
     * Constant: the attribute has a literal default value specified.
     * 
     * @see #getAttributeDefaultValueType
     * @see #getAttributeDefaultValue
     */
    public final static int ATTRIBUTE_DEFAULT_SPECIFIED = 31;

    /**
     * Constant: the attribute was declared #IMPLIED.
     * 
     * @see #getAttributeDefaultValueType
     */
    public final static int ATTRIBUTE_DEFAULT_IMPLIED = 32;

    /**
     * Constant: the attribute was declared #REQUIRED.
     * 
     * @see #getAttributeDefaultValueType
     */
    public final static int ATTRIBUTE_DEFAULT_REQUIRED = 33;

    /**
     * Constant: the attribute was declared #FIXED.
     * 
     * @see #getAttributeDefaultValueType
     * @see #getAttributeDefaultValue
     */
    public final static int ATTRIBUTE_DEFAULT_FIXED = 34;

    //
    // Constants for input.
    //
    private final static int INPUT_NONE = 0;

    private final static int INPUT_INTERNAL = 1;

    private final static int INPUT_READER = 5;

    //
    // Flags for reading literals.
    //
    // expand general entity refs (attribute values in dtd and content)
    private final static int LIT_ENTITY_REF = 2;

    // normalize this value (space chars) (attributes, public ids)
    private final static int LIT_NORMALIZE = 4;

    // literal is an attribute value
    private final static int LIT_ATTRIBUTE = 8;

    // don't expand parameter entities
    private final static int LIT_DISABLE_PE = 16;

    // don't expand [or parse] character refs
    private final static int LIT_DISABLE_CREF = 32;

    // don't parse general entity refs
    private final static int LIT_DISABLE_EREF = 64;

    // literal is a public ID value
    private final static int LIT_PUBID = 256;

    // Emit warnings for relative URIs with no base URI.
    static boolean uriWarnings;
    static {
        String key = "gnu.xml.aelfred2.XmlParser.uriWarnings";
        try {
            uriWarnings = "true".equals(System.getProperty(key));
        } catch (SecurityException e) {
            uriWarnings = false;
        }
    }

    //
    // The current XML handler interface.
    //
    private SAXDriver handler;

    //
    // I/O information.
    //
    private Reader reader; // current reader

    private InputStream is; // current input stream

    private int line; // current line number

    private int linePrev; // the line of the previous character -- hsivonen

    // 2007-09-28

    private int column; // current column number

    private int columnPrev; // the column of the previous character -- hsivonen

    // 2007-09-28

    private boolean nextCharOnNewLine; // indicates whether the next character

    // is on the next line -- hsivonen
    // 2007-09-28

    private int sourceType; // type of input source

    private LinkedList<Input> inputStack; // stack of input sources

    private String characterEncoding; // current character encoding

    private int currentByteCount; // bytes read from current source

    //
    // Buffers for decoded but unparsed character input.
    //
    private char[] readBuffer;

    private int readBufferPos;

    private int readBufferLength;

    private int readBufferOverflow; // overflow from last data chunk.

    //
    // Buffer for undecoded raw byte input.
    //
//    private final static int READ_BUFFER_MAX = 16384;
    private final static int READ_BUFFER_MAX = 60;

    //private byte[] rawReadBuffer;

    //
    // Buffer for attribute values, char refs, DTD stuff.
    //
    private static int DATA_BUFFER_INITIAL = 4096;

    private char[] dataBuffer;

    private int dataBufferPos;

    //
    // Buffer for parsed names.
    //
    private static int NAME_BUFFER_INITIAL = 1024;

    private char[] nameBuffer;

    private int nameBufferPos;

    //
    // Save any standalone flag
    //
    private boolean docIsStandalone;

    //
    // Hashtables for DTD information on elements, entities, and notations.
    // Populated until we start ignoring decls (because of skipping a PE)
    //
    private HashMap<String, ElementDecl> elementInfo;

    private HashMap<String, EntityInfo> entityInfo;

    private HashMap<String, String> notationInfo;

    private boolean skippedPE;

    //
    // Element type currently in force.
    //
    private String currentElement;

    private int currentElementContent;

    //
    // Stack of entity names, to detect recursion.
    //
    private LinkedList<String> entityStack;

    //
    // PE expansion is enabled in most chunks of the DTD, not all.
    // When it's enabled, literals are treated differently.
    //
    private boolean inLiteral;

    private boolean expandPE;

    private boolean peIsError;

    //
    // can't report entity expansion inside two constructs:
    // - attribute expansions (internal entities only)
    // - markup declarations (parameter entities only)
    //
    private boolean doReport;

    //
    // Symbol table, for caching interned names.
    //
    // These show up wherever XML names or nmtokens are used: naming elements,
    // attributes, PIs, notations, entities, and enumerated attribute values.
    //
    // NOTE: This hashtable doesn't grow. The default size is intended to be
    // rather large for most documents. Example: one snapshot of the DocBook
    // XML 4.1 DTD used only about 350 such names. As a rule, only pathological
    // documents (ones that don't reuse names) should ever see much collision.
    //
    // Be sure that SYMBOL_TABLE_LENGTH always stays prime, for best hashing.
    // "2039" keeps the hash table size at about two memory pages on typical
    // 32 bit hardware.
    //
    private final static int SYMBOL_TABLE_LENGTH = 2039;

    private Object[][] symbolTable;

    //
    // Hash table of attributes found in current start tag.
    //
    private String[] tagAttributes;

    private int tagAttributePos;

    //
    // Utility flag: are we in CDATA? If so, whitespace isn't ignorable.
    // 
    private boolean inCDATA;

    //
    // Xml version.
    //  
    private static final int XML_10 = 0;

    private static final int XML_11 = 1;

    private int xmlVersion = XML_10;

    //
    // Normalization checking
    //

    private NormalizationChecker normalizationChecker;

    private CharacterHandler characterHandler;

    // ////////////////////////////////////////////////////////////////////
    // Constructors.
    // //////////////////////////////////////////////////////////////////////

    /**
     * Construct a new parser with no associated handler.
     * 
     * @see #setHandler
     * @see #parse
     */
    // package private
    XmlParser() {
    }

    /**
     * Set the handler that will receive parsing events.
     * 
     * @param handler
     *            The handler to receive callback events.
     * @see #parse
     */
    // package private
    void setHandler(SAXDriver handler) {
        this.handler = handler;
    }

    /**
     * Parse an XML document from the character stream, byte stream, or URI that
     * you provide (in that order of preference). Any URI that you supply will
     * become the base URI for resolving relative URI, and may be used to
     * acquire a reader or byte stream.
     * 
     * <p>
     * Only one thread at a time may use this parser; since it is private to
     * this package, post-parse cleanup is done by the caller, which MUST NOT
     * REUSE the parser (just null it).
     * 
     * @param systemId
     *            Absolute URI of the document; should never be null, but may be
     *            so iff a reader <em>or</em> a stream is provided.
     * @param publicId
     *            The public identifier of the document, or null.
     * @param reader
     *            A character stream; must be null if stream isn't.
     * @param stream
     *            A byte input stream; must be null if reader isn't.
     * @param characterEncoding
     *            The suggested encoding, or null if unknown.
     * @exception java.lang.Exception
     *                Basically SAXException or IOException
     */
    // package private
    void doParse(String systemId, String publicId, Reader reader,
            InputStream stream, String encoding) throws Exception {
        if (handler == null) {
            throw new IllegalStateException("no callback handler");
        }

        alreadyWarnedAboutPrivateUseCharacters = false;
        initializeVariables();

        // predeclare the built-in entities here (replacement texts)
        // we don't need to intern(), since we're guaranteed literals
        // are always (globally) interned.
        setInternalEntity("amp", "&#38;");
        setInternalEntity("lt", "&#60;");
        setInternalEntity("gt", "&#62;");
        setInternalEntity("apos", "&#39;");
        setInternalEntity("quot", "&#34;");

        try {
            // pushURL first to ensure locator is correct in startDocument
            // ... it might report an IO or encoding exception.
            handler.startDocument();
            pushURL(false, "[document]",
                    // default baseURI: null
                    new ExternalIdentifiers(publicId, systemId, null), reader,
                    stream, encoding, false);

            parseDocument();
        } catch (EOFException e) {
            // empty input
            fatal("Empty document, with no root element.");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    /* ignore */
                }
            }
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Error reporting.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Report an error.
     * 
     * @param message
     *            The error message.
     * @param textFound
     *            The text that caused the error (or null).
     * @see SAXDriver#error
     * @see #line
     */
    private void fatal(String message, String textFound, String textExpected)
            throws SAXException {
        // smart quotes -- 2005-08-20 hsivonen
        if (textFound != null) {
            message = message + " (found \u201C" + textFound + "\u201D)";
        }
        if (textExpected != null) {
            message = message + " (expected \u201C" + textExpected + "\u201D)";
        }
        handler.fatal(message);

        // "can't happen"
        throw new FatalSAXException(message);
    }

    /**
     * Report a serious error.
     * 
     * @param message
     *            The error message.
     * @param textFound
     *            The text that caused the error (or null).
     */
    private void fatal(String message, char textFound, String textExpected)
            throws SAXException {
        fatal(message, Character.valueOf(textFound).toString(), textExpected);
    }

    /**
     * Report typical case fatal errors.
     */
    private void fatal(String message) throws SAXException {
        handler.fatal(message);
    }

    /**
     * Report non-fatal errors.
     */
    private void err(String message) throws SAXException {
        handler.verror(message);
    }

    // ////////////////////////////////////////////////////////////////////
    // Major syntactic productions.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Parse an XML document.
     * 
     * <pre>
     *  [1] document ::= prolog element Misc*
     * </pre>
     * 
     * <p>
     * This is the top-level parsing function for a single XML document. As a
     * minimum, a well-formed document must have a document element, and a valid
     * document must have a prolog (one with doctype) as well.
     */
    private void parseDocument() throws Exception {
        try { // added by MHK
            boolean sawDTD = parseProlog();
            require('<');
            parseElement(!sawDTD);
        } catch (EOFException ee) { // added by MHK
            fatal("premature end of file", "[EOF]", null);
        }

        try {
            parseMisc(); // skip all white, PIs, and comments
            char c = readCh(); // if this doesn't throw an exception...
            fatal("unexpected characters after document end", c, null);
        } catch (EOFException e) {
            if (characterHandler != null) {
                characterHandler.end();
            }
            if (normalizationChecker != null) {
                normalizationChecker.end();
            }
            return;
        }
    }

    static final char[] startDelimComment = { '<', '!', '-', '-' };

    static final char[] endDelimComment = { '-', '-' };

    /**
     * Skip a comment.
     * 
     * <pre>
     *  [15] Comment ::= '&lt;!--' ((Char - '-') | ('-' (Char - '-')))* &quot;--&gt;&quot;
     * </pre>
     * 
     * <p>
     * (The <code>&lt;!--</code> has already been read.)
     */
    private void parseComment() throws Exception {
        boolean saved = expandPE;

        expandPE = false;
        parseUntil(endDelimComment);
        require('>');
        expandPE = saved;
        handler.comment(dataBuffer, 0, dataBufferPos);
        dataBufferPos = 0;
    }

    static final char[] startDelimPI = { '<', '?' };

    static final char[] endDelimPI = { '?', '>' };

    /**
     * Parse a processing instruction and do a call-back.
     * 
     * <pre>
     *  [16] PI ::= '&lt;?' PITarget
     *     (S (Char* - (Char* '?&gt;' Char*)))?
     *     '?&gt;'
     *  [17] PITarget ::= Name - ( ('X'|'x') ('M'|m') ('L'|l') )
     * </pre>
     * 
     * <p>
     * (The <code>&lt;?</code> has already been read.)
     */
    private void parsePI() throws SAXException, IOException {
        String name;
        boolean saved = expandPE;

        expandPE = false;
        name = readNmtoken(true);
        // NE08
        if (name.indexOf(':') >= 0) {
            fatal("Illegal character(':') in processing instruction name ",
                    name, null);
        }
        if ("xml".equalsIgnoreCase(name)) {
            fatal("Illegal processing instruction target", name, null);
        }
        if (!tryRead(endDelimPI)) {
            requireWhitespace();
            parseUntil(endDelimPI);
        }
        expandPE = saved;
        handler.processingInstruction(name, dataBufferToString());
    }

    static final char[] endDelimCDATA = { ']', ']', '>' };

    private boolean isDirtyCurrentElement;

    private boolean alreadyWarnedAboutPrivateUseCharacters;

    private char prev;

    /**
     * Parse a CDATA section.
     * 
     * <pre>
     *  [18] CDSect ::= CDStart CData CDEnd
     *  [19] CDStart ::= '&lt;![CDATA['
     *  [20] CData ::= (Char* - (Char* ']]&gt;' Char*))
     *  [21] CDEnd ::= ']]&gt;'
     * </pre>
     * 
     * <p>
     * (The '&lt;![CDATA[' has already been read.)
     */
    private void parseCDSect() throws Exception {
        parseUntil(endDelimCDATA);
        dataBufferFlush();
    }

    /**
     * Parse the prolog of an XML document.
     * 
     * <pre>
     *  [22] prolog ::= XMLDecl? Misc* (Doctypedecl Misc*)?
     * </pre>
     * 
     * <p>
     * We do not look for the XML declaration here, because it was handled by
     * pushURL ().
     * 
     * @see pushURL
     * @return true if a DTD was read.
     */
    private boolean parseProlog() throws Exception {
        parseMisc();

        if (tryRead("<!DOCTYPE")) {
            parseDoctypedecl();
            parseMisc();
            return true;
        }
        return false;
    }

    private void checkLegalVersion(String version) throws SAXException {
        int len = version.length();
        for (int i = 0; i < len; i++) {
            char c = version.charAt(i);
            if (('0' <= c) && (c <= '9')) {
                continue;
            }
            if ((c == '_') || (c == '.') || (c == ':') || (c == '-')) {
                continue;
            }
            if (('a' <= c) && (c <= 'z')) {
                continue;
            }
            if (('A' <= c) && (c <= 'Z')) {
                continue;
            }
            fatal("illegal character in version", version, "1.0");
        }
    }

    /**
     * Parse the XML declaration.
     * 
     * <pre>
     *  [23] XMLDecl ::= '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?&gt;'
     *  [24] VersionInfo ::= S 'version' Eq
     *     (&quot;'&quot; VersionNum &quot;'&quot; | '&quot;' VersionNum '&quot;' )
     *  [26] VersionNum ::= ([a-zA-Z0-9_.:] | '-')*
     *  [32] SDDecl ::= S 'standalone' Eq
     *     ( &quot;'&quot;&quot; ('yes' | 'no') &quot;'&quot;&quot; | '&quot;' (&quot;yes&quot; | &quot;no&quot;) '&quot;' )
     *  [80] EncodingDecl ::= S 'encoding' Eq
     *     ( &quot;'&quot; EncName &quot;'&quot; | &quot;'&quot; EncName &quot;'&quot; )
     *  [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
     * </pre>
     * 
     * <p>
     * (The <code>&lt;?xml</code> and whitespace have already been read.)
     * 
     * @return the encoding in the declaration, uppercased; or null
     * @see #parseTextDecl
     * @see #setupDecoding
     */
    private String parseXMLDecl(String encoding) throws SAXException,
            IOException {
        String version;
        String encodingName = null;
        String standalone = null;
        int flags = LIT_DISABLE_CREF | LIT_DISABLE_PE | LIT_DISABLE_EREF;

        // Read the version.
        require("version");
        parseEq();
        checkLegalVersion(version = readLiteral(flags));
        if (!version.equals("1.0")) {
            if (version.equals("1.1")) {
                fatal("XML 1.1 not supported."); // 2006-04-24 hsivonen
            } else {
                fatal("illegal XML version", version, "1.0"); // removed 1.1
                // -- 2006-04-24
                // hsivonen
            }
        } else {
            xmlVersion = XML_10;
        }
        // Try reading an encoding declaration.
        boolean white = tryWhitespace();

        if (tryRead("encoding")) {
            if (!white) {
                fatal("whitespace required before 'encoding='");
            }
            parseEq();
            encodingName = readLiteral(flags);
            checkEncodingLiteral(encodingName); // 2006-04-28 hsivonen
            if (reader == null) {
                draconianInputStreamReader(encodingName, is, true);
            } else {
                checkEncodingMatch(encoding, encodingName);
            }
        }

        // Try reading a standalone declaration
        if (encodingName != null) {
            white = tryWhitespace();
        } else {
            if (encoding == null) {
                draconianInputStreamReader("UTF-8", is, false); // 2006-04-24
                // hsivonen
            }
            warnAboutLackOfEncodingDecl(encoding);
        }
        if (tryRead("standalone")) {
            if (!white) {
                fatal("whitespace required before 'standalone='");
            }
            parseEq();
            standalone = readLiteral(flags);
            if ("yes".equals(standalone)) {
                docIsStandalone = true;
            } else if (!"no".equals(standalone)) {
                fatal("standalone flag must be 'yes' or 'no'");
            }
        }

        skipWhitespace();
        require("?>");

        return encodingName;
    }

    // hsivonen 2006-04-28
    private void checkEncodingLiteral(String encodingName) throws SAXException {
        if (encodingName == null) {
            return;
        }
        if (encodingName.length() == 0) {
            fatal("The empty string does not a legal encoding name.");
        }
        char c = encodingName.charAt(0);
        if (!(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')))) {
            fatal("The encoding name must start with an ASCII letter.");
        }
        for (int i = 1; i < encodingName.length(); i++) {
            c = encodingName.charAt(i);
            if (!(((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))
                    || ((c >= '0') && (c <= '9')) || (c == '.') || (c == '_') || (c == '-'))) {
                fatal("Illegal character in encoding name: U+"
                        + Integer.toHexString(c) + ".");
            }
        }
    }

    /**
     * Parse a text declaration.
     * 
     * <pre>
     *  [79] TextDecl ::= '&lt;?xml' VersionInfo? EncodingDecl S? '?&gt;'
     *  [80] EncodingDecl ::= S 'encoding' Eq
     *     ( '&quot;' EncName '&quot;' | &quot;'&quot; EncName &quot;'&quot; )
     *  [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
     * </pre>
     * 
     * <p>
     * (The <code>&lt;?xml</code>' and whitespace have already been read.)
     * 
     * @return the encoding in the declaration, uppercased; or null
     * @see #parseXMLDecl
     * @see #setupDecoding
     */
    private String parseTextDecl(String encoding) throws SAXException,
            IOException {
        String encodingName = null;
        int flags = LIT_DISABLE_CREF | LIT_DISABLE_PE | LIT_DISABLE_EREF;

        // Read an optional version.
        if (tryRead("version")) {
            String version;
            parseEq();
            checkLegalVersion(version = readLiteral(flags));
            if (!version.equals("1.0")) {
                if (version.equals("1.1")) {
                    fatal("XML 1.1 not supported."); // 2006-04-24 hsivonen
                } else {
                    fatal("illegal XML version", version, "1.0"); // removed
                    // 1.1 --
                    // 2006-04-24
                    // hsivonen
                }
            }
            requireWhitespace();
        }

        // Read the encoding.
        require("encoding");
        parseEq();
        encodingName = readLiteral(flags);
        checkEncodingLiteral(encodingName); // 2006-04-28 hsivonen
        if (reader == null) {
            draconianInputStreamReader(encodingName, is, true);
        } else {
            checkEncodingMatch(encoding, encodingName);
        }
        skipWhitespace();
        require("?>");

        return encodingName;
    }

    private void checkEncodingMatch(String used, String detected)
            throws SAXException {
        // method added -- 2006-02-03 hsivonen
        if (used == null) {
            if (!characterEncoding.equalsIgnoreCase(detected)) {
                fatal(
                        "Declared character encoding was not the one sniffed from the BOM.",
                        detected, characterEncoding);
            }
        } else {
            if (!"".equals(used) && !used.equalsIgnoreCase(detected)) {
                handler.warn("External encoding information specified "
                        + used
                        + ", but XML declaration specified "
                        + detected
                        + ". Allowing external to override per RFC 3023. The well-formedness status of this document may change when decoupled from the external character encoding information.");
            }
        }
    }

    private void draconianInputStreamReader(String encoding,
            InputStream stream, boolean requireAsciiSuperset)
            throws SAXException, IOException {
        draconianInputStreamReader(encoding, stream, requireAsciiSuperset,
                encoding);
    }

    private void draconianInputStreamReader(String encoding,
            InputStream stream, boolean requireAsciiSuperset, String actualName)
            throws SAXException, IOException {
        // method added -- 2005-08-21 hsivonen
        // revised -- 2008-03-17 hsivonen
        sourceType = INPUT_READER;
        characterEncoding = Encoding.toAsciiLowerCase(actualName);
        encoding = Encoding.toAsciiLowerCase(encoding);
        try {
            Encoding cs = Encoding.forName(encoding);
            String canonName = cs.getCanonName();
            if (requireAsciiSuperset) {
                if (!cs.isAsciiSuperset()) {
                    fatal("The encoding \u201C"
                            + actualName
                            + "\u201D is not an ASCII superset and, therefore, cannot be used in an internal encoding declaration.");
                }
            }
            if (!cs.isRegistered()) {
                if (encoding.startsWith("x-")) {
                    err("The encoding \u201C"
                            + actualName
                            + "\u201D is not an IANA-registered encoding. (Charmod C022)");                    
                } else {
                    err("The encoding \u201C"
                            + actualName
                            + "\u201D is not an IANA-registered encoding and did not use the \u201Cx-\u201D prefix. (Charmod C023)");
                }
            } else if (!canonName.equals(encoding)) {
                err("The encoding \u201C"
                        + actualName
                        + "\u201D is not the preferred name of the character encoding in use. The preferred name is \u201C"
                        + canonName + "\u201D. (Charmod C024)");
            }
            if (!("utf-8".equals(encoding) || "utf-16".equals(encoding)
                    || "utf-16be".equals(encoding)
                    || "utf-16le".equals(encoding)
                    || "iso-8859-1".equals(encoding) || "us-ascii".equals(encoding))) {
                handler.warn("XML processors are required to support the UTF-8 and UTF-16 character encodings. The encoding was \u201C"
                        + actualName
                        + "\u201D instead, which is an incompatibility risk.");
            }
            Encoding htmlActual = cs.getActualHtmlEncoding();
            if (htmlActual != null) {
                handler.warn("Documents encoded as \u201C"
                        + htmlActual.getCanonName()
                        + "\u201D are often mislabeled as \u201C"
                        + actualName
                        + "\u201D, which is the declared encoding of this document.");
            }
            CharsetDecoder decoder = cs.newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            this.reader = new InputStreamReader(stream, decoder);
        } catch (UnsupportedCharsetException e) {
            fatal("Unsupported character encoding \u201C" + actualName
                    + "\u201D.");
        }
    }

    /**
     * Parse miscellaneous markup outside the document element and DOCTYPE
     * declaration.
     * 
     * <pre>
     *  [27] Misc ::= Comment | PI | S
     * </pre>
     */
    private void parseMisc() throws Exception {
        while (true) {
            skipWhitespace();
            if (tryRead(startDelimPI)) {
                parsePI();
            } else if (tryRead(startDelimComment)) {
                parseComment();
            } else {
                return;
            }
        }
    }

    /**
     * Parse a document type declaration.
     * 
     * <pre>
     *   [28] doctypedecl ::= '&lt;!DOCTYPE' S Name (S ExternalID)? S?
     *      ('[' (markupdecl | PEReference | S)* ']' S?)? '&gt;'
     * </pre>
     * 
     * <p>
     * (The <code>&lt;!DOCTYPE</code> has already been read.)
     */
    private void parseDoctypedecl() throws Exception {
        String rootName;
        ExternalIdentifiers ids;

        // Read the document type name.
        requireWhitespace();
        rootName = readNmtoken(true);

        // Read the External subset's IDs
        skipWhitespace();
        ids = readExternalIds(false, true);

        // report (a) declaration of name, (b) lexical info (ids)
        handler.doctypeDecl(rootName, ids.publicId, ids.systemId);

        // Internal subset is parsed first, if present
        skipWhitespace();
        if (tryRead('[')) {

            // loop until the subset ends
            while (true) {
                doReport = expandPE = true;
                skipWhitespace();
                doReport = expandPE = false;
                if (tryRead(']')) {
                    break; // end of subset
                } else {
                    // WFC, PEs in internal subset (only between decls)
                    peIsError = expandPE = true;
                    parseMarkupdecl();
                    peIsError = expandPE = false;
                }
            }
        }
        skipWhitespace();
        require('>');

        // Read the external subset, if any
        InputSource subset;

        if (ids.systemId == null) {
            subset = handler.getExternalSubset(rootName, handler.getSystemId());
        } else {
            subset = null;
        }
        if ((ids.systemId != null) || (subset != null)) {
            pushString(null, ">");

            // NOTE: [dtd] is so we say what SAX2 expects,
            // though it's misleading (subset, not entire dtd)
            if (ids.systemId != null) {
                pushURL(true, "[dtd]", ids, null, null, null, true);
            } else {
                handler.warn("modifying document by adding external subset");
                pushURL(true, "[dtd]", new ExternalIdentifiers(
                        subset.getPublicId(), subset.getSystemId(), null),
                        subset.getCharacterStream(), subset.getByteStream(),
                        subset.getEncoding(), false);
            }

            // Loop until we end up back at '>'
            while (true) {
                doReport = expandPE = true;
                skipWhitespace();
                doReport = expandPE = false;
                if (tryRead('>')) {
                    break;
                } else {
                    expandPE = true;
                    parseMarkupdecl();
                    expandPE = false;
                }
            }

            // the ">" string isn't popped yet
            if (inputStack.size() != 1) {
                fatal("external subset has unmatched '>'");
            }
        }

        // done dtd
        handler.endDoctype();
        expandPE = false;
        doReport = true;
    }

    /**
     * Parse a markup declaration in the internal or external DTD subset.
     * 
     * <pre>
     *   [29] markupdecl ::= elementdecl | Attlistdecl | EntityDecl
     *      | NotationDecl | PI | Comment
     *   [30] extSubsetDecl ::= (markupdecl | conditionalSect
     *      | PEReference | S) *
     * </pre>
     * 
     * <p>
     * Reading toplevel PE references is handled as a lexical issue by the
     * caller, as is whitespace.
     */
    private void parseMarkupdecl() throws Exception {
        char[] saved = null;
        boolean savedPE = expandPE;

        // prevent "<%foo;" and ensures saved entity is right
        require('<');
        unread('<');
        expandPE = false;

        if (tryRead("<!ELEMENT")) {
            saved = readBuffer;
            expandPE = savedPE;
            parseElementDecl();
        } else if (tryRead("<!ATTLIST")) {
            saved = readBuffer;
            expandPE = savedPE;
            parseAttlistDecl();
        } else if (tryRead("<!ENTITY")) {
            saved = readBuffer;
            expandPE = savedPE;
            parseEntityDecl();
        } else if (tryRead("<!NOTATION")) {
            saved = readBuffer;
            expandPE = savedPE;
            parseNotationDecl();
        } else if (tryRead(startDelimPI)) {
            saved = readBuffer;
            expandPE = savedPE;
            parsePI();
        } else if (tryRead(startDelimComment)) {
            saved = readBuffer;
            expandPE = savedPE;
            parseComment();
        } else if (tryRead("<![")) {
            saved = readBuffer;
            expandPE = savedPE;
            if (inputStack.size() > 0) {
                parseConditionalSect(saved);
            } else {
                fatal("conditional sections illegal in internal subset");
            }
        } else {
            fatal("expected markup declaration");
        }

        // VC: Proper Decl/PE Nesting
        if (readBuffer != saved) {
            handler.verror("Illegal Declaration/PE nesting");
        }
    }

    /**
     * Parse an element, with its tags.
     * 
     * <pre>
     *   [39] element ::= EmptyElementTag | STag content ETag
     *   [40] STag ::= '&lt;' Name (S Attribute)* S? '&gt;'
     *   [44] EmptyElementTag ::= '&lt;' Name (S Attribute)* S? '/&gt;'
     * </pre>
     * 
     * <p>
     * (The '&lt;' has already been read.)
     * <p>
     * NOTE: this method actually chains onto parseContent (), if necessary, and
     * parseContent () will take care of calling parseETag ().
     */
    private void parseElement(boolean maybeGetSubset) throws Exception {
        String gi;
        char c;
        int oldElementContent = currentElementContent;
        String oldElement = currentElement;
        ElementDecl element;

        // This is the (global) counter for the
        // array of specified attributes.
        tagAttributePos = 0;

        // Read the element type name.
        gi = readNmtoken(true);

        // If we saw no DTD, and this is the document root element,
        // let the application modify the input stream by providing one.
        if (maybeGetSubset) {
            InputSource subset = handler.getExternalSubset(gi,
                    handler.getSystemId());
            if (subset != null) {
                String publicId = subset.getPublicId();
                String systemId = subset.getSystemId();

                handler.warn("modifying document by adding DTD");
                handler.doctypeDecl(gi, publicId, systemId);
                pushString(null, ">");

                // NOTE: [dtd] is so we say what SAX2 expects,
                // though it's misleading (subset, not entire dtd)
                pushURL(true, "[dtd]", new ExternalIdentifiers(publicId,
                        systemId, null), subset.getCharacterStream(),
                        subset.getByteStream(), subset.getEncoding(), false);

                // Loop until we end up back at '>'
                while (true) {
                    doReport = expandPE = true;
                    skipWhitespace();
                    doReport = expandPE = false;
                    if (tryRead('>')) {
                        break;
                    } else {
                        expandPE = true;
                        parseMarkupdecl();
                        expandPE = false;
                    }
                }

                // the ">" string isn't popped yet
                if (inputStack.size() != 1) {
                    fatal("external subset has unmatched '>'");
                }

                handler.endDoctype();
            }
        }

        // Determine the current content type.
        currentElement = gi;
        element = elementInfo.get(gi);
        currentElementContent = getContentType(element, CONTENT_ANY);

        // Read the attributes, if any.
        // After this loop, "c" is the closing delimiter.
        boolean white = tryWhitespace();
        c = readCh();
        while ((c != '/') && (c != '>')) {
            unread(c);
            if (!white) {
                fatal("need whitespace between attributes");
            }
            parseAttribute(gi);
            white = tryWhitespace();
            c = readCh();
        }

        // Supply any defaulted attributes.
        Iterator<String> atts = declaredAttributes(element);
        if (atts != null) {
            String aname;
            loop: while (atts.hasNext()) {
                aname = atts.next();
                // See if it was specified.
                for (int i = 0; i < tagAttributePos; i++) {
                    if (tagAttributes[i] == aname) {
                        continue loop;
                    }
                }
                // ... or has a default
                String value = getAttributeDefaultValue(gi, aname);

                if (value == null) {
                    continue;
                }
                handler.attribute(aname, value, false);
            }
        }

        // Figure out if this is a start tag
        // or an empty element, and dispatch an
        // event accordingly.
        switch (c) {
            case '>':
                handler.startElement(gi);
                parseContent();
                break;
            case '/':
                require('>');
                handler.startElement(gi);
                handler.endElement(gi);
                break;
        }

        // Restore the previous state.
        currentElement = oldElement;
        currentElementContent = oldElementContent;
    }

    /**
     * Parse an attribute assignment.
     * 
     * <pre>
     *   [41] Attribute ::= Name Eq AttValue
     * </pre>
     * 
     * @param name
     *            The name of the attribute's element.
     * @see SAXDriver#attribute
     */
    private void parseAttribute(String name) throws Exception {
        String aname;
        String type;
        String value;
        int flags = LIT_ATTRIBUTE | LIT_ENTITY_REF;

        // Read the attribute name.
        aname = readNmtoken(true);
        type = getAttributeType(name, aname);

        // Parse '='
        parseEq();

        // Read the value, normalizing whitespace
        // unless it is CDATA.
        if (handler.stringInterning) {
            if ((type == "CDATA") || (type == null)) {
                value = readLiteral(flags);
            } else {
                value = readLiteral(flags | LIT_NORMALIZE);
            }
        } else {
            if ((type == null) || type.equals("CDATA")) {
                value = readLiteral(flags);
            } else {
                value = readLiteral(flags | LIT_NORMALIZE);
            }
        }

        // WFC: no duplicate attributes
        for (int i = 0; i < tagAttributePos; i++) {
            if (aname.equals(tagAttributes[i])) {
                fatal("duplicate attribute", aname, null);
            }
        }

        // Inform the handler about the
        // attribute.
        handler.attribute(aname, value, true);
        dataBufferPos = 0;

        // Note that the attribute has been
        // specified.
        if (tagAttributePos == tagAttributes.length) {
            String newAttrib[] = new String[tagAttributes.length * 2];
            System.arraycopy(tagAttributes, 0, newAttrib, 0, tagAttributePos);
            tagAttributes = newAttrib;
        }
        tagAttributes[tagAttributePos++] = aname;
    }

    /**
     * Parse an equals sign surrounded by optional whitespace.
     * 
     * <pre>
     *   [25] Eq ::= S? '=' S?
     * </pre>
     */
    private void parseEq() throws SAXException, IOException {
        skipWhitespace();
        require('=');
        skipWhitespace();
    }

    /**
     * Parse an end tag.
     * 
     * <pre>
     *   [42] ETag ::= '&lt;/' Name S? '&gt;'
     * </pre>
     * 
     * <p>
     * NOTE: parseContent () chains to here, we already read the "&lt;/".
     */
    private void parseETag() throws Exception {
        require(currentElement);
        skipWhitespace();
        require('>');
        handler.endElement(currentElement);
        // not re-reporting any SAXException re bogus end tags,
        // even though that diagnostic might be clearer ...
    }

    /**
     * Parse the content of an element.
     * 
     * <pre>
     *   [43] content ::= (element | CharData | Reference
     *      | CDSect | PI | Comment)*
     *   [67] Reference ::= EntityRef | CharRef
     * </pre>
     * 
     * <p>
     * NOTE: consumes ETtag.
     */
    private void parseContent() throws Exception {
        char c;

        while (true) {
            // consume characters (or ignorable whitespace) until delimiter
            parseCharData();

            // Handle delimiters
            c = readCh();
            switch (c) {
                case '&': // Found "&"
                    c = readCh();
                    if (c == '#') {
                        parseCharRef();
                    } else {
                        unread(c);
                        parseEntityRef(true);
                    }
                    isDirtyCurrentElement = true;
                    break;

                case '<': // Found "<"
                    dataBufferFlush();
                    c = readCh();
                    switch (c) {
                        case '!': // Found "<!"
                            c = readCh();
                            switch (c) {
                                case '-': // Found "<!-"
                                    require('-');
                                    isDirtyCurrentElement = false;
                                    parseComment();
                                    break;
                                case '[': // Found "<!["
                                    isDirtyCurrentElement = false;
                                    require("CDATA[");
                                    handler.startCDATA();
                                    inCDATA = true;
                                    parseCDSect();
                                    inCDATA = false;
                                    handler.endCDATA();
                                    break;
                                default:
                                    fatal("expected comment or CDATA section",
                                            c, null);
                                    break;
                            }
                            break;

                        case '?': // Found "<?"
                            isDirtyCurrentElement = false;
                            parsePI();
                            break;

                        case '/': // Found "</"
                            isDirtyCurrentElement = false;
                            parseETag();
                            return;

                        default: // Found "<" followed by something else
                            isDirtyCurrentElement = false;
                            unread(c);
                            parseElement(false);
                            break;
                    }
            }
        }
    }

    /**
     * Parse an element type declaration.
     * 
     * <pre>
     *   [45] elementdecl ::= '&lt;!ELEMENT' S Name S contentspec S? '&gt;'
     * </pre>
     * 
     * <p>
     * NOTE: the '&lt;!ELEMENT' has already been read.
     */
    private void parseElementDecl() throws Exception {
        String name;

        requireWhitespace();
        // Read the element type name.
        name = readNmtoken(true);

        requireWhitespace();
        // Read the content model.
        parseContentspec(name);

        skipWhitespace();
        require('>');
    }

    /**
     * Content specification.
     * 
     * <pre>
     *   [46] contentspec ::= 'EMPTY' | 'ANY' | Mixed | elements
     * </pre>
     */
    private void parseContentspec(String name) throws Exception {
        // FIXME: move elementDecl() into setElement(), pass EMTPY/ANY ...
        if (tryRead("EMPTY")) {
            setElement(name, CONTENT_EMPTY, null, null);
            if (!skippedPE) {
                handler.getDeclHandler().elementDecl(name, "EMPTY");
            }
            return;
        } else if (tryRead("ANY")) {
            setElement(name, CONTENT_ANY, null, null);
            if (!skippedPE) {
                handler.getDeclHandler().elementDecl(name, "ANY");
            }
            return;
        } else {
            String model;
            char[] saved;

            require('(');
            saved = readBuffer;
            dataBufferAppend('(');
            skipWhitespace();
            if (tryRead("#PCDATA")) {
                dataBufferAppend("#PCDATA");
                parseMixed(saved);
                model = dataBufferToString();
                setElement(name, CONTENT_MIXED, model, null);
            } else {
                parseElements(saved);
                model = dataBufferToString();
                setElement(name, CONTENT_ELEMENTS, model, null);
            }
            if (!skippedPE) {
                handler.getDeclHandler().elementDecl(name, model);
            }
        }
    }

    /**
     * Parse an element-content model.
     * 
     * <pre>
     *   [47] elements ::= (choice | seq) ('?' | '*' | '+')?
     *   [49] choice ::= '(' S? cp (S? '|' S? cp)+ S? ')'
     *   [50] seq ::= '(' S? cp (S? ',' S? cp)* S? ')'
     * </pre>
     * 
     * <p>
     * NOTE: the opening '(' and S have already been read.
     * 
     * @param saved
     *            Buffer for entity that should have the terminal ')'
     */
    private void parseElements(char[] saved) throws Exception {
        char c;
        char sep;

        // Parse the first content particle
        skipWhitespace();
        parseCp();

        // Check for end or for a separator.
        skipWhitespace();
        c = readCh();
        switch (c) {
            case ')':
                // VC: Proper Group/PE Nesting
                if (readBuffer != saved) {
                    handler.verror("Illegal Group/PE nesting");
                }

                dataBufferAppend(')');
                c = readCh();
                switch (c) {
                    case '*':
                    case '+':
                    case '?':
                        dataBufferAppend(c);
                        break;
                    default:
                        unread(c);
                }
                return;
            case ',': // Register the separator.
            case '|':
                sep = c;
                dataBufferAppend(c);
                break;
            default:
                fatal("bad separator in content model", c, null);
                return;
        }

        // Parse the rest of the content model.
        while (true) {
            skipWhitespace();
            parseCp();
            skipWhitespace();
            c = readCh();
            if (c == ')') {
                // VC: Proper Group/PE Nesting
                if (readBuffer != saved) {
                    handler.verror("Illegal Group/PE nesting");
                }

                dataBufferAppend(')');
                break;
            } else if (c != sep) {
                fatal("bad separator in content model", c, null);
                return;
            } else {
                dataBufferAppend(c);
            }
        }

        // Check for the occurrence indicator.
        c = readCh();
        switch (c) {
            case '?':
            case '*':
            case '+':
                dataBufferAppend(c);
                return;
            default:
                unread(c);
                return;
        }
    }

    /**
     * Parse a content particle.
     * 
     * <pre>
     *   [48] cp ::= (Name | choice | seq) ('?' | '*' | '+')?
     * </pre>
     */
    private void parseCp() throws Exception {
        if (tryRead('(')) {
            dataBufferAppend('(');
            parseElements(readBuffer);
        } else {
            dataBufferAppend(readNmtoken(true));
            char c = readCh();
            switch (c) {
                case '?':
                case '*':
                case '+':
                    dataBufferAppend(c);
                    break;
                default:
                    unread(c);
                    break;
            }
        }
    }

    /**
     * Parse mixed content.
     * 
     * <pre>
     *   [51] Mixed ::= '(' S? ( '#PCDATA' (S? '|' S? Name)*) S? ')*'
     *          | '(' S? ('#PCDATA') S? ')'
     * </pre>
     * 
     * @param saved
     *            Buffer for entity that should have the terminal ')'
     */
    private void parseMixed(char[] saved) throws Exception {
        // Check for PCDATA alone.
        skipWhitespace();
        if (tryRead(')')) {
            // VC: Proper Group/PE Nesting
            if (readBuffer != saved) {
                handler.verror("Illegal Group/PE nesting");
            }

            dataBufferAppend(")*");
            tryRead('*');
            return;
        }

        // Parse mixed content.
        skipWhitespace();
        while (!tryRead(")")) {
            require('|');
            dataBufferAppend('|');
            skipWhitespace();
            dataBufferAppend(readNmtoken(true));
            skipWhitespace();
        }

        // VC: Proper Group/PE Nesting
        if (readBuffer != saved) {
            handler.verror("Illegal Group/PE nesting");
        }

        require('*');
        dataBufferAppend(")*");
    }

    /**
     * Parse an attribute list declaration.
     * 
     * <pre>
     *   [52] AttlistDecl ::= '&lt;!ATTLIST' S Name AttDef* S? '&gt;'
     * </pre>
     * 
     * <p>
     * NOTE: the '&lt;!ATTLIST' has already been read.
     */
    private void parseAttlistDecl() throws Exception {
        String elementName;

        requireWhitespace();
        elementName = readNmtoken(true);
        boolean white = tryWhitespace();
        while (!tryRead('>')) {
            if (!white) {
                fatal("whitespace required before attribute definition");
            }
            parseAttDef(elementName);
            white = tryWhitespace();
        }
    }

    /**
     * Parse a single attribute definition.
     * 
     * <pre>
     *   [53] AttDef ::= S Name S AttType S DefaultDecl
     * </pre>
     */
    private void parseAttDef(String elementName) throws Exception {
        String name;
        String type;
        String enumer = null;

        // Read the attribute name.
        name = readNmtoken(true);

        // Read the attribute type.
        requireWhitespace();
        type = readAttType();

        // Get the string of enumerated values if necessary.
        if (handler.stringInterning) {
            if (("ENUMERATION" == type) || ("NOTATION" == type)) {
                enumer = dataBufferToString();
            }
        } else {
            if ("ENUMERATION".equals(type) || "NOTATION".equals(type)) {
                enumer = dataBufferToString();
            }
        }

        // Read the default value.
        requireWhitespace();
        parseDefault(elementName, name, type, enumer);
    }

    /**
     * Parse the attribute type.
     * 
     * <pre>
     *   [54] AttType ::= StringType | TokenizedType | EnumeratedType
     *   [55] StringType ::= 'CDATA'
     *   [56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY'
     *      | 'ENTITIES' | 'NMTOKEN' | 'NMTOKENS'
     *   [57] EnumeratedType ::= NotationType | Enumeration
     * </pre>
     */
    private String readAttType() throws Exception {
        if (tryRead('(')) {
            parseEnumeration(false);
            return "ENUMERATION";
        } else {
            String typeString = readNmtoken(true);
            if (handler.stringInterning) {
                if ("NOTATION" == typeString) {
                    parseNotationType();
                    return typeString;
                } else if (("CDATA" == typeString) || ("ID" == typeString)
                        || ("IDREF" == typeString) || ("IDREFS" == typeString)
                        || ("ENTITY" == typeString) || ("ENTITIES" == typeString)
                        || ("NMTOKEN" == typeString) || ("NMTOKENS" == typeString)) {
                    return typeString;
                }
            } else {
                if ("NOTATION".equals(typeString)) {
                    parseNotationType();
                    return typeString;
                } else if ("CDATA".equals(typeString)
                        || "ID".equals(typeString)
                        || "IDREF".equals(typeString)
                        || "IDREFS".equals(typeString)
                        || "ENTITY".equals(typeString)
                        || "ENTITIES".equals(typeString)
                        || "NMTOKEN".equals(typeString)
                        || "NMTOKENS".equals(typeString)) {
                    return typeString;
                }
            }
            fatal("illegal attribute type", typeString, null);
            return null;
        }
    }

    /**
     * Parse an enumeration.
     * 
     * <pre>
     *   [59] Enumeration ::= '(' S? Nmtoken (S? '|' S? Nmtoken)* S? ')'
     * </pre>
     * 
     * <p>
     * NOTE: the '(' has already been read.
     */
    private void parseEnumeration(boolean isNames) throws Exception {
        dataBufferAppend('(');

        // Read the first token.
        skipWhitespace();
        dataBufferAppend(readNmtoken(isNames));
        // Read the remaining tokens.
        skipWhitespace();
        while (!tryRead(')')) {
            require('|');
            dataBufferAppend('|');
            skipWhitespace();
            dataBufferAppend(readNmtoken(isNames));
            skipWhitespace();
        }
        dataBufferAppend(')');
    }

    /**
     * Parse a notation type for an attribute.
     * 
     * <pre>
     *   [58] NotationType ::= 'NOTATION' S '(' S? NameNtoks
     *      (S? '|' S? name)* S? ')'
     * </pre>
     * 
     * <p>
     * NOTE: the 'NOTATION' has already been read
     */
    private void parseNotationType() throws Exception {
        requireWhitespace();
        require('(');

        parseEnumeration(true);
    }

    /**
     * Parse the default value for an attribute.
     * 
     * <pre>
     *   [60] DefaultDecl ::= '#REQUIRED' | '#IMPLIED'
     *      | (('#FIXED' S)? AttValue)
     * </pre>
     */
    private void parseDefault(String elementName, String name, String type,
            String enumer) throws Exception {
        int valueType = ATTRIBUTE_DEFAULT_SPECIFIED;
        String value = null;
        int flags = LIT_ATTRIBUTE;
        boolean saved = expandPE;
        String defaultType = null;

        // LIT_ATTRIBUTE forces '<' checks now (ASAP) and turns whitespace
        // chars to spaces (doesn't matter when that's done if it doesn't
        // interfere with char refs expanding to whitespace).

        if (!skippedPE) {
            flags |= LIT_ENTITY_REF;
            if (handler.stringInterning) {
                if ("CDATA" != type) {
                    flags |= LIT_NORMALIZE;
                }
            } else {
                if (!"CDATA".equals(type)) {
                    flags |= LIT_NORMALIZE;
                }
            }
        }

        expandPE = false;
        if (tryRead('#')) {
            if (tryRead("FIXED")) {
                defaultType = "#FIXED";
                valueType = ATTRIBUTE_DEFAULT_FIXED;
                requireWhitespace();
                value = readLiteral(flags);
            } else if (tryRead("REQUIRED")) {
                defaultType = "#REQUIRED";
                valueType = ATTRIBUTE_DEFAULT_REQUIRED;
            } else if (tryRead("IMPLIED")) {
                defaultType = "#IMPLIED";
                valueType = ATTRIBUTE_DEFAULT_IMPLIED;
            } else {
                fatal("illegal keyword for attribute default value");
            }
        } else {
            value = readLiteral(flags);
        }
        expandPE = saved;
        setAttribute(elementName, name, type, enumer, value, valueType);
        if (handler.stringInterning) {
            if ("ENUMERATION" == type) {
                type = enumer;
            } else if ("NOTATION" == type) {
                type = "NOTATION " + enumer;
            }
        } else {
            if ("ENUMERATION".equals(type)) {
                type = enumer;
            } else if ("NOTATION".equals(type)) {
                type = "NOTATION " + enumer;
            }
        }
        if (!skippedPE) {
            handler.getDeclHandler().attributeDecl(elementName, name, type,
                    defaultType, value);
        }
    }

    /**
     * Parse a conditional section.
     * 
     * <pre>
     *   [61] conditionalSect ::= includeSect || ignoreSect
     *   [62] includeSect ::= '&lt;![' S? 'INCLUDE' S? '['
     *      extSubsetDecl ']]&gt;'
     *   [63] ignoreSect ::= '&lt;![' S? 'IGNORE' S? '['
     *      ignoreSectContents* ']]&gt;'
     *   [64] ignoreSectContents ::= Ignore
     *      ('&lt;![' ignoreSectContents* ']]&gt;' Ignore )*
     *   [65] Ignore ::= Char* - (Char* ( '&lt;![' | ']]&gt;') Char* )
     * </pre>
     * 
     * <p>
     * NOTE: the '&gt;![' has already been read.
     */
    private void parseConditionalSect(char[] saved) throws Exception {
        skipWhitespace();
        if (tryRead("INCLUDE")) {
            skipWhitespace();
            require('[');
            // VC: Proper Conditional Section/PE Nesting
            if (readBuffer != saved) {
                handler.verror("Illegal Conditional Section/PE nesting");
            }
            skipWhitespace();
            while (!tryRead("]]>")) {
                parseMarkupdecl();
                skipWhitespace();
            }
        } else if (tryRead("IGNORE")) {
            skipWhitespace();
            require('[');
            // VC: Proper Conditional Section/PE Nesting
            if (readBuffer != saved) {
                handler.verror("Illegal Conditional Section/PE nesting");
            }
            char c;
            expandPE = false;
            for (int nest = 1; nest > 0;) {
                c = readCh();
                switch (c) {
                    case '<':
                        if (tryRead("![")) {
                            nest++;
                        }
                        break;
                    case ']':
                        if (tryRead("]>")) {
                            nest--;
                        }
                }
            }
            expandPE = true;
        } else {
            fatal("conditional section must begin with INCLUDE or IGNORE");
        }
    }

    private void parseCharRef() throws SAXException, IOException {
        parseCharRef(true /* do flushDataBuffer by default */);
    }

    /**
     * Try to read a character reference without consuming data from buffer.
     * 
     * <pre>
     *   [66] CharRef ::= '&amp;#' [0-9]+ ';' | '&amp;#x' [0-9a-fA-F]+ ';'
     * </pre>
     * 
     * <p>
     * NOTE: the '&#' has already been read.
     */
    private void tryReadCharRef() throws SAXException, IOException {
        int value = 0;
        char c;

        if (tryRead('x')) {
            loop1: while (true) {
                c = readCh();
                if (c == ';') {
                    break loop1;
                } else {
                    int n = Character.digit(c, 16);
                    if (n == -1) {
                        fatal("illegal character in character reference", c,
                                null);
                        break loop1;
                    }
                    value *= 16;
                    value += n;
                }
            }
        } else {
            loop2: while (true) {
                c = readCh();
                if (c == ';') {
                    break loop2;
                } else {
                    int n = Character.digit(c, 10);
                    if (n == -1) {
                        fatal("illegal character in character reference", c,
                                null);
                        break loop2;
                    }
                    value *= 10;
                    value += n;
                }
            }
        }

        // check for character refs being legal XML
        if (((value < 0x0020) && !((value == '\n') || (value == '\t') || (value == '\r')))
                || ((value >= 0xD800) && (value <= 0xDFFF))
                || (value == 0xFFFE)
                || (value == 0xFFFF) || (value > 0x0010ffff)) {
            fatal("illegal XML character reference U+"
                    + Integer.toHexString(value));
        } else if ((value >= 0x007F) && (value <= 0x009F)) // 2006-11-13 hsivonen
        {
            handler.warn("Character reference expands to a control character: U+00"
                    + Integer.toHexString(value) + ".");
        }
        if (isPrivateUse(value)) {
            warnAboutPrivateUseChar();
        }
        // Check for surrogates: 00000000 0000xxxx yyyyyyyy zzzzzzzz
        // (1101|10xx|xxyy|yyyy + 1101|11yy|zzzz|zzzz:
        if (value > 0x0010ffff) {
            // too big for surrogate
            fatal("character reference " + value + " is too large for UTF-16",
                    Integer.valueOf(value).toString(), null);
        }

    }

    /**
     * Read and interpret a character reference.
     * 
     * <pre>
     *   [66] CharRef ::= '&amp;#' [0-9]+ ';' | '&amp;#x' [0-9a-fA-F]+ ';'
     * </pre>
     * 
     * <p>
     * NOTE: the '&#' has already been read.
     */
    private void parseCharRef(boolean doFlush) throws SAXException, IOException {
        int value = 0;
        char c;

        if (tryRead('x')) {
            loop1: while (true) {
                c = readCh();
                if (c == ';') {
                    break loop1;
                } else {
                    int n = Character.digit(c, 16);
                    if (n == -1) {
                        fatal("illegal character in character reference", c,
                                null);
                        break loop1;
                    }
                    value *= 16;
                    value += n;
                }
            }
        } else {
            loop2: while (true) {
                c = readCh();
                if (c == ';') {
                    break loop2;
                } else {
                    int n = Character.digit(c, 10);
                    if (n == -1) {
                        fatal("illegal character in character reference", c,
                                null);
                        break loop2;
                    }
                    value *= 10;
                    value += c - '0';
                }
            }
        }

        // check for character refs being legal XML
        if (((value < 0x0020) && !((value == '\n') || (value == '\t') || (value == '\r')))
                || ((value >= 0xD800) && (value <= 0xDFFF))
                || (value == 0xFFFE)
                || (value == 0xFFFF) || (value > 0x0010ffff)) {
            fatal("illegal XML character reference U+"
                    + Integer.toHexString(value));
        } else if ((value >= 0x007F) && (value <= 0x009F)) // 2006-11-13 hsivonen
        {
            handler.warn("Character reference expands to a control character: U+00"
                    + Integer.toHexString(value) + ".");
        }
        if (isPrivateUse(value)) {
            warnAboutPrivateUseChar();
        }

        // Check for surrogates: 00000000 0000xxxx yyyyyyyy zzzzzzzz
        // (1101|10xx|xxyy|yyyy + 1101|11yy|zzzz|zzzz:
        if (value <= 0x0000ffff) {
            // no surrogates needed
            dataBufferAppend((char) value);
        } else if (value <= 0x0010ffff) {
            value -= 0x10000;
            // > 16 bits, surrogate needed
            dataBufferAppend((char) (0xd800 | (value >> 10)));
            dataBufferAppend((char) (0xdc00 | (value & 0x0003ff)));
        } else {
            // too big for surrogate
            fatal("character reference " + value + " is too large for UTF-16",
                    Integer.valueOf(value).toString(), null);
        }
        if (doFlush) {
            dataBufferFlush();
        }
    }

    /**
     * Parse and expand an entity reference.
     * 
     * <pre>
     *   [68] EntityRef ::= '&amp;' Name ';'
     * </pre>
     * 
     * <p>
     * NOTE: the '&amp;' has already been read.
     * 
     * @param externalAllowed
     *            External entities are allowed here.
     */
    private void parseEntityRef(boolean externalAllowed) throws SAXException,
            IOException {
        String name;

        name = readNmtoken(true);
        require(';');
        switch (getEntityType(name)) {
            case ENTITY_UNDECLARED:
                // NOTE: XML REC describes amazingly convoluted handling for
                // this case. Nothing as meaningful as being a WFness error
                // unless the processor might _legitimately_ not have seen a
                // declaration ... which is what this implements.
                String message;

                message = "reference to undeclared general entity " + name;
                if (skippedPE && !docIsStandalone) {
                    handler.verror(message);
                    // we don't know this entity, and it might be external...
                    if (externalAllowed) {
                        handler.skippedEntity(name);
                    }
                } else {
                    fatal(message);
                }
                break;
            case ENTITY_INTERNAL:
                pushString(name, getEntityValue(name));

                // workaround for possible input pop before marking
                // the buffer reading position
                char t = readCh();
                unread(t);
                int bufferPosMark = readBufferPos;

                int end = readBufferPos + getEntityValue(name).length();
                for (int k = readBufferPos; k < end; k++) {
                    t = readCh();
                    if (t == '&') {
                        t = readCh();
                        if (t == '#') {
                            // try to match a character ref
                            tryReadCharRef();

                            // everything has been read
                            if (readBufferPos >= end) {
                                break;
                            }
                            k = readBufferPos;
                            continue;
                        } else if (Character.isLetter(t)) {
                            // looks like an entity ref
                            unread(t);
                            readNmtoken(true);
                            require(';');

                            // everything has been read
                            if (readBufferPos >= end) {
                                break;
                            }
                            k = readBufferPos;
                            continue;
                        }
                        fatal(" malformed entity reference");
                    }

                }
                readBufferPos = bufferPosMark;
                break;
            case ENTITY_TEXT:
                if (externalAllowed) {
                    pushURL(false, name, getEntityIds(name), null, null, null,
                            true);
                } else {
                    fatal("reference to external entity in attribute value.",
                            name, null);
                }
                break;
            case ENTITY_NDATA:
                if (externalAllowed) {
                    fatal("unparsed entity reference in content", name, null);
                } else {
                    fatal("reference to external entity in attribute value.",
                            name, null);
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Parse and expand a parameter entity reference.
     * 
     * <pre>
     *   [69] PEReference ::= '%' Name ';'
     * </pre>
     * 
     * <p>
     * NOTE: the '%' has already been read.
     */
    private void parsePEReference() throws SAXException, IOException {
        String name;

        name = "%" + readNmtoken(true);
        require(';');
        switch (getEntityType(name)) {
            case ENTITY_UNDECLARED:
                // VC: Entity Declared
                handler.verror("reference to undeclared parameter entity "
                        + name);

                // we should disable handling of all subsequent declarations
                // unless this is a standalone document (info discarded)
                break;
            case ENTITY_INTERNAL:
                if (inLiteral) {
                    pushString(name, getEntityValue(name));
                } else {
                    pushString(name, ' ' + getEntityValue(name) + ' ');
                }
                break;
            case ENTITY_TEXT:
                if (!inLiteral) {
                    pushString(null, " ");
                }
                pushURL(true, name, getEntityIds(name), null, null, null, true);
                if (!inLiteral) {
                    pushString(null, " ");
                }
                break;
        }
    }

    /**
     * Parse an entity declaration.
     * 
     * <pre>
     *   [70] EntityDecl ::= GEDecl | PEDecl
     *   [71] GEDecl ::= '&lt;!ENTITY' S Name S EntityDef S? '&gt;'
     *   [72] PEDecl ::= '&lt;!ENTITY' S '%' S Name S PEDef S? '&gt;'
     *   [73] EntityDef ::= EntityValue | (ExternalID NDataDecl?)
     *   [74] PEDef ::= EntityValue | ExternalID
     *   [75] ExternalID ::= 'SYSTEM' S SystemLiteral
     *         | 'PUBLIC' S PubidLiteral S SystemLiteral
     *   [76] NDataDecl ::= S 'NDATA' S Name
     * </pre>
     * 
     * <p>
     * NOTE: the '&lt;!ENTITY' has already been read.
     */
    private void parseEntityDecl() throws Exception {
        boolean peFlag = false;
        int flags = 0;

        // Check for a parameter entity.
        expandPE = false;
        requireWhitespace();
        if (tryRead('%')) {
            peFlag = true;
            requireWhitespace();
        }
        expandPE = true;

        // Read the entity name, and prepend
        // '%' if necessary.
        String name = readNmtoken(true);
        // NE08
        if (name.indexOf(':') >= 0) {
            fatal("Illegal character(':') in entity name ", name, null);
        }
        if (peFlag) {
            name = "%" + name;
        }

        // Read the entity value.
        requireWhitespace();
        char c = readCh();
        unread(c);
        if ((c == '"') || (c == '\'')) {
            // Internal entity ... replacement text has expanded refs
            // to characters and PEs, but not to general entities
            String value = readLiteral(flags);
            setInternalEntity(name, value);
        } else {
            // Read the external IDs
            ExternalIdentifiers ids = readExternalIds(false, false);

            // Check for NDATA declaration.
            boolean white = tryWhitespace();
            if (!peFlag && tryRead("NDATA")) {
                if (!white) {
                    fatal("whitespace required before NDATA");
                }
                requireWhitespace();
                String notationName = readNmtoken(true);
                if (!skippedPE) {
                    setExternalEntity(name, ENTITY_NDATA, ids, notationName);
                    handler.unparsedEntityDecl(name, ids.publicId,
                            ids.systemId, ids.baseUri, notationName);
                }
            } else if (!skippedPE) {
                setExternalEntity(name, ENTITY_TEXT, ids, null);
                handler.getDeclHandler().externalEntityDecl(name, ids.publicId,
                        handler.resolveURIs()
                        // FIXME: ASSUMES not skipped
                        // "false" forces error on bad URI
                        ? handler.absolutize(ids.baseUri, ids.systemId, false)
                                : ids.systemId);
            }
        }

        // Finish the declaration.
        skipWhitespace();
        require('>');
    }

    /**
     * Parse a notation declaration.
     * 
     * <pre>
     *   [82] NotationDecl ::= '&lt;!NOTATION' S Name S
     *      (ExternalID | PublicID) S? '&gt;'
     *   [83] PublicID ::= 'PUBLIC' S PubidLiteral
     * </pre>
     * 
     * <P>
     * NOTE: the '&lt;!NOTATION' has already been read.
     */
    private void parseNotationDecl() throws Exception {
        String nname;
        ExternalIdentifiers ids;

        requireWhitespace();
        nname = readNmtoken(true);
        // NE08
        if (nname.indexOf(':') >= 0) {
            fatal("Illegal character(':') in notation name ", nname, null);
        }
        requireWhitespace();

        // Read the external identifiers.
        ids = readExternalIds(true, false);

        // Register the notation.
        setNotation(nname, ids);

        skipWhitespace();
        require('>');
    }

    /**
     * Parse character data.
     * 
     * <pre>
     *   [14] CharData ::= [&circ;&lt;&amp;]* - ([&circ;&lt;&amp;]* ']]&gt;' [&circ;&lt;&amp;]*)
     * </pre>
     */
    private void parseCharData() throws Exception {
        char c;
        int state = 0;
        boolean pureWhite = false;

        // assert (dataBufferPos == 0);

        // are we expecting pure whitespace? it might be dirty...
        if ((currentElementContent == CONTENT_ELEMENTS)
                && !isDirtyCurrentElement) {
            pureWhite = true;
        }

        // always report right out of readBuffer
        // to minimize (pointless) buffer copies
        while (true) {
            int i;

            loop: for (i = readBufferPos; i < readBufferLength; i++) {
                advanceLocation();
                switch (c = readBuffer[i]) {
                    case '\n':
                        nextCharOnNewLine = true;
                        // pureWhite unmodified
                        break;
                    case '\r': // should not happen!!
                    case '\t':
                    case ' ':
                        // pureWhite unmodified
                        break;
                    case '&':
                    case '<':
                        // pureWhite unmodified
                        // CLEAN end of text sequence
                        state = 1;
                        rollbackLocation();
                        break loop;
                    case ']':
                        // that's not a whitespace char, and
                        // can not terminate pure whitespace either
                        pureWhite = false;
                        // Refill buffer if needed -- 2008-03-06 hsivonen
                        if (readBufferOverflow == -1) {
                            if ((i + 2) >= readBufferLength) {
                                reportText(pureWhite, i);
                                readBufferOverflow = ']';
                                fillBuffer();
                                i = readBufferPos;
                            }
                            if ((readBuffer[i + 1] == ']')
                                    && (readBuffer[i + 2] == '>')) {
                                // ERROR end of text sequence
                                state = 2;
                                rollbackLocation();
                                break loop;
                            }
                        }
                        break;
                    default:
                        if (((c < 0x0020) || (c > 0xFFFD))
                                || ((c >= 0x007f) && (c <= 0x009f)
                                        && (c != 0x0085) && (xmlVersion == XML_11))) {
                            fatal("illegal XML character U+"
                                    + Integer.toHexString(c));
                        } else if ((c >= '\u007F') && (c <= '\u009F')) // 2006-04-25
                        // hsivonen
                        {
                            handler.warn("Saw a control character: U+00"
                                    + Integer.toHexString(c) + ".");
                        }
                        // that's not a whitespace char
                        pureWhite = false;
                }
            }
            // report characters/whitspace
            reportText(pureWhite, i);

            if (state != 0) {
                break;
            }

            // fill next buffer from this entity, or
            // pop stack and continue with previous entity
            unread(readCh());
        }
        if (!pureWhite) {
            isDirtyCurrentElement = true;
        }
        // finish, maybe with error
        if (state != 1) // finish, no error
        {
            fatal("character data may not contain ']]>'");
        }
    }

    /**
     * @param pureWhite
     * @param i
     * @throws SAXException
     */
    private void reportText(boolean pureWhite, int i) throws SAXException {
        int length = i - readBufferPos;

        if (length != 0) {
            int saveLine = line;
            int saveColumn = column;
            line = linePrev;
            column = columnPrev;
            if (pureWhite) {
                handler.ignorableWhitespace(readBuffer, readBufferPos,
                        length);
            } else {
                handler.charData(readBuffer, readBufferPos, length);
            }
            line = saveLine;
            column = saveColumn;
            readBufferPos = i;
        }
    }

    /**
     * 
     */
    private void advanceLocation() {
        linePrev = line;
        columnPrev = column;
        if (nextCharOnNewLine) {
            line++;
            column = 1;
        } else {
            column++;
        }
        nextCharOnNewLine = false;
    }

    // ////////////////////////////////////////////////////////////////////
    // High-level reading and scanning methods.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Require whitespace characters.
     */
    private void requireWhitespace() throws SAXException, IOException {
        char c = readCh();
        if (isWhitespace(c)) {
            skipWhitespace();
        } else {
            fatal("whitespace required", c, null);
        }
    }

    /**
     * Skip whitespace characters.
     * 
     * <pre>
     *   [3] S ::= (#x20 | #x9 | #xd | #xa)+
     * </pre>
     */
    private void skipWhitespace() throws SAXException, IOException {
        // OK, do it the slow way.
        char c = readCh();
        while (isWhitespace(c)) {
            c = readCh();
        }
        unread(c);
    }

    /**
     * Read a name or (when parsing an enumeration) name token.
     * 
     * <pre>
     *   [5] Name ::= (Letter | '_' | ':') (NameChar)*
     *   [7] Nmtoken ::= (NameChar)+
     * </pre>
     */
    private String readNmtoken(boolean isName) throws SAXException, IOException {
        char c;

        nameBufferPos = 0;

        // Read the first character.
        while (true) {
            c = readCh();
            switch (c) {
                case '%':
                case '<':
                case '>':
                case '&':
                case ',':
                case '|':
                case '*':
                case '+':
                case '?':
                case ')':
                case '=':
                case '\'':
                case '"':
                case '[':
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                case ';':
                case '/':
                    unread(c);
                    if (nameBufferPos == 0) {
                        fatal("name expected");
                    }
                    String s = intern(nameBuffer, 0, nameBufferPos);
                    nameBufferPos = 0;
                    return s;
                default:
                    if (isName && (nameBufferPos == 0)
                            && (!(NCName.isNCNameStart(c)))) {
                        fatal("Not a name start character, U+"
                                + Integer.toHexString(c));
                    } else if (!(NCName.isNCNameTrail(c) || c == ':')) {
                        fatal("Not a name character, U+"
                                + Integer.toHexString(c));
                    }
                    if (nameBufferPos >= nameBuffer.length) {
                        nameBuffer = (char[]) extendArray(nameBuffer,
                                nameBuffer.length, nameBufferPos);
                    }
                    nameBuffer[nameBufferPos++] = c;
            }
        }
    }

    /**
     * Read a literal. With matching single or double quotes as delimiters (and
     * not embedded!) this is used to parse:
     * 
     * <pre>
     *    [9] EntityValue ::= ... ([&circ;%&amp;] | PEReference | Reference)* ...
     *    [10] AttValue ::= ... ([&circ;&lt;&amp;] | Reference)* ...
     *    [11] SystemLiteral ::= ... (URLchar - &quot;'&quot;)* ...
     *    [12] PubidLiteral ::= ... (PubidChar - &quot;'&quot;)* ...
     * </pre>
     * 
     * as well as the quoted strings in XML and text declarations (for version,
     * encoding, and standalone) which have their own constraints.
     */
    private String readLiteral(int flags) throws SAXException, IOException {
        char delim, c;
        int startLine = line;
        boolean saved = expandPE;
        boolean savedReport = doReport;

        // Find the first delimiter.
        delim = readCh();
        if ((delim != '"') && (delim != '\'')) {
            fatal("expected '\"' or \"'\"", delim, null);
            return null;
        }
        inLiteral = true;
        if ((flags & LIT_DISABLE_PE) != 0) {
            expandPE = false;
        }
        doReport = false;

        // Each level of input source has its own buffer; remember
        // ours, so we won't read the ending delimiter from any
        // other input source, regardless of entity processing.
        char[] ourBuf = readBuffer;

        // Read the literal.
        try {
            c = readCh();
            loop: while (!((c == delim) && (readBuffer == ourBuf))) {
                switch (c) {
                    // attributes and public ids are normalized
                    // in almost the same ways
                    case '\n':
                    case '\r':
                        if ((flags & (LIT_ATTRIBUTE | LIT_PUBID)) != 0) {
                            c = ' ';
                        }
                        break;
                    case '\t':
                        if ((flags & LIT_ATTRIBUTE) != 0) {
                            c = ' ';
                        }
                        break;
                    case '&':
                        c = readCh();
                        // Char refs are expanded immediately, except for
                        // all the cases where it's deferred.
                        if (c == '#') {
                            if ((flags & LIT_DISABLE_CREF) != 0) {
                                dataBufferAppend('&');
                                break;
                            }
                            parseCharRef(false /* Do not do flushDataBuffer */);

                            // exotic WFness risk: this is an entity literal,
                            // dataBuffer [dataBufferPos - 1] == '&', and
                            // following chars are a _partial_ entity/char ref

                            // It looks like an entity ref ...
                        } else {
                            unread(c);
                            // Expand it?
                            if ((flags & LIT_ENTITY_REF) > 0) {
                                parseEntityRef(false);
                                // Is it just data?
                            } else if ((flags & LIT_DISABLE_EREF) != 0) {
                                dataBufferAppend('&');

                                // OK, it will be an entity ref -- expanded
                                // later.
                            } else {
                                String name = readNmtoken(true);
                                require(';');
                                dataBufferAppend('&');
                                dataBufferAppend(name);
                                dataBufferAppend(';');
                            }
                        }
                        c = readCh();
                        continue loop;

                    case '<':
                        // and why? Perhaps so "&foo;" expands the same
                        // inside and outside an attribute?
                        if ((flags & LIT_ATTRIBUTE) != 0) {
                            fatal("attribute values may not contain '<'");
                        }
                        break;

                    // We don't worry about case '%' and PE refs, readCh does.

                    default:
                        break;
                }
                dataBufferAppend(c);
                c = readCh();
            }
        } catch (EOFException e) {
            fatal("end of input while looking for delimiter (started on line "
                    + startLine + ')', null, Character.valueOf(delim).toString());
        }
        inLiteral = false;
        expandPE = saved;
        doReport = savedReport;

        // Normalise whitespace if necessary.
        if ((flags & LIT_NORMALIZE) > 0) {
            dataBufferNormalize();
        }

        // Return the value.
        return dataBufferToString();
    }

    /**
     * Try reading external identifiers. A system identifier is not required for
     * notations.
     * 
     * @param inNotation
     *            Are we parsing a notation decl?
     * @param isSubset
     *            Parsing external subset decl (may be omitted)?
     * @return A three-member String array containing the identifiers, or nulls.
     *         Order: public, system, baseURI.
     */
    private ExternalIdentifiers readExternalIds(boolean inNotation,
            boolean isSubset) throws Exception {
        char c;
        ExternalIdentifiers ids = new ExternalIdentifiers();
        int flags = LIT_DISABLE_CREF | LIT_DISABLE_PE | LIT_DISABLE_EREF;

        if (tryRead("PUBLIC")) {
            requireWhitespace();
            ids.publicId = readLiteral(LIT_NORMALIZE | LIT_PUBID | flags);
            if (inNotation) {
                skipWhitespace();
                c = readCh();
                unread(c);
                if ((c == '"') || (c == '\'')) {
                    ids.systemId = readLiteral(flags);
                }
            } else {
                requireWhitespace();
                ids.systemId = readLiteral(flags);
            }

            for (int i = 0; i < ids.publicId.length(); i++) {
                c = ids.publicId.charAt(i);
                if ((c >= 'a') && (c <= 'z')) {
                    continue;
                }
                if ((c >= 'A') && (c <= 'Z')) {
                    continue;
                }
                if (" \r\n0123456789-' ()+,./:=?;!*#@$_%".indexOf(c) != -1) {
                    continue;
                }
                fatal("illegal PUBLIC id character U+" + Integer.toHexString(c));
            }
        } else if (tryRead("SYSTEM")) {
            requireWhitespace();
            ids.systemId = readLiteral(flags);
        } else if (!isSubset) {
            fatal("missing SYSTEM or PUBLIC keyword");
        }

        if (ids.systemId != null) {
            if (ids.systemId.indexOf('#') != -1) {
                handler.verror("SYSTEM id has a URI fragment: " + ids.systemId);
            }
            ids.baseUri = handler.getSystemId();
            if ((ids.baseUri == null) && uriWarnings) {
                handler.warn("No base URI; hope URI is absolute: "
                        + ids.systemId);
            }
        }

        return ids;
    }

    /**
     * Test if a character is whitespace.
     * 
     * <pre>
     *   [3] S ::= (#x20 | #x9 | #xd | #xa)+
     * </pre>
     * 
     * @param c
     *            The character to test.
     * @return true if the character is whitespace.
     */
    private boolean isWhitespace(char c) {
        if (c > 0x20) {
            return false;
        }
        if ((c == 0x20) || (c == 0x0a) || (c == 0x09) || (c == 0x0d)) {
            return true;
        }
        return false; // illegal ...
    }

    // ////////////////////////////////////////////////////////////////////
    // Utility routines.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Add a character to the data buffer.
     */
    private void dataBufferAppend(char c) {
        // Expand buffer if necessary.
        if (dataBufferPos >= dataBuffer.length) {
            dataBuffer = (char[]) extendArray(dataBuffer, dataBuffer.length,
                    dataBufferPos);
        }
        dataBuffer[dataBufferPos++] = c;
    }

    /**
     * Add a string to the data buffer.
     */
    private void dataBufferAppend(String s) {
        dataBufferAppend(s.toCharArray(), 0, s.length());
    }

    /**
     * Append (part of) a character array to the data buffer.
     */
    private void dataBufferAppend(char[] ch, int start, int length) {
        dataBuffer = (char[]) extendArray(dataBuffer, dataBuffer.length,
                dataBufferPos + length);

        System.arraycopy(ch, start, dataBuffer, dataBufferPos, length);
        dataBufferPos += length;
    }

    /**
     * Normalise space characters in the data buffer.
     */
    private void dataBufferNormalize() {
        int i = 0;
        int j = 0;
        int end = dataBufferPos;

        // Skip spaces at the start.
        while ((j < end) && (dataBuffer[j] == ' ')) {
            j++;
        }

        // Skip whitespace at the end.
        while ((end > j) && (dataBuffer[end - 1] == ' ')) {
            end--;
        }

        // Start copying to the left.
        while (j < end) {

            char c = dataBuffer[j++];

            // Normalise all other spaces to
            // a single space.
            if (c == ' ') {
                while ((j < end) && (dataBuffer[j++] == ' ')) {
                    continue;
                }
                dataBuffer[i++] = ' ';
                dataBuffer[i++] = dataBuffer[j - 1];
            } else {
                dataBuffer[i++] = c;
            }
        }

        // The new length is <= the old one.
        dataBufferPos = i;
    }

    /**
     * Convert the data buffer to a string.
     */
    private String dataBufferToString() {
        String s = new String(dataBuffer, 0, dataBufferPos);
        dataBufferPos = 0;
        return s;
    }

    /**
     * Flush the contents of the data buffer to the handler, as appropriate, and
     * reset the buffer for new input.
     */
    private void dataBufferFlush() throws SAXException {
        int saveLine = line;
        int saveColumn = column;
        line = linePrev;
        column = columnPrev;
        if ((currentElementContent == CONTENT_ELEMENTS) && (dataBufferPos > 0)
                && !inCDATA) {
            // We can't just trust the buffer to be whitespace, there
            // are (error) cases when it isn't
            for (int i = 0; i < dataBufferPos; i++) {
                if (!isWhitespace(dataBuffer[i])) {
                    handler.charData(dataBuffer, 0, dataBufferPos);
                    dataBufferPos = 0;
                }
            }
            if (dataBufferPos > 0) {
                handler.ignorableWhitespace(dataBuffer, 0, dataBufferPos);
                dataBufferPos = 0;
            }
        } else if (dataBufferPos > 0) {
            handler.charData(dataBuffer, 0, dataBufferPos);
            dataBufferPos = 0;
        }
        line = saveLine;
        column = saveColumn;
    }

    /**
     * Require a string to appear, or throw an exception.
     * <p>
     * <em>Precondition:</em> Entity expansion is not required.
     * <p>
     * <em>Precondition:</em> data buffer has no characters that will get sent
     * to the application.
     */
    private void require(String delim) throws SAXException, IOException {
        int length = delim.length();
        char[] ch;

        if (length < dataBuffer.length) {
            ch = dataBuffer;
            delim.getChars(0, length, ch, 0);
        } else {
            ch = delim.toCharArray();
        }

        for (int i = 0; i < length; i++) {
                require(ch[i]);
        }
    }

    /**
     * Require a character to appear, or throw an exception.
     */
    private void require(char delim) throws SAXException, IOException {
        char c = readCh();

        if (c != delim) {
            fatal("required character", c, Character.valueOf(delim).toString());
        }
    }

    /**
     * Create an interned string from a character array. &AElig;lfred uses this
     * method to create an interned version of all names and name tokens, so
     * that it can test equality with <code>==</code> instead of
     * <code>String.equals ()</code>.
     * 
     * <p>
     * This is much more efficient than constructing a non-interned string
     * first, and then interning it.
     * 
     * @param ch
     *            an array of characters for building the string.
     * @param start
     *            the starting position in the array.
     * @param length
     *            the number of characters to place in the string.
     * @return an interned string.
     * @see #intern (String)
     * @see java.lang.String#intern
     */
    public String intern(char[] ch, int start, int length) {
        int index = 0;
        int hash = 0;
        Object[] bucket;

        // Generate a hash code. This is a widely used string hash,
        // often attributed to Brian Kernighan.
        for (int i = start; i < start + length; i++) {
            hash = 31 * hash + ch[i];
        }
        hash = (hash & 0x7fffffff) % SYMBOL_TABLE_LENGTH;

        // Get the bucket -- consists of {array,String} pairs
        if ((bucket = symbolTable[hash]) == null) {
            // first string in this bucket
            bucket = new Object[8];

            // Search for a matching tuple, and
            // return the string if we find one.
        } else {
            while (index < bucket.length) {
                char[] chFound = (char[]) bucket[index];

                // Stop when we hit an empty entry.
                if (chFound == null) {
                    break;
                }

                // If they're the same length, check for a match.
                if (chFound.length == length) {
                    for (int i = 0; i < chFound.length; i++) {
                        // continue search on failure
                        if (ch[start + i] != chFound[i]) {
                            break;
                        } else if (i == length - 1) {
                            // That's it, we have a match!
                            return (String) bucket[index + 1];
                        }
                    }
                }
                index += 2;
            }
            // Not found -- we'll have to add it.

            // Do we have to grow the bucket?
            bucket = (Object[]) extendArray(bucket, bucket.length, index);
        }
        symbolTable[hash] = bucket;

        // OK, add it to the end of the bucket -- "local" interning.
        // Intern "globally" to let applications share interning benefits.
        // That is, "!=" and "==" work on our strings, not just equals().
        String s = new String(ch, start, length).intern();
        bucket[index] = s.toCharArray();
        bucket[index + 1] = s;
        return s;
    }

    /**
     * Ensure the capacity of an array, allocating a new one if necessary.
     * Usually extends only for name hash collisions.
     */
    private Object extendArray(Object array, int currentSize, int requiredSize) {
        if (requiredSize < currentSize) {
            return array;
        } else {
            Object newArray = null;
            int newSize = currentSize * 2;

            if (newSize <= requiredSize) {
                newSize = requiredSize + 1;
            }

            if (array instanceof char[]) {
                newArray = new char[newSize];
            } else if (array instanceof Object[]) {
                newArray = new Object[newSize];
            } else {
                throw new RuntimeException();
            }

            System.arraycopy(array, 0, newArray, 0, currentSize);
            return newArray;
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // XML query routines.
    // ////////////////////////////////////////////////////////////////////

    boolean isStandalone() {
        return docIsStandalone;
    }

    //
    // Elements
    //

    private int getContentType(ElementDecl element, int defaultType) {
        int retval;

        if (element == null) {
            return defaultType;
        }
        retval = element.contentType;
        if (retval == CONTENT_UNDECLARED) {
            retval = defaultType;
        }
        return retval;
    }

    /**
     * Look up the content type of an element.
     * 
     * @param name
     *            The element type name.
     * @return An integer constant representing the content type.
     * @see #CONTENT_UNDECLARED
     * @see #CONTENT_ANY
     * @see #CONTENT_EMPTY
     * @see #CONTENT_MIXED
     * @see #CONTENT_ELEMENTS
     */
    public int getElementContentType(String name) {
        ElementDecl element = elementInfo.get(name);
        return getContentType(element, CONTENT_UNDECLARED);
    }

    /**
     * Register an element. Array format: [0] element type name [1] content
     * model (mixed, elements only) [2] attribute hash table
     */
    private void setElement(String name, int contentType, String contentModel,
            HashMap<String, AttributeDecl> attributes) throws SAXException {
        if (skippedPE) {
            return;
        }

        ElementDecl element = elementInfo.get(name);

        // first <!ELEMENT ...> or <!ATTLIST ...> for this type?
        if (element == null) {
            element = new ElementDecl();
            element.contentType = contentType;
            element.contentModel = contentModel;
            element.attributes = attributes;
            elementInfo.put(name, element);
            return;
        }

        // <!ELEMENT ...> declaration?
        if (contentType != CONTENT_UNDECLARED) {
            // ... following an associated <!ATTLIST ...>
            if (element.contentType == CONTENT_UNDECLARED) {
                element.contentType = contentType;
                element.contentModel = contentModel;
            } else {
                // VC: Unique Element Type Declaration
                handler.verror("multiple declarations for element type: "
                        + name);
            }
        }

        // first <!ATTLIST ...>, before <!ELEMENT ...> ?
        else if (attributes != null) {
            element.attributes = attributes;
        }
    }

    /**
     * Look up the attribute hash table for an element. The hash table is the
     * second item in the element array.
     */
    private HashMap<String, AttributeDecl> getElementAttributes(String name) {
        ElementDecl element = elementInfo.get(name);
        return (element == null) ? null : element.attributes;
    }

    //
    // Attributes
    //

    /**
     * Get the declared attributes for an element type.
     * 
     * @param elname
     *            The name of the element type.
     * @return An iterator over all the attributes declared for a specific
     *         element type. The results will be valid only after the DTD (if
     *         any) has been parsed.
     * @see #getAttributeType
     * @see #getAttributeEnumeration
     * @see #getAttributeDefaultValueType
     * @see #getAttributeDefaultValue
     * @see #getAttributeExpandedValue
     */
    private Iterator<String> declaredAttributes(ElementDecl element) {
        HashMap<String, AttributeDecl> attlist;

        if (element == null) {
            return null;
        }
        if ((attlist = element.attributes) == null) {
            return null;
        }
        return attlist.keySet().iterator();
    }

    /**
     * Get the declared attributes for an element type.
     * 
     * @param elname
     *            The name of the element type.
     * @return An iterator over all the attributes declared for a specific
     *         element type. The results will be valid only after the DTD (if
     *         any) has been parsed.
     * @see #getAttributeType
     * @see #getAttributeEnumeration
     * @see #getAttributeDefaultValueType
     * @see #getAttributeDefaultValue
     * @see #getAttributeExpandedValue
     */
    public Iterator<String> declaredAttributes(String elname) {
        return declaredAttributes(elementInfo.get(elname));
    }

    /**
     * Retrieve the declared type of an attribute.
     * 
     * @param name
     *            The name of the associated element.
     * @param aname
     *            The name of the attribute.
     * @return An interend string denoting the type, or null indicating an
     *         undeclared attribute.
     */
    public String getAttributeType(String name, String aname) {
        AttributeDecl attribute = getAttribute(name, aname);
        return (attribute == null) ? null : attribute.type;
    }

    /**
     * Retrieve the allowed values for an enumerated attribute type.
     * 
     * @param name
     *            The name of the associated element.
     * @param aname
     *            The name of the attribute.
     * @return A string containing the token list.
     */
    public String getAttributeEnumeration(String name, String aname) {
        AttributeDecl attribute = getAttribute(name, aname);
        // assert: attribute.enumeration is "ENUMERATION" or "NOTATION"
        return (attribute == null) ? null : attribute.enumeration;
    }

    /**
     * Retrieve the default value of a declared attribute.
     * 
     * @param name
     *            The name of the associated element.
     * @param aname
     *            The name of the attribute.
     * @return The default value, or null if the attribute was #IMPLIED or
     *         simply undeclared and unspecified.
     * @see #getAttributeExpandedValue
     */
    public String getAttributeDefaultValue(String name, String aname) {
        AttributeDecl attribute = getAttribute(name, aname);
        return (attribute == null) ? null : attribute.value;
    }

    /*
     * // FIXME: Leaving this in, until W3C finally resolves the confusion //
     * between parts of the XML 2nd REC about when entity declararations // are
     * guaranteed to be known. Current code matches what section 5.1 //
     * (conformance) describes, but some readings of the self-contradicting //
     * text in 4.1 (the "Entity Declared" WFC and VC) seem to expect that //
     * attribute expansion/normalization must be deferred in some cases // (just
     * TRY to identify them!).
     * 
     * Retrieve the expanded value of a declared attribute. <p>General entities
     * (and char refs) will be expanded (once). @param name The name of the
     * associated element. @param aname The name of the attribute. @return The
     * expanded default value, or null if the attribute was #IMPLIED or simply
     * undeclared
     * 
     * @see #getAttributeDefaultValue public String getAttributeExpandedValue
     *      (String name, String aname) throws Exception { AttributeDecl
     *      attribute = getAttribute (name, aname);
     * 
     * if (attribute == null) { return null; } else if (attribute.defaultValue ==
     * null && attribute.value != null) { // we MUST use the same buf for both
     * quotes else the literal // can't be properly terminated char buf [] = new
     * char [1]; int flags = LIT_ENTITY_REF | LIT_ATTRIBUTE; String type =
     * getAttributeType (name, aname);
     * 
     * if (type != "CDATA" && type != null) flags |= LIT_NORMALIZE; buf [0] =
     * '"'; pushCharArray (null, buf, 0, 1); pushString (null, attribute.value);
     * pushCharArray (null, buf, 0, 1); attribute.defaultValue = readLiteral
     * (flags); } return attribute.defaultValue; }
     */

    /**
     * Retrieve the default value mode of a declared attribute.
     * 
     * @see #ATTRIBUTE_DEFAULT_SPECIFIED
     * @see #ATTRIBUTE_DEFAULT_IMPLIED
     * @see #ATTRIBUTE_DEFAULT_REQUIRED
     * @see #ATTRIBUTE_DEFAULT_FIXED
     */
    public int getAttributeDefaultValueType(String name, String aname) {
        AttributeDecl attribute = getAttribute(name, aname);
        return (attribute == null) ? ATTRIBUTE_DEFAULT_UNDECLARED
                : attribute.valueType;
    }

    /**
     * Register an attribute declaration for later retrieval. Format: - String
     * type - String default value - int value type - enumeration - processed
     * default value
     */
    private void setAttribute(String elName, String name, String type,
            String enumeration, String value, int valueType) throws Exception {
        HashMap<String, AttributeDecl> attlist;

        if (skippedPE) {
            return;
        }

        // Create a new hashtable if necessary.
        attlist = getElementAttributes(elName);
        if (attlist == null) {
            attlist = new HashMap<>();
        }

        // ignore multiple attribute declarations!
        if (attlist.get(name) != null) {
            // warn ...
            return;
        } else {
            AttributeDecl attribute = new AttributeDecl();
            attribute.type = type;
            attribute.value = value;
            attribute.valueType = valueType;
            attribute.enumeration = enumeration;
            attlist.put(name, attribute);

            // save; but don't overwrite any existing <!ELEMENT ...>
            setElement(elName, CONTENT_UNDECLARED, null, attlist);
        }
    }

    /**
     * Retrieve the attribute declaration for the given element name and name.
     */
    private AttributeDecl getAttribute(String elName, String name) {
        HashMap<String, AttributeDecl> attlist = getElementAttributes(elName);
        return (attlist == null) ? null : attlist.get(name);
    }

    //
    // Entities
    //

    /**
     * Find the type of an entity.
     * 
     * @returns An integer constant representing the entity type.
     * @see #ENTITY_UNDECLARED
     * @see #ENTITY_INTERNAL
     * @see #ENTITY_NDATA
     * @see #ENTITY_TEXT
     */
    public int getEntityType(String ename) {
        EntityInfo entity = entityInfo.get(ename);
        return (entity == null) ? ENTITY_UNDECLARED : entity.type;
    }

    /**
     * Return an external entity's identifiers.
     * 
     * @param ename
     *            The name of the external entity.
     * @return The entity's public identifier, system identifier, and base URI.
     *         Null if the entity was not declared as an external entity.
     * @see #getEntityType
     */
    public ExternalIdentifiers getEntityIds(String ename) {
        EntityInfo entity = entityInfo.get(ename);
        return (entity == null) ? null : entity.ids;
    }

    /**
     * Return an internal entity's replacement text.
     * 
     * @param ename
     *            The name of the internal entity.
     * @return The entity's replacement text, or null if the entity was not
     *         declared as an internal entity.
     * @see #getEntityType
     */
    public String getEntityValue(String ename) {
        EntityInfo entity = entityInfo.get(ename);
        return (entity == null) ? null : entity.value;
    }

    /**
     * Register an entity declaration for later retrieval.
     */
    private void setInternalEntity(String eName, String value)
            throws SAXException {
        if (skippedPE) {
            return;
        }

        if (entityInfo.get(eName) == null) {
            EntityInfo entity = new EntityInfo();
            entity.type = ENTITY_INTERNAL;
            entity.value = value;
            entityInfo.put(eName, entity);
        }
        if (handler.stringInterning) {
            if (("lt" == eName) || ("gt" == eName) || ("quot" == eName)
                    || ("apos" == eName) || ("amp" == eName)) {
                return;
            }
        } else {
            if ("lt".equals(eName) || "gt".equals(eName)
                    || "quot".equals(eName) || "apos".equals(eName)
                    || "amp".equals(eName)) {
                return;
            }
        }
        handler.getDeclHandler().internalEntityDecl(eName, value);
    }

    /**
     * Register an external entity declaration for later retrieval.
     */
    private void setExternalEntity(String eName, int eClass,
            ExternalIdentifiers ids, String nName) {
        if (entityInfo.get(eName) == null) {
            EntityInfo entity = new EntityInfo();
            entity.type = eClass;
            entity.ids = ids;
            entity.notationName = nName;
            entityInfo.put(eName, entity);
        }
    }

    //
    // Notations.
    //

    /**
     * Report a notation declaration, checking for duplicates.
     */
    private void setNotation(String nname, ExternalIdentifiers ids)
            throws SAXException {
        if (skippedPE) {
            return;
        }

        handler.notationDecl(nname, ids.publicId, ids.systemId, ids.baseUri);
        if (notationInfo.get(nname) == null) {
            notationInfo.put(nname, nname);
        } else {
            // VC: Unique Notation Name
            handler.verror("Duplicate notation name decl: " + nname);
        }
    }

    //
    // Location.
    //

    /**
     * Return the current line number.
     */
    public int getLineNumber() {
        if (line > 0) {
            return line;
        } else {
            return -1;
        }
    }

    /**
     * Return the current column number.
     */
    public int getColumnNumber() {
        if (column > 0) {
            return column;
        } else {
            return -1;
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // High-level I/O.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Read a single character from the readBuffer.
     * <p>
     * The readDataChunk () method maintains the buffer.
     * <p>
     * If we hit the end of an entity, try to pop the stack and keep going.
     * <p>
     * (This approach doesn't really enforce XML's rules about entity
     * boundaries, but this is not currently a validating parser).
     * <p>
     * This routine also attempts to keep track of the current position in
     * external entities, but it's not entirely accurate.
     * 
     * @return The next available input character.
     * @see #unread (char)
     * @see #readDataChunk
     * @see #readBuffer
     * @see #line
     * @return The next character from the current input source.
     */
    private char readCh() throws SAXException, IOException {
        // As long as there's nothing in the
        // read buffer, try reading more data
        // (for an external entity) or popping
        // the entity stack (for either).
        fillBuffer();

        char c = readBuffer[readBufferPos++];
        advanceLocation();
        // copied from fi.iki.hsivonen.htmlparser
        if ((c & 0xFC00) == 0xDC00) {
            // Got a low surrogate. See if prev was high surrogate
            if ((prev & 0xFC00) == 0xD800) {
                int intVal = (prev << 10) + c + SURROGATE_OFFSET;
                if (isNonCharacter(intVal)) {
                    handler.warn("Astral non-character.");
                }
                if (isAstralPrivateUse(intVal)) {
                    warnAboutPrivateUseChar();
                }
            } else {
                if (prev != 0) {
                    // the prev == 0 situation is bogus, but better not to fail
                    fatal("Unmatched low surrogate.");
                }
            }
            prev = c;
        } else {
            // see if there was a lone high surrogate
            if ((prev & 0xFC00) == 0xD800) {
                fatal("Unmatched high surrogate.");
            }
        }

        if (c == '\n') {
            nextCharOnNewLine = true;
        } else {
            if (c == '<') {
                /* the most common return to parseContent () ... NOP */
            } else if ((((c < 0x0020) && (c != '\t') && (c != '\r')) || (c > 0xFFFD))
                    || ((c >= 0x007f) && (c <= 0x009f) && (c != 0x0085) && (xmlVersion == XML_11))) {
                fatal("illegal XML character U+" + Integer.toHexString(c));
            } else if ((c >= '\u007F') && (c <= '\u009F')) // 2006-04-25 hsivonen
            {
                handler.warn("Saw a control character: U+00"
                        + Integer.toHexString(c) + ".");
            }

            if (isPrivateUse(c)) {
                warnAboutPrivateUseChar();
            }
            // If we're in the DTD and in a context where PEs get expanded,
            // do so ... 1/14/2000 errata identify those contexts. There
            // are also spots in the internal subset where PE refs are fatal
            // errors, hence yet another flag.
            else if ((c == '%') && expandPE) {
                if (peIsError) {
                    fatal("PE reference within decl in internal subset.");
                }
                parsePEReference();
                return readCh();
            }
        }

        return c;
    }

    /**
     * @throws SAXException
     * @throws IOException
     */
    private void fillBuffer() throws SAXException, IOException {
        while (readBufferPos >= readBufferLength) {
            switch (sourceType) {
                case INPUT_READER:
                    readDataChunk();
                    while (readBufferLength < 1) {
                        popInput();
                        if (readBufferLength < 1) {
                            readDataChunk();
                        }
                    }
                    break;

                default:

                    popInput();
                    break;
            }
        }
    }

    /**
     * Push a single character back onto the current input stream.
     * <p>
     * This method usually pushes the character back onto the readBuffer.
     * <p>
     * I don't think that this would ever be called with readBufferPos = 0,
     * because the methods always reads a character before unreading it, but
     * just in case, I've added a boundary condition.
     * 
     * @param c
     *            The character to push back.
     * @see #readCh
     * @see #unread (char[])
     * @see #readBuffer
     */
    private void unread(char c) throws SAXException {
        rollbackLocation();
        if (readBufferPos > 0) {
            readBuffer[--readBufferPos] = c;
        } else {
            pushString(null, Character.valueOf(c).toString());
        }
    }

    /**
     * 
     */
    private void rollbackLocation() {
        assert (column != columnPrev) || (line != linePrev);
        nextCharOnNewLine = (column == 1);
        line = linePrev;
        column = columnPrev;
    }

    /**
     * Push a char array back onto the current input stream.
     * <p>
     * NOTE: you must <em>never</em> push back characters that you haven't
     * actually read: use pushString () instead.
     * 
     * @see #readCh
     * @see #unread (char)
     * @see #readBuffer
     * @see #pushString
     */
    private void unread(char[] ch, int length) throws SAXException {
        if (length < readBufferPos) {
            readBufferPos -= length;
        } else {
            pushCharArray(null, ch, 0, length);
        }
    }

    /**
     * Push, or skip, a new external input source. The source will be some kind
     * of parsed entity, such as a PE (including the external DTD subset) or
     * content for the body.
     * 
     * @param url
     *            The java.net.URL object for the entity.
     * @see SAXDriver#resolveEntity
     * @see #pushString
     * @see #sourceType
     * @see #pushInput
     * @see #detectEncoding
     * @see #sourceType
     * @see #readBuffer
     */
    private void pushURL(boolean isPE, String ename, ExternalIdentifiers ids,
            Reader aReader, InputStream aStream, String aEncoding,
            boolean doResolve) throws SAXException, IOException {
        // removed boolean ignoreEncoding -- 2006-02-03 hsivonen
        String systemId;
        InputSource source;
        InputSource scratch = new InputSource();

        if (!isPE) {
            dataBufferFlush();
        }

        scratch.setPublicId(ids.publicId);
        scratch.setSystemId(ids.systemId);

        // See if we should skip or substitute the entity.
        // If we're not skipping, resolving reports startEntity()
        // and updates the (handler's) stack of URIs.
        if (doResolve) {
            // assert (stream == null && reader == null && encoding == null)
            source = handler.resolveEntity(isPE, ename, scratch, ids.baseUri);
            if (source == null) {
                handler.skippedEntity(ename);
                if (isPE) {
                    skippedPE = true;
                }
                return;
            }

            // we might be using alternate IDs/encoding
            systemId = source.getSystemId();
            // The following warning and setting systemId was deleted because
            // the application has the option of not setting systemId
            // provided that it has set the characte/byte stream.
            /*
             * if (systemId == null) { handler.warn ("missing system ID, using " +
             * ids.systemId); systemId = ids.systemId; }
             */
        } else {
            // "[document]", or "[dtd]" via getExternalSubset()
            scratch.setCharacterStream(aReader);
            scratch.setByteStream(aStream);
            scratch.setEncoding(aEncoding);
            source = scratch;
            systemId = ids.systemId;
            if (handler.stringInterning) {
                handler.startExternalEntity(ename, systemId,
                        "[document]" == ename);
            } else {
                handler.startExternalEntity(ename, systemId,
                        "[document]".equals(ename));
            }
        }

        // Push the existing status.
        pushInput(ename);

        // Create a new read buffer.
        // (Note the four-character margin)
        readBuffer = new char[READ_BUFFER_MAX + 4];
        readBufferPos = 0;
        readBufferLength = 0;
        readBufferOverflow = -1;
        is = null;
        reader = null;
        line = 0;
        column = 1;
        linePrev = 0;
        columnPrev = 1;
        nextCharOnNewLine = true;
        currentByteCount = 0;

        // If there's an explicit character stream, just
        // ignore encoding declarations.
        if (source.getCharacterStream() != null) {
            sourceType = INPUT_READER;
            this.reader = source.getCharacterStream();
            // swallow UTF-8 BOM -- 2006-02-03 hsivonen
            if ("UTF-8".equalsIgnoreCase(source.getEncoding())) {
                char bom = readCh();
                if (bom != '\uFEFF') {
                    unread(bom);
                }
            }
            tryEncodingDecl(source.getEncoding() == null ? ""
                    : source.getEncoding());
            return;
        }

        // Else we handle the conversion, and need to ensure
        // it's done right.
        if (source.getByteStream() != null) {
            is = source.getByteStream();
        } else {
            // Stop -- 2006-11-10 hsivonen
            fatal("The entity resolver didn't properly resolve the entity.");
        }

        // If we get to here, there must be
        // an InputStream available.
        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }

        // Zapped bogus external encoding label code -- 2006-11-10 hsivonen

        // if we got an external encoding label, use it ...
        if (source.getEncoding() != null) {
            draconianInputStreamReader(source.getEncoding(), is, false);
            if ("UTF-8".equalsIgnoreCase(source.getEncoding())) {
                char bom = readCh();
                if (bom != '\uFEFF') {
                    unread(bom);
                }
            }
            tryEncodingDecl(source.getEncoding());
            // ... else autodetect from first bytes.
        } else {
            detectEncoding();
            // Read any XML or text declaration.
            String enc = tryEncodingDecl(null);
            if ((enc == null) && ("UTF-32" == characterEncoding)) {
                fatal("UTF-32 was sniffed from the BOM, but there was no matching encoding declaration. The omission of explicit encoding declaration is only allowed with UTF-8 and UTF-16.");
            }
        }
    }

    /**
     * Check for an encoding declaration. This is the second part of the XML
     * encoding autodetection algorithm, relying on detectEncoding to get to the
     * point that this part can read any encoding declaration in the document
     * (using only US-ASCII characters).
     * 
     * <p>
     * Because this part starts to fill parser buffers with this data, it's
     * tricky to setup a reader so that Java's built-in decoders can be used for
     * the character encodings that aren't built in to this parser (such as
     * EUC-JP, KOI8-R, Big5, etc).
     * 
     * @return any encoding in the declaration, uppercased; or null
     * @see detectEncoding
     */
    private String tryEncodingDecl(String encoding) throws SAXException,
            IOException {
        // Read the XML/text declaration.
        if (tryRead("<?xml")) {
            if (tryWhitespace()) {
                if (inputStack.size() > 0) {
                    return parseTextDecl(encoding);
                } else {
                    return parseXMLDecl(encoding);
                }
            } else {
                // <?xml-stylesheet ...?> or similar
                pushString(null, "<?xml");
//                unread('l');
//                unread('m');
//                unread('x');
//                unread('?');
//                unread('<');
            }
        }
        // 2006-02-03 hsivonen
        warnAboutLackOfEncodingDecl(encoding);
        return null;
    }

    /**
     * @param characterEncoding
     * @throws SAXException
     */
    private void warnAboutLackOfEncodingDecl(String encoding)
            throws SAXException {
        if (!((encoding == null) || "".equals(encoding)
                || "UTF-8".equalsIgnoreCase(encoding) || "UTF-16".equalsIgnoreCase(encoding))) {
            handler.warn("External encoding information specified a non-UTF-8/non-UTF-16 encoding ("
                    + encoding
                    + "), but there was no matching internal encoding declaration. The well-formedness status of this document may change when decoupled from the external encoding information.");
        }
    }

    /**
     * Attempt to detect the encoding of an entity.
     * <p>
     * The trick here (as suggested in the XML standard) is that any entity not
     * in UTF-8, or in UCS-2 with a byte-order mark, <b>must</b> begin with an
     * XML declaration or an encoding declaration; we simply have to look for
     * "&lt;?xml" in various encodings.
     * <p>
     * This method has no way to distinguish among 8-bit encodings. Instead, it
     * sets up for UTF-8, then (possibly) revises its assumption later in
     * setupDecoding (). Any ASCII-derived 8-bit encoding should work, but most
     * will be rejected later by setupDecoding ().
     * 
     * @see #tryEncoding (byte[], byte, byte, byte, byte)
     * @see #tryEncoding (byte[], byte, byte)
     * @see #setupDecoding
     */
    private void detectEncoding() throws SAXException, IOException {
        byte[] signature = new byte[6];

        // Read the first six bytes for
        // autodetection.
        // Use 6 bytes for detection -- hsivonen 2010-10-22
        is.mark(6);
        is.read(signature);
        is.reset();

        //
        // FIRST: four byte encodings (who uses these?)
        //
        if (tryEncoding(signature, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x3c)) {
            // UCS-4 must begin with "<?xml"
            // 0x00 0x00 0x00 0x3c: UCS-4, big-endian (1234)
            // "UTF-32BE"
            fatal("UTF-32BE is not supported. (XML processors are only required to support UTF-8 and UTF-16.)"); // 2010-10-22
            // hsivonen
        } else if (tryEncoding(signature, (byte) 0x3c, (byte) 0x00,
                (byte) 0x00, (byte) 0x00)) {
            // 0x3c 0x00 0x00 0x00: UCS-4, little-endian (4321)
            // "UTF-32LE"
            fatal("UTF-32LE is not supported. (XML processors are only required to support UTF-8 and UTF-16.)"); // 2010-10-22
            // hsivonen
        } else if (tryEncoding(signature, (byte) 0x00, (byte) 0x00,
                (byte) 0x3c, (byte) 0x00)) {
            // 0x00 0x00 0x3c 0x00: UCS-4, unusual (2143)
            fatal("Unsupported 32-bit encoding. (XML processors are only required to support UTF-8 and UTF-16.)"); // 2006-02-03
            // hsivonen
        } else if (tryEncoding(signature, (byte) 0x00, (byte) 0x3c,
                (byte) 0x00, (byte) 0x00)) {
            // 0x00 0x3c 0x00 0x00: UCS-4, unusual (3421)
            fatal("Unsupported 32-bit encoding. (XML processors are only required to support UTF-8 and UTF-16.)"); // 2006-02-03
            // hsivonen
        } else if (tryEncoding(signature, (byte) 0x00, (byte) 0x00,
                (byte) 0xfe, (byte) 0xff)) {
            // 00 00 fe ff UCS_4_1234 (with BOM)
            fatal("UTF-32 is not supported. (XML processors are only required to support UTF-8 and UTF-16.)"); // 2010-10-22
            // hsivonen
        } else if (tryEncoding(signature, (byte) 0x00, (byte) 0x3c,
                (byte) 0x00, (byte) 0x00)) {
            // ff fe 00 00 UCS_4_4321 (with BOM)
            fatal("UTF-32 is not supported. (XML processors are only required to support UTF-8 and UTF-16.)"); // 2010-10-22
            // hsivonen
        }
        // SECOND: two byte encodings
        // note ... with 1/14/2000 errata the XML spec identifies some
        // more "broken UTF-16" autodetection cases, with no XML decl,
        // which we don't handle here (that's legal too).
        //
        else if (tryEncoding(signature, (byte) 0xfe, (byte) 0xff)) {
            // UCS-2 with a byte-order marker. (UTF-16)
            // 0xfe 0xff: UCS-2, big-endian (12)
            is.read();
            is.read();
            draconianInputStreamReader("UTF-16BE", is, false, "UTF-16");
        } else if (tryEncoding(signature, (byte) 0xff, (byte) 0xfe)) {
            // UCS-2 with a byte-order marker. (UTF-16)
            // 0xff 0xfe: UCS-2, little-endian (21)
            is.read();
            is.read();
            draconianInputStreamReader("UTF-16LE", is, false, "UTF-16");
        } else if (tryEncoding(signature, (byte) 0x00, (byte) 0x3c,
                (byte) 0x00, (byte) 0x3f)) {
            // UTF-16BE (otherwise, malformed UTF-16)
            // 0x00 0x3c 0x00 0x3f: UCS-2, big-endian, no byte-order mark
            fatal("no byte-order mark for UTF-16 entity"); // s/UCS-2/UTF-16/
            // -- 2006-02-03
            // hsivonen
        } else if (tryEncoding(signature, (byte) 0x3c, (byte) 0x00,
                (byte) 0x3f, (byte) 0x00)) {
            // UTF-16LE (otherwise, malformed UTF-16)
            // 0x3c 0x00 0x3f 0x00: UCS-2, little-endian, no byte-order mark
            fatal("no byte-order mark for UTF-16 entity"); // s/UCS-2/UTF-16/
            // -- 2006-02-03
            // hsivonen
        }
        //
        // THIRD: EBCDIC
        //
        else if (tryEncoding(signature, (byte) 0x4c, (byte) 0x6f, (byte) 0xa7,
                (byte) 0x94)) {
            // 4c 6f a7 94 ... we don't understand EBCDIC flavors
            fatal("Unsupported EBCDIC encoding. (XML processors are only required to support UTF-8 and UTF-16.)");
        }
        //
        // FOURTH: ASCII-derived encodings, fixed and variable lengths
        //
        else if (signature[0] == (byte) 0x3c
                && signature[1] == (byte) 0x3f
                && signature[2] == (byte) 0x78
                && signature[3] == (byte) 0x6d
                && signature[4] == (byte) 0x6c
                && (signature[5] == (byte) 0x20 || signature[5] == (byte) 0x0a
                        || signature[5] == (byte) 0x0d || signature[5] == (byte) 0x09)) {
            // Use 6 bytes for detection -- hsivonen 2010-10-22
            // ASCII derived
            // 0x3c 0x3f 0x78 0x6d: UTF-8 or other 8-bit markup (read ENCODING)
            characterEncoding = null;
            prefetchASCIIEncodingDecl();
        } else if ((signature[0] == (byte) 0xef) && (signature[1] == (byte) 0xbb)
                && (signature[2] == (byte) 0xbf)) {
            // 0xef 0xbb 0xbf: UTF-8 BOM (not part of document text)
            // this un-needed notion slipped into XML 2nd ed through a
            // "non-normative" erratum; now required by MSFT and UDDI,
            // and E22 made it normative.
            is.read();
            is.read();
            is.read();
            draconianInputStreamReader("UTF-8", is, false);
        } else {
            // (default) UTF-8 without encoding/XML declaration
            draconianInputStreamReader("UTF-8", is, false);
        }
    }

    /**
     * Check for a four-byte signature.
     * <p>
     * Utility routine for detectEncoding ().
     * <p>
     * Always looks for some part of "<?XML" in a specific encoding.
     * 
     * @param sig
     *            The first four bytes read.
     * @param b1
     *            The first byte of the signature
     * @param b2
     *            The second byte of the signature
     * @param b3
     *            The third byte of the signature
     * @param b4
     *            The fourth byte of the signature
     * @see #detectEncoding
     */
    private static boolean tryEncoding(byte[] sig, byte b1, byte b2, byte b3,
            byte b4) {
        return ((sig[0] == b1) && (sig[1] == b2) && (sig[2] == b3) && (sig[3] == b4));
    }

    /**
     * Check for a two-byte signature.
     * <p>
     * Looks for a UCS-2 byte-order mark.
     * <p>
     * Utility routine for detectEncoding ().
     * 
     * @param sig
     *            The first four bytes read.
     * @param b1
     *            The first byte of the signature
     * @param b2
     *            The second byte of the signature
     * @see #detectEncoding
     */
    private static boolean tryEncoding(byte[] sig, byte b1, byte b2) {
        return ((sig[0] == b1) && (sig[1] == b2));
    }

    /**
     * This method pushes a string back onto input.
     * <p>
     * It is useful either as the expansion of an internal entity, or for
     * backtracking during the parse.
     * <p>
     * Call pushCharArray () to do the actual work.
     * 
     * @param s
     *            The string to push back onto input.
     * @see #pushCharArray
     */
    private void pushString(String ename, String s) throws SAXException {
        char[] ch = s.toCharArray();
        pushCharArray(ename, ch, 0, ch.length);
    }

    /**
     * Push a new internal input source.
     * <p>
     * This method is useful for expanding an internal entity, or for unreading
     * a string of characters. It creates a new readBuffer containing the
     * characters in the array, instead of characters converted from an input
     * byte stream.
     * 
     * @param ch
     *            The char array to push.
     * @see #pushString
     * @see #pushURL
     * @see #readBuffer
     * @see #sourceType
     * @see #pushInput
     */
    private void pushCharArray(String ename, char[] ch, int start, int length)
            throws SAXException {
        // Push the existing status
        pushInput(ename);
        if ((ename != null) && doReport) {
            dataBufferFlush();
            handler.startInternalEntity(ename);
        }
        sourceType = INPUT_INTERNAL;
        readBuffer = ch;
        readBufferPos = start;
        readBufferLength = length;
        readBufferOverflow = -1;
    }

    /**
     * Save the current input source onto the stack.
     * <p>
     * This method saves all of the global variables associated with the current
     * input source, so that they can be restored when a new input source has
     * finished. It also tests for entity recursion.
     * <p>
     * The method saves the following global variables onto a stack using a
     * fixed-length array:
     * <ol>
     * <li>sourceType
     * <li>externalEntity
     * <li>readBuffer
     * <li>readBufferPos
     * <li>readBufferLength
     * <li>line
     * <li>characterEncoding
     * </ol>
     * 
     * @param ename
     *            The name of the entity (if any) causing the new input.
     * @see #popInput
     * @see #sourceType
     * @see #externalEntity
     * @see #readBuffer
     * @see #readBufferPos
     * @see #readBufferLength
     * @see #line
     * @see #characterEncoding
     */
    private void pushInput(String ename) throws SAXException {
        // Protect against billion laughs -- 2006-12-28 hsivonen
        if (entityStack.size() > 16) {
            fatal("Entity recursion too deep. Stopping to protect against denial of service attacks.");
        }

        // Check for entity recursion.
        if (ename != null) {
            Iterator<String> entities = entityStack.iterator();
            while (entities.hasNext()) {
                String e = entities.next();
                if ((e != null) && (e == ename)) {
                    fatal("recursive reference to entity", ename, null);
                }
            }
        }
        entityStack.addLast(ename);

        // Don't bother if there is no current input.
        if (sourceType == INPUT_NONE) {
            return;
        }

        // Set up a snapshot of the current
        // input source.
        Input input = new Input();

        input.sourceType = sourceType;
        input.readBuffer = readBuffer;
        input.readBufferPos = readBufferPos;
        input.readBufferLength = readBufferLength;
        input.line = line;
        input.linePrev = linePrev;
        input.characterEncoding = characterEncoding;
        input.readBufferOverflow = readBufferOverflow;
        input.is = is;
        input.currentByteCount = currentByteCount;
        input.column = column;
        input.columnPrev = columnPrev;
        input.nextCharOnNewLine = nextCharOnNewLine;
        input.reader = reader;
        input.prev = prev;
        input.normalizationChecker = normalizationChecker;
        input.characterHandler = characterHandler;
        characterHandler = null;

        // Push it onto the stack.
        inputStack.addLast(input);
    }

    /**
     * Restore a previous input source.
     * <p>
     * This method restores all of the global variables associated with the
     * current input source.
     * 
     * @exception java.io.EOFException
     *                If there are no more entries on the input stack.
     * @see #pushInput
     * @see #sourceType
     * @see #readBuffer
     * @see #readBufferPos
     * @see #readBufferLength
     * @see #line
     * @see #characterEncoding
     */
    private void popInput() throws SAXException, IOException {
        String ename = entityStack.removeLast();

        if ((ename != null) && doReport) {
            dataBufferFlush();
        }
        switch (sourceType) {
            case INPUT_READER:
                handler.endExternalEntity(ename);
                reader.close();
                break;
            case INPUT_INTERNAL:
                if ((ename != null) && doReport) {
                    handler.endInternalEntity(ename);
                }
                break;
        }
        if (characterHandler != null) {
            characterHandler.end();
        }
        if (normalizationChecker != null) {
            normalizationChecker.end();
        }

        // Throw an EOFException if there
        // is nothing else to pop.
        if (inputStack.isEmpty()) {
            throw new EOFException("no more input");
        }

        Input input = inputStack.removeLast();

        sourceType = input.sourceType;
        readBuffer = input.readBuffer;
        readBufferPos = input.readBufferPos;
        readBufferLength = input.readBufferLength;
        line = input.line;
        linePrev = input.linePrev;
        characterEncoding = input.characterEncoding;
        readBufferOverflow = input.readBufferOverflow;
        is = input.is;
        currentByteCount = input.currentByteCount;
        column = input.column;
        columnPrev = input.columnPrev;
        nextCharOnNewLine = input.nextCharOnNewLine;
        reader = input.reader;
        prev = input.prev;
        normalizationChecker = input.normalizationChecker;
        characterHandler = input.characterHandler;
    }

    /**
     * Return true if we can read the expected character.
     * <p>
     * Note that the character will be removed from the input stream on success,
     * but will be put back on failure. Do not attempt to read the character
     * again if the method succeeds.
     * 
     * @param delim
     *            The character that should appear next. For a insensitive
     *            match, you must supply this in upper-case.
     * @return true if the character was successfully read, or false if it was
     *         not.
     * @see #tryRead (String)
     */
    private boolean tryRead(char delim) throws SAXException, IOException {
        char c;

        // Read the character
        c = readCh();

        // Test for a match, and push the character
        // back if the match fails.
        if (c == delim) {
            return true;
        } else {
            unread(c);
            return false;
        }
    }

    /**
     * Return true if we can read the expected string.
     * <p>
     * This is simply a convenience method.
     * <p>
     * Note that the string will be removed from the input stream on success,
     * but will be put back on failure. Do not attempt to read the string again
     * if the method succeeds.
     * <p>
     * This method will push back a character rather than an array whenever
     * possible (probably the majority of cases).
     * 
     * @param delim
     *            The string that should appear next.
     * @return true if the string was successfully read, or false if it was not.
     * @see #tryRead (char)
     */
    private boolean tryRead(String delim) throws SAXException, IOException {
        return tryRead(delim.toCharArray());
    }

    private boolean tryRead(char[] ch) throws SAXException, IOException {
        char c;

        // Compare the input, character-
        // by character.
        int saveLine = line;
        int saveColumn = column;
        int saveLinePrev = linePrev;
        int saveColumnPrev = columnPrev;
        boolean saveNextCharOnNewLine = nextCharOnNewLine;

        for (int i = 0; i < ch.length; i++) {
            c = readCh();
            if (c != ch[i]) {
                unread(c);
                if (i != 0) {
                    unread(ch, i);
                }
                line = saveLine;
                column = saveColumn;
                linePrev = saveLinePrev;
                columnPrev = saveColumnPrev;
                nextCharOnNewLine = saveNextCharOnNewLine;
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if we can read some whitespace.
     * <p>
     * This is simply a convenience method.
     * <p>
     * This method will push back a character rather than an array whenever
     * possible (probably the majority of cases).
     * 
     * @return true if whitespace was found.
     */
    private boolean tryWhitespace() throws SAXException, IOException {
        char c;
        c = readCh();
        if (isWhitespace(c)) {
            skipWhitespace();
            return true;
        } else {
            unread(c);
            return false;
        }
    }

    private void parseUntil(char[] delim) throws SAXException, IOException {
        char c;
        int startLine = line;

        try {
            while (!tryRead(delim)) {
                c = readCh();
                dataBufferAppend(c);
            }
        } catch (EOFException e) {
            fatal("end of input while looking for delimiter "
                    + "(started on line " + startLine + ')', null, new String(
                    delim));
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Low-level I/O.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Prefetch US-ASCII XML/text decl from input stream into read buffer.
     * Doesn't buffer more than absolutely needed, so that when an encoding decl
     * says we need to create an InputStreamReader, we can discard our buffer
     * and reset(). Caller knows the first chars of the decl exist in the input
     * stream.
     */
    private void prefetchASCIIEncodingDecl() throws SAXException, IOException {
        int ch;
        readBufferPos = readBufferLength = 0;

        is.mark(readBuffer.length);
        while (true) {
            ch = is.read();
            readBuffer[readBufferLength++] = (char) ch;
            switch (ch) {
                case '>':
                    return;
                case -1:
                    fatal(
                            "file ends before end of XML or encoding declaration.",
                            null, "?>");
            }
            if (readBuffer.length == readBufferLength) {
                fatal("unfinished XML or encoding declaration");
            }
        }
    }

    /**
     * Read a chunk of data from an external input source.
     * <p>
     * This is simply a front-end that fills the rawReadBuffer with bytes, then
     * calls the appropriate encoding handler.
     * 
     * @see #characterEncoding
     * @see #rawReadBuffer
     * @see #readBuffer
     * @see #filterCR
     * @see #copyUtf8ReadBuffer
     * @see #copyIso8859_1ReadBuffer
     * @see #copyUcs_2ReadBuffer
     * @see #copyUcs_4ReadBuffer
     */
    private void readDataChunk() throws SAXException, IOException {
        int count;

        // See if we have any overflow (filterCR sets for CR at end)
        if (readBufferOverflow > -1) {
            readBuffer[0] = (char) readBufferOverflow;
            readBufferOverflow = -1;
            readBufferPos = 1;
        } else {
            readBufferPos = 0;
        }

        try {
            count = reader.read(readBuffer, readBufferPos, READ_BUFFER_MAX
                    - readBufferPos);
        } catch (CharacterCodingException cce) {
            // 2006-04-25 hsivonen
            fatal("Input data does not conform to the input encoding. The input encoding was "
                    + characterEncoding + ".");
            return; // never happens
        }
        if ((characterHandler != null) && (count > 0)) {
            characterHandler.characters(readBuffer, readBufferPos, count);
        }
        if ((normalizationChecker != null) && (count > 0)) {
            normalizationChecker.characters(readBuffer, readBufferPos, count);
        }
        if (count < 0) {
            readBufferLength = readBufferPos;
        } else {
            readBufferLength = readBufferPos + count;
        }
        if (readBufferLength > 0) {
            filterCR(count >= 0);
        }
    }

    /**
     * Filter carriage returns in the read buffer. CRLF becomes LF; CR becomes
     * LF.
     * 
     * @param moreData
     *            true iff more data might come from the same source
     * @see #readDataChunk
     * @see #readBuffer
     * @see #readBufferOverflow
     */
    private void filterCR(boolean moreData) {
        int i, j;

        readBufferOverflow = -1;

        loop: for (i = j = readBufferPos; j < readBufferLength; i++, j++) {
            switch (readBuffer[j]) {
                case '\r':
                    if (j == readBufferLength - 1) {
                        if (moreData) {
                            readBufferOverflow = '\r';
                            readBufferLength--;
                        } else // CR at end of buffer
                        {
                            readBuffer[i++] = '\n';
                        }
                        break loop;
                    } else if (readBuffer[j + 1] == '\n') {
                        j++;
                    }
                    readBuffer[i] = '\n';
                    break;

                case '\n':
                default:
                    readBuffer[i] = readBuffer[j];
                    break;
            }
        }
        readBufferLength = i;
    }

    private void warnAboutPrivateUseChar() throws SAXException {
        if (!alreadyWarnedAboutPrivateUseCharacters) {
            handler.warn("Document uses the Unicode Private Use Area(s), which should not be used in publicly exchanged documents. (Charmod C073)");
            alreadyWarnedAboutPrivateUseCharacters = true;
        }
    }

    // copied from fi.iki.hsivonen.htmlparser

    private boolean isPrivateUse(char c) {
        return (c >= '\uE000') && (c <= '\uF8FF');
    }

    private boolean isPrivateUse(int c) {
        return ((c >= 0xE000) && (c <= 0xF8FF)) || ((c >= 0xF0000) && (c <= 0xFFFFD))
                || ((c >= 0x100000) && (c <= 0x10FFFD));
    }

    private boolean isAstralPrivateUse(int c) {
        return ((c >= 0xF0000) && (c <= 0xFFFFD))
                || ((c >= 0x100000) && (c <= 0x10FFFD));
    }

    private boolean isNonCharacter(int c) {
        return (c & 0xFFFE) == 0xFFFE;
    }

    // ////////////////////////////////////////////////////////////////////
    // Local Variables.
    // ////////////////////////////////////////////////////////////////////

    /**
     * Re-initialize the variables for each parse.
     * 
     * @throws SAXException
     */
    private void initializeVariables() throws SAXException {
        prev = '\u0000';
        // First line
        line = 0;
        column = 1;
        linePrev = 0;
        columnPrev = 1;
        nextCharOnNewLine = true;

        // Set up the buffers for data and names
        dataBufferPos = 0;
        dataBuffer = new char[DATA_BUFFER_INITIAL];
        nameBufferPos = 0;
        nameBuffer = new char[NAME_BUFFER_INITIAL];

        // Set up the DTD hash tables
        elementInfo = new HashMap<>();
        entityInfo = new HashMap<>();
        notationInfo = new HashMap<>();
        skippedPE = false;

        // Set up the variables for the current
        // element context.
        currentElement = null;
        currentElementContent = CONTENT_UNDECLARED;

        // Set up the input variables
        sourceType = INPUT_NONE;
        inputStack = new LinkedList<>();
        entityStack = new LinkedList<>();
        tagAttributePos = 0;
        tagAttributes = new String[100];
        //rawReadBuffer = new byte[READ_BUFFER_MAX];
        readBufferOverflow = -1;

        inLiteral = false;
        expandPE = false;
        peIsError = false;

        doReport = false;

        inCDATA = false;

        symbolTable = new Object[SYMBOL_TABLE_LENGTH][];

        if (handler.checkNormalization) {
            normalizationChecker = new NormalizationChecker(handler);
            normalizationChecker.setErrorHandler(handler.getErrorHandler());
            normalizationChecker.start();
        } else {
            normalizationChecker = null;
        }
        if (handler.characterHandler != null) {
            characterHandler = handler.characterHandler;
            handler.characterHandler = null;
            characterHandler.start();
        } else {
            characterHandler = null;
        }
    }

    static class ExternalIdentifiers {

        String publicId;

        String systemId;

        String baseUri;

        ExternalIdentifiers() {
        }

        ExternalIdentifiers(String publicId, String systemId, String baseUri) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.baseUri = baseUri;
        }

    }

    static class EntityInfo {

        int type;

        ExternalIdentifiers ids;

        String value;

        String notationName;

    }

    static class AttributeDecl {

        String type;

        String value;

        int valueType;

        String enumeration;

        String defaultValue;

    }

    static class ElementDecl {

        int contentType;

        String contentModel;

        HashMap<String, AttributeDecl> attributes;

    }

    static class Input {
        CharacterHandler characterHandler;

        boolean nextCharOnNewLine;

        int columnPrev;

        int linePrev;

        char prev;

        int sourceType;

        char[] readBuffer;

        int readBufferPos;

        int readBufferLength;

        int line;

        String characterEncoding;

        int readBufferOverflow;

        InputStream is;

        int currentByteCount;

        int column;

        Reader reader;

        NormalizationChecker normalizationChecker;
    }

    public String getEncoding() {
        return characterEncoding;
    }

}
