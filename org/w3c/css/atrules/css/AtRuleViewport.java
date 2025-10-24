//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang University, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

public class AtRuleViewport extends AtRule {

    static CssIdent auto;

    static {
        auto = CssIdent.getIdent("auto");
    }

    public String keyword() {
        return "viewport";
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return false;
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append('@');
        ret.append(keyword());
        return ret.toString();
    }

    public AtRuleViewport() {
    }

    public static CssValue checkViewportLenght(CssValue v, CssProperty caller, ApplContext ac)
            throws InvalidParamException {
        switch (v.getType()) {
            case CssTypes.CSS_NUMBER:
                v.getCheckableValue().checkEqualsZero(ac, caller);
                return v;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                v.getCheckableValue().checkPositiveness(ac, caller);
                return v;
            case CssTypes.CSS_IDENT:
                if (auto.equals(v)) {
                    return auto;
                }
            default:
                throw new InvalidParamException("value",
                        v.toString(),
                        caller.getPropertyName(), ac);
        }
    }

    public static CssValue checkViewportZoom(CssValue v, CssProperty caller, ApplContext ac)
            throws InvalidParamException {
        switch (v.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
                v.getCheckableValue().checkPositiveness(ac, caller);
                return v;
            case CssTypes.CSS_IDENT:
                if (auto.equals(v)) {
                    return auto;
                }
            default:
                throw new InvalidParamException("value",
                        v.toString(),
                        caller.getPropertyName(), ac);
        }
    }
}

