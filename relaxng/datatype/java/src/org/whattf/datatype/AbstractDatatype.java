/*
 * Copyright (c) 2006 Henri Sivonen
 * Copyright (c) 2007-2009 Mozilla Foundation
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

import java.util.ArrayList;
import java.util.List;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;

/**
 * Common superclass for HTML5 datatypes. Implements all methods of the 
 * <code>Datatype</code> interface and leaves a new <code>checkValid</code> for 
 * subclasses to implement.
 * 
 * @version $Id$
 * @author hsivonen
 */
public abstract class AbstractDatatype implements Datatype {

    /**
     * Constructor
     */
    AbstractDatatype() {
        super();
    }

    /**
     * Calls <code>checkValid(CharSequence literal)</code>.
     * @param literal the value
     * @param context the validation context (ignored by subclasses)
     * @return <code>true</code> if valid and <code>false</code> if not
     * @see org.relaxng.datatype.Datatype#isValid(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    public final boolean isValid(String literal, ValidationContext context) {
        try {
            checkValid(literal);
        } catch (DatatypeException e) {
            return false;
        }
        return true;
    }

    /**
     * Delegates to <code>checkValid(CharSequence literal)</code>.
     * @param literal the value
     * @param context the validation context (ignored by subclasses)
     * @throws DatatypeException if the literal does not conform to the datatype definition
     * @see org.relaxng.datatype.Datatype#checkValid(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    public void checkValid(String literal, ValidationContext context) throws DatatypeException {
        checkValid(literal);
    }

    public abstract void checkValid(CharSequence literal) throws DatatypeException;
    
    /**
     * Merely returns a <code>DatatypeStreamingValidatorImpl</code>.
     * @param context the validation context (ignored by subclasses)
     * @return An unoptimized <code>DatatypeStreamingValidator</code>
     * @see org.relaxng.datatype.Datatype#createStreamingValidator(org.relaxng.datatype.ValidationContext)
     */
    public DatatypeStreamingValidator createStreamingValidator(
            ValidationContext context) {
        return new DatatypeStreamingValidatorImpl(this);
    }

    /**
     * Implements strict string equality semantics by returning <code>literal</code> 
     * itself.
     * @param literal the value (get returned)
     * @param context ignored
     * @return the <code>literal</code> that was passed in
     * @see org.relaxng.datatype.Datatype#createValue(java.lang.String, org.relaxng.datatype.ValidationContext)
     */
    public Object createValue(String literal, ValidationContext context) {
        return literal;
    }

    /**
     * Implements strict string equality semantics by performing a standard 
     * <code>equals</code> check on the arguments.
     * @param value1 an object returned by <code>createValue</code>
     * @param value2 another object returned by <code>createValue</code>
     * @return <code>true</code> if the values are equal, <code>false</code> otherwise
     * @see org.relaxng.datatype.Datatype#sameValue(java.lang.Object, java.lang.Object)
     */
    public final boolean sameValue(Object value1, Object value2) {
        if (value1 == null) {
            return (value2 == null);
        }
        return value1.equals(value2);
    }

    /**
     * Implements strict stirng equality semantics by returning the 
     * <code>java.lang.Object</code>-level <code>hashCode</code> of 
     * the object.
     * @param value an object returned by <code>createValue</code>
     * @return the hash code
     * @see org.relaxng.datatype.Datatype#valueHashCode(java.lang.Object)
     */
    public final int valueHashCode(Object value) {
        return value.hashCode();
    }

    /**
     * Always returns <code>Datatype.ID_TYPE_NULL</code>.
     * @return <code>Datatype.ID_TYPE_NULL</code>
     * @see org.relaxng.datatype.Datatype#getIdType()
     */
    public final int getIdType() {
        return Datatype.ID_TYPE_NULL;
    }

    /**
     * Returns <code>false</code>
     * @return <code>false</code>
     * @see org.relaxng.datatype.Datatype#isContextDependent()
     */
    public boolean isContextDependent() {
        return false;
    }

    /**
     * Checks if a UTF-16 code unit represents a whitespace character (U+0020, 
     * U+0009, U+000D or U+000A).
     * @param c the code unit
     * @return <code>true</code> if whitespace, <code>false</code> otherwise
     */
    protected final boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }
    
    protected final boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    protected static final char toAsciiLowerCase(char c) {
        if (c >= 'A' && c <= 'Z') {
            c += 0x20;
        }
        return c;
    }

    protected static final String toAsciiLowerCase(CharSequence str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

    protected static final char toAsciiUpperCase(char c) {
        if (c >= 'a' && c <= 'z') {
            c -= 0x20;
        }
        return c;
    }

    protected static final String toAsciiUpperCase(CharSequence str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'z') {
                c -= 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

    protected DatatypeException newDatatypeException(String message) {
        return new Html5DatatypeException(this.getClass(), this.getName(), message);
    }

    protected DatatypeException newDatatypeException(String head, String literal, String tail) {
        return new Html5DatatypeException(this.getClass(), this.getName(), head, literal, tail);
    }

    protected DatatypeException newDatatypeException(String head, char literal, String tail) {
        return new Html5DatatypeException(this.getClass(), this.getName(), head, String.valueOf(literal), tail);
    }
    
    protected DatatypeException newDatatypeException(int position, String message) {
        return new Html5DatatypeException(position, this.getClass(), this.getName(), message);
    }

    protected DatatypeException newDatatypeException(int position, String head, String literal, String tail) {
        return new Html5DatatypeException(position, this.getClass(), this.getName(), head, literal, tail);
    }

    protected DatatypeException newDatatypeException(int position, String head, char literal, String tail) {
        return new Html5DatatypeException(position, this.getClass(), this.getName(), head, String.valueOf(literal), tail);
    }    
    
    /* for datatype exceptions that are handled as warnings, the following are
     * alternative forms of all the above, with an additional "warning" parameter */

    protected DatatypeException newDatatypeException(String message, boolean warning) {
        return new Html5DatatypeException(this.getClass(), this.getName(), message, warning);
    }

    protected DatatypeException newDatatypeException(String head, String literal, String tail, boolean warning) {
        return new Html5DatatypeException(this.getClass(), this.getName(), head, literal, tail, warning);
    }

    protected DatatypeException newDatatypeException(String head, char literal, String tail, boolean warning) {
        return new Html5DatatypeException(this.getClass(), this.getName(), head, String.valueOf(literal), tail, warning);
    }
    
    protected DatatypeException newDatatypeException(int position, String message, boolean warning) {
        return new Html5DatatypeException(position, this.getClass(), this.getName(), message, warning);
    }

    protected DatatypeException newDatatypeException(int position, String head, String literal, String tail, boolean warning) {
        return new Html5DatatypeException(position, this.getClass(), this.getName(), head, literal, tail, warning);
    }

    protected DatatypeException newDatatypeException(int position, String head, char literal, String tail, boolean warning) {
        return new Html5DatatypeException(position, this.getClass(), this.getName(), head, String.valueOf(literal), tail, warning);
    }    
    
    public abstract String getName();
    
    protected List<CharSequenceWithOffset> split(CharSequence sequence, char delimiter) {
        List<CharSequenceWithOffset> rv = new ArrayList<CharSequenceWithOffset>();
        int offset = 0;
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (c == delimiter) {
                rv.add(new CharSequenceWithOffset(sequence.subSequence(offset, i), offset));
                offset = i + 1;
            }
        }
        rv.add(new CharSequenceWithOffset(sequence.subSequence(offset, sequence.length()), offset));
        return rv;
    }
    
    protected class CharSequenceWithOffset {
        private final CharSequence sequence;
        private final int offset;
        /**
         * @param sequence
         * @param offset
         */
        public CharSequenceWithOffset(final CharSequence sequence, final int offset) {
            this.sequence = sequence;
            this.offset = offset;
        }
        /**
         * Returns the offset.
         * 
         * @return the offset
         */
        public int getOffset() {
            return offset;
        }
        /**
         * Returns the sequence.
         * 
         * @return the sequence
         */
        public CharSequence getSequence() {
            return sequence;
        }
    }
}
