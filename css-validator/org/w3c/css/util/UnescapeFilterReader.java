package org.w3c.css.util;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class UnescapeFilterReader extends FilterReader {

    public UnescapeFilterReader(Reader r) {
        super(r);
    }

    @Override
    public int read()
            throws IOException {
        int esc;
        int c = in.read();
        // https://www.w3.org/TR/css-syntax-3/#input-preprocessing
        if (c == 0x000d) { // U+000D CARRIAGE RETURN (CR)
            mark(1);
            c = in.read();
            // eat any LF
            if (c != 0x000a) { // U+000A LINE FEED (LF)
                reset();
            }
            return 0x000a; // U+000A LINE FEED (LF)
        }
        if (c == 0x000c) { //U+000C FORM FEED (FF)
            return 0x000a;// U+000A LINE FEED (LF)
        }
        if (c == 0) { // U+0000 NULL
            return 0xfffd; // U+FFFD REPLACEMENT CHARACTER
        }

        // now specific case of CSS unicode escape for ascii values [A-Za-z0-9].
        if (c != '\\') {
            return c;
        }
        in.mark(6);
        int val = 0;
        for (int i = 0; i < 6; i++) {
            esc = in.read();
            // 0-9
            if (esc > 47 && esc < 58) {
                val = (val << 4) + (esc - 48);
            } else if (esc > 64 && esc < 71) {
                // A_F
                val = (val << 4) + (esc - 55);
            } else if (esc > 96 && esc < 103) {
                val = (val << 4) + (esc - 87);
            } else if (esc == 10 || esc == 9 || esc == 32) { // CSS whitespace.
                // U+000A LINE FEED, U+0009 CHARACTER TABULATION, or U+0020 SPACE.
                if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                    return val;
                }
            } else {
                if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                    //we must unread 1
                    in.reset();
                    i++;
                    for (int j = 0; j < i; j++) {
                        in.read();
                    }
                    return val;
                }
                in.reset();
                return c;
            }
        }
        // we read up to 6 char test value first
        if ((val <= 96 || val >= 123) && (val <= 64 || val >= 91)) {
            in.reset();
            return c;
        }
        mark(1);
        c = in.read();
        // not a CSS WHITESPACE
        if (c != 10 && c != 9 && c != 32) {
            in.reset();
        }
        return val;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int i, j, k, l, cki;
        boolean ignoreEscape = false;

        char[] chars = new char[len + 8];
        in.mark(len + 8);
        l = super.read(chars, 0, len + 8);

        if (l <= 0) {
            return l;
        }
        for (i = 0, j = 0; i < l && j < len; i++) {
            // pre-processing
            if (chars[i] == 0x000d) {
                chars[j++] = 0x000a;
                // test for CRLF
                if (i + 1 < l && chars[i + 1] == 0x000a) {
                    i++;
                }
            } else if (chars[i] == 0x000c) {
                chars[j++] = 0x000a;
            } else if (chars[i] == 0) {
                chars[j++] = 0xfffd;
            }
            // escaping

            if (chars[i] == '\\') {
                int val = 0;
                boolean escaped = false;
                ignoreEscape = false;
                for (k = 1; k < 7 && k + i < l; k++) {
                    cki = chars[k + i];
                    // 0-9
                    if (cki > 47 && cki < 58) {
                        val = (val << 4) + (cki - 48);
                    } else if (cki > 64 && cki < 71) {
                        // A_F
                        val = (val << 4) + (cki - 55);
                    } else if (cki > 96 && cki < 103) {
                        val = (val << 4) + (cki - 87);
                    } else if (cki == 10 || cki == 9 || cki == 32) { // CSS whitespace.
                        // U+000A LINE FEED, U+0009 CHARACTER TABULATION, or U+0020 SPACE.
                        if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                            chars[j++] = (char) val;
                            escaped = true;
                            i += k;
                        } else {
                            escaped = true;
                            ignoreEscape = true;
                        }
                        break;
                    } else {
                        if (val == 0) {
                            if ((cki > 96 && cki < 123) || (cki > 64 && cki < 91)) {
                                // so we found a regular char, just remove the escaping
                                ++i;
                                ignoreEscape = true;
                                break;
                            }
                        }
                        if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                            chars[j++] = (char) val;
                            escaped = true;
                            i += k - 1;
                            break;
                        } else {
                            ignoreEscape = true;
                            break;
                        }
                    }
                }
                if (!ignoreEscape) {
                    if (k == 7 && !escaped) {
                        if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                            chars[j++] = (char) val;
                            escaped = true;
                            i += k - 1;
                            if (i + 1 < l) {
                                cki = chars[i + 1];
                                // skip extra space
                                if (cki == 10 || cki == 9 || cki == 32) {
                                    i++;
                                }
                            }
                        } else {
                            // do nothing
                            ignoreEscape = true;
                            chars[j++] = chars[i];
                        }
                    } else {
                        // we reached the end, unescaping didn't happen let's stop here
                        // unless we are the last
                        if (!escaped) {
                            if (j != 0) {
                                in.reset();
                                in.skip(i);
                                break;
                            } else {
                                chars[j++] = chars[i];
                            }
                        }
                    }
                } else {
                    chars[j++] = chars[i];
                }
            } else {
                chars[j++] = chars[i];
            }
        }

        System.arraycopy(chars, 0, cbuf, off, j);
        // as we read more to read a past escaped character, we must "unread" those.
        if (i < l) {
            in.reset();
            in.skip(i);
        }
        return j;
    }
}
