// $Id$
// @author Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS1
 */
public class CssBorderTop extends CssProperty {

    // those are @since CSS1
    public CssBorderTopWidth _width;
    // and @since CSS2
    public CssBorderTopColor _color;
    public CssBorderTopStyle _style;

    /**
     * Create a new CssBorderTop
     */
    public CssBorderTop() {
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderTop(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderTop(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);

    }


    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "border-top";
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }


    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css1Style css1Style = (Css1Style) style;
        css1Style.cssBorder.byUser = byUser;
        if (css1Style.cssBorder.borderTop != null) {
            style.addRedefinitionWarning(ac, this);
        }
        css1Style.cssBorder.borderTop = this;
        if (_width != null) {
            _width.addToStyle(ac, style);
        }
        if (_color != null) {
            _color.addToStyle(ac, style);
        }
        if (_style != null) {
            _style.addToStyle(ac, style);
        }
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getBorder().borderTop;
        } else {
            return ((Css1Style) style).cssBorder.borderTop;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        CssBorderTop other;
        try {
            other = (CssBorderTop) property;
            return ((_width == null && other._width == null) || (_width != null && _width.equals(other._width))) &&
                    ((_color == null && other._color == null) || (_color != null && _color.equals(other._color))) &&
                    ((_style == null && other._style == null) || (_style != null && _style.equals(other._style)));
        } catch (ClassCastException cce) {
            return false;
        }
    }
}
