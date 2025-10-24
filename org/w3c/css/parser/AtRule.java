//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 * AtRule.java
 * $Id$
 */
package org.w3c.css.parser;

/**
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public abstract class AtRule {

    /**
     * Returns the at rule keyword
     */
    public abstract String keyword();

    /**
     * The second must be exactly the same of this one
     */
    public abstract boolean canApply(AtRule atRule);

    /**
     * The second must only match this one
     */
    public abstract boolean canMatch(AtRule atRule);

    public abstract boolean isEmpty();

    /**
     * By default, lookupPrefix is not empty.
     * It is used to return (or not) a prefix used to lookup
     * properties. This should be used only if there are
     * properties specific to that at-rule.
     *
     * @return a string
     * @see .isPropertyLookupStrict
     *      <p/>
     *      Example is @font-face.src,
     *      prefix is 'font-face',
     *      specific property is 'src', and is not a CSS-wide property
     */
    public String lookupPrefix() {
        return keyword();
    }

    /**
     * When a prefix is defined, this stops or not looking up property
     * in the general set of properties.
     * <p/>
     * Example 1:
     *
     * @return a boolean, true if the lookup has to be strict
     * @page arule, z-index property.
     * <p/>
     * prefix should be @page, as it contain a @page specific property: 'page'
     * and lookup should not be strict to allow looking up 'z-index' without having
     * to duplicate as @page.z-index
     * <p/>
     * Example 2:
     * @font-face atrule color property.
     * <p/>
     * The prefix is 'font-face' as specific properties exist (like 'src'),
     * however 'color' and other css-wide properties are not valid there,
     * so lookup should be strict.
     */
    public boolean isPropertyLookupStrict() {
        return true;
    }
}
