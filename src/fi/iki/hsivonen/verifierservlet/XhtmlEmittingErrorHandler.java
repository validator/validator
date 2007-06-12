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
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class XhtmlEmittingErrorHandler extends SaxEmittingErrorHandler {

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
    
    private boolean listOpen = false;

    /**
     * @param contentHandler
     */
    public XhtmlEmittingErrorHandler(ContentHandler contentHandler) {
        super(contentHandler);
    }

    private void maybeOpenList() throws SAXException {
        if (!this.listOpen) {
            this.emitter.startElement("ol");
            this.listOpen = true;
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
     * @param e
     * @throws SAXException
     */
    protected void warningImpl(SAXParseException e) throws SAXException {
        this.emitError(WARNING, e);
    }

    /**
     * @param e
     * @throws SAXException
     */
    protected void errorImpl(SAXParseException e) throws SAXException {
        this.emitError(ERROR, e);
    }

    /**
     * @param e
     * @throws SAXException
     */
    protected void fatalErrorImpl(SAXParseException e) throws SAXException {
            this.emitError(FATAL_ERROR, e);
    }

    /**
     * @param str
     * @throws SAXException
     */
    protected void infoImpl(String str) throws SAXException {
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
     * @param e
     * @throws SAXException
     */
    protected void ioErrorImpl(IOException e) throws SAXException {
        this.emitError(IO_ERROR, e);
    }

    /**
     * @param message
     * @throws SAXException
     */
    protected void internalErrorImpl(String message) throws SAXException {
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
     * @param e
     * @throws SAXException
     */
    protected void schemaErrorImpl(Exception e) throws SAXException {
        this.emitError(SCHEMA_ERROR, e);
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

}