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
import nu.validator.servlet.InfoErrorHandler;
import nu.validator.source.Location;
import nu.validator.source.SourceCode;
import nu.validator.source.SourceHandler;
import nu.validator.xml.CharacterUtil;

import org.whattf.checker.NormalizationChecker;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.icu.text.Normalizer;


public final class MessageEmitterAdapter implements InfoErrorHandler {

    private int warnings = 0;

    private int errors = 0;

    private int fatalErrors = 0;

    private final SourceCode sourceCode;
    
    private final MessageEmitter emitter;
    
    private final ExactErrorHandler exactErrorHandler;
    
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

    public MessageEmitterAdapter(SourceCode sourceCode, MessageEmitter messageEmitter) {
        super();
        this.sourceCode = sourceCode;
        this.emitter = messageEmitter;
        this.exactErrorHandler = new ExactErrorHandler(this);
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

    public boolean isErrors() {
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
    private void warning(SAXParseException e, boolean exact) throws SAXException {
        if (fatalErrors > 0) {
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
        if (fatalErrors > 0) {
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
    private void fatalError(SAXParseException e, boolean exact) throws SAXException {
        if (fatalErrors > 0) {
            return;
        }
        this.fatalErrors++;
        Exception wrapped = e.getException();
        if (wrapped instanceof IOException) {
            message(MessageType.IO, ((IOException) wrapped).getMessage(), null, -1, -1 , false);
        } else {
            messageFromSAXParseException(MessageType.FATAL, e, exact);
        }
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#info(java.lang.String)
     */
    public void info(String str) throws SAXException {
        message(MessageType.INFO, str, null, -1, -1 , false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#ioError(java.io.IOException)
     */
    public void ioError(IOException e) throws SAXException {
        this.fatalErrors++;
        message(MessageType.IO, e.getMessage(), null, -1, -1 , false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#internalError(java.lang.Throwable,
     *      java.lang.String)
     */
    public void internalError(Throwable e, String message) throws SAXException {
        this.fatalErrors++;
        message(MessageType.INTERNAL, message, null, -1, -1 , false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#schemaError(java.lang.Exception)
     */
    public void schemaError(Exception e) throws SAXException {
        this.fatalErrors++;
        message(MessageType.SCHEMA, e.getMessage(), null, -1, -1 , false);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#start()
     */
    public void start(String documentUri) throws SAXException {
        emitter.startMessages(scrub(documentUri));
    }
    
    /**
     * @see nu.validator.servlet.InfoErrorHandler#end()
     */
    public void end(String successMessage, String failureMessage)
            throws SAXException {
        // XXX figure out API here
        SourceHandler sourceHandler = emitter.startFullSource();
        if (sourceHandler != null) {
            sourceCode.emitSource(sourceHandler);
        }
        emitter.endFullSource();
        emitter.endMessages();
    }

    private void messageFromSAXParseException(MessageType type, SAXParseException spe, boolean exact) throws SAXException {
        message(type, spe.getMessage(), spe.getSystemId(), spe.getLineNumber(), spe.getColumnNumber(), exact);
    }
    
    private void message(MessageType type, String message, String systemId, int oneBasedLine, int oneBasedColumn, boolean exact) throws SAXException {
        String uri = sourceCode.getUri();
        if (oneBasedLine > -1 && (uri == systemId || (uri != null && uri.equals(systemId)))) {
            if (oneBasedColumn > -1) {
                if (exact) {
                    messageWithExact(type, message, oneBasedLine, oneBasedColumn);
                } else {
                    messageWithRange(type, message, oneBasedLine, oneBasedColumn);
                }
            } else {
                messageWithLine(type, message, oneBasedLine);
            }
        } else {
            messageWithoutExtract(type, message, systemId, oneBasedLine, oneBasedColumn);   
        }
    }

    private void messageWithRange(MessageType type, String message, int oneBasedLine, int oneBasedColumn) throws SAXException {
        Location rangeLast = sourceCode.newLocatorLocation(oneBasedLine, oneBasedColumn);
        if (!sourceCode.isWithinKnownSource(rangeLast)) {
            messageWithoutExtract(type, message, null, oneBasedLine, oneBasedColumn);
            return;
        }
        Location rangeStart = sourceCode.rangeStartForRangeLast(rangeLast);
        emitter.startMessage(type, null, rangeStart.getLine() + 1, rangeStart.getColumn() + 1, oneBasedLine, oneBasedColumn, false);
        messageText(message);
        SourceHandler sourceHandler = emitter.startSource();
        if (sourceHandler != null) {
            sourceCode.rangeEndError(rangeStart, rangeLast, sourceHandler);
        }
        emitter.endSource();
        // XXX elaboration
        emitter.endMessage();
    }

    private void messageWithExact(MessageType type, String message, int oneBasedLine, int oneBasedColumn) throws SAXException {
        emitter.startMessage(type, null, oneBasedLine, oneBasedColumn, oneBasedLine, oneBasedColumn, true);
        messageText(message);
        Location location = sourceCode.newLocatorLocation(oneBasedLine, oneBasedColumn);
        if (sourceCode.isWithinKnownSource(location)) {
            SourceHandler sourceHandler = emitter.startSource();
            if (sourceHandler != null) {
                sourceCode.exactError(location, sourceHandler);
            }
            emitter.endSource();
        }
        // XXX elaboration
        emitter.endMessage();
    }

    private void messageWithLine(MessageType type, String message, int oneBasedLine) throws SAXException {
        if (!sourceCode.isWithinKnownSource(oneBasedLine)) {
            throw new RuntimeException("Bug. Line out of range!");
        }
        emitter.startMessage(type, null, oneBasedLine, -1, oneBasedLine, -1, false);
        messageText(message);
        SourceHandler sourceHandler = emitter.startSource();
        if (sourceHandler != null) {
            sourceCode.lineError(oneBasedLine, sourceHandler);
        }
        emitter.endSource();
        // XXX elaboration
        emitter.endMessage();
    }

    private void messageWithoutExtract(MessageType type, String message, String systemId, int oneBasedLine, int oneBasedColumn) throws SAXException {
        emitter.startMessage(type, scrub(systemId), oneBasedLine, oneBasedColumn, oneBasedLine, oneBasedColumn, false);
        messageText(message);
        // XXX elaboration
        emitter.endMessage();
    }

    /**
     * @param message
     * @throws SAXException
     */
    private void messageText(String message) throws SAXException {
        MessageTextHandler messageTextHandler = emitter.startText();
        if (messageTextHandler != null) {
            emitStringWithQurlyQuotes(message, messageTextHandler);
        }
        emitter.endText();
    }
    
    private void emitStringWithQurlyQuotes(String message, MessageTextHandler messageTextHandler) throws SAXException {
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
}