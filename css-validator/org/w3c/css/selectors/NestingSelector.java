//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * The nesting selector, "&": In a nested style rule it represents the
 * elements the parent rule matches; elsewhere, the same elements as
 * ":scope".
 *
 * @spec https://drafts.csswg.org/css-nesting/#nest-selector
 */
public class NestingSelector implements Selector {

    /**
     * @see Selector#toString()
     */
    public String toString() {
        return "&";
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
        return "&";
    }
}
