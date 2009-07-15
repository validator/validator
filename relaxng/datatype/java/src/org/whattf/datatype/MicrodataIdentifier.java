/*
 * Copyright (c) 2009 Mozilla Foundation
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

import java.util.List;

import org.relaxng.datatype.DatatypeException;

import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.StringPrepParseException;

public class MicrodataIdentifier extends Iri {

    /**
     * The singleton instance.
     */
    public static final MicrodataIdentifier THE_INSTANCE = new MicrodataIdentifier();
    
    protected MicrodataIdentifier() {
        super();
    }

    @Override
    public String getName() {
        return "microdata identifier";
    }

    /**
     * @see org.whattf.datatype.IriRef#checkValid(java.lang.CharSequence)
     */
    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        int len = literal.length();
        if (len == 0) {
            throw newDatatypeException("A microdata identifier must not be the empty string.");
        }
        for (int i = 1; i < len; i++) {
            char c = literal.charAt(0);
            switch (c) {
                case '.':
                    checkReverseDns(literal);
                    return;
                case ':':
                    super.checkValid(literal);
                    return;
            }            
        }
    }

    private void checkReverseDns(CharSequence literal) throws DatatypeException {
        StringBuffer asAscii;
        try {
            asAscii = IDNA.convertIDNToASCII(literal.toString(), IDNA.USE_STD3_RULES | IDNA.ALLOW_UNASSIGNED);
        } catch (StringPrepParseException e) {
            throw newDatatypeException(e.getErrorOffset(), e.getMessage());
        }
        List<CharSequenceWithOffset> tokens = split(asAscii, '.');
        for (CharSequenceWithOffset token : tokens) {
            
        }
    }


}
