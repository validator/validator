/*
 * Copyright (c) 2014 Mozilla Foundation
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
 * Encapsulate an error or warning for attribute value with datatype mismatch.
 *
 * <p>
 * This exception enables the nu.validator.messages.MessageEmitterAdapter
 * elaboration() and elaborateDatatypes() code to provide advice for instances
 * of "custom" cases of datatype mismatches in bad attribute values. It's
 * intended for use in cases of reporting datatype mismatches when checking
 * attributes for which expressing the association between the attribute and the
 * expected datatype(s) of it value using a schema may not be possible or
 * practical or preferrable (for example, the case of the img|source "srcset"
 * attribute, which has different expected datatypes depending on whether the
 * img|source also has a "sizes" attribute).
 * </p>
 *
 * @see nu.validator.messages.MessageEmitterAdapter#elaboration
 * @see nu.validator.messages.MessageEmitterAdapter#elaborateDatatypes
 * @see nu.validator.datatype
 *
 */
public class VnuBadAttrValueException extends SAXParseException {

    private final Name element;

    private final Name attributeName;

    private final String attributeValue;

    private final Map<String, DatatypeException> exceptions;

    public VnuBadAttrValueException(String elementName, String uri,
            String attributeName, String attributeValue, String message,
            Locator locator, Class<?> datatypeClass, boolean warning)
            throws SAXException, ClassNotFoundException {
        super("Bad value \u201c" + attributeValue
                + "\u201d for attribute \u201c" + attributeName
                + "\u201d on element \u201c" + elementName + "\u201d: "
                + message, locator);
        Html5DatatypeException ex5 = new Html5DatatypeException(0,
                datatypeClass, "attribute", message, warning);
        this.element = new Name(uri, elementName);
        this.attributeName = new Name("", attributeName);
        this.attributeValue = attributeValue;
        this.exceptions = new HashMap<>();
        this.exceptions.put("", ex5);
    }

    public Name getCurrentElement() {
        return element;
    }

    public Name getAttributeName() {
        return attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public Map<String, DatatypeException> getExceptions() {
        return exceptions;
    }
}
