/*
 * Copyright (c) 2005 Henri Sivonen
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

package nu.validator.xml;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 * @author hsivonen
 */
public class SystemErrErrorHandler implements ErrorHandler {

    private Writer out;
    
    private boolean inError = false;
    
    public SystemErrErrorHandler() {
        try {
            out = new OutputStreamWriter(System.err, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e) throws SAXException {
        try {
            out.write("Warning:\n");
            out.write(e.getMessage());
            out.write("\nFile: ");
            String systemId = e.getSystemId();
            out.write((systemId == null) ? "Unknown" : systemId);
            out.write("\nLine: ");
            out.write(Integer.toString(e.getLineNumber()));
            out.write(" Col: ");
            out.write(Integer.toString(e.getColumnNumber()));
            out.write("\n\n");
            out.flush();
        } catch (IOException e1) {
            throw new SAXException(e1);
        }
    }

    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException {
        inError = true;
        try {
            out.write("Error:\n");
            out.write(e.getMessage());
            out.write("\nFile: ");
            String systemId = e.getSystemId();
            out.write((systemId == null) ? "Unknown" : systemId);
            out.write("\nLine: ");
            out.write(Integer.toString(e.getLineNumber()));
            out.write(" Col: ");
            out.write(Integer.toString(e.getColumnNumber()));
            out.write("\n\n");
            out.flush();
        } catch (IOException e1) {
            throw new SAXException(e1);
        }
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException e) throws SAXException {
        inError = true;
        try {
            out.write("Fatal Error:\n");
            out.write(e.getMessage());
            out.write("\nFile: ");
            String systemId = e.getSystemId();
            out.write((systemId == null) ? "Unknown" : systemId);
            out.write("\nLine: ");
            out.write(Integer.toString(e.getLineNumber()));
            out.write(" Col: ");
            out.write(Integer.toString(e.getColumnNumber()));
            out.write("\n\n");
            out.flush();
        } catch (IOException e1) {
            throw new SAXException(e1);
        }
    }

    /**
     * Returns the inError.
     * 
     * @return the inError
     */
    public boolean isInError() {
        return inError;
    }

    public void reset() {
        inError = false;
    }
    
    
}
