/*
 * Copyright (c) 2008 Mozilla Foundation
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

public class MetaCharset extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MetaCharset THE_INSTANCE = new MetaCharset();
    
//    private static final Pattern THE_PATTERN = Pattern.compile("^[tT][eE][xX][tT]/[hH][tT][mM][lL]; ?[cC][hH][aA][rR][sS][eE][tT]=[0-9a-zA-Z!#$%&'+_`{}~^-]+$");

    public MetaCharset() {
        super();
    }

    @Override
    public void checkValid(CharSequence literal) throws DatatypeException {
        String lower = toAsciiLowerCase(literal);
        if (!lower.startsWith("text/html;")) {
            throw newDatatypeException(
                "The legacy encoding declaration did not start with ", "text/html;", ".");
        }
        if (lower.length() == 10) {
            throw newDatatypeException(
                    "The legacy encoding declaration ended prematurely.");            
        }
        int offset = 10;
        if (lower.charAt(offset) == ' ') {
            offset++;
        }
        if (!lower.startsWith("charset=", offset)) {
            if (offset == 10) {
                throw newDatatypeException(
                    "The legacy encoding did not contain ", "charset=", " immediately after the semicolon.");             
            } else {
                throw newDatatypeException(
                        "The legacy encoding did not contain ", "charset=", " immediately after the space.");                             
            }
        }
        offset += 8;
        for (int i = offset; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || c == '-' || c == '!' || c == '#' || c == '$' || c == '%' || c == '&' || c == '\'' || c == '+' || c == '_' || c == '`' || c == '{' || c == '}' || c == '~' || c == '^')) {
                throw newDatatypeException(
                        "The legacy encoding contained ", c, ", which is not a valid character in an encoding name.");                   
            }
        }
    }

    @Override
    public String getName() {
        return "legacy character encoding declaration";
    }

}
