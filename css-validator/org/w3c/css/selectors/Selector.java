// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * Selector<br />
 * Basic class for all the selectors
 * Created: Sep 1, 2005 3:31:35 PM<br />
 */
public interface Selector {

    /**
     * Returns a String representation of this Selector
     *
     * @return the String representation of this Selector
     */
    public String toString();

    /**
     * Returns the name of this selector
     *
     * @return
     */
    public String getName();

    /**
     * Returns <code>true</code> if a selector can be applied to this selector
     *
     * @param other
     * @return
     */
    public boolean canApply(Selector other);
}
