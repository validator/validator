/*
 * Copyright (c) 2006 Henri Sivonen
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

package org.whattf.checker.jing;

import org.whattf.checker.Checker;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;


/**
 * Wraps a <code>Checker</code> so that it can be used like a Jing <code>Validator</code>.
 * 
 * @version $Id$
 * @author hsivonen
 */
public final class CheckerValidator implements Validator {

    /**
     * The wrapped <code>Checker</code>
     */
    private final Checker checker;
    
    /**
     * Constructor
     * 
     * @param checker the <code>Checker</code> to wrap
     * @param propertyMap a property map containing a mapping for 
     * <code>ValidateProperty.ERROR_HANDLER</code>
     */
    public CheckerValidator(Checker checker, PropertyMap propertyMap) {
        super();
        this.checker = checker;
        this.checker.setErrorHandler((ErrorHandler) propertyMap.get(ValidateProperty.ERROR_HANDLER));
    }

    /**
     * Returns the wrapped <code>Checker</code>.
     * @return the wrapped <code>Checker</code>
     * @see com.thaiopensource.validate.Validator#getContentHandler()
     */
    public ContentHandler getContentHandler() {
        return checker;
    }
    
    /**
     * Returns <code>null</code>.
     * @return <code>null</code>
     * @see com.thaiopensource.validate.Validator#getDTDHandler()
     */
    public DTDHandler getDTDHandler() {
        return null;
    }

    /**
     * Resets the wrapped <code>Checker</code>.
     * @see com.thaiopensource.validate.Validator#reset()
     */
    public void reset() {
        this.checker.reset();
    }

}
