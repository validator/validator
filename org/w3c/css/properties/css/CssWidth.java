//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT, ERCIM and Keio University 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;

/**
 * @since CSS1
 */
public class CssWidth extends CssProperty {

    public static CssIdent auto = CssIdent.getIdent("auto");


    /**
     * Create a new CssWidth
     */
    public CssWidth() {
    }

    /**
     * Create a new CssWidth.
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssWidth(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
    }

    /**
     * Returns the value of this property.
     */
    public Object get() {
        return null;
    }

    /**
     * Returns the name of this property.
     */
    public final String getPropertyName() {
        return "width";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return inherit == value;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }


    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css1Style style0 = (Css1Style) style;
        if (style0.cssWidth != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.cssWidth = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getWidth();
        } else {
            return ((Css1Style) style).cssWidth;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssWidth) && ((CssWidth) property).value.equals(value);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }
}
