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

package nu.validator.servlet;

import java.io.IOException;

import nu.validator.xml.CharacterUtil;

import org.whattf.checker.NormalizationChecker;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.ibm.icu.text.Normalizer;


public abstract class AbstractErrorHandler implements InfoErrorHandler {

    private int warnings = 0;

    private int errors = 0;

    private int fatalErrors = 0;

    protected static String scrub(String s) throws SAXException {
        s = CharacterUtil.prudentlyScrubCharacterData(s);
        if (NormalizationChecker.startsWithComposingChar(s)) {
            s = " " + s;
        }
        return Normalizer.normalize(s, Normalizer.NFC, 0);
    }

    public AbstractErrorHandler() {
        super();
    }

    /**
     * @return Returns the errors.
     */
    public final int getErrors() {
        return errors;
    }

    /**
     * @return Returns the fatalErrors.
     */
    public final int getFatalErrors() {
        return fatalErrors;
    }

    /**
     * @return Returns the warnings.
     */
    public final int getWarnings() {
        return warnings;
    }

    public final boolean isErrors() {
        return !(errors == 0 && fatalErrors == 0);
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public final void warning(SAXParseException e) throws SAXException {
        this.warnings++;
        warningImpl(e);
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public final void error(SAXParseException e) throws SAXException {
        this.errors++;
        errorImpl(e);
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public final void fatalError(SAXParseException e) throws SAXException {
        this.fatalErrors++;
        Exception wrapped = e.getException();
        if (wrapped instanceof IOException) {
            ioErrorImpl((IOException) wrapped);
        } else {
            fatalErrorImpl(e);
        }
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#info(java.lang.String)
     */
    public final void info(String str) throws SAXException {
        infoImpl(str);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#ioError(java.io.IOException)
     */
    public final void ioError(IOException e) throws SAXException {
        this.fatalErrors++;
        ioErrorImpl(e);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#internalError(java.lang.Throwable,
     *      java.lang.String)
     */
    public final void internalError(Throwable e, String message) throws SAXException {
        this.fatalErrors++;
        internalErrorImpl(message);
    }

    /**
     * @see nu.validator.servlet.InfoErrorHandler#schemaError(java.lang.Exception)
     */
    public final void schemaError(Exception e) throws SAXException {
        this.fatalErrors++;
        schemaErrorImpl(e);
    }

    /**
     * @param e
     * @throws SAXException
     */
    protected abstract void warningImpl(SAXParseException e)
            throws SAXException;

    /**
     * @param e
     * @throws SAXException
     */
    protected abstract void errorImpl(SAXParseException e) throws SAXException;

    /**
     * @param e
     * @throws SAXException
     */
    protected abstract void fatalErrorImpl(SAXParseException e)
            throws SAXException;



    /**
     * @param str
     * @throws SAXException
     */
    protected abstract void infoImpl(String str) throws SAXException;

    /**
     * @param e
     * @throws SAXException
     */
    protected abstract void ioErrorImpl(IOException e) throws SAXException;

    /**
     * @param message
     * @throws SAXException
     */
    protected abstract void internalErrorImpl(String message)
            throws SAXException;

    /**
     * @param e
     * @throws SAXException
     */
    protected abstract void schemaErrorImpl(Exception e) throws SAXException;

    /**
     * @see nu.validator.servlet.InfoErrorHandler#start()
     */
    public void start(String documentUri) throws SAXException {

    }
    
    /**
     * @see nu.validator.servlet.InfoErrorHandler#end()
     */
    public void end(String successMessage, String failureMessage)
            throws SAXException {

    }
    
}