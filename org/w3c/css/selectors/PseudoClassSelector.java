// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

import java.util.Arrays;

/**
 * PseudoClass<br />
 * Created: Sep 1, 2005 3:58:43 PM<br />
 */
public class PseudoClassSelector implements Selector {

    String name;

    private static final String[] USER_ACTION_CLASSES = {
            "hover", "active", "focus"
    };

    /**
     * Creates a new pseudo-class given its name
     *
     * @param name the name of this pseudo-class
     */
    public PseudoClassSelector(String name) {
        this.name = name;
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this pseudo-class
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see Selector#toString()
     */
    public String toString() {
        return ":" + name;
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return false;
    }

    public boolean isUserAction() {
        return Arrays.asList(USER_ACTION_CLASSES).contains(this.name);
    }

}
