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

import static nu.validator.checker.bodystyle.CssTokenizer.AT_KEYWORD;
import static nu.validator.checker.bodystyle.CssTokenizer.FUNCTION;
import static nu.validator.checker.bodystyle.CssTokenizer.LEFT_BRACE;
import static nu.validator.checker.bodystyle.CssTokenizer.LEFT_BRACKET;
import static nu.validator.checker.bodystyle.CssTokenizer.LEFT_PAREN;
import static nu.validator.checker.bodystyle.CssTokenizer.RIGHT_BRACE;
import static nu.validator.checker.bodystyle.CssTokenizer.RIGHT_BRACKET;
import static nu.validator.checker.bodystyle.CssTokenizer.RIGHT_PAREN;
import static nu.validator.checker.bodystyle.CssTokenizer.SEMICOLON;
import static nu.validator.checker.bodystyle.CssTokenizer.WHITESPACE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.xml.sax.Attributes;

import nu.validator.checker.bodystyle.CssTokenizer.Token;

/**
 * Checks a "style" element in "body": It must be the first child of its
 * parent (ignoring whitespace and comments), and its style sheet must not
 * have "@import" rules, and each style rule and each "@scope" rule with a
 * scope start in it, other than those in an "@mixin" or "@supports-condition"
 * rule, must be in an "@scope" rule without a scope start.
 *
 * The scoping root of an "@scope" rule without a scope start is the "style"
 * element's parent — so its rules can only style the parent and what comes
 * after the "style" element, never content that might already have been
 * rendered.
 *
 * The contents of "template" elements are skipped; a "style" element in a
 * template (or in a declarative shadow root) isn't checked at all.
 *
 * https://github.com/whatwg/html/pull/13007
 */
public final class BodyStyleTracker {

    private static final String HTML = "http://www.w3.org/1999/xhtml";

    /**
     * Group rules whose blocks can hold style rules. The blocks of any other
     * at-rules (e.g., "@keyframes", "@page", "@font-feature-values") hold
     * declarations or rules of their own that aren't style rules.
     */
    private static final String[] GROUPING_RULES = { "media", "supports",
            "container", "starting-style", "layer", "when", "else",
            "navigation" };

    /**
     * Something the style sheet of a "style" element in "body" isn't allowed
     * to have. Lines and columns are 1-based, and relative to the start of
     * the style sheet; the end column is inclusive.
     */
    public static final class Problem {

        public enum Kind {
            /**
             * A style rule outside an "@scope" rule; the text is its
             * selector list.
             */
            STYLE_RULE,

            /**
             * An "@scope" rule with a scope start; the text is the scope
             * start, with its parentheses.
             */
            SCOPE_START,

            /**
             * An "@import" rule; the text is its name, with the "@".
             */
            AT_RULE
        }

        private final Kind kind;

        private final String text;

        private final int line;

        private final int column;

        private final int endLine;

        private final int endColumn;

        Problem(Kind kind, String text, int line, int column, int endLine,
                int endColumn) {
            this.kind = kind;
            this.text = text;
            this.line = line;
            this.column = column;
            this.endLine = endLine;
            this.endColumn = endColumn;
        }

        public Kind getKind() {
            return kind;
        }

        /**
         * The problem's source text, with each run of whitespace collapsed
         * to a single space.
         */
        public String getText() {
            return text;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }

        public int getEndLine() {
            return endLine;
        }

        public int getEndColumn() {
            return endColumn;
        }
    }

    /**
     * An open element.
     */
    private static final class Frame {
        /**
         * True for "body" and its descendants.
         */
        final boolean inBody;

        boolean hasChild;

        boolean hasNonWhitespaceText;

        Frame(boolean inBody) {
            this.inBody = inBody;
        }
    }

    private final List<Frame> openElements = new ArrayList<>();

    private int templatesDeep;

    /**
     * The text of the "style" element in "body" that's open, or null.
     */
    private StringBuilder styleText;

    public void startDocument() {
        openElements.clear();
        templatesDeep = 0;
        styleText = null;
    }

    private Frame current() {
        return openElements.isEmpty() ? null
                : openElements.get(openElements.size() - 1);
    }

    /**
     * Returns true if the element is a "style" element in "body" that isn't
     * the first child of its parent.
     */
    public boolean startElement(String uri, String localName,
            Attributes atts) {
        boolean isHtml = HTML.equals(uri);
        if (templatesDeep > 0) {
            if (isHtml && "template".equals(localName)) {
                templatesDeep++;
            }
            return false;
        }
        Frame parent = current();
        boolean notFirstChild = false;
        if (isHtml && "style".equals(localName) && parent != null
                && parent.inBody) {
            notFirstChild = parent.hasChild || parent.hasNonWhitespaceText;
            styleText = new StringBuilder();
        }
        if (parent != null) {
            parent.hasChild = true;
        }
        openElements.add(new Frame((parent != null && parent.inBody)
                || (isHtml && "body".equals(localName))));
        if (isHtml && "template".equals(localName)) {
            templatesDeep = 1;
        }
        return notFirstChild;
    }

    public void characters(char[] ch, int start, int length) {
        Frame frame = current();
        if (templatesDeep > 0 || frame == null) {
            return;
        }
        if (styleText != null) {
            styleText.append(ch, start, length);
        }
        if (!frame.hasNonWhitespaceText) {
            for (int i = start; i < start + length; i++) {
                char c = ch[i];
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r'
                        && c != '\f') {
                    frame.hasNonWhitespaceText = true;
                    break;
                }
            }
        }
    }

    /**
     * Closes the current element. If it's a "style" element in "body",
     * returns what its style sheet isn't allowed to have.
     */
    public List<Problem> endElement(String uri, String localName) {
        boolean isHtml = HTML.equals(uri);
        if (templatesDeep > 0) {
            if (isHtml && "template".equals(localName)) {
                templatesDeep--;
            }
            if (templatesDeep > 0) {
                return Collections.emptyList();
            }
        }
        if (openElements.isEmpty()) {
            return Collections.emptyList();
        }
        openElements.remove(openElements.size() - 1);
        if (styleText == null || !isHtml || !"style".equals(localName)) {
            return Collections.emptyList();
        }
        String css = styleText.toString();
        styleText = null;
        List<Token> tokens = CssTokenizer.tokenize(css);
        List<Problem> problems = new ArrayList<>();
        checkBlock(css, tokens, afterLeadingCharset(tokens),
                tokens.size(), problems);
        return problems;
    }

    /**
     * Returns the index just past a "@charset" rule that's the first rule in
     * the style sheet, or 0 if there's none. Parsing a style sheet drops such
     * a rule, so it's never at the top level of the style sheet; Blink
     * (CSSParserImpl::ParseStyleSheet), Gecko (rust-cssparser), and WebKit
     * (CSSParser::parseStyleSheet) all drop it too. Any other "@charset" is
     * invalid, and is left for the CSS checker to report.
     *
     * https://drafts.csswg.org/css-syntax/#parse-stylesheet
     */
    private static int afterLeadingCharset(List<Token> tokens) {
        int i = 0;
        while (i < tokens.size() && (tokens.get(i).type == WHITESPACE
                || tokens.get(i).type == CssTokenizer.CDO
                || tokens.get(i).type == CssTokenizer.CDC)) {
            i++;
        }
        if (i >= tokens.size() || tokens.get(i).type != AT_KEYWORD
                || !"charset".equals(
                        tokens.get(i).value.toLowerCase(Locale.ROOT))) {
            return 0;
        }
        int stop = scan(tokens, i + 1, tokens.size());
        if (stop < tokens.size() && tokens.get(stop).type != SEMICOLON) {
            return 0;
        }
        return stop + 1;
    }

    /**
     * Checks the rules between from and to: the top level of the style
     * sheet, or the block of a group rule that's not in an "@scope" rule
     * without a scope start.
     */
    private static void checkBlock(String css, List<Token> tokens, int from,
            int to, List<Problem> problems) {
        int i = from;
        while (i < to) {
            Token token = tokens.get(i);
            int type = token.type;
            if (type == WHITESPACE || type == SEMICOLON
                    || type == RIGHT_BRACE || type == CssTokenizer.CDO
                    || type == CssTokenizer.CDC) {
                i++;
                continue;
            }
            int stop = scan(tokens, type == AT_KEYWORD ? i + 1 : i, to);
            boolean hasBlock = stop < to
                    && tokens.get(stop).type == LEFT_BRACE;
            int next = hasBlock ? findClose(tokens, stop, to) + 1 : stop + 1;
            if (type != AT_KEYWORD) {
                // A style rule; without a block, it's just junk that the
                // CSS parser reports.
                if (hasBlock) {
                    problems.add(problem(css, Problem.Kind.STYLE_RULE,
                            token.start, lastNonWhitespace(tokens, i, stop)));
                }
            } else {
                String name = token.value.toLowerCase(Locale.ROOT);
                if ("import".equals(name)) {
                    problems.add(problem(css, Problem.Kind.AT_RULE,
                            token.start, token.end));
                } else if (!hasBlock) {
                    // "@namespace", "@layer" statements, "@custom-media",
                    // and the like are all allowed.
                } else if ("scope".equals(name)) {
                    int open = i + 1;
                    while (open < stop
                            && tokens.get(open).type == WHITESPACE) {
                        open++;
                    }
                    if (open < stop && tokens.get(open).type == LEFT_PAREN) {
                        int close = Math.min(findClose(tokens, open, stop),
                                stop - 1);
                        problems.add(problem(css, Problem.Kind.SCOPE_START,
                                tokens.get(open).start,
                                tokens.get(close).end));
                    }
                } else if (isOneOf(name, GROUPING_RULES)) {
                    checkBlock(css, tokens, stop + 1, next - 1, problems);
                }
                // Any other at-rule with a block is allowed, and its block
                // isn't checked: "@mixin" and "@supports-condition" are
                // allowed to hold style rules and "@scope" rules with a scope
                // start, and the blocks of the rest don't hold style rules.
            }
            i = next;
        }
    }

    private static boolean isOneOf(String name, String[] names) {
        for (String candidate : names) {
            if (candidate.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the first semicolon or left brace that's not
     * nested in a block, starting at from — or of a right brace that closes
     * the enclosing block, or to, if there's neither.
     */
    private static int scan(List<Token> tokens, int from, int to) {
        int depth = 0;
        for (int i = from; i < to; i++) {
            int type = tokens.get(i).type;
            if (depth == 0 && (type == SEMICOLON || type == LEFT_BRACE)) {
                return i;
            }
            if (type == LEFT_BRACKET || type == LEFT_PAREN
                    || type == FUNCTION) {
                depth++;
            } else if (type == RIGHT_BRACKET || type == RIGHT_PAREN) {
                depth = Math.max(0, depth - 1);
            } else if (type == RIGHT_BRACE && depth == 0) {
                return i;
            }
        }
        return to;
    }

    /**
     * Returns the index of the token that closes the block or parenthesized
     * group opened at open — or to - 1, if it's never closed.
     */
    private static int findClose(List<Token> tokens, int open, int to) {
        int depth = 0;
        for (int i = open; i < to; i++) {
            int type = tokens.get(i).type;
            if (type == LEFT_BRACE || type == LEFT_BRACKET
                    || type == LEFT_PAREN || type == FUNCTION) {
                depth++;
            } else if (type == RIGHT_BRACE || type == RIGHT_BRACKET
                    || type == RIGHT_PAREN) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return to - 1;
    }

    /**
     * Returns the end offset of the last non-whitespace token from from up
     * to (but not including) to.
     */
    private static int lastNonWhitespace(List<Token> tokens, int from,
            int to) {
        int last = to - 1;
        while (last > from && tokens.get(last).type == WHITESPACE) {
            last--;
        }
        return tokens.get(last).end;
    }

    private static Problem problem(String css, Problem.Kind kind, int start,
            int end) {
        int[] from = lineAndColumn(css, start);
        int[] to = lineAndColumn(css, end - 1);
        String text = css.substring(start, end).replaceAll("\\s+", " ");
        return new Problem(kind, text, from[0], from[1], to[0], to[1]);
    }

    private static int[] lineAndColumn(String css, int offset) {
        int line = 1;
        int lineStart = 0;
        for (int i = 0; i < offset; i++) {
            if (css.charAt(i) == '\n') {
                line++;
                lineStart = i + 1;
            }
        }
        return new int[] { line, offset - lineStart + 1 };
    }
}
