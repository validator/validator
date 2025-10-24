// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.attributes;

import org.w3c.css.selectors.AttributeSelector;
import org.w3c.css.selectors.Selector;
import org.w3c.css.util.ApplContext;

/**
 * AttributeBegin<br />
 * Created: Sep 1, 2005 4:26:18 PM<br />
 */
public class AttributeBegin extends AttributeSelector {

    private String value;

    public AttributeBegin(String name, String value) {
        setName(name);
        this.value = value;
    }

    public AttributeBegin(String name, String value, String prefix) {
        setName(name);
        this.value = value;
        setPrefix(prefix);

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
        if (other instanceof AttributeAny) {
            // [lang|=fr][lang]
            return true;
        } else if (other instanceof AttributeExact) {
            String v = ((AttributeExact) other).getValue();
            int index = v.indexOf('-');
            if (index > 0) {
                v = v.substring(0, index);
            }
            if (!value.equals(v)) {
                // [lang|=fr][lang=en-US]
                return false;
            } else {
                // [lang|=en][lang=en-US]
                return true;
            }
        } else if (other instanceof AttributeOneOf) {
            return true;
        } else if (other instanceof AttributeBegin) {
            if (!value.equals(((AttributeBegin) other).value)) {
                // [lang|=fr][lang|=en]
                return false;
            } else {
                // [lang|=en][lang|=en]
                return true;
            }
        }
        return false;
    }

    public void applyAttribute(ApplContext ac, AttributeSelector attr) {
        String name = getName();
        if (name.equals(attr.getName())) {
            // attribute exact knows how to match, delegate...
            if (attr instanceof AttributeExact) {
                ((AttributeExact) attr).applyAttribute(ac, this);
            } else if (attr instanceof AttributeBegin) {
                String val = ((AttributeBegin) attr).getValue();
                // check if one start with the other or not
                if (!val.equals(value) && !value.startsWith(val + '-')
                        && !val.startsWith(value + '-')) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            } else if (attr instanceof AttributeStart) {
                String val = ((AttributeStart) attr).getValue();
                if (!val.equals(value) && !value.startsWith(val)
                        && !val.startsWith(value + '-')) {
                    ac.getFrame().addWarning("incompatible",
                            new String[]{toString(), attr.toString()});
                }
            } else if (attr instanceof AttributeOneOf) {
                ((AttributeOneOf) attr).applyAttribute(ac, this);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(getPrefixedName());
        sb.append("|=\"").append(value).append('"');
        sb.append(getEndingString());
        return sb.toString();
    }

}
