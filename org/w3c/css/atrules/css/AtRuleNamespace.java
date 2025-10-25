//
// $Id$
//
// (c) COPYRIGHT MIT, Keio University and ERCIM, 2009.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 * AtRuleMedia.java
 * $Id$
 */
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

/**
 * This class manages all imports
 *
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class AtRuleNamespace extends AtRule {

    boolean is_default = false;
    boolean is_url = false;
    String prefix = null;
    String nsname = null;

    public String keyword() {
        return "namespace";
    }

    public boolean isEmpty() {
        return true;
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
        ret.append(' ');
        if (!is_default) {
            ret.append(prefix);
            ret.append(' ');
        }
        ret.append((is_url) ? "url(\"" : '\"');
        ret.append(nsname);
        ret.append((is_url) ? "\")" : '\"');
        ret.append(';');
        return ret.toString();
    }

    public AtRuleNamespace(String prefix, String nsname, boolean is_url) {
        this.prefix = prefix;
        is_default = (prefix == null);
        this.nsname = nsname;
        this.is_url = is_url;
    }
}

