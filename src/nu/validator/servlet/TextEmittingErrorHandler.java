/*
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

package nu.validator.servlet;

import java.io.IOException;
import java.io.Writer;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TextEmittingErrorHandler extends AbstractErrorHandler {

    private final Writer writer;

    /**
     * @param writer
     */
    public TextEmittingErrorHandler(final Writer writer) {
        this.writer = writer;
    }

    @Override
    protected void errorImpl(SAXParseException e) throws SAXException {
        try {
            String msg = e.getMessage();
            if (msg == null) {
                w("Error.");                
            } else {
                w("Error: ");
                w(scrub(msg));
            }
            writeLocation(e);
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    private void writeLocation(SAXParseException e) throws IOException {
        int line = e.getLineNumber();
        int col = e.getColumnNumber();
        if (line > -1) {
            w("\nLine: ");
            w("" + line);
            if (col > -1) {
                w(", column: ");
                w("" + col);
            }
        }
    }

    @Override
    protected void fatalErrorImpl(SAXParseException e) throws SAXException {
        try {
            String msg = e.getMessage();
            if (msg == null) {
                w("Fatal Error.");                
            } else {
                w("Fatal Error: ");
                w(scrub(msg));
            }
            writeLocation(e);
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    @Override
    protected void infoImpl(String str) throws SAXException {
        try {
            w("Info: ");
            w(scrub(str));
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    @Override
    protected void internalErrorImpl(String message) throws SAXException {
        try {
            w("Internal Error: ");
            w(scrub(message));
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    @Override
    protected void ioErrorImpl(IOException e) throws SAXException {
        try {
            String msg = e.getMessage();
            if (msg == null) {
                w("IO Error.");                
            } else {
                w("IO Error: ");
                w(scrub(msg));
            }
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    @Override
    protected void schemaErrorImpl(Exception e) throws SAXException {
        try {
            String msg = e.getMessage();
            if (msg == null) {
                w("Schema Error.");                
            } else {
                w("Schema Error: ");
                w(scrub(msg));
            }
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    @Override
    protected void warningImpl(SAXParseException e) throws SAXException {
        try {
            String msg = e.getMessage();
            if (msg == null) {
                w("Warning.");                
            } else {
                w("Warning: ");
                w(scrub(msg));
            }
            writeLocation(e);
            w("\n\n");       
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * @see nu.validator.servlet.AbstractErrorHandler#end(java.lang.String, java.lang.String)
     */
    @Override
    public void end(String successMessage, String failureMessage)
            throws SAXException {
        // TODO Auto-generated method stub
        try {
            if (isErrors()) {
                w(scrub(failureMessage));
            } else {
                w(scrub(successMessage));
            }
            w("\n");
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * @see nu.validator.servlet.AbstractErrorHandler#start(java.lang.String)
     */
    @Override
    public void start(String documentUri) throws SAXException {
        try {
            w("Results");
            if (documentUri != null) {
                w(" for ");
                w(scrub(documentUri));
            }
            w("\n\n");
        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    private final void w(String str) throws IOException {
        writer.write(str);
    }
}
