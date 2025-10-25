// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * IdSelector<br />
 * Created: Sep 1, 2005 4:45:00 PM<br />
 */
public class IdSelector implements Selector {

    String name;

    /**
     * Creates a new id selector given its name
     *
     * @param name
     */
    public IdSelector(String name) {
        this.name = name;
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this id selector
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
        return '#' + name;
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return false;
    }

}
