// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS3
 */
public class CssBorderRadius extends CssProperty {

    // those are @since CSS3
    public CssBorderTopLeftRadius topLeft;
    public CssBorderTopRightRadius topRight;
    public CssBorderBottomLeftRadius bottomLeft;
    public CssBorderBottomRightRadius bottomRight;

    public boolean shorthand;

    /**
     * Create a new CssBorderRadius
     */
    public CssBorderRadius() {
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderRadius(ApplContext ac, CssExpression expression)
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
    public CssBorderRadius(ApplContext ac, CssExpression expression,
                           boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);

    }


    /**
     * Returns the value of this property
     */
    public Object get() {
        return null;
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "border-radius";
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
        ((Css1Style) style).cssBorder.borderRadius.shorthand = shorthand;
        ((Css1Style) style).cssBorder.borderRadius.byUser = byUser;
        if (topLeft != null) {
            topLeft.addToStyle(ac, style);
        }
        if (topRight != null) {
            topRight.addToStyle(ac, style);
        }
        if (bottomLeft != null) {
            bottomLeft.addToStyle(ac, style);
        }
        if (bottomRight != null) {
            bottomRight.addToStyle(ac, style);
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
            return ((Css1Style) style).getBorder().borderRadius;
        } else {
            return ((Css1Style) style).cssBorder.borderRadius;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return false; // FIXME
    }
}
