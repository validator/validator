//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang University, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

public class AtRuleCounterStyle extends AtRule {

    String name = null;

    public String keyword() {
        return "counter-style";
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

    public void setName(String name) {
        this.name = name;
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

    public AtRuleCounterStyle(String name) {
        this.name = name;
    }

    public AtRuleCounterStyle() {
    }
}

