//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.parser.CssSelectors;

import java.util.ArrayList;

/**
 * @scope [(&lt;scope-start&gt;)]? [to (&lt;scope-end&gt;)]?
 *
 * @spec https://drafts.csswg.org/css-cascade-6/#scope-atrule
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
        return false;
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return false;
    }

    public void setStart(ArrayList<CssSelectors> start) {
        this.start = start;
    }

    public void setEnd(ArrayList<CssSelectors> end) {
        this.end = end;
    }

    private static void appendSelectors(StringBuilder ret,
                                        ArrayList<CssSelectors> selectors) {
        ret.append('(');
        boolean first = true;
        for (CssSelectors selector : selectors) {
            if (!first) {
                ret.append(", ");
            } else {
                first = false;
            }
            ret.append(selector);
        }
        ret.append(')');
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append('@');
        ret.append(keyword());
        if (start != null) {
            ret.append(' ');
            appendSelectors(ret, start);
        }
        if (end != null) {
            ret.append(" to ");
            appendSelectors(ret, end);
        }
        return ret.toString();
    }

    public String lookupPrefix() {
        return "";
    }
}
