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

import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

/**
 * The factory for datatype library autodiscovery.
 * 
 * @version $Id$
 * @author hsivonen
 */
public class Html5DatatypeLibraryFactory implements
        DatatypeLibraryFactory {

    /**
     * The library namespace URI.
     */
    private static final String NAMESPACE = "http://whattf.org/datatype-draft";

    /**
     * The constructor.
     */
    public Html5DatatypeLibraryFactory() {
        super();
    }

    /**
     * Returns a <code>Html5DatatypeLibrary</code> on the library namespace and <code>null</code>
     * otherwise.
     * @param namespaceURI a namespace URI
     * @return a <code>DatatypeLibrary</code> or <code>null</code>
     * @see org.relaxng.datatype.DatatypeLibraryFactory#createDatatypeLibrary(java.lang.String)
     */
    public DatatypeLibrary createDatatypeLibrary(String namespaceURI) {
        if (NAMESPACE.equals(namespaceURI)) {
            return new Html5DatatypeLibrary();
        }
        return null;
    }
}
