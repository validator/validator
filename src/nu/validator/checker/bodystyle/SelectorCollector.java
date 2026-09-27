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
import static nu.validator.checker.bodystyle.CssTokenizer.CDC;
import static nu.validator.checker.bodystyle.CssTokenizer.CDO;
import static nu.validator.checker.bodystyle.CssTokenizer.COLON;
import static nu.validator.checker.bodystyle.CssTokenizer.COMMA;
import static nu.validator.checker.bodystyle.CssTokenizer.DELIM;
import static nu.validator.checker.bodystyle.CssTokenizer.FUNCTION;
import static nu.validator.checker.bodystyle.CssTokenizer.HASH;
import static nu.validator.checker.bodystyle.CssTokenizer.IDENT;
import static nu.validator.checker.bodystyle.CssTokenizer.LEFT_BRACE;
import static nu.validator.checker.bodystyle.CssTokenizer.LEFT_BRACKET;
import static nu.validator.checker.bodystyle.CssTokenizer.LEFT_PAREN;
import static nu.validator.checker.bodystyle.CssTokenizer.RIGHT_BRACE;
import static nu.validator.checker.bodystyle.CssTokenizer.RIGHT_BRACKET;
import static nu.validator.checker.bodystyle.CssTokenizer.RIGHT_PAREN;
import static nu.validator.checker.bodystyle.CssTokenizer.SEMICOLON;
import static nu.validator.checker.bodystyle.CssTokenizer.STRING;
import static nu.validator.checker.bodystyle.CssTokenizer.WHITESPACE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.validator.checker.bodystyle.CssTokenizer.Token;
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
import nu.validator.checker.bodystyle.Selectors.Unknown;

/**
 * Collects the selector of every style rule in a style sheet — including
 * rules inside conditional group rules, "@scope" rules, and other style
 * rules (CSS nesting) — resolving "&" and the implicit relative selectors
 * of nested and scoped rules as it goes.
 *
 * https://drafts.csswg.org/css-nesting/
 * https://drafts.csswg.org/css-cascade-6/#scoped-styles
 */
final class SelectorCollector {

    private static final Pattern AN_PLUS_B = Pattern.compile(
            "([+-]?\\d*)[nN](?:([+-])(\\d+))?|([+-]?\\d+)");

    /**
     * At-rules whose blocks hold style rules that apply as if they weren't
     * wrapped in the at-rule at all, as far as selector matching goes.
     */
    private static final String[] GROUPING_RULES = { "media", "supports",
            "layer", "container", "starting-style", "document",
            "-moz-document" };

    private static final int MODE_PLAIN = 0;

    private static final int MODE_NESTED = 1;

    private static final int MODE_SCOPED = 2;

    private static final int MODE_RELATIVE = 3;

    private static final class Context {
        /**
         * The selectors of the parent style rule, or null.
         */
        final Complex[] parentSelectors;

        final ScopeRule scope;

        /**
         * True for rules directly inside an "@scope" rule, whose selectors
         * are implicitly relative to ":where(:scope)".
         */
        final boolean inScopeBody;

        Context(Complex[] parentSelectors, ScopeRule scope,
                boolean inScopeBody) {
            this.parentSelectors = parentSelectors;
            this.scope = scope;
            this.inScopeBody = inScopeBody;
        }
    }

    private static final class ParseFailure extends Exception {
        private static final long serialVersionUID = 1L;

        ParseFailure() {
            super(null, null, false, false);
        }
    }

    private final String css;

    private final List<Token> tokens;

    private final List<Entry> entries = new ArrayList<>();

    private SelectorCollector(String css) {
        this.css = css;
        this.tokens = CssTokenizer.tokenize(css);
    }

    static List<Entry> collect(String css) {
        SelectorCollector collector = new SelectorCollector(css);
        collector.consumeBlockContents(0, collector.tokens.size(),
                new Context(null, null, false));
        return collector.entries;
    }

    private Token token(int i) {
        return tokens.get(i);
    }

    /**
     * Returns the index of the first semicolon — or, if stopAtBrace is
     * true, left brace — that's not nested in a block, starting at from;
     * or to, if there's none.
     */
    private int scan(int from, int to, boolean stopAtBrace) {
        int depth = 0;
        for (int i = from; i < to; i++) {
            int type = token(i).type;
            if (depth == 0 && (type == SEMICOLON
                    || (stopAtBrace && type == LEFT_BRACE))) {
                return i;
            }
            if (type == LEFT_BRACE || type == LEFT_BRACKET
                    || type == LEFT_PAREN || type == FUNCTION) {
                depth++;
            } else if (type == RIGHT_BRACE || type == RIGHT_BRACKET
                    || type == RIGHT_PAREN) {
                if (depth == 0 && type == RIGHT_BRACE) {
                    return i;
                }
                depth = Math.max(0, depth - 1);
            }
        }
        return to;
    }

    /**
     * Returns the index of the token that closes the block, parenthesized
     * group, or function opened at open; or to, if it's never closed.
     */
    private int findClose(int open, int to) {
        int depth = 0;
        for (int i = open; i < to; i++) {
            int type = token(i).type;
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
        return to;
    }

    /**
     * Consumes the contents of a style sheet or of a block that can hold
     * declarations and rules, returning true if it had any declarations.
     */
    private boolean consumeBlockContents(int from, int to, Context context) {
        boolean hasDeclarations = false;
        int i = from;
        while (i < to) {
            Token t = token(i);
            switch (t.type) {
                case WHITESPACE:
                case SEMICOLON:
                case CDO:
                case CDC:
                case RIGHT_BRACE:
                    i++;
                    break;
                case AT_KEYWORD:
                    i = consumeAtRule(i, to, context);
                    break;
                case IDENT:
                    if (t.value.startsWith("--")) {
                        i = scan(i, to, false) + 1;
                        hasDeclarations = true;
                        break;
                    }
                    int stop = scan(i, to, true);
                    if (stop < to && token(stop).type == LEFT_BRACE) {
                        i = consumeStyleRule(i, to, context);
                    } else {
                        i = stop + 1;
                        hasDeclarations = true;
                    }
                    break;
                default:
                    i = consumeStyleRule(i, to, context);
            }
        }
        return hasDeclarations;
    }

    private int consumeStyleRule(int from, int to, Context context) {
        int open = scan(from, to, true);
        if (open >= to || token(open).type != LEFT_BRACE) {
            return open + 1;
        }
        int close = findClose(open, to);
        int mode = context.parentSelectors != null ? MODE_NESTED
                : context.inScopeBody ? MODE_SCOPED : MODE_PLAIN;
        Complex[] selectors = parseSelectorList(from, open,
                context.parentSelectors, mode);
        for (Complex selector : selectors) {
            entries.add(new Entry(selector, context.scope));
        }
        consumeBlockContents(open + 1, close,
                new Context(selectors, context.scope, false));
        return close + 1;
    }

    private int consumeAtRule(int from, int to, Context context) {
        String name = Selectors.asciiLowercase(token(from).value);
        int open = scan(from + 1, to, true);
        if (open >= to || token(open).type != LEFT_BRACE) {
            return open + 1;
        }
        int close = findClose(open, to);
        if ("scope".equals(name)) {
            consumeScopeRule(from + 1, open, close, context);
        } else {
            for (String grouping : GROUPING_RULES) {
                if (grouping.equals(name)) {
                    consumeBlockContents(open + 1, close, context);
                    break;
                }
            }
        }
        return close + 1;
    }

    private int skipWhitespace(int i, int to) {
        while (i < to && token(i).type == WHITESPACE) {
            i++;
        }
        return i;
    }

    private void consumeScopeRule(int from, int open, int close,
            Context context) {
        Complex[] roots = null;
        Complex[] limits = null;
        int i = skipWhitespace(from, open);
        if (i < open && token(i).type == LEFT_PAREN) {
            int end = findClose(i, open);
            roots = parseSelectorList(i + 1, end, context.parentSelectors,
                    MODE_PLAIN);
            i = skipWhitespace(end + 1, open);
        }
        if (i < open && token(i).type == IDENT
                && "to".equals(Selectors.asciiLowercase(token(i).value))) {
            i = skipWhitespace(i + 1, open);
            if (i < open && token(i).type == LEFT_PAREN) {
                int end = findClose(i, open);
                limits = parseSelectorList(i + 1, end, null, MODE_SCOPED);
            }
        }
        ScopeRule scope = new ScopeRule(roots,
                roots == null ? context.parentSelectors : null, limits,
                context.scope);
        boolean hasDeclarations = consumeBlockContents(open + 1, close,
                new Context(null, scope, true));
        if (hasDeclarations && roots != null && roots.length > 0) {
            // Declarations directly in the rule apply to the scoping roots.
            Complex scopeSelector = new Complex(
                    new Compound[] { new Compound(new Scope()) },
                    new char[0], roots[0].start, roots[roots.length - 1].end);
            entries.add(new Entry(scopeSelector, scope));
        }
    }

    /**
     * Parses a comma-separated list of selectors between from and to. The
     * mode says how to treat a selector that starts with a combinator, or
     * that has no "&" (or ":scope"):
     *
     * MODE_PLAIN: As is; a leading combinator makes it unparseable.
     *
     * MODE_NESTED: Relative to "&".
     *
     * MODE_SCOPED: Relative to ":scope", unless it has "&" or ":scope".
     *
     * MODE_RELATIVE: Relative to the anchor of a ":has()" argument.
     */
    private Complex[] parseSelectorList(int from, int to,
            Complex[] parentSelectors, int mode) {
        List<Complex> list = new ArrayList<>();
        int start = from;
        int depth = 0;
        for (int i = from; i <= to; i++) {
            int type = i < to ? token(i).type : COMMA;
            if (type == LEFT_BRACKET || type == LEFT_PAREN
                    || type == FUNCTION) {
                depth++;
            } else if (type == RIGHT_BRACKET || type == RIGHT_PAREN) {
                depth--;
            } else if (type == COMMA && (depth == 0 || i == to)) {
                Complex selector = parseSelector(start, i, parentSelectors,
                        mode);
                if (selector != null) {
                    list.add(selector);
                }
                start = i + 1;
            }
        }
        return list.toArray(new Complex[0]);
    }

    private Complex parseSelector(int from, int to,
            Complex[] parentSelectors, int mode) {
        from = skipWhitespace(from, to);
        while (to > from && token(to - 1).type == WHITESPACE) {
            to--;
        }
        if (from >= to) {
            return null;
        }
        int start = token(from).start;
        int end = token(to - 1).end;
        Complex selector;
        char leading;
        try {
            int[] cursor = { from };
            leading = combinatorAt(cursor, to);
            if (leading != '\0' && mode == MODE_PLAIN) {
                return Complex.unknown(start, end);
            }
            List<Compound> compounds = new ArrayList<>();
            StringBuilder combinators = new StringBuilder();
            while (true) {
                Compound compound = parseCompound(cursor, to,
                        parentSelectors);
                if (compound == null) {
                    throw new ParseFailure();
                }
                compounds.add(compound);
                if (cursor[0] >= to) {
                    break;
                }
                char combinator = combinatorAt(cursor, to);
                if (combinator == '\0') {
                    throw new ParseFailure();
                }
                combinators.append(combinator);
            }
            selector = new Complex(compounds.toArray(new Compound[0]),
                    combinators.toString().toCharArray(), start, end);
        } catch (ParseFailure e) {
            return Complex.unknown(start, end);
        }
        switch (mode) {
            case MODE_NESTED:
                if (leading != '\0' || !contains(selector, false)) {
                    selector = selector.prepend(
                            new Compound(new Nesting(parentSelectors)),
                            leading == '\0' ? ' ' : leading);
                }
                break;
            case MODE_SCOPED:
                if (leading != '\0' || !contains(selector, true)) {
                    selector = selector.prepend(new Compound(new Scope()),
                            leading == '\0' ? ' ' : leading);
                }
                break;
            case MODE_RELATIVE:
                selector = selector.prepend(new Compound(new Anchor()),
                        leading == '\0' ? ' ' : leading);
                break;
            default:
                break;
        }
        return selector;
    }

    /**
     * Consumes a combinator, with any whitespace around it, at cursor[0],
     * and returns it — or returns '\0' if there's none.
     */
    private char combinatorAt(int[] cursor, int to) {
        int i = cursor[0];
        boolean whitespace = false;
        while (i < to && token(i).type == WHITESPACE) {
            whitespace = true;
            i++;
        }
        if (i < to && token(i).type == DELIM) {
            char c = token(i).value.charAt(0);
            if (c == '>' || c == '+' || c == '~') {
                cursor[0] = skipWhitespace(i + 1, to);
                return c;
            }
        }
        cursor[0] = i;
        return whitespace && i < to ? ' ' : '\0';
    }

    /**
     * True if the selector has "&" anywhere — or, if alsoScope is true,
     * ":scope" — including in the arguments of functional pseudo-classes.
     */
    private static boolean contains(Complex selector, boolean alsoScope) {
        for (Compound compound : selector.compounds) {
            for (Simple simple : compound.simples) {
                if (simple instanceof Nesting
                        || (alsoScope && simple instanceof Scope)) {
                    return true;
                }
                Complex[] arguments = simple instanceof Logical
                        ? ((Logical) simple).arguments
                        : simple instanceof Nth ? ((Nth) simple).of : null;
                if (arguments != null) {
                    for (Complex argument : arguments) {
                        if (contains(argument, alsoScope)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isDelim(int i, int to, char c) {
        return i < to && token(i).isDelim(c);
    }

    private boolean isNameOrStar(int i, int to) {
        return i < to && (token(i).type == IDENT || token(i).isDelim('*'));
    }

    private Compound parseCompound(int[] cursor, int to,
            Complex[] parentSelectors) throws ParseFailure {
        int i = cursor[0];
        String type = null;
        int namespace = Selectors.NS_ANY;
        boolean hasType = false;
        if (isNameOrStar(i, to) && isDelim(i + 1, to, '|')
                && isNameOrStar(i + 2, to)) {
            if (token(i).type == IDENT) {
                namespace = Selectors.NS_UNKNOWN;
            }
            type = token(i + 2).type == IDENT ? token(i + 2).value : null;
            hasType = true;
            i += 3;
        } else if (isDelim(i, to, '|') && isNameOrStar(i + 1, to)) {
            namespace = Selectors.NS_NONE;
            type = token(i + 1).type == IDENT ? token(i + 1).value : null;
            hasType = true;
            i += 2;
        } else if (isNameOrStar(i, to)) {
            type = token(i).type == IDENT ? token(i).value : null;
            hasType = true;
            i++;
        }
        List<Simple> simples = new ArrayList<>();
        while (i < to) {
            Token t = token(i);
            if (t.type == HASH) {
                simples.add(new Id(t.value));
                i++;
            } else if (t.isDelim('.') && i + 1 < to
                    && token(i + 1).type == IDENT) {
                simples.add(new ClassName(token(i + 1).value));
                i += 2;
            } else if (t.isDelim('&')) {
                simples.add(new Nesting(parentSelectors));
                i++;
            } else if (t.type == LEFT_BRACKET) {
                int close = findClose(i, to);
                simples.add(parseAttribute(i + 1, close));
                i = close + 1;
            } else if (t.type == COLON) {
                i++;
                boolean element = false;
                if (i < to && token(i).type == COLON) {
                    element = true;
                    i++;
                }
                if (i >= to) {
                    throw new ParseFailure();
                }
                Token name = token(i);
                if (name.type == IDENT) {
                    simples.add(element ? new PseudoElement(name.value)
                            : pseudoClass(name.value));
                    i++;
                } else if (name.type == FUNCTION) {
                    int close = findClose(i, to);
                    simples.add(element ? new PseudoElement(name.value)
                            : functionalPseudoClass(name.value, i + 1,
                                    close, parentSelectors));
                    i = close + 1;
                } else {
                    throw new ParseFailure();
                }
            } else {
                break;
            }
        }
        cursor[0] = i;
        if (!hasType && simples.isEmpty()) {
            return null;
        }
        return new Compound(type, namespace, simples.toArray(new Simple[0]));
    }

    private Simple parseAttribute(int from, int to) throws ParseFailure {
        int i = skipWhitespace(from, to);
        int namespace = Selectors.ATTR_NO_NAMESPACE;
        String name;
        if (isNameOrStar(i, to) && isDelim(i + 1, to, '|') && i + 2 < to
                && token(i + 2).type == IDENT) {
            namespace = token(i).type == IDENT
                    ? Selectors.ATTR_UNKNOWN_NAMESPACE
                    : Selectors.ATTR_ANY_NAMESPACE;
            name = token(i + 2).value;
            i += 3;
        } else if (isDelim(i, to, '|') && i + 1 < to
                && token(i + 1).type == IDENT) {
            name = token(i + 1).value;
            i += 2;
        } else if (i < to && token(i).type == IDENT) {
            name = token(i).value;
            i++;
        } else {
            throw new ParseFailure();
        }
        i = skipWhitespace(i, to);
        if (i >= to) {
            return new Attribute(namespace, name, '\0', null, '\0');
        }
        char operator;
        if (isDelim(i, to, '=')) {
            operator = '=';
            i++;
        } else if (i < to && token(i).type == DELIM
                && "~|^$*".indexOf(token(i).value.charAt(0)) >= 0
                && isDelim(i + 1, to, '=')) {
            operator = token(i).value.charAt(0);
            i += 2;
        } else {
            throw new ParseFailure();
        }
        i = skipWhitespace(i, to);
        if (i >= to
                || (token(i).type != IDENT && token(i).type != STRING)) {
            throw new ParseFailure();
        }
        String value = token(i).value;
        i = skipWhitespace(i + 1, to);
        char flag = '\0';
        if (i < to && token(i).type == IDENT) {
            String f = Selectors.asciiLowercase(token(i).value);
            if (!"i".equals(f) && !"s".equals(f)) {
                throw new ParseFailure();
            }
            flag = f.charAt(0);
            i = skipWhitespace(i + 1, to);
        }
        if (i < to) {
            throw new ParseFailure();
        }
        return new Attribute(namespace, name, operator, value, flag);
    }

    private static Simple pseudoClass(String name) {
        switch (Selectors.asciiLowercase(name)) {
            case "first-child":
                return new Nth(false, false, 0, 1, null);
            case "last-child":
                return new Nth(true, false, 0, 1, null);
            case "first-of-type":
                return new Nth(false, true, 0, 1, null);
            case "last-of-type":
                return new Nth(true, true, 0, 1, null);
            case "scope":
                return new Scope();
            case "host":
                return new Never();
            default:
                return new PseudoClass(name);
        }
    }

    private Simple functionalPseudoClass(String name, int from, int to,
            Complex[] parentSelectors) throws ParseFailure {
        String lowercaseName = Selectors.asciiLowercase(name);
        switch (lowercaseName) {
            case "is":
            case "where":
            case "matches":
            case "-webkit-any":
            case "-moz-any":
                return new Logical(Selectors.LOGICAL_IS, parseSelectorList(
                        from, to, parentSelectors, MODE_PLAIN));
            case "not":
                return new Logical(Selectors.LOGICAL_NOT, parseSelectorList(
                        from, to, parentSelectors, MODE_PLAIN));
            case "has":
                return new Logical(Selectors.LOGICAL_HAS, parseSelectorList(
                        from, to, parentSelectors, MODE_RELATIVE));
            case "nth-child":
            case "nth-last-child":
            case "nth-of-type":
            case "nth-last-of-type":
                return parseNth(lowercaseName, from, to, parentSelectors);
            case "host":
            case "host-context":
                return new Never();
            default:
                return new Unknown();
        }
    }

    private Simple parseNth(String name, int from, int to,
            Complex[] parentSelectors) {
        boolean last = name.contains("last");
        boolean ofType = name.endsWith("of-type");
        int ofIndex = -1;
        if (!ofType) {
            for (int i = from; i < to; i++) {
                if (token(i).type == IDENT && "of".equals(
                        Selectors.asciiLowercase(token(i).value))) {
                    ofIndex = i;
                    break;
                }
            }
        }
        int argumentEnd = ofIndex < 0 ? to : ofIndex;
        int first = skipWhitespace(from, argumentEnd);
        if (first >= argumentEnd) {
            return new Unknown();
        }
        String text = css.substring(token(first).start,
                token(argumentEnd - 1).end).replaceAll("\\s+", "");
        int a;
        int b;
        String lowercaseText = Selectors.asciiLowercase(text);
        if ("odd".equals(lowercaseText)) {
            a = 2;
            b = 1;
        } else if ("even".equals(lowercaseText)) {
            a = 2;
            b = 0;
        } else {
            Matcher m = AN_PLUS_B.matcher(text);
            if (!m.matches()) {
                return new Unknown();
            }
            try {
                if (m.group(4) != null) {
                    a = 0;
                    b = Integer.parseInt(m.group(4).replace("+", ""));
                } else {
                    String coefficient = m.group(1);
                    if (coefficient.isEmpty() || "+".equals(coefficient)) {
                        a = 1;
                    } else if ("-".equals(coefficient)) {
                        a = -1;
                    } else {
                        a = Integer.parseInt(coefficient.replace("+", ""));
                    }
                    b = m.group(3) == null ? 0
                            : Integer.parseInt(m.group(3));
                    if ("-".equals(m.group(2))) {
                        b = -b;
                    }
                }
            } catch (NumberFormatException e) {
                return new Unknown();
            }
        }
        Complex[] of = ofIndex < 0 ? null
                : parseSelectorList(ofIndex + 1, to, parentSelectors,
                        MODE_PLAIN);
        return new Nth(last, ofType, a, b, of);
    }
}
