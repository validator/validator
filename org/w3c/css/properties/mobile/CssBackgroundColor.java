// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.mobile;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2008/CR-css-mobile-20081210/#properties
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/colors.html#propdef-background-color
 */
public class CssBackgroundColor extends org.w3c.css.properties.css.CssBackgroundColor {

    public CssValue color;

    /**
     * Create a new CssBackgroundColor
     */
    public CssBackgroundColor() {
        color = transparent;
    }

    /**
     * Create a new CssBackgroundColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssBackgroundColor(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_HASH_IDENT:
                org.w3c.css.values.CssColor c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.toString());
                setColor(c);
                break;
            case CssTypes.CSS_COLOR:
                setColor(val);
                break;
            case CssTypes.CSS_IDENT:
                if (transparent.equals(val)) {
                    setColor(transparent);
                    break;
                }
                if (inherit.equals(val)) {
                    setColor(inherit);
                    break;
                }
                setColor(new org.w3c.css.values.CssColor(ac,
                        (String) val.get()));
                break;
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssBackgroundColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * @param color The color to set.
     */
    public void setColor(CssValue color) {
        this.color = color;
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return color;
    }

    /**
     * Returns the color
     */
    public final CssValue getColor() {
        return color;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return color.equals(inherit);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (color != null) {
            return color.toString();
        }
        return "";
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return color == transparent;
    }
}
