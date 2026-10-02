//
// Author: Jens Oliver Meiert
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * The nesting selector (`&amp;`), representing the parent rule's elements
 *
 * @spec https://www.w3.org/TR/2026/WD-css-nesting-1-20260122/#nest-selector
 */
public class NestingSelector implements Selector {

    /**
     * @see Selector#toString()
     */
    public String toString() {
        return "&";
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return "&";
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return true;
    }

}