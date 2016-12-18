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

package nu.validator.checker;

import java.util.HashMap;
import java.util.Map;

import nu.validator.datatype.Html5DatatypeException;

import org.relaxng.datatype.DatatypeException;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.xml.util.Name;

/**
 * Encapsulate an error or warning for a prohibited element name.
 *
 * <p>
 * This exception enables the nu.validator.messages.MessageEmitterAdapter
 * elaboration() and elaborateDatatypes() code to provide advice for instances
 * of element names that don't match an expected format (for example, the
 * requirements for custom element names).
 * </p>
 *
 * @see nu.validator.messages.MessageEmitterAdapter#elaboration
 * @see nu.validator.messages.MessageEmitterAdapter#elaborateDatatypes
 * @see nu.validator.datatype
 *
 */
public class VnuBadElementNameException extends SAXParseException {

    private final Name element;

    private final Map<String, DatatypeException> exceptions;

    public VnuBadElementNameException(String elementName, String uri,
            String message, Locator locator, Class<?> datatypeClass,
            boolean warning) throws SAXException, ClassNotFoundException {
        super("Bad element name \u201c" + elementName + "\u201d: " + message,
                locator);
        Html5DatatypeException ex5 = new Html5DatatypeException(0,
                datatypeClass, "element name", message, warning);
        this.element = new Name(uri, elementName);
        this.exceptions = new HashMap<>();
        this.exceptions.put("", ex5);
    }

    public Name getElement() {
        return element;
    }

    public String getElementName() {
        return element.getLocalName();
    }

    public Map<String, DatatypeException> getExceptions() {
        return exceptions;
    }
}
