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
import org.w3c.css.values.CssValue;

/**
 * @since CSS1
 */
public class CssBorderColor extends CssProperty {

    // those are @since CSS2
    public CssBorderTopColor top;
    public CssBorderBottomColor bottom;
    public CssBorderLeftColor left;
    public CssBorderRightColor right;

    public boolean shorthand;

    /**
     * Create a new CssBorderColor
     */
    public CssBorderColor() {
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderColor(ApplContext ac, CssExpression expression)
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
    public CssBorderColor(ApplContext ac, CssExpression expression,
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
     * Returns the color
     */
    public CssValue getColor() {
        return value;
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "border-color";
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
        CssBorder cssBorder = ((Css1Style) style).cssBorder;
        cssBorder.borderColor.byUser = byUser;
        if (cssBorder.borderColor.shorthand) {
            style.addRedefinitionWarning(ac, this);
        } else {
            top.addToStyle(ac, style);
            right.addToStyle(ac, style);
            bottom.addToStyle(ac, style);
            left.addToStyle(ac, style);
        }
        cssBorder.borderColor.shorthand = shorthand;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getBorder().borderColor;
        } else {
            return ((Css1Style) style).cssBorder.borderColor;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        try {
            CssBorderColor other = (CssBorderColor) property;
            // FIXME check compound ?
            return ((left != null && left.equals(other.left)) || (left == null && other.left == null)) &&
                    ((bottom != null && bottom.equals(other.bottom)) || (bottom == null && other.bottom == null)) &&
                    ((right != null && right.equals(other.right)) || (right == null && other.right == null)) &&
                    ((top != null && top.equals(other.top)) || (top == null && other.top == null));


        } catch (ClassCastException cce) {
        }
        return false;
    }
}
