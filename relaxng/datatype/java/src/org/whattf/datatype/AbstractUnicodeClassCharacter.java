/*
 * Copyright (c) 2007-2008 Mozilla Foundation
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

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UnicodeSet;

public abstract class AbstractUnicodeClassCharacter extends AbstractDatatype {

    private static final int SURROGATE_OFFSET = 0x10000 - (0xD800 << 10) - 0xDC00;
    
    protected abstract UnicodeSet getUnicodeSet();
    
    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        switch (literal.length()) {
            case 0:
                throw newDatatypeException("The empty string is not a " + getName() + ".");            
            case 1:
                char c = literal.charAt(0);
                if (!getUnicodeSet().contains(c)) {
                    throw newDatatypeException(0, "The character ", c, " is not a " + getName() + ".");
                }
                return;
            case 2:
                char hi = literal.charAt(0);
                char lo = literal.charAt(1);
                if ((lo & 0xFC00) == 0xDC00 && (hi & 0xFC00) == 0xD800) {
                    int codepoint = (hi << 10) + lo + SURROGATE_OFFSET;
                    if (!getUnicodeSet().contains(codepoint)) {
                        throw newDatatypeException(0, "The character ", "" + hi + lo, " is not a " + getName() + ".");
                    }
                    return;                    
                }
                // else fall through.
            default:
                throw newDatatypeException("A " + getName() + " must be a single character.");
        }
    }

    /**
     * @see org.whattf.datatype.AbstractDatatype#createStreamingValidator(org.relaxng.datatype.ValidationContext)
     */
    @Override public DatatypeStreamingValidator createStreamingValidator(
            ValidationContext context) {
        return new DatatypeStreamingValidator () {

            int codepoint = -2;
            
            private void addCharacter(char c) {
                if (codepoint == -1) {
                    return;
                } else if ((codepoint & 0xFC00) == 0xD800) {
                    if ((c & 0xFC00) == 0xDC00) {
                        codepoint = (codepoint << 10) + c + SURROGATE_OFFSET;
                    } else {
                        codepoint = -1;
                    }
                } else if (codepoint == -2) {
                    codepoint = c;
                } else {
                    codepoint = -1;
                }
            }
            
            public void addCharacters(char[] buf, int start, int len) {
                if (codepoint == -1) {
                    return;
                } else {
                    for (int i = start; i < start + len; i++) {
                        addCharacter(buf[i]);
                    }
                }
            }

            public void checkValid() throws DatatypeException {
                if (codepoint == -2) {
                    throw newDatatypeException("The empty string is not a " + getName() + ".");
                } else if (codepoint == -1) {
                    throw newDatatypeException("A " + getName() + " must be a single character.");                    
                } else if (!getUnicodeSet().contains(codepoint)) {
                    throw newDatatypeException(0, "The character ", UCharacter.toString(codepoint), " is not a " + getName() + ".");
                }
            }

            public boolean isValid() {
                try {
                    checkValid();
                    return true;
                } catch (DatatypeException e) {
                    return false;
                }
            }
            
        };
    }

}
