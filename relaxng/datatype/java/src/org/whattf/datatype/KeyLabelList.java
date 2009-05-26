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

import org.relaxng.datatype.DatatypeException;

import java.util.Arrays;
import com.ibm.icu.lang.UCharacter;

public class KeyLabelList extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final KeyLabelList THE_INSTANCE = new KeyLabelList();

    private KeyLabelList() {
        super();
    }

    @Override
      public void checkValid(CharSequence literal) throws DatatypeException {
        String[] keylabels = literal.toString().split("\\s+");
        Arrays.sort(keylabels);
        for (int i = 0; i < keylabels.length; i++) {
          String label = keylabels[i];
          if (i > 0 && label.equals(keylabels[i-1])) {
            throw newDatatypeException(
                "Duplicate key label ",label,". Each key label must be unique.");
          }
          if (label.length() == 2) {
            char[] chars = label.toCharArray();
            if (!(UCharacter.isHighSurrogate(chars[0])
                  && UCharacter.isLowSurrogate(chars[1]))) {
              throw newDatatypeException(
                  "Key label ",label," has multiple characters. Each key label must be a single character.");
            }
          }
          if (label.length() > 2) {
            throw newDatatypeException(
                "Key label ",label," has multiple characters. Each key label must be a single character.");
          }
        }
      }

    @Override
    public String getName() {
        return "key label list";
    }

}
