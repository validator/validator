// $Id$
// (c) COPYRIGHT MIT, ERCIM and Keio, 2009.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.combinators;

import org.w3c.css.selectors.Selector;

/**
 * GeneralSibling<br />
 */
public class SubsequentSiblingCombinator implements Selector {

    /**
     * @see Selector#toString()
     */
    public String toString() {
        return " ~ ";
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return "~";
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return false;
    }

}
