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

package nu.validator.checker.test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.checker.bodystyle.BodyStyleTracker;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * Unit tests for BodyStyleTracker, which checks a "style" element in "body":
 * It must be the first child of its parent, and its style sheet must not
 * have "@import" rules, and each style rule and each "@scope" rule with a
 * scope start in it, other than those in an "@mixin" or "@supports-condition"
 * rule, must be in an "@scope" rule without a scope start.
 *
 * See: https://github.com/validator/validator/issues/2143
 * See: https://github.com/whatwg/html/pull/13007
 */
public class BodyStyleTrackerTest {

    private static int passed = 0;

    private static int failed = 0;

    private static final class Result {
        final List<String> problems = new ArrayList<>();

        final List<String> positions = new ArrayList<>();

        int notFirstChild = 0;
    }

    private static Result run(String html) throws Exception {
        final Result result = new Result();
        final BodyStyleTracker tracker = new BodyStyleTracker();
        HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
        parser.setContentHandler(new DefaultHandler() {
            @Override
            public void startDocument() {
                tracker.startDocument();
            }

            @Override
            public void startElement(String uri, String localName,
                    String qName, Attributes atts) {
                if (tracker.startElement(uri, localName, atts)) {
                    result.notFirstChild++;
                }
            }

            @Override
            public void endElement(String uri, String localName,
                    String qName) {
                for (BodyStyleTracker.Problem problem : tracker.endElement(
                        uri, localName)) {
                    result.problems.add(problem.getKind() + ": "
                            + problem.getText());
                    result.positions.add(problem.getLine() + "."
                            + problem.getColumn() + "-" + problem.getEndLine()
                            + "." + problem.getEndColumn());
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) {
                tracker.characters(ch, start, length);
            }
        });
        parser.parse(new InputSource(new StringReader(html)));
        return result;
    }

    private static String doc(String body) {
        return "<!doctype html><html lang=en><title>t</title><body>" + body;
    }

    /**
     * A document with a "style" element in "body" holding the given style
     * sheet.
     */
    private static String sheet(String css) {
        return doc("<div><style>" + css + "</style></div>");
    }

    private static void expectProblems(String name, String html,
            String... expected) throws Exception {
        Result result = run(html);
        List<String> want = Arrays.asList(expected);
        check(name, want.equals(result.problems),
                "expected " + want + " but got " + result.problems);
    }

    private static void expectNotFirstChild(String name, String html,
            int count) throws Exception {
        Result result = run(html);
        check(name, result.notFirstChild == count, "expected " + count
                + " first-child problems but got " + result.notFirstChild);
    }

    private static void expectPositions(String name, String html,
            String... expected) throws Exception {
        Result result = run(html);
        List<String> want = Arrays.asList(expected);
        check(name, want.equals(result.positions),
                "expected " + want + " but got " + result.positions);
    }

    private static void check(String name, boolean ok, String detail) {
        if (ok) {
            passed++;
            System.out.println("  PASS: " + name);
        } else {
            failed++;
            System.out.println("  FAIL: " + name + " (" + detail + ")");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Testing where a style element is checked...");
        expectProblems("style in head is not checked",
                "<!doctype html><title>t</title><style>p { }</style><p>x");
        expectProblems("style in template contents is not checked",
                doc("<div><template><style>p { }</style></template></div>"));
        expectProblems("style in a declarative shadow root is not checked",
                doc("<div><template shadowrootmode=open>"
                        + "<style>p { }</style><p>y</p></template></div>"));
        expectProblems("SVG style is not checked",
                doc("<svg><style>p { }</style></svg>"));
        expectProblems("style directly in body is checked",
                doc("<style>p { }</style>"), "STYLE_RULE: p");

        System.out.println();
        System.out.println("Testing the first-child requirement...");
        expectNotFirstChild("first child of div",
                doc("<div><style></style><p>x</p></div>"), 0);
        expectNotFirstChild("whitespace and a comment before it",
                doc("<div>\n  <!-- c -->\n  <style></style></div>"), 0);
        expectNotFirstChild("first child of body",
                "<!doctype html><title>t</title><body>\n<style></style>", 0);
        expectNotFirstChild("text before it",
                doc("<div>text<style></style></div>"), 1);
        expectNotFirstChild("an element before it",
                doc("<div><span></span><style></style></div>"), 1);
        expectNotFirstChild("a second style element",
                doc("<div><style></style><style></style></div>"), 1);
        expectNotFirstChild("style in head is never a problem",
                "<!doctype html><title>t</title><style></style>", 0);
        expectNotFirstChild("first child of a template's contents",
                doc("<div><template><p></p><style></style></template>"
                        + "</div>"),
                0);

        System.out.println();
        System.out.println("Testing what the style sheet allows...");
        expectProblems("empty style sheet", sheet(""));
        expectProblems("comments and whitespace only",
                sheet("\n  /* nothing */\n"));
        expectProblems("@scope without a scope start",
                sheet("@scope { :scope { } p { } & > b { } }"));
        expectProblems("@scope with only a scope end",
                sheet("@scope to (.x) { p { } }"));
        expectProblems("@scope nested in allowed group rules",
                sheet("@media screen { @supports (display: grid) {"
                        + " @container (width > 1px) { @starting-style {"
                        + " @layer base { @layer { @scope { p { } } } } } }"
                        + " } }"));
        expectProblems("@namespace rules and @layer statements",
                sheet("@namespace svg url(http://www.w3.org/2000/svg);"
                        + " @layer a, b; @layer c;"));
        expectProblems("anything goes inside an allowed @scope",
                sheet("@scope { @scope (.x) to (.y) { p { } }"
                        + " @media print { p { } } }"));
        expectProblems("a leading @charset, which parsing drops",
                sheet("@charset \"utf-8\"; @scope { p { } }"));
        expectProblems("a leading @charset after whitespace and a comment",
                sheet("\n  /* c */ @CHARSET \"utf-8\"; @scope { }"));
        expectProblems("at-rule names are case-insensitive",
                sheet("@MEDIA screen { @Scope { p { } } }"));
        expectProblems("at-rules with global effects",
                sheet("@font-face { font-family: x } @keyframes k { }"
                        + " @property --x { syntax: '*' } @page { }"
                        + " @counter-style x { } @font-palette-values --p"
                        + " { } @font-feature-values x { }"));
        expectProblems("more at-rules with global effects",
                sheet("@view-transition { navigation: auto }"
                        + " @position-try --p { top: anchor(bottom) }"
                        + " @color-profile --c { src: url(c.icc) }"
                        + " @custom-media --narrow (width < 30em);"
                        + " @function --f(--x) { @media (width > 1px) {"
                        + " result: 2 } result: 1 }"));
        expectProblems("keyframe rules aren't style rules",
                sheet("@keyframes k { from { } 50% { } to { } }"
                        + " @-webkit-keyframes k { from { } to { } }"));
        expectProblems("@page with page selectors and margin rules",
                sheet("@page :first { margin: 1cm; @top-left {"
                        + " content: 'x' } }"));
        expectProblems("@font-feature-values with feature value blocks",
                sheet("@font-feature-values Font One { @swash {"
                        + " fancy: 1 } }"));
        expectProblems("at-rules with global effects in allowed group rules",
                sheet("@media print { @font-face { font-family: x } }"
                        + " @layer x { @supports (display: grid) {"
                        + " @keyframes k { from { } } } }"));
        expectProblems("at-rules with global effects in an allowed @scope",
                sheet("@scope { @font-face { font-family: x }"
                        + " @keyframes k { from { } } }"));
        expectProblems("style rules and @scope with a scope start in @mixin",
                sheet("@mixin --m { p { } & b { } @scope (.x) { p { } } }"
                        + " @mixin --n(--x) { @media print { p { } } }"));
        expectProblems("style rules in @supports-condition",
                sheet("@supports-condition --nesting { & { } p { } }"
                        + " @supports-condition --s { @scope (.x) { } }"));
        expectProblems("@mixin and @supports-condition in a group rule",
                sheet("@media print { @mixin --m { p { } }"
                        + " @supports-condition --c { p { } } }"));
        expectProblems("@mixin and @supports-condition are case-insensitive",
                sheet("@MIXIN --m { p { } } @Supports-Condition --c {"
                        + " p { } }"));
        expectProblems("@charset that's not the first rule",
                sheet("@scope { } @charset \"utf-8\";"));
        expectProblems("a second @charset",
                sheet("@charset \"utf-8\"; @charset \"utf-8\";"));
        expectProblems("@charset in a group rule",
                sheet("@media print { @charset \"utf-8\"; }"));
        expectProblems("unknown at-rules without style rules",
                sheet("@frobnicate { } @whatever;"));
        expectProblems("a block with no prelude isn't a style rule",
                sheet("{`p { color: red }`}"));
        expectProblems("a block with no prelude after whitespace",
                sheet("\n  { p { } }"));
        expectProblems("a block with no prelude in an allowed group rule",
                sheet("@media print { { } }"));
        expectProblems("a block with no prelude after a style rule",
                sheet("p { } { }"), "STYLE_RULE: p");

        System.out.println();
        System.out.println("Testing what the style sheet doesn't allow...");
        expectProblems("a style rule", sheet("p { color: red }"),
                "STYLE_RULE: p");
        expectProblems("a style rule with a selector list",
                sheet(".a,\n  .b > p { }"), "STYLE_RULE: .a, .b > p");
        expectProblems("a style rule with nested rules",
                sheet(".a { & p { } }"), "STYLE_RULE: .a");
        expectProblems("a style rule in an allowed group rule",
                sheet("@media screen { @layer x { p { } } }"),
                "STYLE_RULE: p");
        expectProblems("@scope with a scope start",
                sheet("@scope (.card) { p { } }"),
                "SCOPE_START: (.card)");
        expectProblems("@scope with a scope start and a scope end",
                sheet("@scope (.a) to (.b) { p { } }"),
                "SCOPE_START: (.a)");
        expectProblems("@scope with a scope start in a group rule",
                sheet("@supports (display: grid) { @scope (.a) { } }"),
                "SCOPE_START: (.a)");
        expectProblems("@import", sheet("@import url(x.css);"),
                "AT_RULE: @import");
        expectProblems("@IMPORT", sheet("@IMPORT url(x.css);"),
                "AT_RULE: @IMPORT");
        expectProblems("a style rule in @when and @else",
                sheet("@when media(screen) { p { } } @else { b { } }"),
                "STYLE_RULE: p", "STYLE_RULE: b");
        expectProblems("a style rule in @navigation",
                sheet("@navigation (at: --x) { p { } }"), "STYLE_RULE: p");
        expectProblems("@scope with a scope start in @navigation",
                sheet("@navigation (at: --x) { @scope (.a) { } }"),
                "SCOPE_START: (.a)");
        expectProblems("a style rule in @when in an allowed group rule",
                sheet("@media print { @when media(screen) { p { } } }"),
                "STYLE_RULE: p");
        expectProblems("everything after an allowed rule is still checked",
                sheet("@scope { p { } } p { } @scope { } @scope (.a) { }"),
                "STYLE_RULE: p", "SCOPE_START: (.a)");
        expectProblems("everything after @mixin is still checked",
                sheet("@mixin --m { p { } } b { } @supports-condition --c {"
                        + " p { } } i { }"),
                "STYLE_RULE: b", "STYLE_RULE: i");
        expectProblems("everything after a global at-rule is still checked",
                sheet("@keyframes k { from { } } p { } @font-face {"
                        + " font-family: x } @scope (.a) { }"),
                "STYLE_RULE: p", "SCOPE_START: (.a)");

        System.out.println();
        System.out.println("Testing reported positions...");
        expectPositions("positions of problems",
                sheet("@import url(x);\n@scope { }\np,\n  .a { }\n"
                        + "  @scope (.x) { }"),
                "1.1-1.7", "3.1-4.4", "5.10-5.13");

        System.out.println();
        System.out.println(
                "Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
