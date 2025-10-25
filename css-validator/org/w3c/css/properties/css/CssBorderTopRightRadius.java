// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.properties.css3.Css3Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

/**
 * @version $Revision$
 * @since CSS3
 */
public class CssBorderTopRightRadius extends CssProperty {

    public CssValue h_radius;
    public CssValue v_radius;

    /**
     * Create a new CssBorderTopRightRadius
     */
    public CssBorderTopRightRadius() {
    }

    /**
     * Creates a new CssBorderTopRightRadius
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderTopRightRadius(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                expression.getValue().toString(),
                getPropertyName(), ac);
    }

    public CssBorderTopRightRadius(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    private void syncval() {
        if (h_radius.equals(v_radius)) {
            value = h_radius;
        } else {
            CssValueList vlist = new CssValueList();
            vlist.add(h_radius);
            vlist.add(v_radius);
            value = vlist;
        }
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        if ((value == null) && (h_radius != null)) {
            syncval();
        }
        return value;
    }


    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "border-top-right-radius";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        if ((value == null) && (h_radius != null)) {
            syncval();
        }
        return value.equals(inherit);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if ((value == null) && (h_radius != null)) {
            syncval();
        }
        return value.toString();
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css1Style) style).cssBorder.borderRadius.topRight != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssBorder.borderRadius.topRight = this;
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        if ((value == null) && (h_radius != null)) {
            syncval();
        }
        return (property instanceof CssBorderTopRightRadius &&
                value.equals(((CssBorderTopRightRadius) property).value));
    }


    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getBorderTopRightRadius();
        } else {
            return ((Css1Style) style).cssBorder.borderRadius.topRight;
        }
    }
}

