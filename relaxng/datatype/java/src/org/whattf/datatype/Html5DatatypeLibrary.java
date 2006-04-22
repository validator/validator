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

package org.whattf.datatype;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.helpers.ParameterlessDatatypeBuilder;

/**
 * Factory for HTML5 datatypes.
 * @version $Id$
 * @author hsivonen
 */
public class Html5DatatypeLibrary implements DatatypeLibrary {
    
    public Html5DatatypeLibrary() {
        super();
    }

    /**
     * Returns a <code>DatatypeBuilder</code> for a named datatype. This method is 
     * unnecessary for direct access. Just use <code>createDatatype</code>.
     * @param baseTypeLocalName the local name
     * @return a <code>ParameterlessDatatypeBuilder</code> for the local name
     * @see org.relaxng.datatype.DatatypeLibrary#createDatatypeBuilder(java.lang.String)
     */
    public DatatypeBuilder createDatatypeBuilder(String baseTypeLocalName)
            throws DatatypeException {
        return new ParameterlessDatatypeBuilder(createDatatype(baseTypeLocalName));
    }

    /**
     * The factory method for the datatypes of this library.
     * @param typeLocalName the local name
     * @return a <code>Datatype</code> instance for the local name
     * @see org.relaxng.datatype.DatatypeLibrary#createDatatype(java.lang.String)
     */
    public Datatype createDatatype(String typeLocalName)
            throws DatatypeException {
        if ("ID".equals(typeLocalName)) {
            return new Id();
        } else if ("IDREF".equals(typeLocalName)) {
            return new Idref();
        } else if ("IDREFS".equals(typeLocalName)) {
            return new Idrefs();
        } else if ("pattern".equals(typeLocalName)) {
            return new Pattern();
        } else if ("datetime".equals(typeLocalName)) {
            return new Datetime();
        } else if ("datetime-local".equals(typeLocalName)) {
            return new DatetimeLocal();
        } else if ("datetime-tz".equals(typeLocalName)) {
            return new DatetimeTz();
        } else if ("date".equals(typeLocalName)) {
            return new Date();
        } else if ("month".equals(typeLocalName)) {
            return new Month();
        } else if ("week".equals(typeLocalName)) {
            return new Week();
        } else if ("time".equals(typeLocalName)) {
            return new Time();
        } else if ("iri".equals(typeLocalName)) {
            return new Iri();
        } else if ("iri-ref".equals(typeLocalName)) {
            return new IriRef();
        }
        throw new DatatypeException("Unknown local name for datatype: " + typeLocalName);
    }

}
