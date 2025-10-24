// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css21;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/colors.html#propdef-color
 * Note that Color check against CSS levels is mostly done in CSS values.
 */
public class CssColor extends org.w3c.css.properties.css.CssColor {

    org.w3c.css.values.CssColor color;
    String attrvalue = null;
    boolean inherited;

    /**
     * Create a new CssColor
     */
    public CssColor() {
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();
        setByUser();
        switch (val.getType()) {
            case CssTypes.CSS_HASH_IDENT:
                org.w3c.css.values.CssColor c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.toString());
                color = c;
                break;
            case CssTypes.CSS_IDENT:
                if (inherit.equals(val)) {
                    inherited = true;
                } else {
                    color = new org.w3c.css.values.CssColor(ac, (String) val.get());
                }
                break;
            // in the parser, rgb func and hexval func generate a CssColor directly
            // so, no need for a CSS_FUNCTION case
            case CssTypes.CSS_COLOR:
                try {
                    color = (org.w3c.css.values.CssColor) val;
                } catch (ClassCastException ex) {
                    // as we checked the type, it can't happen
                    throw new InvalidParamException("value", expression.getValue(),
                            getPropertyName(), ac);
                }
                break;
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return (inherited) ? inherit : color;
    }

    /**
     * Returns the color
     */
    public org.w3c.css.values.CssColor getColor() {
        return (inherited) ? null : color;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return inherited;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (attrvalue != null) {
            return attrvalue;
        } else {
            return (inherited) ? inherit.toString() : color.toString();
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        CssColor other;
        try {
            other = (CssColor) property;
            return ((inherited && other.inherited) ||
                    (!inherited && !other.inherited) && (color.equals(other.color)));
        } catch (ClassCastException ex) {
            return false;
        }
    }
}
