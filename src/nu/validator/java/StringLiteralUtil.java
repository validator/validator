/*
 * Copyright (c) 2005 Henri Sivonen
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

package nu.validator.java;

/**
 * @version $Id: StringLiteralUtil.java,v 1.4 2006/11/18 00:05:24 hsivonen Exp $
 * @author hsivonen
 */
public class StringLiteralUtil {
    public static String unquotedCharLiteral(char c) {
        // http://java.sun.com/docs/books/jls/second_edition/html/lexical.doc.html#101089
        switch (c) {
            case '\b':
                return "\\b";
            case '\t':
                return "\\t";
            case '\n':
                return "\\n";
            case '\f':
                return "\\f";
            case '\r':
                return "\\r";
            case '\"':
                return "\\\"";
            case '\'':
                return "\\\'";
            case '\\':
                return "\\\\";
            default:
                if (c >= ' ' && c <= '~') {
                    return "" + c;
                } else {
                    String hex = Integer.toHexString((int) c);
                    switch (hex.length()) {
                        case 1:
                            return "\\u000" + hex;
                        case 2:
                            return "\\u00" + hex;
                        case 3:
                            return "\\u0" + hex;
                        default:
                            return "\\u" + hex;
                    }
                }
        }
    }

    public static String charLiteral(char c) {
        return "\'" + unquotedCharLiteral(c) + "\'";
    }

    public static String stringLiteral(CharSequence cs) {
        if (cs == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\"');
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            sb.append(unquotedCharLiteral(cs.charAt(i)));
        }
        sb.append('\"');
        return sb.toString();
    }

    public static String charArrayLiteral(CharSequence cs) {
        if (cs == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            sb.append(" \'");
            sb.append(unquotedCharLiteral(cs.charAt(i)));
            sb.append("\',");
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(" }");
        return sb.toString();
    }

}