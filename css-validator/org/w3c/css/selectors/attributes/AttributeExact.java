// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.attributes;

import org.w3c.css.selectors.AttributeSelector;
import org.w3c.css.selectors.Selector;
import org.w3c.css.util.ApplContext;

/**
 * AttributeExact<br />
 * Created: Sep 1, 2005 4:22:42 PM<br />
 */
public class AttributeExact extends AttributeSelector {

    private String value;

    public AttributeExact(String name, String value) {
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
        if (getName().equals(other.getName())) {
            if (other instanceof AttributeAny) {
                // [lang=fr][lang]
                return true;
            } else if (other instanceof AttributeExact ||
                    other instanceof AttributeOneOf ||
                    other instanceof AttributeBegin) {
                // FIXME sounds like canApply is not used anyway
                // + the comparison function is not right and doesn't
                // take into account all kind the selector attributes
                if (!value.equals(((AttributeExact) other).getValue())) {
                    // [lang=fr][lang=en]
                    return false;
                } else {
                    // [lang=en][lang=en]
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void applyAttribute(ApplContext ac, AttributeSelector attr) {
        String name = getName();
        // if same name... check if they are incompatible or not
        if (name.equals(attr.getName())) {
            if (attr instanceof AttributeExact) {
                // and not the same value, raise a warning
                if (!value.equals(((AttributeExact) attr).getValue())) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            } else if (attr instanceof AttributeOneOf) {
                // delegate the match to OneOf
                ((AttributeOneOf) attr).applyAttribute(ac, this);
            } else if (attr instanceof AttributeBegin) {
                String othervalue = ((AttributeBegin) attr).getValue();
                // check if [lang|=en][lang=fr-FR] are incompatible
                // form CSS3 selectors about AttributeBegin
                // [[ its value either being exactly "val" or beginning
                //    with "val" immediately followed by "-" (U+002D).  ]]
                if (!value.equals(othervalue) &&
                        !value.startsWith(othervalue + "-")) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            } else if (attr instanceof AttributeSubstr) {
                String othervalue = ((AttributeSubstr) attr).getValue();
                if (value.indexOf(othervalue) < 0) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            } else if (attr instanceof AttributeStart) {
                String othervalue = ((AttributeStart) attr).getValue();
                if (!value.startsWith(othervalue)) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            } else if (attr instanceof AttributeSuffix) {
                String othervalue = ((AttributeSuffix) attr).getValue();
                if (!value.endsWith(othervalue)) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(getPrefixedName());
        sb.append("=\"").append(value).append('"');
        sb.append(getEndingString());
        return sb.toString();
    }

}
