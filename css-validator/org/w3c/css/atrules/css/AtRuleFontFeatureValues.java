// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.properties.css3.fontpalettevalues.CssFontFamily;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

/**
 * @see CssFontFamily
 */
public class AtRuleFontFeatureValues extends AtRule {
    CssValue target = null;

    /**
     * Create a new AtRuleFontFeatureValues
     */
    public AtRuleFontFeatureValues() {
    }

    public AtRuleFontFeatureValues(CssValue target) {
        this.target = target;
    }

    public void setTarget(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        // as it has the same syntax as @font-palette-values.font-family, let's reuse that


        CssFontFamily fontFamily = null;
        try {
            fontFamily = new CssFontFamily(ac, expression);
        } catch (InvalidParamException iex) {
            throw new InvalidParamException("value",
                    expression.toStringFromStart(),
                    keyword(), ac);
        }
        this.target = fontFamily.value;
    }

    /**
     * Returns the at rule keyword
     */
    public String keyword() {
        return "font-feature-values";
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return (atRule instanceof AtRuleFontFeatureValues);
    }

    /**
     * Return true if other is an instance of AtRUleFontFeatureValues
     */
    public boolean equals(Object other) {
        return (other instanceof AtRuleFontFeatureValues);
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return (atRule instanceof AtRuleFontFeatureValues);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(keyword());
        if (target != null) {
            sb.append(' ').append(target.getType());
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean isPropertyLookupStrict() {
        return true;
    }
}
