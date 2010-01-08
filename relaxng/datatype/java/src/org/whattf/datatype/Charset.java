/*
 * Copyright (c) 2008-2010 Mozilla Foundation
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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.whattf.datatype.data.CharsetData;
import org.relaxng.datatype.DatatypeException;

public class Charset extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final Charset THE_INSTANCE = new Charset();

    private static String[] preferred = null;

    private static Map<String, String> nameByAliasMap = new HashMap<String, String>();

    static {
        try {
            CharsetData data = new CharsetData();
            preferred = data.getPreferred();
            nameByAliasMap = data.getNameByAliasMap();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Charset() {
        super();
    }

    @Override public void checkValid(CharSequence literal) throws DatatypeException {
        if (literal.length() == 0) {
            throw newDatatypeException("The empty string is not a valid character encoding name.");
        }
        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);
            // http://tools.ietf.org/html/rfc2978
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z') || c == '-' || c == '!'
                    || c == '#' || c == '$' || c == '%' || c == '&'
                    || c == '\'' || c == '+' || c == '_' || c == '`'
                    || c == '{' || c == '}' || c == '~' || c == '^')) {
                throw newDatatypeException("Value contained ", c,
                        ", which is not a valid character in an encoding name.");
            }
        }
        String encodingName = literal.toString();
        encodingName = toAsciiLowerCase(encodingName);
        if (!isPreferred(encodingName)) {
            String preferred = nameByAliasMap.get(encodingName);
            if (preferred == null) {
                throw newDatatypeException("\u201c" + encodingName
                        + "\u201d is not a valid character encoding name.");
            }
            throw newDatatypeException("\u201c" + encodingName
                    + "\u201d is not a preferred MIME name." + " Use \u201C"
                    + preferred + "\u201D instead.");
        }
    }

    private boolean isPreferred(String encodingName) {
        return Arrays.binarySearch(preferred, encodingName) > -1;
    }

    @Override public String getName() {
        return "encoding name";
    }

}
