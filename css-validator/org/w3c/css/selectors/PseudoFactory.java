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
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionSlotted;
import org.w3c.css.selectors.pseudofunctions.PseudoFunctionWhere;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssProfile;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

import java.util.ArrayList;

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
