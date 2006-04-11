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
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.helpers.StreamingValidatorImpl;

public abstract class AbstractDatatype implements Datatype {

    public AbstractDatatype() {
        super();
    }

    public boolean isValid(String literal, ValidationContext context) {
        try {
            checkValid(literal, context);
        } catch (DatatypeException e) {
            return false;
        }
        return true;
    }

    public DatatypeStreamingValidator createStreamingValidator(
            ValidationContext context) {
        return new StreamingValidatorImpl(this, context);
    }

    public Object createValue(String literal, ValidationContext context) {
        return literal;
    }

    public boolean sameValue(Object value1, Object value2) {
        if (value1 == null) {
            return (value2 == null);
        }
        return value1.equals(value2);
    }

    public int valueHashCode(Object value) {
        return value.hashCode();
    }

    public int getIdType() {
        return Datatype.ID_TYPE_NULL;
    }

    public boolean isContextDependent() {
        return false;
    }
}
