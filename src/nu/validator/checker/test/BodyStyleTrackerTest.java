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
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import nu.validator.checker.bodystyle.BodyStyleTracker;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * Unit tests for BodyStyleTracker, which checks a "style" element in "body":
 * It must be the first child of its parent, and none of its selectors may
 * match an element that comes before its parent in tree order.
 *
 * The tests with @scope and CSS nesting can't be document tests, since the
 * css-validator rejects both.
 *
 * See: https://github.com/validator/validator/issues/2143
 * See: https://github.com/whatwg/html/issues/12951
 */
public class BodyStyleTrackerTest {

    private static int passed = 0;

    private static int failed = 0;

    private static final class Result {
        final List<String> selectors = new ArrayList<>();

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
                for (BodyStyleTracker.SelectorProblem problem : tracker.endElement(
                        uri, localName)) {
                    result.selectors.add((problem.isImport() ? "import: " : "")
                            + problem.getSelector());
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

    private static void expectFlagged(String name, String html,
            String... expected) throws Exception {
        Result result = run(html);
        List<String> want = new ArrayList<>();
        for (String s : expected) {
            want.add(s);
        }
        check(name, want.equals(result.selectors),
                "expected " + want + " but got " + result.selectors);
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
        List<String> want = new ArrayList<>();
        for (String s : expected) {
            want.add(s);
        }
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
        expectFlagged("style in head is not checked",
                "<!doctype html><title>t</title><style>p { }</style><p>x",
                new String[0]);
        expectFlagged("style in template contents is not checked",
                doc("<p>x</p><div><template><style>p { }</style></template>"
                        + "</div>"),
                new String[0]);
        expectFlagged("style in a declarative shadow root is not checked",
                doc("<p>x</p><div><template shadowrootmode=open>"
                        + "<style>p { }</style><p>y</p></template></div>"),
                new String[0]);
        expectFlagged("SVG style is not checked",
                doc("<p>x</p><svg><style>p { }</style></svg>"),
                new String[0]);

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

        System.out.println();
        System.out.println("Testing selectors against earlier elements...");
        expectFlagged("type selector matching an earlier element",
                doc("<p>x</p><div><style>p { color: red }</style></div>"),
                "p");
        expectFlagged("type selector matching only later elements",
                doc("<div><style>p { color: red }</style><p>x</p></div>"),
                new String[0]);
        expectFlagged("type selector matching only the parent",
                doc("<div><style>div { }</style></div>"), new String[0]);
        expectFlagged("type selector matching an ancestor",
                doc("<div><style>body { }</style></div>"), "body");
        expectFlagged("universal selector", doc("<div><style>* { }</style>"),
                "*");
        expectFlagged(":root", doc("<div><style>:root { }</style>"), ":root");
        expectFlagged("class only on later elements",
                doc("<p>x</p><div><style>.later { }</style>"
                        + "<p class=later>y</p></div>"),
                new String[0]);
        expectFlagged("class on an earlier element",
                doc("<p class='a later'>x</p><div><style>.later { }</style>"),
                ".later");
        expectFlagged("id on an earlier element",
                doc("<p id=x>x</p><div><style>#x { } #y { }</style>"), "#x");
        expectFlagged("selector list reports each selector",
                doc("<p>x</p><div><style>.later, p , span { }</style>"),
                "p");
        expectFlagged("descendant combinator",
                doc("<section><p>x</p></section><div><style>section p { }"
                        + " div p { }</style>"),
                "section p");
        expectFlagged("child combinator",
                doc("<section><div><p>x</p></div></section><div><style>"
                        + "section > p { } div > p { }</style>"),
                "div > p");
        expectFlagged("next-sibling combinator",
                doc("<h2>x</h2><p>y</p><div><style>h2 + p { } h2 + div p"
                        + " { }</style>"),
                "h2 + p");
        expectFlagged("subsequent-sibling combinator",
                doc("<h2>x</h2><span></span><p>y</p><div><style>h2 ~ p { }"
                        + " p ~ h2 { }</style>"),
                "h2 ~ p");
        expectFlagged("attribute selectors",
                doc("<input type=text lang=en-US data-x='a b'><div><style>"
                        + "[type] { } [type=text] { } [type=radio] { }"
                        + " [lang|=en] { } [data-x~=b] { } [data-x^=a] { }"
                        + " [data-x$=c] { } [data-x*=' '] { }"
                        + " [type=TEXT i] { } [data-x='A B' s] { }</style>"),
                "[type]", "[type=text]", "[lang|=en]", "[data-x~=b]",
                "[data-x^=a]", "[data-x*=' ']", "[type=TEXT i]");
        expectFlagged(":not() and :is()",
                doc("<p class=a>x</p><div><style>p:not(.a) { } p:is(.a, .b)"
                        + " { } :where(.b) { }</style>"),
                "p:is(.a, .b)");
        expectFlagged("pseudo-element on an earlier element",
                doc("<p>x</p><div><style>p::before { } p:after { }"
                        + " span::after { }</style>"),
                "p::before", "p:after");
        expectFlagged("state pseudo-class counts as a match",
                doc("<a href=#>x</a><div><style>a:hover { } b:hover { }"
                        + "</style>"),
                "a:hover");
        expectFlagged(":any-link and :link on an HTML link",
                doc("<a href=#>x</a><a>y</a><div><style>a:any-link { }"
                        + " a:link { } a:not(:any-link) { } p:any-link { }"
                        + "</style>"),
                "a:any-link", "a:link", "a:not(:any-link)");
        expectFlagged(":any-link on an SVG link",
                doc("<svg><a href=x></a><a xlink:href=y></a></svg><div>"
                        + "<style>a:any-link { } a:link { }</style>"),
                "a:any-link", "a:link");
        expectFlagged(":any-link on an SVG a element with no href",
                doc("<svg><a></a></svg><div><style>a:any-link { }"
                        + "</style>"),
                new String[0]);
        expectFlagged(":any-link on a MathML element with href",
                doc("<math><mi href=x>x</mi><mo>+</mo></math><div><style>"
                        + "mi:any-link { } mo:any-link { }</style>"),
                "mi:any-link");
        expectFlagged("unknown pseudo-class counts as a match",
                doc("<p>x</p><div><style>p:frobnicate { } span:frobnicate"
                        + " { }</style>"),
                "p:frobnicate");
        expectFlagged("comments and at-rules without selectors",
                doc("<p>x</p><div><style>/* p { } */"
                        + " @font-face { font-family: x } @keyframes k {"
                        + " from { } to { } } @page :first { }"
                        + " @layer a, b;</style>"),
                new String[0]);
        expectFlagged("conditional group rules",
                doc("<p>x</p><div><style>@media screen { p { } .later { } }"
                        + " @supports (display: grid) { @layer l { p > b { }"
                        + " p { } } } @container (width > 1px) { p { } }"
                        + "</style>"),
                "p", "p", "p");

        System.out.println();
        System.out.println("Testing structural pseudo-classes...");
        expectFlagged(":first-child and :nth-child()",
                doc("<section><p>a</p><p>b</p></section><div><style>"
                        + "p:first-child { } p:nth-child(3) { }"
                        + " p:nth-child(2n) { } span:first-child { }</style>"),
                "p:first-child", "p:nth-child(2n)");
        expectFlagged(":last-child in a closed parent",
                doc("<section><p>a</p><p>b</p></section><div><style>"
                        + "p:first-child:last-child { } p:last-child { }"
                        + "</style>"),
                "p:last-child");
        expectFlagged(":last-of-type in a still-open parent",
                doc("<p>a</p><div><style>p:last-of-type { }</style>"),
                "p:last-of-type");
        expectFlagged(":last-child with a later sibling already parsed",
                doc("<p>a</p><div><style>p:last-child { }</style>"),
                new String[0]);
        expectFlagged(":nth-last-child() in a still-open parent",
                doc("<p>a</p><div><style>p:nth-last-child(5) { }</style>"),
                "p:nth-last-child(5)");
        expectFlagged(":empty",
                doc("<p></p><b>x</b><div><style>p:empty { } b:empty { }"
                        + " body:empty { }</style>"),
                "p:empty");
        expectFlagged(":nth-child(of S)",
                doc("<section><p class=a>a</p><p>b</p><p class=a>c</p>"
                        + "</section><div><style>:nth-child(2 of .a) { }"
                        + " :nth-child(3 of .a) { }</style>"),
                ":nth-child(2 of .a)");

        System.out.println();
        System.out.println("Testing :has()...");
        expectFlagged(":has() with complete subtrees",
                doc("<section><h2>x</h2></section><div><style>"
                        + "section:has(h2) { } section:has(h3) { }"
                        + " section:has(> h2) { }</style>"),
                "section:has(h2)", "section:has(> h2)");
        expectFlagged(":has() looking at later siblings of an earlier element",
                doc("<h2>x</h2><div><style>h2:has(~ .later) { }"
                        + " h2:has(+ .later) { }</style><p class=later>y"),
                "h2:has(~ .later)");
        expectFlagged(":has() on an ancestor",
                doc("<div><style>body:has(.later) { }</style>"
                        + "<p class=later>y"),
                "body:has(.later)");
        expectFlagged(":has() with later siblings in a closed parent",
                doc("<section><h2>x</h2><p>y</p></section><div><style>"
                        + "h2:has(+ .later) { } h2:has(+ p) { }</style>"),
                "h2:has(+ p)");

        System.out.println();
        System.out.println("Testing @scope...");
        expectFlagged("@scope with no prelude",
                doc("<p>x</p><div><style>@scope { p { } :scope { }"
                        + " :scope > p { } & p { } }</style><p>y</p></div>"),
                new String[0]);
        expectFlagged("@scope with no prelude, selector escaping the scope",
                doc("<p>x</p><div><style>@scope { body p { } }</style>"),
                new String[0]);
        expectFlagged("@scope with a root selector",
                doc("<div class=card><p>x</p></div><div><style>"
                        + "@scope (.card) { p { } span { } }</style>"),
                "p");
        expectFlagged("@scope with a root and a limit",
                doc("<div class=a><span>s</span><div class=b><p>x</p></div>"
                        + "</div><div><style>@scope (.a) to (.b) { p { }"
                        + " .b { } span { } }</style>"),
                "span");
        expectFlagged("@scope with a root that only later elements match",
                doc("<p>x</p><div><style>@scope (.later) { p { } }</style>"),
                new String[0]);
        expectFlagged(":scope outside @scope is the root element",
                doc("<div><style>:scope { }</style>"), ":scope");

        System.out.println();
        System.out.println("Testing CSS nesting...");
        expectFlagged("nested rule with &",
                doc("<section class=x><p>y</p></section><div><style>"
                        + ".x { & p { } & span { } }</style>"),
                ".x", "& p");
        expectFlagged("nested rule without & is relative",
                doc("<p>y</p><div><style>.later { p { } > p { } }</style>"),
                new String[0]);
        expectFlagged("nested rule matching earlier content",
                doc("<section><p>y</p></section><div><style>section {"
                        + " color: red; p { color: blue } }</style>"),
                "section", "p");
        expectFlagged("& at the top level is :scope",
                doc("<div><style>& { }</style>"), "&");
        expectFlagged("nested rules inside a conditional group rule",
                doc("<section><p>y</p></section><div><style>section {"
                        + " @media screen { color: red; p { } } }</style>"),
                "section", "p");
        expectFlagged("declaration that looks like a nested rule",
                doc("<section><p>y</p></section><div><style>.later {"
                        + " font: 12px serif; p:hover { } }</style>"),
                new String[0]);

        System.out.println();
        System.out.println("Testing @import...");
        expectFlagged("@import in a body style",
                doc("<p>x</p><div><style>@import url(x.css); .later { }"
                        + "</style>"),
                "import: @import url(x.css)");
        expectFlagged("@import with a media query and a layer",
                doc("<div><style>@import \"x.css\" layer(base) screen;\n"
                        + "@import url(y.css)</style>"),
                "import: @import \"x.css\" layer(base) screen",
                "import: @import url(y.css)");
        expectFlagged("@import in a head style is not checked",
                "<!doctype html><title>t</title><style>@import url(x.css);"
                        + "</style><p>x",
                new String[0]);
        expectFlagged("@import in template contents is not checked",
                doc("<div><template><style>@import url(x.css);</style>"
                        + "</template></div>"),
                new String[0]);
        expectPositions("position of a flagged @import",
                doc("<div><style>\n  @import url(x.css);</style>"),
                "2.3-2.20");

        System.out.println();
        System.out.println("Testing reported positions...");
        expectPositions("positions of flagged selectors",
                doc("<p><b>x</b></p><div><style>p { }\n  .later,\n  p > b { }"
                        + "</style>"),
                "1.1-1.1", "3.3-3.7");

        System.out.println();
        System.out.println(
                "Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
