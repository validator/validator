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

import nu.validator.checker.bodystyle.Selectors.Anchor;
import nu.validator.checker.bodystyle.Selectors.Attribute;
import nu.validator.checker.bodystyle.Selectors.ClassName;
import nu.validator.checker.bodystyle.Selectors.Complex;
import nu.validator.checker.bodystyle.Selectors.Compound;
import nu.validator.checker.bodystyle.Selectors.Entry;
import nu.validator.checker.bodystyle.Selectors.Id;
import nu.validator.checker.bodystyle.Selectors.Logical;
import nu.validator.checker.bodystyle.Selectors.Nesting;
import nu.validator.checker.bodystyle.Selectors.Never;
import nu.validator.checker.bodystyle.Selectors.Nth;
import nu.validator.checker.bodystyle.Selectors.PseudoClass;
import nu.validator.checker.bodystyle.Selectors.PseudoElement;
import nu.validator.checker.bodystyle.Selectors.Scope;
import nu.validator.checker.bodystyle.Selectors.ScopeRule;
import nu.validator.checker.bodystyle.Selectors.Simple;

/**
 * Matches selectors against the elements recorded so far, with three
 * possible results: NO, MAYBE, or YES. MAYBE covers whatever can't be
 * known from the markup parsed so far — user-interaction state (":hover"),
 * content not yet parsed (":last-child" in a parent that's still open,
 * ":has()" on an element that's still open), and anything this code doesn't
 * know how to match.
 */
final class SelectorMatcher {

    static final int NO = 0;

    static final int MAYBE = 1;

    static final int YES = 2;

    private static final class Context {
        /**
         * The element ":scope" matches, or null for the root element.
         */
        final TreeElement scopeRoot;

        /**
         * The anchor element of the ":has()" argument being matched.
         */
        final TreeElement anchor;

        Context(TreeElement scopeRoot, TreeElement anchor) {
            this.scopeRoot = scopeRoot;
            this.anchor = anchor;
        }
    }

    private interface RootMatch {
        int match(TreeElement root);
    }

    private static final Context DOCUMENT = new Context(null, null);

    /**
     * The parent of the "style" element: the scoping root of an "@scope"
     * rule with no prelude and no parent style rule.
     */
    private final TreeElement styleParent;

    SelectorMatcher(TreeElement styleParent) {
        this.styleParent = styleParent;
    }

    private static int and(int a, int b) {
        return Math.min(a, b);
    }

    private static int or(int a, int b) {
        return Math.max(a, b);
    }

    private static int not(int a) {
        return YES - a;
    }

    static String[] splitOnWhitespace(String s) {
        List<String> parts = new ArrayList<>();
        int start = -1;
        for (int i = 0; i <= s.length(); i++) {
            boolean space = i == s.length() || isWhitespace(s.charAt(i));
            if (space && start >= 0) {
                parts.add(s.substring(start, i));
                start = -1;
            } else if (!space && start < 0) {
                start = i;
            }
        }
        return parts.toArray(new String[0]);
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f';
    }

    int match(Entry entry, TreeElement element) {
        if (entry.scope == null) {
            return matchComplex(entry.selector, element, DOCUMENT);
        }
        return matchInScope(entry.scope, element,
                root -> matchComplex(entry.selector, element,
                        new Context(root, null)));
    }

    /**
     * Matches an element that must be in the given scope, trying each of
     * its inclusive ancestors as the scoping root.
     */
    private int matchInScope(ScopeRule scope, TreeElement element,
            RootMatch inner) {
        int result = NO;
        for (TreeElement root = element; root != null; root = root.parent) {
            int isRoot = isScopingRoot(scope, root);
            if (isRoot == NO) {
                continue;
            }
            int limited = isBelowLimit(scope, element, root);
            result = or(result, and(isRoot, and(not(limited),
                    inner.match(root))));
            if (result == YES) {
                return YES;
            }
        }
        return result;
    }

    private int isScopingRoot(ScopeRule scope, TreeElement candidate) {
        Complex[] selectors = scope.roots != null ? scope.roots
                : scope.parentSelectors;
        if (selectors == null) {
            return candidate == styleParent ? YES : NO;
        }
        if (scope.outer == null) {
            return matchAny(selectors, candidate, DOCUMENT);
        }
        return matchInScope(scope.outer, candidate,
                outerRoot -> matchAny(selectors, candidate,
                        new Context(outerRoot, null)));
    }

    /**
     * Whether the element or any of its ancestors below the scoping root
     * is a scoping limit — so that the element is out of scope.
     */
    private int isBelowLimit(ScopeRule scope, TreeElement element,
            TreeElement root) {
        if (scope.limits == null) {
            return NO;
        }
        Context context = new Context(root, null);
        int result = NO;
        for (TreeElement e = element; e != null && e != root; e = e.parent) {
            result = or(result, matchAny(scope.limits, e, context));
            if (result == YES) {
                return YES;
            }
        }
        return result;
    }

    private int matchAny(Complex[] selectors, TreeElement element,
            Context context) {
        int result = NO;
        for (Complex selector : selectors) {
            result = or(result, matchComplex(selector, element, context));
            if (result == YES) {
                return YES;
            }
        }
        return result;
    }

    private int matchComplex(Complex selector, TreeElement element,
            Context context) {
        if (selector.unknown) {
            return MAYBE;
        }
        return matchFrom(selector, selector.compounds.length - 1, element,
                context);
    }

    /**
     * Matches compounds 0 through i of the selector, with compound i
     * matching the given element.
     */
    private int matchFrom(Complex selector, int i, TreeElement element,
            Context context) {
        int result = matchCompound(selector.compounds[i], element, context);
        if (result == NO || i == 0) {
            return result;
        }
        int rest = NO;
        switch (selector.combinators[i - 1]) {
            case ' ':
                for (TreeElement a = element.parent; a != null; a = a.parent) {
                    rest = or(rest, matchFrom(selector, i - 1, a, context));
                    if (rest == YES) {
                        break;
                    }
                }
                break;
            case '>':
                if (element.parent != null) {
                    rest = matchFrom(selector, i - 1, element.parent, context);
                }
                break;
            case '+':
                if (element.previousSibling != null) {
                    rest = matchFrom(selector, i - 1, element.previousSibling,
                            context);
                }
                break;
            default:
                for (TreeElement s = element.previousSibling; s != null;
                        s = s.previousSibling) {
                    rest = or(rest, matchFrom(selector, i - 1, s, context));
                    if (rest == YES) {
                        break;
                    }
                }
                break;
        }
        return and(result, rest);
    }

    private int matchCompound(Compound compound, TreeElement element,
            Context context) {
        int result = YES;
        if (compound.type != null) {
            boolean sameName = element.isHtml
                    ? compound.lowercaseType.equals(element.localName)
                    : compound.type.equals(element.localName);
            if (!sameName) {
                return NO;
            }
        }
        if (compound.namespace == Selectors.NS_NONE) {
            return NO;
        }
        if (compound.namespace == Selectors.NS_UNKNOWN) {
            result = MAYBE;
        }
        for (Simple simple : compound.simples) {
            result = and(result, matchSimple(simple, element, context));
            if (result == NO) {
                return NO;
            }
        }
        return result;
    }

    private int matchSimple(Simple simple, TreeElement element,
            Context context) {
        if (simple instanceof Id) {
            return ((Id) simple).name.equals(element.getAttribute("id")) ? YES
                    : NO;
        }
        if (simple instanceof ClassName) {
            return element.hasClass(((ClassName) simple).name) ? YES : NO;
        }
        if (simple instanceof Attribute) {
            return matchAttribute((Attribute) simple, element);
        }
        if (simple instanceof PseudoClass) {
            return matchPseudoClass(((PseudoClass) simple).name, element,
                    context);
        }
        if (simple instanceof Nth) {
            return matchNth((Nth) simple, element, context);
        }
        if (simple instanceof Logical) {
            Logical logical = (Logical) simple;
            if (logical.kind == Selectors.LOGICAL_HAS) {
                int result = NO;
                for (Complex argument : logical.arguments) {
                    result = or(result, matchRelative(argument, element,
                            context));
                    if (result == YES) {
                        break;
                    }
                }
                return result;
            }
            int result = matchAny(logical.arguments, element, context);
            return logical.kind == Selectors.LOGICAL_NOT ? not(result)
                    : result;
        }
        if (simple instanceof Nesting) {
            Complex[] parent = ((Nesting) simple).parent;
            return parent == null ? matchScope(element, context)
                    : matchAny(parent, element, context);
        }
        if (simple instanceof Scope) {
            return matchScope(element, context);
        }
        if (simple instanceof Anchor) {
            return element == context.anchor ? YES : NO;
        }
        if (simple instanceof PseudoElement) {
            String name = ((PseudoElement) simple).name;
            if ("slotted".equals(name)) {
                return NO;
            }
            // A pseudo-element restyles its originating element's box.
            return "part".equals(name) ? MAYBE : YES;
        }
        if (simple instanceof Never) {
            return NO;
        }
        return MAYBE;
    }

    private static int matchScope(TreeElement element, Context context) {
        if (context.scopeRoot == null) {
            return element.parent == null ? YES : NO;
        }
        return element == context.scopeRoot ? YES : NO;
    }

    private static int matchAttribute(Attribute selector,
            TreeElement element) {
        int result = NO;
        for (int i = 0; i < element.attributeCount(); i++) {
            String name = element.attributeLocalName(i);
            if (!(element.isHtml ? selector.lowercaseName.equals(name)
                    : selector.name.equals(name))) {
                continue;
            }
            int namespaceMatch;
            switch (selector.namespace) {
                case Selectors.ATTR_ANY_NAMESPACE:
                    namespaceMatch = YES;
                    break;
                case Selectors.ATTR_UNKNOWN_NAMESPACE:
                    namespaceMatch = MAYBE;
                    break;
                default:
                    namespaceMatch = element.attributeNamespace(i).isEmpty()
                            ? YES
                            : NO;
                    break;
            }
            result = or(result, and(namespaceMatch, matchAttributeValue(
                    selector, element.attributeValue(i), element.isHtml)));
            if (result == YES) {
                return YES;
            }
        }
        return result;
    }

    private static int matchAttributeValue(Attribute selector, String value,
            boolean isHtml) {
        if (selector.operator == '\0') {
            return YES;
        }
        if (selector.flag == 'i') {
            return compareValue(selector.operator,
                    Selectors.asciiLowercase(selector.value),
                    Selectors.asciiLowercase(value)) ? YES : NO;
        }
        if (compareValue(selector.operator, selector.value, value)) {
            return YES;
        }
        // Some HTML attributes' values match case-insensitively, e.g.
        // "type" — so a case-insensitive match might be a match.
        if (selector.flag == '\0' && isHtml
                && compareValue(selector.operator,
                        Selectors.asciiLowercase(selector.value),
                        Selectors.asciiLowercase(value))) {
            return MAYBE;
        }
        return NO;
    }

    private static boolean compareValue(char operator, String expected,
            String actual) {
        switch (operator) {
            case '=':
                return actual.equals(expected);
            case '~':
                if (expected.isEmpty()) {
                    return false;
                }
                for (String part : splitOnWhitespace(actual)) {
                    if (part.equals(expected)) {
                        return true;
                    }
                }
                return false;
            case '|':
                return actual.equals(expected)
                        || actual.startsWith(expected + "-");
            case '^':
                return !expected.isEmpty() && actual.startsWith(expected);
            case '$':
                return !expected.isEmpty() && actual.endsWith(expected);
            default:
                return !expected.isEmpty() && actual.contains(expected);
        }
    }

    private int matchPseudoClass(String name, TreeElement element,
            Context context) {
        switch (name) {
            case "root":
                return element.parent == null ? YES : NO;
            case "empty":
                if (element.firstChild != null || element.hasText) {
                    return NO;
                }
                return element.closed ? YES : MAYBE;
            case "only-child":
                return and(matchNth(FIRST_CHILD, element, context),
                        matchNth(LAST_CHILD, element, context));
            case "only-of-type":
                return and(matchNth(FIRST_OF_TYPE, element, context),
                        matchNth(LAST_OF_TYPE, element, context));
            case "any-link":
            case "-webkit-any-link":
                return matchLink(element);
            case "link":
            case "visited":
                return matchLink(element) == NO ? NO : MAYBE;
            case "defined":
                return element.isHtml && element.localName.indexOf('-') >= 0
                        ? MAYBE
                        : YES;
            case "before":
            case "after":
            case "first-line":
            case "first-letter":
                // Legacy single-colon pseudo-elements, which restyle their
                // originating element's box.
                return YES;
            default:
                return MAYBE;
        }
    }

    private static final Nth FIRST_CHILD = new Nth(false, false, 0, 1, null);

    private static final Nth LAST_CHILD = new Nth(true, false, 0, 1, null);

    private static final Nth FIRST_OF_TYPE = new Nth(false, true, 0, 1,
            null);

    private static final Nth LAST_OF_TYPE = new Nth(true, true, 0, 1, null);

    private static final String SVG = "http://www.w3.org/2000/svg";

    private static final String MATHML = "http://www.w3.org/1998/Math/MathML";

    private static final String XLINK = "http://www.w3.org/1999/xlink";

    /**
     * Whether the element is a hyperlink source, for ":any-link": HTML "a"
     * and "area" with "href", and SVG "a" with "href" or "xlink:href". A
     * MathML element with "href" might be one, since engines differ: Gecko
     * and WebKit make any MathML element with "href" a link (unless the
     * mathml.href_link_on_non_anchor_element.disabled pref or the
     * MathMLDisableHrefOnNonAnchorElement setting limits it to "a"), but
     * Blink's MathMLAnchorElement is still behind a test-only flag.
     */
    private static int matchLink(TreeElement element) {
        if (element.isHtml) {
            return ("a".equals(element.localName)
                    || "area".equals(element.localName))
                    && element.getAttribute("href") != null ? YES : NO;
        }
        if (SVG.equals(element.namespace)) {
            boolean hasHref = element.getAttribute("href") != null
                    || element.getAttribute(XLINK, "href") != null;
            return "a".equals(element.localName) && hasHref ? YES : NO;
        }
        if (MATHML.equals(element.namespace)) {
            return element.getAttribute("href") != null ? MAYBE : NO;
        }
        return NO;
    }

    private int matchNth(Nth nth, TreeElement element, Context context) {
        int self = YES;
        if (nth.of != null) {
            self = matchAny(nth.of, element, context);
            if (self == NO) {
                return NO;
            }
        }
        int position = 1;
        boolean uncertain = false;
        for (TreeElement s = nth.last ? element.nextSibling
                : element.previousSibling; s != null; s = nth.last
                        ? s.nextSibling
                        : s.previousSibling) {
            if (nth.ofType && !s.hasSameTypeAs(element)) {
                continue;
            }
            if (nth.of != null) {
                int m = matchAny(nth.of, s, context);
                if (m == NO) {
                    continue;
                }
                if (m == MAYBE) {
                    uncertain = true;
                }
            }
            position++;
        }
        if (nth.last && !element.isParentClosed()) {
            // More siblings can still come, so the position counted from
            // the end is only a lower bound.
            boolean reachable = nth.a > 0 || nth.b >= position;
            return and(self, reachable ? MAYBE : NO);
        }
        if (uncertain) {
            return and(self, MAYBE);
        }
        return and(self, isNth(nth.a, nth.b, position) ? YES : NO);
    }

    private static boolean isNth(int a, int b, int position) {
        if (a == 0) {
            return position == b;
        }
        int d = position - b;
        return d % a == 0 && d / a >= 0;
    }

    /**
     * Matches a ":has()" argument, whose first compound is the anchor.
     */
    private int matchRelative(Complex selector, TreeElement anchor,
            Context context) {
        if (selector.unknown) {
            return MAYBE;
        }
        Context relative = new Context(context.scopeRoot, anchor);
        int last = selector.compounds.length - 1;
        char first = selector.combinators[0];
        int result = NO;
        boolean incomplete;
        if (first == ' ' || first == '>') {
            result = matchSubtree(selector, last, anchor.firstChild, anchor,
                    relative);
            incomplete = !anchor.closed;
        } else {
            for (TreeElement s = anchor.nextSibling; s != null
                    && result != YES; s = s.nextSibling) {
                result = or(result, matchFrom(selector, last, s, relative));
                if (result != YES) {
                    result = or(result, matchSubtree(selector, last,
                            s.firstChild, s, relative));
                }
            }
            incomplete = !anchor.isParentClosed() && (first == '~'
                    || last > 1 || anchor.nextSibling == null);
        }
        if (result != YES && incomplete) {
            result = MAYBE;
        }
        return result;
    }

    /**
     * Matches every element from start through the rest of bound's
     * subtree, in tree order.
     */
    private int matchSubtree(Complex selector, int last, TreeElement start,
            TreeElement bound, Context context) {
        int result = NO;
        TreeElement e = start;
        while (e != null) {
            result = or(result, matchFrom(selector, last, e, context));
            if (result == YES) {
                return YES;
            }
            if (e.firstChild != null) {
                e = e.firstChild;
                continue;
            }
            while (e != null && e != bound && e.nextSibling == null) {
                e = e.parent;
            }
            e = e == null || e == bound ? null : e.nextSibling;
        }
        return result;
    }
}
