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

/**
 * The parsed form of the selectors in a style sheet, as SelectorCollector
 * produces them and SelectorMatcher matches them.
 */
final class Selectors {

    private Selectors() {
    }

    /**
     * A complex selector: compounds joined by combinators.
     */
    static final class Complex {
        final Compound[] compounds;

        /**
         * combinators[i] joins compounds[i] and compounds[i + 1]: one of
         * ' ', '>', '+', or '~'.
         */
        final char[] combinators;

        final int start;

        final int end;

        /**
         * True if the selector couldn't be parsed — so that it's treated as
         * possibly matching anything.
         */
        final boolean unknown;

        Complex(Compound[] compounds, char[] combinators, int start,
                int end) {
            this.compounds = compounds;
            this.combinators = combinators;
            this.start = start;
            this.end = end;
            this.unknown = false;
        }

        private Complex(int start, int end) {
            this.compounds = new Compound[0];
            this.combinators = new char[0];
            this.start = start;
            this.end = end;
            this.unknown = true;
        }

        static Complex unknown(int start, int end) {
            return new Complex(start, end);
        }

        /**
         * Returns this selector with the given compound prepended, joined to
         * it by the given combinator.
         */
        Complex prepend(Compound first, char combinator) {
            if (unknown) {
                return this;
            }
            Compound[] c = new Compound[compounds.length + 1];
            char[] k = new char[combinators.length + 1];
            c[0] = first;
            k[0] = combinator;
            System.arraycopy(compounds, 0, c, 1, compounds.length);
            System.arraycopy(combinators, 0, k, 1, combinators.length);
            return new Complex(c, k, start, end);
        }
    }

    static final int NS_ANY = 0;

    static final int NS_NONE = 1;

    static final int NS_UNKNOWN = 2;

    static final class Compound {
        /**
         * The type selector's name, or null for the universal selector or
         * no type selector at all.
         */
        final String type;

        final String lowercaseType;

        /**
         * The namespace constraint: NS_ANY for no prefix (with no default
         * namespace declared) or "*|", NS_NONE for "|", and NS_UNKNOWN for
         * any declared prefix.
         */
        final int namespace;

        final Simple[] simples;

        Compound(String type, int namespace, Simple[] simples) {
            this.type = type;
            this.lowercaseType = type == null ? null : asciiLowercase(type);
            this.namespace = namespace;
            this.simples = simples;
        }

        Compound(Simple simple) {
            this(null, NS_ANY, new Simple[] { simple });
        }
    }

    abstract static class Simple {
    }

    static final class Id extends Simple {
        final String name;

        Id(String name) {
            this.name = name;
        }
    }

    static final class ClassName extends Simple {
        final String name;

        ClassName(String name) {
            this.name = name;
        }
    }

    static final int ATTR_NO_NAMESPACE = 0;

    static final int ATTR_ANY_NAMESPACE = 1;

    static final int ATTR_UNKNOWN_NAMESPACE = 2;

    static final class Attribute extends Simple {
        final int namespace;

        final String name;

        final String lowercaseName;

        /**
         * One of '\0' (no value: the attribute just has to exist), '=',
         * '~', '|', '^', '$', or '*'.
         */
        final char operator;

        final String value;

        /**
         * One of '\0' (no flag), 'i', or 's'.
         */
        final char flag;

        Attribute(int namespace, String name, char operator, String value,
                char flag) {
            this.namespace = namespace;
            this.name = name;
            this.lowercaseName = asciiLowercase(name);
            this.operator = operator;
            this.value = value;
            this.flag = flag;
        }
    }

    /**
     * A pseudo-class with no arguments, e.g. ":hover" — or one of the
     * pseudo-elements that also have a legacy single-colon syntax.
     */
    static final class PseudoClass extends Simple {
        final String name;

        PseudoClass(String name) {
            this.name = asciiLowercase(name);
        }
    }

    static final int LOGICAL_IS = 0;

    static final int LOGICAL_NOT = 1;

    static final int LOGICAL_HAS = 2;

    /**
     * ":is()", ":where()", ":not()", or ":has()". For ":has()", every
     * argument starts with an Anchor compound, joined to the rest by the
     * relative selector's leading combinator.
     */
    static final class Logical extends Simple {
        final int kind;

        final Complex[] arguments;

        Logical(int kind, Complex[] arguments) {
            this.kind = kind;
            this.arguments = arguments;
        }
    }

    /**
     * ":nth-child()", ":nth-last-child()", ":nth-of-type()",
     * ":nth-last-of-type()" — and their argument-less forms, e.g.
     * ":first-child" as ":nth-child(1)".
     */
    static final class Nth extends Simple {
        final boolean last;

        final boolean ofType;

        final int a;

        final int b;

        /**
         * The "of S" selector list, or null.
         */
        final Complex[] of;

        Nth(boolean last, boolean ofType, int a, int b, Complex[] of) {
            this.last = last;
            this.ofType = ofType;
            this.a = a;
            this.b = b;
            this.of = of;
        }
    }

    /**
     * The nesting selector "&". It matches whatever the parent style
     * rule's selectors match — or, with no parent style rule, whatever
     * ":scope" matches.
     */
    static final class Nesting extends Simple {
        final Complex[] parent;

        Nesting(Complex[] parent) {
            this.parent = parent;
        }
    }

    static final class Scope extends Simple {
    }

    /**
     * Matches only the anchor element of a ":has()" argument.
     */
    static final class Anchor extends Simple {
    }

    static final class PseudoElement extends Simple {
        final String name;

        PseudoElement(String name) {
            this.name = asciiLowercase(name);
        }
    }

    /**
     * Something that never matches an element in a document's own style
     * sheets, e.g. ":host".
     */
    static final class Never extends Simple {
    }

    /**
     * Something this code doesn't know how to match — so that it's
     * treated as possibly matching.
     */
    static final class Unknown extends Simple {
    }

    /**
     * An "@scope" rule: its roots and limits, and the "@scope" rule it's
     * nested in, if any.
     */
    static final class ScopeRule {
        /**
         * The "scope-start" selectors — or null if there's no prelude, in
         * which case the roots are what parentSelectors match or, with no
         * parent style rule either, the "style" element's parent.
         */
        final Complex[] roots;

        final Complex[] parentSelectors;

        /**
         * The "scope-end" selectors, or null.
         */
        final Complex[] limits;

        final ScopeRule outer;

        ScopeRule(Complex[] roots, Complex[] parentSelectors,
                Complex[] limits, ScopeRule outer) {
            this.roots = roots;
            this.parentSelectors = parentSelectors;
            this.limits = limits;
            this.outer = outer;
        }
    }

    /**
     * One selector from a style rule, with the "@scope" rule it's in, if
     * any.
     */
    static final class Entry {
        final Complex selector;

        final ScopeRule scope;

        Entry(Complex selector, ScopeRule scope) {
            this.selector = selector;
            this.scope = scope;
        }
    }

    static String asciiLowercase(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                char[] chars = s.toCharArray();
                for (int j = i; j < chars.length; j++) {
                    if (chars[j] >= 'A' && chars[j] <= 'Z') {
                        chars[j] += 'a' - 'A';
                    }
                }
                return new String(chars);
            }
        }
        return s;
    }
}
