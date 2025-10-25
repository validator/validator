//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 * AtRulePage.java
 * $Id$
 */
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;


/**
 * This class manages all media defines by CSS2
 *
 * @author Philippe Le Hégaret
 * @version $Revision$
 */
public abstract class AtRulePage extends AtRule {

    public ArrayList<String> names = null;

    public ArrayList<ArrayList<String>> pseudos = null;

    /**
     * Returns the at rule keyword
     */
    public final String keyword() {
        return "page";
    }

    public String effectiveKeyword() {
        return keyword();
    }

    /**
     * Sets the name of the page
     * name will be a pseudo name :first, :left, :right
     * or a random name without semi-colon at the beginning
     */
    public abstract AtRulePage addSelector(String name, ArrayList<String> pseudo, ApplContext ac)
            throws InvalidParamException;


    /**
     * Return true if atRule is exactly the same as current
     */
    public boolean equals(Object atRule) {
        AtRulePage other;
        try {
            other = (AtRulePage) atRule;
        } catch (ClassCastException cce) {
            // not an AtRulePage, fail
            return false;
        }
        if ((names != null) && (other.names != null)) {
            if (!names.equals(other.names)) {
                return false;
            }
        } else {
            if ((names != null) || (other.names != null)) {
                return false;
            }
        }
        if ((pseudos != null) && (other.pseudos != null)) {
            return pseudos.equals(other.pseudos);
        } else {
            if ((pseudos != null) || (other.pseudos != null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        AtRulePage other;
        try {
            other = (AtRulePage) atRule;
        } catch (ClassCastException cce) {
            // not an AtRulePage, fail
            return false;
        }
        if ((names != null) && (other.names != null)) {
            if (!names.equals(other.names)) {
                return false;
            }
        } else {
            if ((names != null) || (other.names != null)) {
                return false;
            }
        }
        if ((pseudos != null) && (other.pseudos != null)) {
            return pseudos.equals(other.pseudos);
        } else {
            if ((pseudos != null) || (other.pseudos != null)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        AtRulePage atRulePage;
        try {
            atRulePage = (AtRulePage) atRule;
        } catch (ClassCastException cce) {
            // not an AtRulePage, fail
            return false;
        }
        if ((names != null) && !names.equals(atRulePage.names)) {
            return false;
        }
        if ((pseudos != null) && !pseudos.equals(atRulePage.pseudos)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append('@').append(effectiveKeyword());
        if (names != null) {
            int l = names.size();
            for (int i = 0; i < l; i++) {
                if (i != 0) {
                    ret.append(',');
                }
                ret.append(' ');
                if (names.get(i) != null) {
                    ret.append(names.get(i));
                }
                ArrayList<String> p = pseudos.get(i);
                if (p != null) {
                    for (String pp : p) {
                        ret.append(pp);
                    }
                }
            }
        }
        return ret.toString();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public static final AtRulePage getInstance(CssVersion version) {
        switch (version) {
            case CSS2:
            case CSS21:
                return new org.w3c.css.atrules.css2.AtRulePage();
            case CSS3:
            case CSS:
            case CSS_2015:
                return new org.w3c.css.atrules.css3.AtRulePage();
            default:
                throw new IllegalArgumentException(
                        "AtRulePage.getInstance called with unhandled"
                                + " CssVersion \"" + version.toString() + "\".");
        }
    }
}
