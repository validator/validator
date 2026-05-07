// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.Arrays;

public class AtRuleKeyframes extends AtRule {

    static final CssIdent to, from;
    static final CssIdent[] forbiddenValues;

    static {
        to = CssIdent.getIdent("to");
        from = CssIdent.getIdent("from");

        String[] _forbiddenValues = {"none"};
        forbiddenValues = new CssIdent[_forbiddenValues.length];
        for (int i = 0; i < forbiddenValues.length; i++) {
            forbiddenValues[i] = CssIdent.getIdent(_forbiddenValues[i]);
        }
        Arrays.sort(forbiddenValues);
    }

    public static final boolean isForbiddenValue(CssIdent ident) {
        for (CssIdent id : forbiddenValues) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    public static void checkSelectorValue(CssValue selector, ApplContext ac)
            throws InvalidParamException {
        switch (selector.getType()) {
            case CssTypes.CSS_PERCENTAGE:
                CssPercentage percentage = selector.getPercentage();
                if (!percentage.isPositive() || percentage.floatValue() > 100.f) {
                    throw new InvalidParamException("range", ac);
                }
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = (CssIdent) selector;
                if (to.equals(id) || from.equals(id)) {
                    break;
                }
            default:
                throw new InvalidParamException("selectorname", selector.toString(), ac);
        }
    }

    String name = null;

    public String keyword() {
        return "keyframes";
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

    public void setName(ApplContext ac, String name)
            throws InvalidParamException {
        if (name != null) {
            if (name.charAt(0) != '"') {
                CssIdent ident = CssIdent.getIdent(name.toLowerCase());
                if (CssIdent.isCssWide(ident) || isForbiddenValue(ident)) {
                    throw new InvalidParamException("unrecognize", name, ac);
                }
            }
        }
        this.name = name;
    }

    public String lookupPrefix() {
        return "";
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append('@');
        ret.append(keyword());
        ret.append(' ');
        ret.append(name);
        return ret.toString();
    }

    public AtRuleKeyframes(String name) {
        this.name = name;
    }
}

