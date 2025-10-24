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
import org.w3c.css.values.CssValue;

/**
 * @since CSS1
 */
public class CssBorder extends CssProperty {

    // @since CSS1
    public CssBorderColor borderColor;
    public CssBorderStyle borderStyle;
    public CssBorderWidth borderWidth;

    public CssBorderRight borderRight;
    public CssBorderTop borderTop;
    public CssBorderBottom borderBottom;
    public CssBorderLeft borderLeft;

    // @since CSS3
    public CssBorderRadius borderRadius;
    public CssBorderImage borderImage;

    public boolean shorthand = false;

    /**
     * Create a new CssBorder
     */
    public CssBorder() {
    }

    // a small init for the holder in Style
    public CssBorder(boolean holder) {
        if (holder) {
            // those are holding stuff...
            borderColor = new CssBorderColor();
            borderStyle = new CssBorderStyle();
            borderWidth = new CssBorderWidth();
            // we are not generating border-(sides)
            // css3 holders
            borderRadius = new CssBorderRadius();
            borderImage = new CssBorderImage();
        }
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorder(ApplContext ac, CssExpression expression)
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
    public CssBorder(ApplContext ac, CssExpression expression,
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
        if (borderColor == null) {
            return null;
        } else {
            return borderColor.getColor();
        }
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "border";
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
        cssBorder.byUser = byUser;
        if (cssBorder.borderColor.shorthand) {
            style.addRedefinitionWarning(ac, this);
        } else {
            if (borderColor != null) {
                borderColor.addToStyle(ac, style);
            }
            if (borderStyle != null) {
                borderStyle.addToStyle(ac, style);
            }
            if (borderWidth != null) {
                borderWidth.addToStyle(ac, style);
            }
        }
        cssBorder.shorthand = shorthand;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getBorder();
        } else {
            return ((Css1Style) style).cssBorder;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        try {
            CssBorder other = (CssBorder) property;
            return (value != null && value.equals(other.value)) || (value == null && other.value == null);
        } catch (ClassCastException cce) {
            return false; // FIXME
        }
    }
}
