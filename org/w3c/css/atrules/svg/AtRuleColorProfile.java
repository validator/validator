//
// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// (c) COPYRIGHT 1995-2000  World Wide Web Consortium (MIT, INRIA, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720
package org.w3c.css.atrules.svg;

import org.w3c.css.parser.AtRule;

public class AtRuleColorProfile extends AtRule {

    static int internal = 0;
    int hash;

    /**
     * Create a new AtRuleColorProfile
     */
    public AtRuleColorProfile() {
        hash = ++internal;
    }

    /**
     * Returns the at rule keyword
     */
    public String keyword() {
        return "color-profile";
    }

    /**
     * The second must be exactly the same as this one
     */
    public boolean canApply(AtRule atRule) {
        return (atRule instanceof AtRuleColorProfile);
    }

    /**
     * Return true if other is an instance of AtRuleColorProfile
     */
    public boolean equals(Object other) {
        return (other instanceof AtRuleColorProfile);
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return (atRule instanceof AtRuleColorProfile);
    }

    /**
     * Returns a string representation of the object
     */
    public String toString() {
        return "@" + keyword();
    }

    public int hashCode() {
        return hash;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
