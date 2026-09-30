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
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;

import nu.validator.checker.bodystyle.Selectors.Entry;

/**
 * Checks a "style" element in "body": It must be the first child of its
 * parent (ignoring whitespace and comments), and none of its selectors may
 * match an element that comes before its parent in tree order — since
 * applying its rules to content that's already been parsed (and maybe
 * rendered) means restyling that content.
 *
 * To know what comes before, this records every element it sees, as a
 * lightweight tree. It skips the contents of "template" elements, which
 * aren't in the document tree; a "style" element in a template (or in a
 * declarative shadow root) isn't checked at all.
 *
 * At the "style" end tag, each selector is matched against the tree as it
 * stands then. A match that's uncertain — because it depends on
 * user-interaction state, on content that's not parsed yet, or on
 * something this code doesn't know how to match — counts as a match.
 *
 * https://github.com/whatwg/html/issues/12951
 */
public final class BodyStyleTracker {

    private static final String HTML = "http://www.w3.org/1999/xhtml";

    /**
     * A selector that matches an element before the parent of its "style"
     * element — or an "@import" rule, since the selectors of the style sheet
     * it imports can't be checked. Lines and columns are 1-based, and
     * relative to the start of the style sheet; the end column is inclusive.
     */
    public static final class SelectorProblem {
        private final String selector;

        private final int line;

        private final int column;

        private final int endLine;

        private final int endColumn;

        private final boolean isImport;

        SelectorProblem(String selector, int line, int column, int endLine,
                int endColumn, boolean isImport) {
            this.isImport = isImport;
            this.selector = selector;
            this.line = line;
            this.column = column;
            this.endLine = endLine;
            this.endColumn = endColumn;
        }

        /**
         * The selector's source text (or the whole "@import" rule's), with
         * each run of whitespace collapsed to a single space.
         */
        public String getSelector() {
            return selector;
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

        /**
         * True for an "@import" rule, rather than a selector.
         */
        public boolean isImport() {
            return isImport;
        }
    }

    private final List<TreeElement> elements = new ArrayList<>();

    /**
     * The innermost open element.
     */
    private TreeElement current;

    private int templatesDeep;

    /**
     * The text of the "style" element in "body" that's open, or null.
     */
    private StringBuilder styleText;

    public void startDocument() {
        elements.clear();
        current = null;
        templatesDeep = 0;
        styleText = null;
    }

    /**
     * Records an element. Returns true if it's a "style" element in "body"
     * that isn't the first child of its parent.
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
        boolean notFirstChild = false;
        if (isHtml && "style".equals(localName) && isInBody(current)) {
            notFirstChild = current.firstChild != null
                    || current.hasNonWhitespaceText;
            styleText = new StringBuilder();
        }
        TreeElement element = new TreeElement(uri, localName, atts, current,
                elements.size());
        elements.add(element);
        current = element;
        if (isHtml && "template".equals(localName)) {
            templatesDeep = 1;
        }
        return notFirstChild;
    }

    public void characters(char[] ch, int start, int length) {
        if (templatesDeep > 0 || current == null || length == 0) {
            return;
        }
        if (styleText != null) {
            styleText.append(ch, start, length);
        }
        current.hasText = true;
        if (!current.hasNonWhitespaceText) {
            for (int i = start; i < start + length; i++) {
                char c = ch[i];
                if (c != ' ' && c != '\t' && c != '\n' && c != '\r'
                        && c != '\f') {
                    current.hasNonWhitespaceText = true;
                    break;
                }
            }
        }
    }

    /**
     * Closes the current element. If it's a "style" element in "body",
     * returns the selectors that match an element before its parent.
     */
    public List<SelectorProblem> endElement(String uri, String localName) {
        boolean isHtml = HTML.equals(uri);
        if (templatesDeep > 0) {
            if (isHtml && "template".equals(localName)) {
                templatesDeep--;
            }
            if (templatesDeep > 0) {
                return Collections.emptyList();
            }
        }
        if (current == null) {
            return Collections.emptyList();
        }
        TreeElement element = current;
        element.closed = true;
        current = element.parent;
        if (styleText == null || !isHtml || !"style".equals(localName)) {
            return Collections.emptyList();
        }
        String css = styleText.toString();
        styleText = null;
        return findProblems(css, element.parent);
    }

    private static boolean isInBody(TreeElement element) {
        for (TreeElement e = element; e != null; e = e.parent) {
            if (e.isHtml && "body".equals(e.localName)) {
                return true;
            }
        }
        return false;
    }

    private List<SelectorProblem> findProblems(String css,
            TreeElement parent) {
        List<SelectorProblem> problems = new ArrayList<>();
        SelectorMatcher matcher = new SelectorMatcher(parent);
        SelectorCollector collector = SelectorCollector.collect(css);
        for (int[] range : collector.getImports()) {
            problems.add(problem(css, range[0], range[1], true));
        }
        for (Entry entry : collector.getEntries()) {
            for (int i = 0; i < parent.index; i++) {
                if (matcher.match(entry, elements.get(i))
                        != SelectorMatcher.NO) {
                    problems.add(problem(css, entry.selector.start,
                            entry.selector.end, false));
                    break;
                }
            }
        }
        return problems;
    }

    private static SelectorProblem problem(String css, int start, int end,
            boolean isImport) {
        int[] from = lineAndColumn(css, start);
        int[] to = lineAndColumn(css, end - 1);
        String text = css.substring(start, end).replaceAll("\\s+", " ");
        return new SelectorProblem(text, from[0], from[1], to[0], to[1],
                isImport);
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
