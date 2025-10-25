//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 * AtRuleFontFace.java
 * $Id$
 */
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

/**
 * This class manages all media defines by CSS2
 *
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class AtRuleFontFace extends AtRule {
    static int internal = 0;
    int hash;

    /**
     * Create a new AtRuleFontFace
     */
    public AtRuleFontFace() {
        hash = ++internal;
    }


    /**
     * Returns the at rule keyword
     */
    public String keyword() {
        return "font-face";
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return (atRule instanceof AtRuleFontFace);
    }

    /**
     * Return true if other is an instance of AtRUleFontFace
     */
    public boolean equals(Object other) {
        return (other instanceof AtRuleFontFace);
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return (atRule instanceof AtRuleFontFace);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return "@" + keyword();
    }

    public int hashCode() {
        return hash;
    }

    public boolean isEmpty() {
        return false;
    }
}
