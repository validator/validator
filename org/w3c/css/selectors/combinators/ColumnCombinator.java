//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.combinators;

import org.w3c.css.selectors.Selector;

/**
 * Column Combinator
 */
public class ColumnCombinator implements Selector {

    /**
     * @see Selector#toString()
     */
    public String toString() {
        return " || ";
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return "||";
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return false;
    }

}
