/*
 * Copyright (c) 2008 Mozilla Foundation
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

package nu.validator.perftest;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import nu.validator.htmlparser.io.Driver;

public class DriverWrapper implements XMLReader {

    private final Driver driver;

    /**
     * @param driver
     */
    public DriverWrapper(Driver driver) {
        this.driver = driver;
    }

    public ContentHandler getContentHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public DTDHandler getDTDHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public EntityResolver getEntityResolver() {
        // TODO Auto-generated method stub
        return null;
    }

    public ErrorHandler getErrorHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        driver.tokenize(input);
    }

    public void parse(String systemId) throws IOException, SAXException {
        // TODO Auto-generated method stub
        
    }

    public void setContentHandler(ContentHandler handler) {
        // TODO Auto-generated method stub
        
    }

    public void setDTDHandler(DTDHandler handler) {
        // TODO Auto-generated method stub
        
    }

    public void setEntityResolver(EntityResolver resolver) {
        // TODO Auto-generated method stub
        
    }

    public void setErrorHandler(ErrorHandler handler) {
        // TODO Auto-generated method stub
        
    }

    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub
        
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub
        
    }
    
    
    
}
