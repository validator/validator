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

import nu.validator.messages.types.MessageType;
import nu.validator.relaxng.exceptions.AbstractValidationException;
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

import org.whattf.checker.NormalizationChecker;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.icu.text.Normalizer;
import com.thaiopensource.xml.util.Name;

public final class MessageEmitterAdapter implements ErrorHandler {

    private final static char[] INDETERMINATE_MESSAGE = "The result cannot be determined due to a non-document-error.".toCharArray();

    private final static char[] ELEMENT_SPECIFIC_ATTRIBUTES_BEFORE = "Element-specific attributes for element ".toCharArray();

    private final static char[] ELEMENT_SPECIFIC_ATTRIBUTES_AFTER = ":".toCharArray();

    private final static char[] CONTENT_MODEL_BEFORE = "Content model for element ".toCharArray();
    
    private final static char[] CONTENT_MODEL_AFTER = ":".toCharArray();

    private final static char[] CONTEXT_BEFORE = "Contexts in which element ".toCharArray();
    
    private final static char[] CONTEXT_AFTER = " may be used:".toCharArray();
    
    private final AttributesImpl attributesImpl = new AttributesImpl();

    private int warnings = 0;

    private int errors = 0;

    private int fatalErrors = 0;

    private int nonDocumentErrors = 0;

    private final SourceCode sourceCode;

    private final MessageEmitter emitter;

    private final ExactErrorHandler exactErrorHandler;

    private final boolean showSource;

    private Spec spec = EmptySpec.THE_INSTANCE;

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
        String msg = message.getMessage();
        if (msg != null) {
            MessageTextHandler messageTextHandler = emitter.startText();
            if (messageTextHandler != null) {
                emitStringWithQurlyQuotes(msg, messageTextHandler);
            }
            emitter.endText();
        }
    }

    private void emitStringWithQurlyQuotes(String message,
            MessageTextHandler messageTextHandler) throws SAXException {
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

    // if (e instanceof ImpossibleAttributeIgnoredException) {
    // ImpossibleAttributeIgnoredException ex =
    // (ImpossibleAttributeIgnoredException) e;
    //      
    // } else if (e instanceof OnlyTextNotAllowedException) {
    // OnlyTextNotAllowedException ex = (OnlyTextNotAllowedException) e;
    //        
    // } else if (e instanceof OutOfContextElementException) {
    // OutOfContextElementException ex = (OutOfContextElementException) e;
    //        
    // } else if (e instanceof RequiredAttributesMissingException) {
    // RequiredAttributesMissingException ex =
    // (RequiredAttributesMissingException) e;
    //        
    // } else if (e instanceof RequiredElementsMissingException) {
    // RequiredElementsMissingException ex = (RequiredElementsMissingException)
    // e;
    //        
    // } else if (e instanceof StringNotAllowedException) {
    // StringNotAllowedException ex = (StringNotAllowedException) e;
    //        
    // } else if (e instanceof TextNotAllowedException) {
    // TextNotAllowedException ex = (TextNotAllowedException) e;
    //        
    // } else if (e instanceof UnfinishedElementException) {
    // UnfinishedElementException ex = (UnfinishedElementException) e;
    //        
    // } else if (e instanceof UnknownElementException) {
    // UnknownElementException ex = (UnknownElementException) e;
    //        
    // }

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
}