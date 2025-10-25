// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.attributes;

import org.w3c.css.selectors.AttributeSelector;
import org.w3c.css.selectors.Selector;
import org.w3c.css.util.ApplContext;

/**
 * AttributeStart<br />
 * Created: Sep 1, 2005 4:32:57 PM<br />
 */
public class AttributeStart extends AttributeSelector {

    private String value;

    public AttributeStart(String name, String value) {
        setName(name);
        this.value = value;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean canApply(Selector other) {
        return true;
    }

    public void applyAttribute(ApplContext ac, AttributeSelector attr) {
        String name = getName();
        if (name.equals(attr.getName())) {
            // attribute exact knows how to match, delegate...
            if (attr instanceof AttributeExact) {
                ((AttributeExact) attr).applyAttribute(ac, this);
            } else if (attr instanceof AttributeBegin) {
                ((AttributeBegin) attr).applyAttribute(ac, this);
            } else if (attr instanceof AttributeOneOf) {
                ((AttributeOneOf) attr).applyAttribute(ac, this);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(getPrefixedName());
        sb.append("^=\"").append(value).append('"');
        sb.append(getEndingString());
        return sb.toString();
    }

}
