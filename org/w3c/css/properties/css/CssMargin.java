// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
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
public class CssMargin extends CssProperty {



    public CssMarginRight marginRight;
    public CssMarginTop marginTop;
    public CssMarginBottom marginBottom;
    public CssMarginLeft marginLeft;

    public boolean shorthand = false;

    /**
     * Create a new CssMargin
     */
    public CssMargin() {
    }


    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssMargin(ApplContext ac, CssExpression expression)
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
    public CssMargin(ApplContext ac, CssExpression expression,
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
        return "margin";
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return inherit.equals(value);
    }

    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssMargin cssMargin = ((Css1Style) style).cssMargin;
        cssMargin.byUser = byUser;
        cssMargin.shorthand = shorthand;
        marginBottom.addToStyle(ac, style);
        marginLeft.addToStyle(ac, style);
        marginTop.addToStyle(ac, style);
        marginRight.addToStyle(ac, style);
    }

    /**
     * Update the source file and the line.
     * Overrides this method for a macro
     *
     * @param line   The line number where this property is defined
     * @param source The source file where this property is defined
     */
    public void setInfo(int line, String source) {
        super.setInfo(line, source);
        marginLeft.setInfo(line, source);
        marginRight.setInfo(line, source);
        marginTop.setInfo(line, source);
        marginBottom.setInfo(line, source);
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getMargin();
        } else {
            return ((Css1Style) style).cssMargin;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        try {
            CssMargin other = (CssMargin) property;
            return (value != null && value.equals(other.value)) || (value == null && other.value == null);
        } catch (ClassCastException cce) {
            return false; // FIXME
        }
    }
}
