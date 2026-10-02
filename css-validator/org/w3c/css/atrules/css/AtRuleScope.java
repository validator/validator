//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.parser.CssSelectors;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-cascade-6-20240906/#scope-syntax
 */
public class AtRuleScope extends AtRule {

    ArrayList<CssSelectors> start = null;
    ArrayList<CssSelectors> end = null;

    public String keyword() {
        return "scope";
    }

    public boolean isEmpty() {
        return false;
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return (atRule instanceof AtRuleScope) && toString().equals(atRule.toString());
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return canApply(atRule);
    }

    /**
     * @scope is a group rule: its body holds regular properties, not
     * descriptors, so they are looked up without a prefix.
     */
    public String lookupPrefix() {
        return "";
    }

    public void setStart(ArrayList<CssSelectors> start) {
        this.start = start;
    }

    public void setEnd(ArrayList<CssSelectors> end) {
        this.end = end;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append('@');
        ret.append(keyword());
        if (start != null) {
            ret.append(" (").append(CssSelectors.toArrayString(start)).append(')');
        }
        if (end != null) {
            ret.append(" to (").append(CssSelectors.toArrayString(end)).append(')');
        }
        return ret.toString();
    }
}
