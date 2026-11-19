// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionDir;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionHas;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionHost;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionHostContext;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionIs;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionLang;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionNot;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionNthChild;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionNthCol;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionNthLastChild;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionNthLastOfType;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionNthOfType;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionPseudoElement;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionSlotted;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionWhere;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssProfile;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PseudoFactory<br />
 * Created: Sep 2, 2005 2:41:09 PM<br />
 */
public class PseudoFactory {

    private static final String[] PSEUDOCLASS_CONSTANTSCSS3 = {
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#elemental-selectors
            "defined",
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#location
            "any-link", "link", "visited", "local-link", "target",
            "scope",
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#useraction-pseudos
            "hover", "active", "focus", "focus-visible", "focus-within",
            // https://www.w3.org/TR/2018/WD-selectors-4-20181121/#time-pseudos
            "current", "past", "future",
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#resource-pseudos
            "playing", "paused", "seeking", "buffering", "stalled", "muted",
            "volume-locked",
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#display-state-pseudos
            "open", "popover-open", "modal", "fullscreen", "picture-in-picture",
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#input-pseudos
            "enabled", "disabled", "read-only", "read-write", "placeholder-shown",
            "autofill", "default", "checked", "unchecked", "indeterminate",
            "blank", "valid", "invalid", "in-range", "out-of-range", "required", "optional",
            "user-valid", "user-invalid",
            // https://www.w3.org/TR/2026/WD-selectors-4-20260122/#structural-pseudos
            "root", "empty", "first-child", "last-child", "only-child",
            "first-of-type", "last-of-type", "only-of-type",
            // https://www.w3.org/TR/2018/WD-css-page-3-20181018/#page-selectors
            "left", "right", "first",
            // https://www.w3.org/TR/2014/WD-css-scoping-1-20140403/#selectordef-host0
            "host",
            // https://drafts.csswg.org/css-shadow-1/#selectordef-has-slotted 20260428
            "has-slotted",
            // https://html.spec.whatwg.org/multipage/semantics-other.html#pseudo-classes
            "autofill", "defined"
    };

    private static final String[] PSEUDOCLASS_CONSTANTSCSS2 = {
            "link", "visited", "active", "focus",
            "hover", "first-child"
    };


    private static final String[] PSEUDOCLASS_CONSTANTSTV = {
            "link", "visited", "active", "focus", "first-child"
    };

    private static final String[] PSEUDOCLASS_CONSTANTSCSS1 = {
            "link", "visited", "active"
    };

    private static final String[] PSEUDOCLASS_CONSTANTS_MOBILE = {
            "link", "visited", "active", "focus"
    };

    private static final String[] PSEUDOELEMENT_CONSTANTSCSS3 = {
            // https://www.w3.org/TR/2025/WD-css-pseudo-4-20250627/#typographic-pseudos
            "first-line", "first-letter", "prefix", "suffix", // TODO check that prefix and suffix can only be on ::first-letter
            // https://www.w3.org/TR/2025/WD-css-pseudo-4-20250627/#highlight-pseudos
            "selection", "search-text", "target-text", "spelling-error", "grammar-error",
            // https://www.w3.org/TR/2025/WD-css-pseudo-4-20250627/#treelike
            "before", "after", "marker", "placeholder", "file-selector-button",
            // https://www.w3.org/TR/2025/WD-css-pseudo-4-20250627/#element-like
            "file-selector-button", "details-content",
            // https://fullscreen.spec.whatwg.org/#::backdrop-pseudo-element
            "backdrop",
            // https://www.w3.org/TR/2026/CRD-webvtt1-20260520/#css-extensions
            "cue", "cue-region",
            // https://www.w3.org/TR/2014/WD-css-scoping-1-20140403/ (20260428 draft)
            "shadow"
    };

    private static final String[] PSEUDOELEMENT_FUNCTION_CONSTANTSCSS3 = {
            // https://drafts.csswg.org/css-pseudo-4/#selectordef-highlight 20261118
            "highlight",
            // https://drafts.csswg.org/css-shadow-1/#selectordef-part 20261118
            "part",
            // https://drafts.csswg.org/css-forms-1/#selectordef-picker 20261118
            "picker",
            // https://drafts.csswg.org/css-overflow-5/#selectordef-scroll-button 20261118
            "scroll-button",
            // https://drafts.csswg.org/css-view-transitions-2/#pseudo 20261118
            "view-transition-group", "view-transition-image-pair",
            "view-transition-old", "view-transition-new",
            // https://drafts.csswg.org/css-view-transitions-2/#selectordef-view-transition-group-children 20261118
            "view-transition-group-children"
    };

    // https://drafts.csswg.org/css-overflow-5/#typedef-scroll-button-direction
    private static final Set<String> SCROLL_BUTTON_DIRECTIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("up", "down", "left", "right", "block-start",
                    "block-end", "inline-start", "inline-end", "prev", "next")));

    // https://www.w3.org/TR/css-values-4/#custom-idents
    private static final Set<String> CUSTOM_IDENT_EXCLUSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("initial", "inherit", "unset", "revert",
                    "revert-layer", "default")));

    // https://www.w3.org/TR/css-syntax-3/#ident-token-diagram
    private static final String ESCAPE = "\\\\(?:[0-9a-fA-F]{1,6}(?:\\r\\n|[ \\t\\n\\r\\f])?|[^\\n\\r\\f0-9a-fA-F])";
    private static final String IDENT = "(?:--|-?(?:[_a-zA-Z]|[^\\x00-\\x7F]|" + ESCAPE + "))"
            + "(?:[_a-zA-Z0-9-]|[^\\x00-\\x7F]|" + ESCAPE + ")*";

    // one token of a functional pseudo-element's argument, with any whitespace before it:
    // an ident, '*', or '.'
    private static final Pattern ARG_TOKEN = Pattern.compile("\\G([ \\t\\n\\r\\f]*)(?:(" + IDENT + ")|(\\*)|(\\.))");

    private static final String[] PSEUDOELEMENT_CONSTANTSCSS2 = {
            "first-line", "first-letter", "before", "after"
    };

    private static final String[] PSEUDOELEMENT_CONSTANTSCSS1 = {
            "first-line", "first-letter"
    };

    private static final String[] PSEUDOCLASS_FUNCTION_CONSTANTSCSS3 = {
            "nth-child", "nth-last-child", "nth-of-type", "nth-last-of-type",
            "lang", "not" // from selectors-4 unstable list (20190624)
            , "nth-col", "nth-last-col", "is", "where", "has", "dir",
            // // https://www.w3.org/TR/2014/WD-css-scoping-1-20140403/
            "host", "host-context", "slotted"
    };

    private static final String[] PSEUDOFUNCTION_CONSTANTSCSS2 = {
            "lang"
    };

    /**
     * Returns the possible pseudo-classes for a version/profile pair
     *
     * @param version the CSS Level to get associated pseudo-classes definitions
     * @param profile the profile to get associated pseudo-classes
     * @return the possible pseudo-classes for the version/profile
     */
    public static String[] getPseudoClass(CssVersion version, CssProfile profile) {
        // TODO we might need some merging in some cases
        // unused for now, but leaving the TODO to find it easily.
        switch (profile) {
            case TV:
                return PSEUDOCLASS_CONSTANTSTV;
            case MOBILE:
                return PSEUDOCLASS_CONSTANTS_MOBILE;
        }
        // not one of a specific version, let's match on CSS Version
        switch (version) {
            case CSS1:
                return PSEUDOCLASS_CONSTANTSCSS1;
            case CSS2:
            case CSS21:
                return PSEUDOCLASS_CONSTANTSCSS2;
            case CSS3:
                return PSEUDOCLASS_CONSTANTSCSS3;
        }
        // and the default
        return null;
    }

    /**
     * Returns the possible pseudo-elements for a profile
     *
     * @param version the CSS Version^Wlevel to get associated pseudo-elements
     * @return the possible pseudo-elements for the profile
     */
    public static String[] getPseudoElement(CssVersion version) {
        switch (version) {
            case CSS2:
            case CSS21:
                return PSEUDOELEMENT_CONSTANTSCSS2;
            case CSS3:
                return PSEUDOELEMENT_CONSTANTSCSS3;
            case CSS1:
                return PSEUDOELEMENT_CONSTANTSCSS1;
            default:
                return null;

        }
    }

    /**
     * Returns the possible functional pseudo-elements for a version
     *
     * @param version the CSS level to get associated functional pseudo-elements
     * @return the possible functional pseudo-elements for the version
     */
    public static String[] getPseudoElementFunction(CssVersion version) {
        switch (version) {
            case CSS3:
                return PSEUDOELEMENT_FUNCTION_CONSTANTSCSS3;
            default:
                return null;
        }
    }

    /**
     * Returns a functional pseudo-element whose argument isn't a selector, after
     * checking that the argument matches the grammar for that pseudo-element
     *
     * @param name  the name of the pseudo-element
     * @param param its argument, as written
     * @throws InvalidParamException if the pseudo-element is unknown, or its
     *                               argument doesn't match its grammar
     */
    public static PseudoFunctionSelector newPseudoElementFunction(String name,
                                                                  String param,
                                                                  ApplContext ac)
            throws InvalidParamException {
        String[] names = getPseudoElementFunction(ac.getCssVersion());
        boolean known = false;
        if (names != null) {
            for (String s : names) {
                if (s.equals(name)) {
                    known = true;
                    break;
                }
            }
        }
        if (!known) {
            throw new InvalidParamException("pseudo", "::" + name + "()", ac);
        }
        String arg = (param == null) ? "" : param;
        int[] span = new int[2];
        ArrayList<ArgToken> tokens = tokenize(arg, span);
        boolean valid = tokens != null && !tokens.isEmpty();
        if (valid) {
            switch (name) {
                case "highlight":
                    // ::highlight( '*' | <custom-ident> )
                    valid = tokens.size() == 1
                            && (tokens.get(0).isStar() || tokens.get(0).isCustomIdent());
                    break;
                case "part":
                    // ::part( <ident>+ )
                    for (ArgToken t : tokens) {
                        valid = valid && t.ident != null;
                    }
                    break;
                case "picker":
                    // ::picker( <form-control-identifier>+ ), where
                    // <form-control-identifier> = select
                    for (ArgToken t : tokens) {
                        valid = valid && t.ident != null && t.keyword().equals("select");
                    }
                    break;
                case "scroll-button":
                    // ::scroll-button( '*' | <scroll-button-direction> )
                    valid = tokens.size() == 1 && (tokens.get(0).isStar()
                            || (tokens.get(0).ident != null
                            && SCROLL_BUTTON_DIRECTIONS.contains(tokens.get(0).keyword())));
                    break;
                case "view-transition-group-children":
                    // ::view-transition-group-children( <pt-name-selector> ), where
                    // <pt-name-selector> = '*' | <custom-ident>
                    valid = tokens.size() == 1
                            && (tokens.get(0).isStar() || tokens.get(0).isCustomIdent());
                    break;
                default:
                    // the other ::view-transition-*() pseudo-elements
                    valid = isPtNameAndClassSelector(tokens);
                    break;
            }
        }
        // the argument without the whitespace around it, but with any that ends an escape
        String shown = (tokens == null) ? arg.trim() : arg.substring(span[0], span[1]);
        if (!valid) {
            throw new InvalidParamException("value", shown, "::" + name + "()", ac);
        }
        return new PseudoFunctionPseudoElement(name, shown);
    }

    /**
     * A token in the argument of a functional pseudo-element: an ident, '*',
     * or '.'
     */
    private static class ArgToken {
        final String ident;
        final char delim;
        final boolean spaceBefore;

        ArgToken(String ident, char delim, boolean spaceBefore) {
            this.ident = ident;
            this.delim = delim;
            this.spaceBefore = spaceBefore;
        }

        boolean isStar() {
            return delim == '*';
        }

        boolean isDot() {
            return delim == '.';
        }

        // the ident with its escapes decoded, and ASCII-lowercased for comparing with keywords
        String keyword() {
            return decodeIdent(ident).toLowerCase(Locale.ROOT);
        }

        boolean isCustomIdent() {
            return ident != null && !CUSTOM_IDENT_EXCLUSIONS.contains(keyword());
        }
    }

    /**
     * Splits the argument of a functional pseudo-element into idents, '*'s,
     * and '.'s, or returns null if it has anything else
     *
     * @param span gets the start of the first token and the end of the last
     */
    private static ArrayList<ArgToken> tokenize(String arg, int[] span) {
        ArrayList<ArgToken> tokens = new ArrayList<>();
        Matcher m = ARG_TOKEN.matcher(arg);
        int end = 0;
        while (m.find()) {
            if (tokens.isEmpty()) {
                span[0] = m.end(1);
            }
            char delim = (m.group(3) != null) ? '*' : (m.group(4) != null) ? '.' : 0;
            tokens.add(new ArgToken(m.group(2), delim, !m.group(1).isEmpty()));
            end = m.end();
        }
        span[1] = end;
        for (int i = end; i < arg.length(); i++) {
            if (" \t\n\r\f".indexOf(arg.charAt(i)) < 0) {
                return null;
            }
        }
        return tokens;
    }

    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * Returns an ident with its escapes replaced by the characters they stand for
     */
    private static String decodeIdent(String ident) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int len = ident.length();
        while (i < len) {
            char c = ident.charAt(i);
            if (c != '\\' || i + 1 >= len) {
                sb.append(c);
                i++;
                continue;
            }
            int j = i + 1;
            while (j < len && j - i <= 6 && isHexDigit(ident.charAt(j))) {
                j++;
            }
            if (j == i + 1) {
                sb.append(ident.charAt(j));
                i = j + 1;
                continue;
            }
            int cp = Integer.parseInt(ident.substring(i + 1, j), 16);
            if (cp == 0 || cp > 0x10FFFF || (cp >= 0xD800 && cp <= 0xDFFF)) {
                cp = 0xFFFD;
            }
            sb.appendCodePoint(cp);
            if (ident.startsWith("\r\n", j)) {
                j += 2;
            } else if (j < len && " \t\n\r\f".indexOf(ident.charAt(j)) >= 0) {
                j++;
            }
            i = j;
        }
        return sb.toString();
    }

    /**
     * Checks a <pt-name-and-class-selector>:
     * <pt-name-selector> <pt-class-selector>? | <pt-class-selector>, where
     * <pt-name-selector> = '*' | <custom-ident> and
     * <pt-class-selector> = ['.' <custom-ident>]+
     */
    private static boolean isPtNameAndClassSelector(ArrayList<ArgToken> tokens) {
        int i = 0;
        int size = tokens.size();
        if (tokens.get(0).isStar() || tokens.get(0).isCustomIdent()) {
            i = 1;
        }
        while (i < size) {
            if (i + 1 >= size || !tokens.get(i).isDot() || !tokens.get(i + 1).isCustomIdent()
                    || tokens.get(i + 1).spaceBefore) {
                return false;
            }
            i += 2;
        }
        for (int k = 1; k < size; k++) {
            if (tokens.get(k).spaceBefore) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the possible pseudo-functions for a profile
     *
     * @param version the profile to get associated pseudo-functions
     * @return the possible pseudo-functions for the profile
     */
    public static String[] getPseudoFunction(CssVersion version) {
        switch (version) {
            case CSS2:
            case CSS21:
                return PSEUDOFUNCTION_CONSTANTSCSS2;
            case CSS3:
                return PSEUDOCLASS_FUNCTION_CONSTANTSCSS3;
            case CSS1:
            default:
                return null;
        }
    }

    /**
     * Returns the possible pseudo-elements written as pseudo-classes
     * for a specific profile
     *
     * @param version the profile to get associated exceptions to the rule
     * @return the possible pseudo-elements/classes for the profile
     */
    public static String[] getPseudoElementExceptions(CssVersion version) {
        switch (version) {
            case CSS2:
            case CSS21:
            case CSS3:
                return PSEUDOELEMENT_CONSTANTSCSS2;
            case CSS1:
            default:
                return null;
        }
    }

    /**
     * Returns a PseudoFunctionSelector based on the name of the
     * selector
     *
     * @param name,  the name of the pseudofun selector
     * @param value, its value
     * @throws InvalidParamException
     */
    public static PseudoFunctionSelector newPseudoFunction(String name,
                                                           String value, ApplContext ac)
            throws InvalidParamException {
        if (name == null) {
            throw new InvalidParamException("pseudo",
                    "null pseudofunction", ac);
        }
        if (name.equals("not")) {
            return new PseudoFunctionNot(name, value);
        }
        if (name.equals("slotted")) {
            return new PseudoFunctionSlotted(name, value);
        }
        if (name.equals("host")) {
            return new PseudoFunctionSlotted(name, value);
        }
        if (name.equals("host-context")) {
            return new PseudoFunctionSlotted(name, value);
        }
        throw new InvalidParamException("pseudo",
                ":" + name, ac);
    }

    public static PseudoFunctionSelector newPseudoFunction(String name,
                                                           ArrayList<CssSelectors> value,
                                                           ApplContext ac)
            throws InvalidParamException {
        if (name == null) {
            throw new InvalidParamException("pseudo",
                    "null pseudofunction", ac);
        }
        if (name.equals("not")) {
            return new PseudoFunctionNot(name, value);
        }
        if (name.equals("is")) {
            return new PseudoFunctionIs(name, value);
        }
        if (name.equals("where")) {
            return new PseudoFunctionWhere(name, value);
        }
        if (name.equals("has")) {
            return new PseudoFunctionHas(name, value);
        }
        if (name.equals("slotted")) {
            return new PseudoFunctionSlotted(name, value);
        }
        if (name.equals("host")) {
            return new PseudoFunctionHost(name, value);
        }
        if (name.equals("host-context")) {
            return new PseudoFunctionHostContext(name, value);
        }
        throw new InvalidParamException("pseudo",
                ":" + name, ac);
    }

    public static PseudoFunctionSelector newPseudoFunction(String name,
                                                           CssExpression exp,
                                                           ApplContext ac)
            throws InvalidParamException {
        if (name == null) {
            throw new InvalidParamException("pseudo",
                    "null pseudofunction", ac);
        }
        if (name.equals("dir")) {
            return new PseudoFunctionDir(name, exp, ac);
        }
        if (name.equals("lang")) {
            return new PseudoFunctionLang(name, exp, ac);
        }
        if (name.equals("nth-col")) {
            return new PseudoFunctionNthCol(name, exp, ac);
        }
        if (name.equals("nth-last-col")) {
            return new PseudoFunctionNthCol(name, exp, ac);
        }
        throw new InvalidParamException("pseudo",
                ":" + name, ac);
    }

    public static PseudoFunctionSelector newPseudoFunction(String name,
                                                           CssExpression exp,
                                                           ArrayList<CssSelectors> selector_list,

                                                           ApplContext ac)
            throws InvalidParamException {
        if (name == null) {
            throw new InvalidParamException("pseudo",
                    "null pseudofunction", ac);
        }
        if (name.equals("nth-child")) {
            return new PseudoFunctionNthChild(name, exp, selector_list, ac);
        }
        if (name.equals("nth-last-child")) {
            return new PseudoFunctionNthLastChild(name, exp, selector_list, ac);
        }
        if (name.equals("nth-of-type")) {
            return new PseudoFunctionNthOfType(name, exp, selector_list, ac);
        }
        if (name.equals("nth-last-of-type")) {
            return new PseudoFunctionNthLastOfType(name, exp, selector_list, ac);
        }
        throw new InvalidParamException("pseudo",
                ":" + name, ac);
    }
}
