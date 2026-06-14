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
            in.mark(1);
            c = in.read();
            // eat any LF
            if (c != 0x000a) { // U+000A LINE FEED (LF)
                in.reset();
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
            if (esc > 47 && esc < 58) {          // 0-9
                val = (val << 4) + (esc - 48);
            } else if (esc > 64 && esc < 71) {   // A-F
                val = (val << 4) + (esc - 55);
            } else if (esc > 96 && esc < 103) {   // a-f
                val = (val << 4) + (esc - 87);
            } else if (esc == 0x000a || esc == 0x0009 || esc == 0x0020
                    || esc == 0x000d || esc == 0x000c) { // CSS whitespace.
                // U+000A LINE FEED, U+0009 CHARACTER TABULATION, or U+0020 SPACE.
                if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                    if (esc == 0x000d) {
                        // must test if it is followed by a LF
                        in.mark(1);
                        esc = in.read();
                        if (esc != 0x000a) {
                            in.reset();
                        }
                    }
                    return val;
                }
                // whitespace terminated a non-letter escape: don't unescape
                in.reset();
                return c;
            } else if (i == 0) {
                return esc;
            } else {
                if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                    //we must unread 1
                    in.reset();
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
        in.mark(1);
        c = in.read();
        // not a CSS WHITESPACE
        if (c != 0x000a && c != 0x0009 && c != 0x0020
                && c != 0x000d && c != 0x000c) {
            in.reset();
        }
        return val;
    }

    @Override
    public int read(char[] cbuf, int off, int len)
            throws IOException {
        int i, j, k, l, cki;
        boolean ignoreEscape = false;
        boolean in_escape = false;

        if (len == 0) {
            return 0;
        }
        if (len == 1) {
            int c = read();
            if (c == -1) {
                return -1;
            }
            cbuf[off] = (char) c;
            return 1;
        }
        char[] chars = new char[len + 8];
        in.mark(len);
        l = super.read(chars, 0, len);

        if (l <= 0) {
            return l;
        }
        for (i = 0, j = 0; i < l && j < len; i++) {
            // pre-processing
            if (chars[i] == 0x000d) {
                chars[j++] = 0x000a;
                // test for CRLF
                if (i + 1 < l) {
                    if (chars[i + 1] == 0x000a) {
                        i++;
                    }
                } else {
                    // the CR is the last char read, check the stream for a LF
                    in.mark(1);
                    if (in.read() != 0x000a) {
                        in.reset();
                    }
                }
                continue;
            } else if (chars[i] == 0x000c) {
                chars[j++] = 0x000a;
                continue;
            } else if (chars[i] == 0) {
                chars[j++] = 0xfffd;
                continue;
            }
            // escaping

            if (chars[i] == '\\') {
                int val = 0;
                boolean escaped = false;
                ignoreEscape = false;
                in_escape = true;
                for (k = 1; k < 7 && k + i < l; k++) {
                    cki = chars[k + i];
                    if (cki > 47 && cki < 58) {          // 0-9
                        val = (val << 4) + (cki - 48);
                    } else if (cki > 64 && cki < 71) {     // A-F
                        val = (val << 4) + (cki - 55);
                    } else if (cki > 96 && cki < 103) {    // a-f
                        val = (val << 4) + (cki - 87);
                    } else if (cki == 0x000a || cki == 0x0009 || cki == 0x0020 || cki == 0x000c
                            || cki == 0x000d) { // CSS whitespace.
                        // U+000A LINE FEED, U+0009 CHARACTER TABULATION, or U+0020 SPACE.
                        if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                            chars[j++] = (char) val;
                            escaped = true;
                            if (cki == 0x000d) {
                                // CR? => test for LF
                                if (k + i + 1 < l) {
                                    if (chars[k + i + 1] == 0x000a) {
                                        k++;
                                    }
                                } else {
                                    // the CR is the last char read, check the
                                    // stream for a LF
                                    in.mark(1);
                                    if (in.read() != 0x000a) {
                                        in.reset();
                                    }
                                }
                            }
                            i += k;
                        } else {
                            escaped = true;
                            ignoreEscape = true;
                        }
                        in_escape = false;
                        break;
                    } else {
                        if (k == 1) {
                            if ((cki > 96 && cki < 123) || (cki > 64 && cki < 91)) {
                                // so we found a regular char, just remove the escaping
                                ++i;
                                ignoreEscape = true;
                                in_escape = false;
                                break;
                            }
                        }
                        if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                            chars[j++] = (char) val;
                            escaped = true;
                            i += k - 1;
                            in_escape = false;
                            break;
                        } else {
                            ignoreEscape = true;
                            in_escape = false;
                            break;
                        }
                    }
                }
                if (!ignoreEscape) {
                    if (k == 7 && !escaped) {
                        // process 6 digits.
                        if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                            chars[j++] = (char) val;
                            escaped = true;
                            i += k - 1;
                            if (i + 1 < l) {
                                cki = chars[i + 1];
                                // skip extra space
                                if (cki == 0x000a || cki == 0x0009 || cki == 0x0020
                                        || cki == 0x000c || cki == 0x000d) {
                                    i++;
                                    if (cki == 0x000d) {
                                        if (i + 1 < l) {
                                            if (chars[i + 1] == 0x000a) {
                                                i++;
                                            }
                                        } else {
                                            // the CR is the last char read,
                                            // check the stream for a LF
                                            in.mark(1);
                                            if (in.read() != 0x000a) {
                                                in.reset();
                                            }
                                        }
                                    }
                                }
                            } else {
                                // got an escape at the end, check for possible
                                // whitespace to consume from the stream
                                consumeTrailingWhitespace();
                            }
                        } else {
                            // do nothing
                            ignoreEscape = true;
                            chars[j++] = chars[i];
                        }
                        in_escape = false;
                    } else {
                        // we reached the end, unescaping didn't happen let's stop here
                        // unless we are the last
                        if (in_escape && (k + i >= l)) {
                            // get more from the underlying stream
                            // to finish unescaping (or not)
                            int k0 = k;
                            in.mark(8); // at most 6 hexadecimal digits + 1 terminator
                            while (in_escape && k < 7) {
                                int c = in.read();
                                if (c < 0) {
                                    // EOF
                                    if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                                        chars[j++] = (char) val;
                                        i = l - 1; // everything is consumed
                                    } else {
                                        // not a letter: keep it as-is, push
                                        // back the digits read here
                                        in.reset();
                                        chars[j++] = chars[i];
                                    }
                                    in_escape = false;
                                } else if (c > 47 && c < 58) {          // 0-9
                                    val = (val << 4) + (c - 48);
                                    k++;
                                } else if (c > 64 && c < 71) {          // A-F
                                    val = (val << 4) + (c - 55);
                                    k++;
                                } else if (c > 96 && c < 103) {         // a-f
                                    val = (val << 4) + (c - 87);
                                    k++;
                                } else if (c == 0x000a || c == 0x0009
                                        || c == 0x0020 || c == 0x000c || c == 0x000d) {
                                    if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                                        chars[j++] = (char) val;
                                        escaped = true;
                                        if (c == 0x000d) {
                                            // CR? => test for LF
                                            in.mark(1);
                                            if (in.read() != 0x000a) {
                                                in.reset();
                                            }
                                        }
                                        i = l - 1;
                                    } else {
                                        // failed, leave it unescaped:
                                        in.reset();
                                        chars[j++] = chars[i];
                                    }
                                    in_escape = false;
                                } else {
                                    // c terminates the escape and is not a whitespace
                                    if (k == 1 && ((c > 96 && c < 123) || (c > 64 && c < 91))) {
                                        // so we found a regular char, just remove the escaping
                                        chars[j++] = (char) c;
                                        escaped = true;
                                        i = l - 1; // position at the end of escape
                                    } else if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                                        chars[j++] = (char) val;
                                        escaped = true;
                                        // unread c, keep the digits read here
                                        in.reset();
                                        int to_skip = k - k0;
                                        while (to_skip > 0) {
                                            long s = in.skip(to_skip);
                                            if (s <= 0) {
                                                if (in.read() == -1) {
                                                    break;
                                                }
                                                s = 1;
                                            }
                                            to_skip -= s;
                                        }
                                        i = l - 1; // the buffered part is consumed
                                    } else {
                                        // failed, leave it unescaped: push
                                        // back everything read here
                                        in.reset();
                                        chars[j++] = chars[i];
                                    }
                                    in_escape = false;
                                }
                            }
                            if (in_escape) {
                                // we read six hexadecimal digits in total
                                if ((val > 96 && val < 123) || (val > 64 && val < 91)) {
                                    chars[j++] = (char) val;
                                    escaped = true;
                                    consumeTrailingWhitespace();
                                    i = l - 1; // the buffered part is consumed
                                } else {
                                    // failed, leave it unescaped: push back
                                    // the digits read here
                                    in.reset();
                                    chars[j++] = chars[i];
                                }
                                in_escape = false;
                            }
                        } else {
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

    /**
     * Consume one optional whitespace after a decoded escape
     * from the underlying stream
     */
    private void consumeTrailingWhitespace()
            throws IOException {
        int c;
        in.mark(2);
        c = in.read();
        if (c == 0x000d) {
            // must test if it is followed by a LF
            in.mark(1);
            if (in.read() != 0x000a) {
                in.reset();
            }
        } else if (c != 0x000a && c != 0x0009 && c != 0x0020
                && c != 0x000c && c != -1) {
            in.reset();
        }
    }
}
