// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * Universal<br />
 * Created: Sep 1, 2005 3:45:13 PM<br />
 */
public class UniversalSelector implements Selector {

    String prefix = null;

    public UniversalSelector(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @see Selector#toString()
     */
    public String toString() {
        if (prefix != null) {
            return prefix + "|*";
        }
        return "*";
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return true;
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return "*";
    }

}
