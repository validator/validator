//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.util;

public class StringUtils {

    private static char hexdigits[] = {'0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'};

    public static String convertStringIndex(String s, int start, int len,
                                            boolean escapeFirst, ApplContext ac)
            throws InvalidParamException {
        int index = start;
        int t;
        int maxCount = 0;
        boolean gotWhiteSpace = false;
        int count = 0;
        if ((start == 0) && (len == s.length()) && (s.indexOf('\\') == -1)) {
            return s;
        }
        StringBuilder buf = new StringBuilder(len);
        maxCount = ((ac.getCssVersion() == CssVersion.CSS1) ? 4 : 6);

        while (index < len) {
            char c = s.charAt(index);
            if (c == '\\') {
                if (++index < len) {
                    c = s.charAt(index);
                    switch (c) {
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            int numValue = Character.digit(c, 16);
                            count = 1;
                            while (index + 1 < len) {
                                c = s.charAt(index + 1);
                                t = Character.digit(c, 16);
                                if (t != -1 && count++ < maxCount) {
                                    numValue = (numValue << 4) | t;
                                    index++;
                                } else {
                                    if (c == ' ' || c == '\t' ||
                                            c == '\n' || c == '\f') {
                                        // skip the latest white space
                                        index++;
                                        gotWhiteSpace = true;
                                    } else if (c == '\r') {
                                        index++;
                                        gotWhiteSpace = true;
                                        // special case for \r\n
                                        if (index + 1 < len) {
                                            if (s.charAt(index + 1) == '\n') {
                                                index++;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            if (!escapeFirst && numValue < 255 && numValue > 31) {
                                if (!((numValue > 96 && numValue < 123) // [a-z]
                                        || (numValue > 64 && numValue < 91) // [A-Z]
                                        || (numValue > 47 && numValue < 58) // [0-9]
                                        || (numValue == 95) // _
                                        || (numValue == 45) // -
                                )
                                ) {
                                    buf.append('\\');
                                }
                                buf.append((char) numValue);
                                break;
                            }
                            char b[];
                            // we fully escape when we have a space
                            if (gotWhiteSpace) {
                                b = new char[maxCount];
                                t = maxCount;
                            } else {
                                b = new char[count];
                                t = count;
                            }
                            while (t > 0) {
                                b[--t] = hexdigits[numValue & 0xF];
                                numValue >>>= 4;
                            }
                            buf.append('\\').append(b);
                            break;
                        case '\n':
                        case '\f':
                            break;
                        case '\r':
                            if (index + 1 < len) {
                                if (s.charAt(index + 1) == '\n') {
                                    index++;
                                }
                            }
                            break;
                        case '-':
                        case '_':
                        case 'g':
                        case 'G':
                        case 'h':
                        case 'H':
                        case 'i':
                        case 'I':
                        case 'j':
                        case 'J':
                        case 'k':
                        case 'K':
                        case 'l':
                        case 'L':
                        case 'm':
                        case 'M':
                        case 'n':
                        case 'N':
                        case 'o':
                        case 'O':
                        case 'p':
                        case 'P':
                        case 'q':
                        case 'Q':
                        case 'r':
                        case 'R':
                        case 's':
                        case 'S':
                        case 't':
                        case 'T':
                        case 'u':
                        case 'U':
                        case 'v':
                        case 'V':
                        case 'w':
                        case 'W':
                        case 'x':
                        case 'X':
                        case 'y':
                        case 'Y':
                        case 'z':
                        case 'Z':
                            buf.append(c);
                            break;
                        default:
                            buf.append('\\').append(c);
                    }
                } else {
                    throw new InvalidParamException("unrecognize", s, ac);
                }
            } else {
                buf.append(c);
            }
            escapeFirst = false;
            index++;
        }
        return buf.toString();
    }

    public static String convertIdent(String s, ApplContext ac)
            throws InvalidParamException {
        return convertStringIndex(s, 0, s.length(), false, ac);
    }

    public static String convertClassIdent(String s, ApplContext ac)
            throws InvalidParamException {
        return convertStringIndex(s, 0, s.length(), true, ac);
    }

    public static String convertString(String s, ApplContext ac)
            throws InvalidParamException {
        return convertStringIndex(s, 0, s.length(), false, ac);
    }
}


