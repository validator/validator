//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

public class AtRulePositionTry extends AtRule {
    String name = null;

    /**
     * Create a new AtRulePositionTry
     */
    public AtRulePositionTry() {
    }

    public AtRulePositionTry(String name) {
        this.name = name;
    }

    /**
     * Returns the at rule keyword
     */
    public String keyword() {
        return "position-try";
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return (atRule instanceof AtRulePositionTry);
    }

    /**
     * Return true if other is an instance of AtRulePositionTry
     */
    public boolean equals(Object other) {
        return (other instanceof AtRulePositionTry);
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return (atRule instanceof AtRulePositionTry);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(keyword());
        if (name !=null ) {
            sb.append(' ').append(name);
        }
        return sb.toString();
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isEmpty() {
        return false;
    }
}
