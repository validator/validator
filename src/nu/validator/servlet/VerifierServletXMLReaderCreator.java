/*
 * Copyright (c) 2005, 2006 Henri Sivonen
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

import nu.validator.gnu.xml.aelfred2.SAXDriver;

import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.thaiopensource.xml.sax.XMLReaderCreator;


/**
 * @version $Id$
 * @author hsivonen
 */
public class VerifierServletXMLReaderCreator implements XMLReaderCreator {

    private ErrorHandler errorHandler;

    private EntityResolver entityResolver;

    /**
     * @param errorHandler
     * @param entityResolver
     */
    public VerifierServletXMLReaderCreator(ErrorHandler errorHandler,
            EntityResolver entityResolver) {
        this.errorHandler = errorHandler;
        this.entityResolver = entityResolver;
    }

    /**
     * @see com.thaiopensource.xml.sax.XMLReaderCreator#createXMLReader()
     */
    @Override
    public XMLReader createXMLReader() throws SAXException {
        XMLReader r = new SAXDriver();
        r.setFeature("http://xml.org/sax/features/external-general-entities",
                true);
        r.setFeature("http://xml.org/sax/features/external-parameter-entities",
                true);
        r.setEntityResolver(this.entityResolver);
        r.setErrorHandler(this.errorHandler);
        return r;
    }

}
