/*
 * Copyright (c) 2005, 2006, 2007 Henri Sivonen
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

package fi.iki.hsivonen.verifierservlet;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.icu.text.Normalizer;

import fi.iki.hsivonen.xml.XhtmlSaxEmitter;
import org.whattf.checker.NormalizationChecker;
import fi.karppinen.xml.CharacterUtil;

/**
 * @version $Id$
 * @author hsivonen
 */
public class XhtmlEmittingErrorHandler implements InfoErrorHandler {

    private static final char[] INFO = "Info:".toCharArray();

    private static final char[] WARNING = "Warning:".toCharArray();

    private static final char[] ERROR = "Error:".toCharArray();

    private static final char[] FATAL_ERROR = "Fatal Error:".toCharArray();

    private static final char[] IO_ERROR = "IO Error:".toCharArray();

    private static final char[] INTERNAL_ERROR = "Internal Error:".toCharArray();

    private static final char[] SCHEMA_ERROR = "Schema Error:".toCharArray();

    private static final char[] SPACE = { ' ' };

    private static final char[] LINE = "Line ".toCharArray();

    private static final char[] COLUMN = ", column ".toCharArray();

    private static final char[] IN_RESOURCE = " in resource ".toCharArray();
    
    private XhtmlSaxEmitter emitter;

    private boolean listOpen = false;

    private int warnings = 0;

    private int errors = 0;

    private int fatalErrors = 0;

    private static String scrub(String s) throws SAXException {
        s = CharacterUtil.prudentlyScrubCharacterData(s);
        if (NormalizationChecker.startsWithComposingChar(s)) {
            s = " " + s;
        }
        return Normalizer.normalize(s, Normalizer.NFC, 0);
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

    /**
     * @param contentHandler
     */
    public XhtmlEmittingErrorHandler(ContentHandler contentHandler) {
        this.emitter = new XhtmlSaxEmitter(contentHandler);
    }

    private void maybeOpenList() throws SAXException {
        if (!this.listOpen) {
            this.emitter.startElement("ol");
            this.listOpen = true;
        }
    }
    
    private void emitMessage(String message) throws SAXException {
        int len = message.length();
        int start = 0;
        int startQuotes = 0;
        for (int i = 0; i < len; i++) {
            char c = message.charAt(i);
            if (c == '\u201C') {
                startQuotes++;
                if (startQuotes == 1) {
                    this.emitter.characters(scrub(message.substring(start, i)));
                    start = i + 1;
                    this.emitter.startElement("code");
                }
            } else if (c == '\u201D' && startQuotes > 0) {
                startQuotes--;
                if (startQuotes == 0) {
                    this.emitter.characters(scrub(message.substring(start, i)));
                    start = i + 1;
                    this.emitter.endElement("code");                    
                }
            }
        }
        if (start < len) {
            this.emitter.characters(scrub(message.substring(start, len)));            
        }
        if (startQuotes > 0) {
            this.emitter.endElement("code");                                
        }
    }

    private void emitErrorLevel(char[] level) throws SAXException {
        this.emitter.startElement("strong");
        this.emitter.characters(level);
        this.emitter.endElement("strong");
    }

    private void emitError(char[] level, SAXParseException e)
            throws SAXException {
        this.maybeOpenList();
        this.emitter.startElementWithClass("li", levelToClass(level));
        this.emitErrorMsg(level, e);
        this.emitErrorLocation(e);
        this.emitter.endElement("li");
    }

    private void emitError(char[] level, Exception e) throws SAXException {
        this.maybeOpenList();
        this.emitter.startElementWithClass("li", levelToClass(level));
        this.emitErrorMsg(level, e);
        this.emitter.endElement("li");
    }

    /**
     * @param e
     * @throws SAXException
     */
    private void emitErrorLocation(SAXParseException e) throws SAXException {
        int line = e.getLineNumber();
        String systemId = e.getSystemId();
        if (systemId == null) {
            return;
        }
        this.emitter.startElement("p");
        if (line > -1) {
            this.emitter.characters(LINE);
            this.emitter.characters("" + line);
            this.emitter.characters(COLUMN);
            this.emitter.characters("" + e.getColumnNumber());
            this.emitter.characters(IN_RESOURCE);
        }
        this.emitter.characters(scrub(systemId));
        this.emitter.endElement("p");
    }

    /**
     * @param level
     * @param e
     * @throws SAXException
     */
    private void emitErrorMsg(char[] level, Exception e) throws SAXException {
        this.emitter.startElement("p");
        this.emitErrorLevel(level);

        this.emitter.characters(SPACE);
        String msg = e.getMessage();
        if (msg == null) {
            msg = e.getClass().getName();
        }
        this.emitMessage(msg);
        this.emitter.endElement("p");
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e) throws SAXException {
        this.warnings++;
        this.emitError(WARNING, e);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        this.errors++;
        this.emitError(ERROR, e);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        this.fatalErrors++;
        if (e.getException() instanceof IOException) {
            this.emitError(IO_ERROR, e);
        } else {
            this.emitError(FATAL_ERROR, e);
        }
    }

    /**
     * @see fi.iki.hsivonen.verifierservlet.InfoErrorHandler#end()
     */
    public void end(String successMessage, String failureMessage) throws SAXException {
        if (this.listOpen) {
            this.emitter.endElement("ol");
            this.listOpen = false;
        }
        if (isErrors()) {
            emitter.startElementWithClass("p", "failure");
            emitter.characters(successMessage);
            emitter.endElement("p");
        } else {
            emitter.startElementWithClass("p", "success");
            emitter.characters(failureMessage);
            emitter.endElement("p");
        }
    }

    /**
     * @see fi.iki.hsivonen.verifierservlet.InfoErrorHandler#start()
     */
    public void start() throws SAXException {

    }

    /**
     * @see fi.iki.hsivonen.verifierservlet.InfoErrorHandler#info(java.lang.String)
     */
    public void info(String str) throws SAXException {
        this.maybeOpenList();
        this.emitter.startElementWithClass("li", "info");
        this.emitter.startElement("p");
        this.emitErrorLevel(INFO);
        this.emitter.characters(SPACE);
        this.emitMessage(str);
        this.emitter.endElement("p");
        this.emitter.endElement("li");
    }
    
    /**
     * @see fi.iki.hsivonen.verifierservlet.InfoErrorHandler#ioError(java.io.IOException)
     */
    public void ioError(IOException e) throws SAXException {
        this.fatalErrors++;
        this.emitError(IO_ERROR, e);
    }

    /**
     * @see fi.iki.hsivonen.verifierservlet.InfoErrorHandler#internalError(java.lang.Throwable, java.lang.String)
     */
    public void internalError(Throwable e, String message) throws SAXException {
        this.fatalErrors++;
        this.maybeOpenList();
        this.emitter.startElementWithClass("li", "internalerror");
        this.emitter.startElement("p");
        this.emitErrorLevel(INTERNAL_ERROR);
        this.emitter.characters(SPACE);
        this.emitMessage(message);
        this.emitter.endElement("p");
        this.emitter.endElement("li");
    }

    private String levelToClass(char[] level) {
        if (level == WARNING) {
            return "warning";
        } else if (level == ERROR) {
            return "error";
        } else if (level == FATAL_ERROR) {
            return "fatalerror";
        } else if (level == SCHEMA_ERROR) {
            return "schemaerror";
        } else {
            return "ioerror";
        }
    }

    /**
     * @see fi.iki.hsivonen.verifierservlet.InfoErrorHandler#schemaError(java.lang.Exception)
     */
    public void schemaError(Exception e) throws SAXException {
        this.fatalErrors++;
        this.emitError(SCHEMA_ERROR, e);
    }
    
    public boolean isErrors() {
        return !(errors == 0 && fatalErrors == 0);
    }
}