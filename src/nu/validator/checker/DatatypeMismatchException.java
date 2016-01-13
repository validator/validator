/*
 * Copyright (c) 2010 Mozilla Foundation
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

package nu.validator.checker;

import java.util.Map;
import java.util.HashMap;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import nu.validator.datatype.Html5DatatypeException;
import org.relaxng.datatype.DatatypeException;

/**
 * Encapsulate an error or warning for a datatype mismatch.
 * 
 * <p>
 * This exception enables the nu.validator.messages.MessageEmitterAdapter
 * elaboration() and elaborateDatatypes() code to provide advice for instances
 * of datatype mismatchess in types of content other than just attribute values
 * (for example, in text content and in pseudo-attribute values in XML PIs).
 * </p>
 * 
 * @see nu.validator.messages.MessageEmitterAdapter#elaboration
 * @see nu.validator.messages.MessageEmitterAdapter#elaborateDatatypes
 * @see nu.validator.datatype
 * 
 */
public class DatatypeMismatchException extends SAXParseException {

    private final Map<String, DatatypeException> exceptions;

    final boolean warning;

    /**
     * @param message
     * @param locator
     * @param datatypeClass
     * @param warning
     *            true if should be handled as warning, false if as error
     * @throws ClassNotFoundException
     * @throws SAXException
     *
     */
    public DatatypeMismatchException(String message, Locator locator,
            Class<?> datatypeClass, boolean warning) throws SAXException,
            ClassNotFoundException {
        super(message, locator);
        this.warning = warning;
        Html5DatatypeException ex5 = new Html5DatatypeException(0,
                datatypeClass, "", "", warning);
        this.exceptions = new HashMap<>();
        this.exceptions.put("", ex5);
    }

    /**
     * Returns the exceptions.
     * 
     * @return the exceptions
     */
    public Map<String, DatatypeException> getExceptions() {
        return exceptions;
    }

    /**
     * Returns true if the datatype exception should be handled as a warning,
     * false otherwise.
     * 
     * @return true if the datatype exception should be handled as a warning,
     *         false otherwise.
     */
    public boolean isWarning() {
        return warning;
    }
}
