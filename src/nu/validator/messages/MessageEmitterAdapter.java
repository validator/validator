/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
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

package nu.validator.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nu.validator.messages.types.MessageType;
import nu.validator.relaxng.exceptions.AbstractValidationException;
import nu.validator.relaxng.exceptions.BadAttributeValueException;
import nu.validator.relaxng.exceptions.ImpossibleAttributeIgnoredException;
import nu.validator.relaxng.exceptions.OnlyTextNotAllowedException;
import nu.validator.relaxng.exceptions.OutOfContextElementException;
import nu.validator.relaxng.exceptions.RequiredAttributesMissingException;
import nu.validator.relaxng.exceptions.RequiredElementsMissingException;
import nu.validator.relaxng.exceptions.StringNotAllowedException;
import nu.validator.relaxng.exceptions.TextNotAllowedException;
import nu.validator.relaxng.exceptions.UnfinishedElementException;
import nu.validator.relaxng.exceptions.UnknownElementException;
import nu.validator.saxtree.DocumentFragment;
import nu.validator.saxtree.TreeParser;
import nu.validator.source.Location;
import nu.validator.source.SourceCode;
import nu.validator.source.SourceHandler;
import nu.validator.spec.EmptySpec;
import nu.validator.spec.Spec;
import nu.validator.xml.AttributesImpl;
import nu.validator.xml.CharacterUtil;
import nu.validator.xml.EmptyAttributes;
import nu.validator.xml.XhtmlSaxEmitter;

import org.apache.log4j.Logger;
import org.relaxng.datatype.DatatypeException;
import org.whattf.checker.NormalizationChecker;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.icu.text.Normalizer;
import com.thaiopensource.xml.util.Name;

public final class MessageEmitterAdapter implements ErrorHandler {

    private static final Logger log4j = Logger.getLogger(MessageEmitterAdapter.class);
    
    private final static Map<String, char[]> WELL_KNOWN_NAMESPACES = new HashMap<String, char[]>();
    
    static {
        WELL_KNOWN_NAMESPACES.put("", "unnamespaced".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/xhtml", "XHTML".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2000/svg", "SVG".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1998/Math/MathML", "MathML".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2005/Atom", "Atom".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/xlink", "XLink".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://docbook.org/ns/docbook", "DocBook".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://relaxng.org/ns/structure/1.0", "RELAX NG".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/XML/1998/namespace", "XML".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/XSL/Transform", "XSLT".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/ns/xbl", "XBL".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul", "XUL".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://purl.org/dc/elements/1.1/", "Dublin Core".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2001/XMLSchema-instance", "XML Schema Instance".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.w3.org/2002/06/xhtml2/", "XHTML2".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://www.ascc.net/xml/schematron", "Schematron".toCharArray());
        WELL_KNOWN_NAMESPACES.put("http://purl.oclc.org/dsdl/schematron", "ISO Schematron".toCharArray());
    }
    
    private final static char[] INDETERMINATE_MESSAGE = "The result cannot be determined due to a non-document-error.".toCharArray();

    private final static char[] ELEMENT_SPECIFIC_ATTRIBUTES_BEFORE = "Element-specific attributes for element ".toCharArray();

    private final static char[] ELEMENT_SPECIFIC_ATTRIBUTES_AFTER = ":".toCharArray();

    private final static char[] CONTENT_MODEL_BEFORE = "Content model for element ".toCharArray();
    
    private final static char[] CONTENT_MODEL_AFTER = ":".toCharArray();

    private final static char[] CONTEXT_BEFORE = "Contexts in which element ".toCharArray();
    
    private final static char[] CONTEXT_AFTER = " may be used:".toCharArray();

    private final static char[] BAD_VALUE = "Bad value ".toCharArray();
    
    private final static char[] FOR = " for ".toCharArray();

    private final static char[] ATTRIBUTE = "attribute ".toCharArray();

    private final static char[] FROM_NAMESPACE = " from namespace ".toCharArray();
    
    private final static char[] SPACE = " ".toCharArray();
    
    private final static char[] ON = " on ".toCharArray();

    private final static char[] ELEMENT = "element ".toCharArray();
    
    private final static char[] PERIOD = ".".toCharArray();

    private final static char[] COLON = ":".toCharArray();

    private final static char[] NOT_ALLOWED_ON = " not allowed on ".toCharArray();
    
    private final static char[] AT_THIS_POINT = " at this point.".toCharArray();

    private final static char[] ONLY_TEXT = " is not allowed to have content that consists solely of text.".toCharArray();
    
    private final static char[] NOT_ALLOWED = " not allowed".toCharArray();

    private final static char[] AS_CHILD_OF = " as child of ".toCharArray();
    
    private final static char[] IN_THIS_CONTEXT_SUPPRESSING = " in this context. (Suppressing further error errors from this subtree.)".toCharArray();
    
    private final static char[] REQUIRED_ATTRIBUTES_MISSING = "Required attributes missing on ".toCharArray();

    private final static char[] REQUIRED_ELEMENTS_MISSING = "Required elements missing.".toCharArray();
    
    private final static char[] REQUIRED_CHILDREN_MISSING_FROM = "Required children missing from ".toCharArray();
    
    private final static char[] BAD_CHARACTER_CONTENT = "Bad character content ".toCharArray();

    private final static char[] IN_THIS_CONTEXT = " in this context.".toCharArray();
    
    private final static char[] TEXT_NOT_ALLOWED_IN = "Text not allowed in ".toCharArray();
    
    private final static char[] UNKNOWN = "Unknown ".toCharArray();
    
    private final AttributesImpl attributesImpl = new AttributesImpl();
    
    private final char[] oneChar = {'\u0000'};

    private int warnings = 0;

    private int errors = 0;

    private int fatalErrors = 0;

    private int nonDocumentErrors = 0;

    private final SourceCode sourceCode;

    private final MessageEmitter emitter;

    private final ExactErrorHandler exactErrorHandler;

    private final boolean showSource;

    private Spec spec = EmptySpec.THE_INSTANCE;
    
    private boolean html = false;

    protected static String scrub(String s) throws SAXException {
        if (s == null) {
            return null;
        }
        s = CharacterUtil.prudentlyScrubCharacterData(s);
        if (NormalizationChecker.startsWithComposingChar(s)) {
            s = " " + s;
        }
        return Normalizer.normalize(s, Normalizer.NFC, 0);
    }

    public MessageEmitterAdapter(SourceCode sourceCode, boolean showSource,
            MessageEmitter messageEmitter) {
        super();
        this.sourceCode = sourceCode;
        this.emitter = messageEmitter;
        this.exactErrorHandler = new ExactErrorHandler(this);
        this.showSource = showSource;
    }

    /**
     * @return Returns the errors.
     */
    public int getErrors() {
        return errors;
    }

    /**
     * @return Returns the fatalErrors.
     */
    public int getFatalErrors() {
        return fatalErrors;
    }

    /**
     * @return Returns the warnings.
     */
    public int getWarnings() {
        return warnings;
    }

    private boolean isErrors() {
        return !(errors == 0 && fatalErrors == 0);
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e) throws SAXException {
        warning(e, false);
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void warning(SAXParseException e, boolean exact)
            throws SAXException {
        if (fatalErrors > 0 || nonDocumentErrors > 0) {
            return;
        }
        this.warnings++;
        messageFromSAXParseException(MessageType.WARNING, e, exact);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        error(e, false);
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void error(SAXParseException e, boolean exact) throws SAXException {
        if (fatalErrors > 0 || nonDocumentErrors > 0) {
            return;
        }
        this.errors++;
        messageFromSAXParseException(MessageType.ERROR, e, exact);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        fatalError(e, false);
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void fatalError(SAXParseException e, boolean exact)
            throws SAXException {
        if (fatalErrors > 0 || nonDocumentErrors > 0) {
            return;
        }
        this.fatalErrors++;
        Exception wrapped = e.getException();
        if (wrapped instanceof IOException) {
            message(MessageType.IO, wrapped, null, -1, -1, false);
        } else {
            messageFromSAXParseException(MessageType.FATAL, e, exact);
        }
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#info(java.lang.String)
     */
    public void info(String str) throws SAXException {
        message(MessageType.INFO, new Exception(str), null, -1, -1, false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#ioError(java.io.IOException)
     */
    public void ioError(IOException e) throws SAXException {
        this.nonDocumentErrors++;
        message(MessageType.IO, e, null, -1, -1, false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#internalError(java.lang.Throwable,
     *      java.lang.String)
     */
    public void internalError(Throwable e, String message) throws SAXException {
        this.nonDocumentErrors++;
        message(MessageType.INTERNAL, new Exception(message), null, -1, -1,
                false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#schemaError(java.lang.Exception)
     */
    public void schemaError(Exception e) throws SAXException {
        this.nonDocumentErrors++;
        message(MessageType.SCHEMA, e, null, -1, -1, false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#start()
     */
    public void start(String documentUri) throws SAXException {
        emitter.startMessages(scrub(documentUri), showSource);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#end()
     */
    public void end(String successMessage, String failureMessage)
            throws SAXException {
        ResultHandler resultHandler = emitter.startResult();
        if (resultHandler != null) {
            if (isIndeterminate()) {
                resultHandler.startResult(Result.INDETERMINATE);
                resultHandler.characters(INDETERMINATE_MESSAGE, 0,
                        INDETERMINATE_MESSAGE.length);
                resultHandler.endResult();
            } else if (isErrors()) {
                resultHandler.startResult(Result.FAILURE);
                resultHandler.characters(failureMessage.toCharArray(), 0,
                        failureMessage.length());
                resultHandler.endResult();
            } else {
                resultHandler.startResult(Result.SUCCESS);
                resultHandler.characters(successMessage.toCharArray(), 0,
                        successMessage.length());
                resultHandler.endResult();
            }
        }
        emitter.endResult();

        if (showSource) {
            SourceHandler sourceHandler = emitter.startFullSource();
            if (sourceHandler != null) {
                sourceCode.emitSource(sourceHandler);
            }
            emitter.endFullSource();
        }
        emitter.endMessages();
    }

    private boolean isIndeterminate() {
        return nonDocumentErrors > 0;
    }

    private void messageFromSAXParseException(MessageType type,
            SAXParseException spe, boolean exact) throws SAXException {
        message(type, spe, spe.getSystemId(), spe.getLineNumber(),
                spe.getColumnNumber(), exact);
    }

    private void message(MessageType type, Exception message, String systemId,
            int oneBasedLine, int oneBasedColumn, boolean exact)
            throws SAXException {
        log4j.info(new StringBuilder().append(systemId).append('\t').append(message.getMessage()));
        String uri = sourceCode.getUri();
        if (oneBasedLine > -1
                && (uri == systemId || (uri != null && uri.equals(systemId)))) {
            if (oneBasedColumn > -1) {
                if (exact) {
                    messageWithExact(type, message, oneBasedLine,
                            oneBasedColumn);
                } else {
                    messageWithRange(type, message, oneBasedLine,
                            oneBasedColumn);
                }
            } else {
                messageWithLine(type, message, oneBasedLine);
            }
        } else {
            messageWithoutExtract(type, message, systemId, oneBasedLine,
                    oneBasedColumn);
        }
    }

    private void messageWithRange(MessageType type, Exception message,
            int oneBasedLine, int oneBasedColumn) throws SAXException {
        Location rangeLast = sourceCode.newLocatorLocation(oneBasedLine,
                oneBasedColumn);
        if (!sourceCode.isWithinKnownSource(rangeLast)) {
            messageWithoutExtract(type, message, null, oneBasedLine,
                    oneBasedColumn);
            return;
        }
        Location rangeStart = sourceCode.rangeStartForRangeLast(rangeLast);
        emitter.startMessage(type, null, rangeStart.getLine() + 1,
                rangeStart.getColumn() + 1, oneBasedLine, oneBasedColumn, false);
        messageText(message);
        SourceHandler sourceHandler = emitter.startSource();
        if (sourceHandler != null) {
            sourceCode.rangeEndError(rangeStart, rangeLast, sourceHandler);
        }
        emitter.endSource();
        elaboration(message);
        emitter.endMessage();
    }

    private void messageWithExact(MessageType type, Exception message,
            int oneBasedLine, int oneBasedColumn) throws SAXException {
        emitter.startMessage(type, null, oneBasedLine, oneBasedColumn,
                oneBasedLine, oneBasedColumn, true);
        messageText(message);
        Location location = sourceCode.newLocatorLocation(oneBasedLine,
                oneBasedColumn);
        if (sourceCode.isWithinKnownSource(location)) {
            SourceHandler sourceHandler = emitter.startSource();
            if (sourceHandler != null) {
                sourceCode.exactError(location, sourceHandler);
            }
            emitter.endSource();
        } else {
            sourceCode.rememberExactError(location);
        }
        elaboration(message);
        emitter.endMessage();
    }

    private void messageWithLine(MessageType type, Exception message,
            int oneBasedLine) throws SAXException {
        if (!sourceCode.isWithinKnownSource(oneBasedLine)) {
            throw new RuntimeException("Bug. Line out of range!");
        }
        emitter.startMessage(type, null, oneBasedLine, -1, oneBasedLine, -1,
                false);
        messageText(message);
        SourceHandler sourceHandler = emitter.startSource();
        if (sourceHandler != null) {
            sourceCode.lineError(oneBasedLine, sourceHandler);
        }
        emitter.endSource();
        elaboration(message);
        emitter.endMessage();
    }

    private void messageWithoutExtract(MessageType type, Exception message,
            String systemId, int oneBasedLine, int oneBasedColumn)
            throws SAXException {
        emitter.startMessage(type, scrub(systemId), oneBasedLine,
                oneBasedColumn, oneBasedLine, oneBasedColumn, false);
        messageText(message);
        elaboration(message);
        emitter.endMessage();
    }

    /**
     * @param message
     * @throws SAXException
     */
    private void messageText(Exception message) throws SAXException {
        if (message instanceof AbstractValidationException) {
            AbstractValidationException ave = (AbstractValidationException) message;
            rngMessageText(ave);
        } else {
            String msg = message.getMessage();
            if (msg != null) {
                MessageTextHandler messageTextHandler = emitter.startText();
                if (messageTextHandler != null) {
                    emitStringWithQurlyQuotes(messageTextHandler, msg);
                }
                emitter.endText();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void rngMessageText(AbstractValidationException e)
            throws SAXException {
        MessageTextHandler messageTextHandler = emitter.startText();
        if (messageTextHandler != null) {
            if (e instanceof BadAttributeValueException) {
                BadAttributeValueException ex = (BadAttributeValueException) e;
                messageTextString(messageTextHandler, BAD_VALUE, false);
                codeString(messageTextHandler, ex.getAttributeValue());
                messageTextString(messageTextHandler, FOR, false);
                attribute(messageTextHandler, ex.getAttributeName(), ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, ON, false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                emitDatatypeErrors(messageTextHandler, ex.getExceptions());
            } else if (e instanceof ImpossibleAttributeIgnoredException) {
                ImpossibleAttributeIgnoredException ex = (ImpossibleAttributeIgnoredException) e;
                attribute(messageTextHandler, ex.getAttributeName(), ex.getCurrentElement(), true);
                messageTextString(messageTextHandler, NOT_ALLOWED_ON, false);
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, AT_THIS_POINT, false);                
            } else if (e instanceof OnlyTextNotAllowedException) {
                OnlyTextNotAllowedException ex = (OnlyTextNotAllowedException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler, ONLY_TEXT, false);                                
            } else if (e instanceof OutOfContextElementException) {
                OutOfContextElementException ex = (OutOfContextElementException) e;
                element(messageTextHandler, ex.getCurrentElement(), true);
                messageTextString(messageTextHandler, NOT_ALLOWED, false);                                
                if (ex.getParent() != null) {
                    messageTextString(messageTextHandler, AS_CHILD_OF, false);                                
                    element(messageTextHandler, ex.getParent(), false);                    
                }
                messageTextString(messageTextHandler, IN_THIS_CONTEXT_SUPPRESSING, false);                                                
            } else if (e instanceof RequiredAttributesMissingException) {
                RequiredAttributesMissingException ex = (RequiredAttributesMissingException) e;
                messageTextString(messageTextHandler, REQUIRED_ATTRIBUTES_MISSING, false);                                                                
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, PERIOD, false);                                                                                
            } else if (e instanceof RequiredElementsMissingException) {
                RequiredElementsMissingException ex = (RequiredElementsMissingException) e;
                if (ex.getParent() == null) {
                    messageTextString(messageTextHandler, REQUIRED_ELEMENTS_MISSING, false);                                                                                                    
                } else {
                    messageTextString(messageTextHandler, REQUIRED_CHILDREN_MISSING_FROM, false);                                                                                                    
                    element(messageTextHandler, ex.getParent(), false);
                    messageTextString(messageTextHandler, PERIOD, false);                    
                }
            } else if (e instanceof StringNotAllowedException) {
                StringNotAllowedException ex = (StringNotAllowedException) e;
                messageTextString(messageTextHandler, BAD_CHARACTER_CONTENT, false);     
                codeString(messageTextHandler, ex.getValue());
                messageTextString(messageTextHandler, FOR, false);                     
                element(messageTextHandler, ex.getCurrentElement(), false);
                emitDatatypeErrors(messageTextHandler, ex.getExceptions());
            } else if (e instanceof TextNotAllowedException) {
                TextNotAllowedException ex = (TextNotAllowedException) e;
                messageTextString(messageTextHandler, TEXT_NOT_ALLOWED_IN, false);     
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, IN_THIS_CONTEXT, false);                     
            } else if (e instanceof UnfinishedElementException) {
                UnfinishedElementException ex = (UnfinishedElementException) e;
                messageTextString(messageTextHandler, REQUIRED_CHILDREN_MISSING_FROM, false);                                                                                                    
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, PERIOD, false);                                    
            } else if (e instanceof UnknownElementException) {
                UnknownElementException ex = (UnknownElementException) e;
                messageTextString(messageTextHandler, UNKNOWN, false);                                                
                element(messageTextHandler, ex.getCurrentElement(), false);
                messageTextString(messageTextHandler, NOT_ALLOWED, false);                                
                if (ex.getParent() != null) {
                    messageTextString(messageTextHandler, AS_CHILD_OF, false);                                
                    element(messageTextHandler, ex.getParent(), false);                    
                }
                messageTextString(messageTextHandler, PERIOD, false);                                                    
            }
        }
        emitter.endText();
    }

    /**
     * @param messageTextHandler
     * @param datatypeErrors
     * @throws SAXException
     */
    private void emitDatatypeErrors(MessageTextHandler messageTextHandler, Map<String, DatatypeException> datatypeErrors) throws SAXException {
        if (datatypeErrors.isEmpty()) {
            messageTextString(messageTextHandler, PERIOD, false);                    
        } else {
            messageTextString(messageTextHandler, COLON, false);                    
            for (String err : datatypeErrors.keySet()) {
                messageTextString(messageTextHandler, SPACE, false);
                emitStringWithQurlyQuotes(messageTextHandler, err);
            }
        }
    }

    private void element(MessageTextHandler messageTextHandler, Name element, boolean atSentenceStart) throws SAXException {
        if (html) {
            messageTextString(messageTextHandler, ELEMENT, atSentenceStart);
            linkedCodeString(messageTextHandler, element.getLocalName(), spec.elementLink(element));
        } else {
            String ns = element.getNamespaceUri();
            char[] humanReadable = WELL_KNOWN_NAMESPACES.get(ns);
            if (humanReadable == null) {
                messageTextString(messageTextHandler, ELEMENT, atSentenceStart);
                linkedCodeString(messageTextHandler, element.getLocalName(), spec.elementLink(element));
                messageTextString(messageTextHandler, FROM_NAMESPACE, false);
                codeString(messageTextHandler, ns);
            } else {
                messageTextString(messageTextHandler, humanReadable, atSentenceStart);
                messageTextString(messageTextHandler, SPACE, false);
                messageTextString(messageTextHandler, ELEMENT, false);
                linkedCodeString(messageTextHandler, element.getLocalName(), spec.elementLink(element));                
            }
        }
    }

    private void linkedCodeString(MessageTextHandler messageTextHandler, String str, String url) throws SAXException {
        if (url != null) {
            messageTextHandler.startLink(url, null);
        }
        codeString(messageTextHandler, str);
        if (url != null) {
            messageTextHandler.endLink();
        }

    }

    private void attribute(MessageTextHandler messageTextHandler, Name attributeName, Name elementName, boolean atSentenceStart) throws SAXException {
        String ns = attributeName.getNamespaceUri();
        if (html || "".equals(ns)) {
            messageTextString(messageTextHandler, ATTRIBUTE, atSentenceStart);
            codeString(messageTextHandler, attributeName.getLocalName());
        } else if ("http://www.w3.org/XML/1998/namespace".equals(ns)) {
            messageTextString(messageTextHandler, ATTRIBUTE, atSentenceStart);
            codeString(messageTextHandler, "xml:" + attributeName.getLocalName());            
        } else {
            char[] humanReadable = WELL_KNOWN_NAMESPACES.get(ns);
            if (humanReadable == null) {
                log4j.info(new StringBuilder().append("UNKNOWN_NS:\t").append(ns));
                messageTextString(messageTextHandler, ATTRIBUTE, atSentenceStart);
                codeString(messageTextHandler, attributeName.getLocalName());
                messageTextString(messageTextHandler, FROM_NAMESPACE, false);
                codeString(messageTextHandler, ns);                
            } else {
                messageTextString(messageTextHandler, humanReadable, atSentenceStart);
                messageTextString(messageTextHandler, SPACE, false);
                messageTextString(messageTextHandler, ATTRIBUTE, false);
                codeString(messageTextHandler, attributeName.getLocalName());
            }
        }
    }

    private void codeString(MessageTextHandler messageTextHandler, String str) throws SAXException {
        messageTextHandler.startCode();
        messageTextHandler.characters(str.toCharArray(), 0, str.length());
        messageTextHandler.endCode();
    }

    private void messageTextString(MessageTextHandler messageTextHandler, char[] ch, boolean capitalize) throws SAXException {
        if (capitalize && ch[0] >= 'a' && ch[0] <= 'z') {
            oneChar[0] = (char) (ch[0] - 0x20);
            messageTextHandler.characters(oneChar, 0, 1);
            if (ch.length > 1) {
                messageTextHandler.characters(ch, 1, ch.length - 1);                
            }
        } else {
            messageTextHandler.characters(ch, 0, ch.length);
        }
    }

    private void emitStringWithQurlyQuotes(MessageTextHandler messageTextHandler,
            String message) throws SAXException {
        if (message == null) {
            message = "";
        }
        message = scrub(message);
        int len = message.length();
        int start = 0;
        int startQuotes = 0;
        for (int i = 0; i < len; i++) {
            char c = message.charAt(i);
            if (c == '\u201C') {
                startQuotes++;
                if (startQuotes == 1) {
                    char[] scrubbed = scrub(message.substring(start, i)).toCharArray();
                    messageTextHandler.characters(scrubbed, 0, scrubbed.length);
                    start = i + 1;
                    messageTextHandler.startCode();
                }
            } else if (c == '\u201D' && startQuotes > 0) {
                startQuotes--;
                if (startQuotes == 0) {
                    char[] scrubbed = scrub(message.substring(start, i)).toCharArray();
                    messageTextHandler.characters(scrubbed, 0, scrubbed.length);
                    start = i + 1;
                    messageTextHandler.endCode();
                }
            }
        }
        if (start < len) {
            char[] scrubbed = scrub(message.substring(start, len)).toCharArray();
            messageTextHandler.characters(scrubbed, 0, scrubbed.length);
        }
        if (startQuotes > 0) {
            messageTextHandler.endCode();
        }
    }

    private void elaboration(Exception e) throws SAXException {
        if (!(e instanceof AbstractValidationException)) {
            return;
        }

        if (e instanceof ImpossibleAttributeIgnoredException) {
            ImpossibleAttributeIgnoredException ex = (ImpossibleAttributeIgnoredException) e;
            Name elt = ex.getCurrentElement();
            elaborateElementSpecificAttributes(elt);
        } else if (e instanceof OnlyTextNotAllowedException) {
            OnlyTextNotAllowedException ex = (OnlyTextNotAllowedException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof OutOfContextElementException) {
            OutOfContextElementException ex = (OutOfContextElementException) e;
            Name parent = ex.getParent();
            Name child = ex.getCurrentElement();
            elaborateContentModelandContext(parent, child);
        } else if (e instanceof RequiredAttributesMissingException) {
            RequiredAttributesMissingException ex = (RequiredAttributesMissingException) e;
            Name elt = ex.getCurrentElement();
            elaborateElementSpecificAttributes(elt);
        } else if (e instanceof RequiredElementsMissingException) {
            RequiredElementsMissingException ex = (RequiredElementsMissingException) e;
            Name elt = ex.getParent();
            elaborateContentModel(elt);
        } else if (e instanceof StringNotAllowedException) {
            StringNotAllowedException ex = (StringNotAllowedException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof TextNotAllowedException) {
            TextNotAllowedException ex = (TextNotAllowedException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof UnfinishedElementException) {
            UnfinishedElementException ex = (UnfinishedElementException) e;
            Name elt = ex.getCurrentElement();
            elaborateContentModel(elt);
        } else if (e instanceof UnknownElementException) {
            UnknownElementException ex = (UnknownElementException) e;
            Name elt = ex.getParent();
            elaborateContentModel(elt);
        }
    }

    /**
     * @param elt
     * @throws SAXException
     */
    private void elaborateContentModel(Name elt) throws SAXException {
        DocumentFragment dds = spec.contentModelDescription(elt);
        if (dds != null) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                TreeParser treeParser = new TreeParser(ch, null);
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                xhtmlSaxEmitter.startElement("dl");
                emitContentModelDt(xhtmlSaxEmitter, elt);
                treeParser.parse(dds);
                xhtmlSaxEmitter.endElement("dl");
            }
            emitter.endElaboration();
        }
    }

    private void elaborateContentModelandContext(Name parent, Name child) throws SAXException {
        DocumentFragment contentModelDds = spec.contentModelDescription(parent);
        DocumentFragment contextDds = spec.contentModelDescription(child);
        if (contentModelDds != null && contextDds != null) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                TreeParser treeParser = new TreeParser(ch, null);
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                xhtmlSaxEmitter.startElement("dl");
                emitContextDt(xhtmlSaxEmitter, child);
                treeParser.parse(contextDds);
                emitContentModelDt(xhtmlSaxEmitter, parent);
                treeParser.parse(contentModelDds);
                xhtmlSaxEmitter.endElement("dl");
            }
            emitter.endElaboration();
        }
    }
    
    /**
     * @param elt
     * @throws SAXException
     */
    private void elaborateElementSpecificAttributes(Name elt) throws SAXException {
        DocumentFragment dds = spec.elementSpecificAttributesDescription(elt);
        if (dds != null) {
            ContentHandler ch = emitter.startElaboration();
            if (ch != null) {
                TreeParser treeParser = new TreeParser(ch, null);
                XhtmlSaxEmitter xhtmlSaxEmitter = new XhtmlSaxEmitter(ch);
                xhtmlSaxEmitter.startElement("dl");
                emitElementSpecificAttributesDt(xhtmlSaxEmitter, elt);
                treeParser.parse(dds);
                xhtmlSaxEmitter.endElement("dl");
            }
            emitter.endElaboration();
        }
    }

    private void emitElementSpecificAttributesDt(XhtmlSaxEmitter xhtmlSaxEmitter, Name elt) throws SAXException {
        xhtmlSaxEmitter.startElement("dt");
        xhtmlSaxEmitter.characters(ELEMENT_SPECIFIC_ATTRIBUTES_BEFORE);
        emitLinkifiedLocalName(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.characters(ELEMENT_SPECIFIC_ATTRIBUTES_AFTER);
        xhtmlSaxEmitter.endElement("dt");
    }

    private void emitContextDt(XhtmlSaxEmitter xhtmlSaxEmitter, Name elt) throws SAXException {
        xhtmlSaxEmitter.startElement("dt");
        xhtmlSaxEmitter.characters(CONTEXT_BEFORE);
        emitLinkifiedLocalName(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.characters(CONTEXT_AFTER);
        xhtmlSaxEmitter.endElement("dt");
    }
    
    private void emitContentModelDt(
            XhtmlSaxEmitter xhtmlSaxEmitter, Name elt) throws SAXException {
        xhtmlSaxEmitter.startElement("dt");
        xhtmlSaxEmitter.characters(CONTENT_MODEL_BEFORE);
        emitLinkifiedLocalName(xhtmlSaxEmitter, elt);
        xhtmlSaxEmitter.characters(CONTENT_MODEL_AFTER);
        xhtmlSaxEmitter.endElement("dt");
    }

    private void emitLinkifiedLocalName(XhtmlSaxEmitter xhtmlSaxEmitter, Name elt) throws SAXException {
        String url = spec.elementLink(elt);
        if (url != null) {
            attributesImpl.clear();
            attributesImpl.addAttribute("href", url);
            xhtmlSaxEmitter.startElement("a", attributesImpl);
        }
        xhtmlSaxEmitter.startElement("code");
        xhtmlSaxEmitter.characters(elt.getLocalName());
        xhtmlSaxEmitter.endElement("code");
        if (url != null) {
            xhtmlSaxEmitter.endElement("a");
        }
    }

    private final class ExactErrorHandler implements ErrorHandler {

        private final MessageEmitterAdapter owner;

        /**
         * @param owner
         */
        ExactErrorHandler(final MessageEmitterAdapter owner) {
            this.owner = owner;
        }

        public void error(SAXParseException exception) throws SAXException {
            owner.error(exception, true);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            owner.fatalError(exception, true);
        }

        public void warning(SAXParseException exception) throws SAXException {
            owner.warning(exception, true);
        }

    }

    /**
     * Returns the exactErrorHandler.
     * 
     * @return the exactErrorHandler
     */
    public ErrorHandler getExactErrorHandler() {
        return exactErrorHandler;
    }

    /**
     * Sets the spec.
     * 
     * @param spec the spec to set
     */
    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    /**
     * Sets the html.
     * 
     * @param html the html to set
     */
    public void setHtml(boolean html) {
        this.html = html;
    }
}