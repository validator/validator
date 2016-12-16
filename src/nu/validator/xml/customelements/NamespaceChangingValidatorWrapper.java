/*
 * Copyright (c) 2016 Mozilla Foundation
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

package nu.validator.xml.customelements;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;

public class NamespaceChangingValidatorWrapper implements Validator {

    private final Validator delegate;

    private final PropertyMap properties;

    /**
     * @param delegate
     * @param properties
     */
    public NamespaceChangingValidatorWrapper(Validator delegate,
            PropertyMap properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    /**
     * @return
     * @see com.thaiopensource.validate.Validator#getContentHandler()
     */
    @Override
    public ContentHandler getContentHandler() {
        return new NamespaceChangingContentHandlerWrapper(
                delegate.getContentHandler(),
                (ErrorHandler) properties.get(ValidateProperty.ERROR_HANDLER));
    }

    /**
     * @return
     * @see com.thaiopensource.validate.Validator#getDTDHandler()
     */
    @Override
    public DTDHandler getDTDHandler() {
        return delegate.getDTDHandler();
    }

    /**
     *
     * @see com.thaiopensource.validate.Validator#reset()
     */
    @Override
    public void reset() {
        delegate.reset();
    }

}
