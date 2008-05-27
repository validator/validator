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

package nu.validator.xml.dataattributes;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

import com.thaiopensource.validate.Validator;

public class DataAttributeDroppingValidatorWrapper implements Validator {

    private final Validator delegate;

    /**
     * @param delegate
     */
    public DataAttributeDroppingValidatorWrapper(Validator delegate) {
        this.delegate = delegate;
    }

    /**
     * @return
     * @see com.thaiopensource.validate.Validator#getContentHandler()
     */
    public ContentHandler getContentHandler() {
        return new DataAttributeDroppingContentHandlerWrapper(delegate.getContentHandler());
    }

    /**
     * @return
     * @see com.thaiopensource.validate.Validator#getDTDHandler()
     */
    public DTDHandler getDTDHandler() {
        return delegate.getDTDHandler();
    }

    /**
     * 
     * @see com.thaiopensource.validate.Validator#reset()
     */
    public void reset() {
        delegate.reset();
    }
    
}
