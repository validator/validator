// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors;

/**
 * Type<br />
 * Created: Sep 1, 2005 3:57:05 PM<br />
 */
public class TypeSelector implements Selector {

    String prefix;
    String name;

    public TypeSelector(String prefix, String type) {
        this.prefix = prefix;
        this.name = type;
    }

    /**
     * Creates a new TypeSelector which name name is type
     *
     * @param type the name of this type selector
     */
    public TypeSelector(String type) {
        this(null, type);
    }

    /**
     * @see Selector#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this selector
     *
     * @param type The name to set.
     */
    public void setName(String type) {
        this.name = type;
    }

    /**
     * @see Selector#canApply(Selector)
     */
    public boolean canApply(Selector other) {
        return false;
    }

    /**
     * @see Selector#toString()
     */
    public String toString() {
        if (prefix != null) {
            StringBuilder sb = new StringBuilder(prefix);
            sb.append('|');
            sb.append(name);
            return sb.toString();
        }
        return name;
    }

}
