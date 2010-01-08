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

public class MetaCharset extends AbstractDatatype {

    /**
     * The singleton instance.
     */
    public static final MetaCharset THE_INSTANCE = new MetaCharset();

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

    // private static final Pattern THE_PATTERN =
    // Pattern.compile("^[tT][eE][xX][tT]/[hH][tT][mM][lL]; ?[cC][hH][aA][rR][sS][eE][tT]=[0-9a-zA-Z!#$%&'+_`{}~^-]+$");

    public MetaCharset() {
        super();
    }

    @Override public void checkValid(CharSequence literal)
            throws DatatypeException {
        String lower = toAsciiLowerCase(literal);
        if (!lower.startsWith("text/html;")) {
            throw newDatatypeException(
                    "The legacy encoding declaration did not start with ",
                    "text/html;", ".");
        }
        if (lower.length() == 10) {
            throw newDatatypeException("The legacy encoding declaration ended prematurely.");
        }
        int offset = 10;
        paramloop: for (int i = 10; i < lower.length(); i++) {
            char c = lower.charAt(i);
            switch (c) {
                case ' ':
                case '\t':
                case '\n':
                case '\u000C':
                case '\r':
                    offset++;
                    continue;
                case 'c':
                    break paramloop;
                default:
                    throw newDatatypeException(
                            "The legacy encoding declaration"
                                    + " did not start with space characters or ",
                            "charset=", " after the semicolon. "
                                    + " Found \u201c" + c + "\u201d instead.");
            }
        }
        if (!lower.startsWith("charset=", offset)) {
            throw newDatatypeException("The legacy encoding declaration"
                    + "did not contain ", "charset=", " after the semicolon.");
        }
        offset += 8;
        if (lower.length() == offset) {
            throw newDatatypeException("The empty string is not a valid character encoding name.");
        }
        for (int i = offset; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || c == '-'
                    || c == '!' || c == '#' || c == '$' || c == '%' || c == '&'
                    || c == '\'' || c == '+' || c == '_' || c == '`'
                    || c == '{' || c == '}' || c == '~' || c == '^')) {
                throw newDatatypeException("The legacy encoding contained ", c,
                        ", which is not a valid character in an encoding name.");
            }
        }
        String encodingName = lower.substring(offset);
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
        return "legacy character encoding declaration";
    }

}
