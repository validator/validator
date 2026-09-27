/*
 * Copyright (c) 2026 Mozilla Foundation
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


package nu.validator.checker.bodystyle;

import java.util.ArrayList;
import java.util.List;

/**
 * A tokenizer following the CSS Syntax Module's tokenization algorithm,
 * closely enough to find rules and selectors. Comments are dropped, and
 * every token records its start and end offsets in the source.
 *
 * https://drafts.csswg.org/css-syntax/#tokenization
 */
final class CssTokenizer {

    static final int IDENT = 1;

    static final int FUNCTION = 2;

    static final int AT_KEYWORD = 3;

    static final int HASH = 4;

    static final int STRING = 5;

    static final int BAD_STRING = 6;

    static final int URL = 7;

    static final int BAD_URL = 8;

    static final int DELIM = 9;

    static final int NUMBER = 10;

    static final int PERCENTAGE = 11;

    static final int DIMENSION = 12;

    static final int WHITESPACE = 13;

    static final int CDO = 14;

    static final int CDC = 15;

    static final int COLON = 16;

    static final int SEMICOLON = 17;

    static final int COMMA = 18;

    static final int LEFT_BRACKET = 19;

    static final int RIGHT_BRACKET = 20;

    static final int LEFT_PAREN = 21;

    static final int RIGHT_PAREN = 22;

    static final int LEFT_BRACE = 23;

    static final int RIGHT_BRACE = 24;

    static final class Token {
        final int type;

        /**
         * The unescaped name for IDENT, FUNCTION, AT_KEYWORD, and HASH; the
         * unescaped value for STRING and URL; the character for DELIM; and
         * the source text for everything else.
         */
        final String value;

        final int start;

        final int end;

        Token(int type, String value, int start, int end) {
            this.type = type;
            this.value = value;
            this.start = start;
            this.end = end;
        }

        boolean isDelim(char c) {
            return type == DELIM && value.charAt(0) == c;
        }

        @Override
        public String toString() {
            return type + ":" + value;
        }
    }

    private final String css;

    private int pos;

    private final List<Token> tokens = new ArrayList<>();

    private CssTokenizer(String css) {
        this.css = css;
    }

    static List<Token> tokenize(String css) {
        CssTokenizer tokenizer = new CssTokenizer(css);
        tokenizer.run();
        return tokenizer.tokens;
    }

    private char at(int i) {
        return i < css.length() ? css.charAt(i) : '\0';
    }

    private boolean atEnd(int i) {
        return i >= css.length();
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static boolean isNameStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
                || c >= 0x80;
    }

    private static boolean isName(char c) {
        return isNameStart(c) || isDigit(c) || c == '-';
    }

    private boolean isValidEscape(int i) {
        return at(i) == '\\' && !atEnd(i + 1) && at(i + 1) != '\n';
    }

    private boolean startsIdent(int i) {
        char c = at(i);
        if (c == '-') {
            return (!atEnd(i + 1) && (isNameStart(at(i + 1))
                    || at(i + 1) == '-')) || isValidEscape(i + 1);
        }
        if (isNameStart(c) && !atEnd(i)) {
            return true;
        }
        return isValidEscape(i);
    }

    private boolean startsNumber(int i) {
        char c = at(i);
        if (c == '+' || c == '-') {
            return isDigit(at(i + 1))
                    || (at(i + 1) == '.' && isDigit(at(i + 2)));
        }
        if (c == '.') {
            return isDigit(at(i + 1));
        }
        return isDigit(c) && !atEnd(i);
    }

    /**
     * Consumes an escape whose backslash has already been consumed, and
     * appends the escaped code point.
     */
    private void consumeEscape(StringBuilder sb) {
        if (atEnd(pos)) {
            sb.append('�');
            return;
        }
        char c = at(pos);
        if (isHexDigit(c)) {
            int start = pos;
            while (pos < css.length() && pos - start < 6
                    && isHexDigit(at(pos))) {
                pos++;
            }
            int cp = Integer.parseInt(css.substring(start, pos), 16);
            if (isWhitespace(at(pos)) && !atEnd(pos)) {
                pos++;
            }
            if (cp == 0 || cp > 0x10FFFF || (cp >= 0xD800 && cp <= 0xDFFF)) {
                cp = 0xFFFD;
            }
            sb.appendCodePoint(cp);
            return;
        }
        sb.append(c);
        pos++;
    }

    private String consumeName() {
        StringBuilder sb = new StringBuilder();
        while (!atEnd(pos)) {
            char c = at(pos);
            if (isName(c)) {
                sb.append(c);
                pos++;
            } else if (isValidEscape(pos)) {
                pos++;
                consumeEscape(sb);
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private void add(int type, String value, int start) {
        tokens.add(new Token(type, value, start, pos));
    }

    private void run() {
        while (!atEnd(pos)) {
            int start = pos;
            char c = at(pos);
            if (c == '/' && at(pos + 1) == '*') {
                int close = css.indexOf("*/", pos + 2);
                pos = close < 0 ? css.length() : close + 2;
                continue;
            }
            if (isWhitespace(c)) {
                while (!atEnd(pos) && isWhitespace(at(pos))) {
                    pos++;
                }
                add(WHITESPACE, " ", start);
            } else if (c == '"' || c == '\'') {
                consumeString(c, start);
            } else if (c == '#') {
                if (!atEnd(pos + 1)
                        && (isName(at(pos + 1)) || isValidEscape(pos + 1))) {
                    pos++;
                    add(HASH, consumeName(), start);
                } else {
                    pos++;
                    add(DELIM, "#", start);
                }
            } else if (c == '(') {
                pos++;
                add(LEFT_PAREN, "(", start);
            } else if (c == ')') {
                pos++;
                add(RIGHT_PAREN, ")", start);
            } else if (c == '[') {
                pos++;
                add(LEFT_BRACKET, "[", start);
            } else if (c == ']') {
                pos++;
                add(RIGHT_BRACKET, "]", start);
            } else if (c == '{') {
                pos++;
                add(LEFT_BRACE, "{", start);
            } else if (c == '}') {
                pos++;
                add(RIGHT_BRACE, "}", start);
            } else if (c == ',') {
                pos++;
                add(COMMA, ",", start);
            } else if (c == ':') {
                pos++;
                add(COLON, ":", start);
            } else if (c == ';') {
                pos++;
                add(SEMICOLON, ";", start);
            } else if ((c == '+' || c == '.') && startsNumber(pos)) {
                consumeNumeric(start);
            } else if (c == '-') {
                if (startsNumber(pos)) {
                    consumeNumeric(start);
                } else if (at(pos + 1) == '-' && at(pos + 2) == '>') {
                    pos += 3;
                    add(CDC, "-->", start);
                } else if (startsIdent(pos)) {
                    consumeIdentLike(start);
                } else {
                    pos++;
                    add(DELIM, "-", start);
                }
            } else if (c == '<' && css.startsWith("<!--", pos)) {
                pos += 4;
                add(CDO, "<!--", start);
            } else if (c == '@') {
                if (startsIdent(pos + 1)) {
                    pos++;
                    add(AT_KEYWORD, consumeName(), start);
                } else {
                    pos++;
                    add(DELIM, "@", start);
                }
            } else if (c == '\\') {
                if (isValidEscape(pos)) {
                    consumeIdentLike(start);
                } else {
                    pos++;
                    add(DELIM, "\\", start);
                }
            } else if (isDigit(c)) {
                consumeNumeric(start);
            } else if (isNameStart(c)) {
                consumeIdentLike(start);
            } else {
                pos++;
                add(DELIM, String.valueOf(c), start);
            }
        }
    }

    private void consumeString(char quote, int start) {
        pos++;
        StringBuilder sb = new StringBuilder();
        while (!atEnd(pos)) {
            char c = at(pos);
            if (c == quote) {
                pos++;
                add(STRING, sb.toString(), start);
                return;
            }
            if (c == '\n') {
                add(BAD_STRING, sb.toString(), start);
                return;
            }
            if (c == '\\') {
                if (atEnd(pos + 1)) {
                    pos++;
                } else if (at(pos + 1) == '\n') {
                    pos += 2;
                } else {
                    pos++;
                    consumeEscape(sb);
                }
                continue;
            }
            sb.append(c);
            pos++;
        }
        add(STRING, sb.toString(), start);
    }

    private void consumeNumeric(int start) {
        if (at(pos) == '+' || at(pos) == '-') {
            pos++;
        }
        while (isDigit(at(pos)) && !atEnd(pos)) {
            pos++;
        }
        if (at(pos) == '.' && isDigit(at(pos + 1))) {
            pos++;
            while (isDigit(at(pos)) && !atEnd(pos)) {
                pos++;
            }
        }
        if ((at(pos) == 'e' || at(pos) == 'E')
                && (isDigit(at(pos + 1)) || ((at(pos + 1) == '+'
                        || at(pos + 1) == '-') && isDigit(at(pos + 2))))) {
            pos += 2;
            while (isDigit(at(pos)) && !atEnd(pos)) {
                pos++;
            }
        }
        if (startsIdent(pos)) {
            consumeName();
            add(DIMENSION, css.substring(start, pos), start);
        } else if (at(pos) == '%' && !atEnd(pos)) {
            pos++;
            add(PERCENTAGE, css.substring(start, pos), start);
        } else {
            add(NUMBER, css.substring(start, pos), start);
        }
    }

    private void consumeIdentLike(int start) {
        String name = consumeName();
        if (at(pos) != '(' || atEnd(pos)) {
            add(IDENT, name, start);
            return;
        }
        pos++;
        if (!name.equalsIgnoreCase("url")) {
            add(FUNCTION, name, start);
            return;
        }
        int p = pos;
        while (isWhitespace(at(p)) && !atEnd(p)) {
            p++;
        }
        if (at(p) == '"' || at(p) == '\'') {
            add(FUNCTION, name, start);
            return;
        }
        pos = p;
        StringBuilder sb = new StringBuilder();
        while (!atEnd(pos)) {
            char c = at(pos);
            if (c == ')') {
                pos++;
                add(URL, sb.toString(), start);
                return;
            }
            if (isWhitespace(c)) {
                int q = pos;
                while (isWhitespace(at(q)) && !atEnd(q)) {
                    q++;
                }
                if (atEnd(q) || at(q) == ')') {
                    pos = atEnd(q) ? q : q + 1;
                    add(URL, sb.toString(), start);
                    return;
                }
            }
            if (isWhitespace(c) || c == '"' || c == '\'' || c == '(') {
                // Consume the remnants of a bad URL.
                while (!atEnd(pos) && at(pos) != ')') {
                    if (isValidEscape(pos)) {
                        pos++;
                    }
                    pos++;
                }
                if (!atEnd(pos)) {
                    pos++;
                }
                add(BAD_URL, sb.toString(), start);
                return;
            }
            if (c == '\\') {
                pos++;
                consumeEscape(sb);
                continue;
            }
            sb.append(c);
            pos++;
        }
        add(URL, sb.toString(), start);
    }
}
