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
public class CssPadding extends CssProperty {



    public CssPaddingRight paddingRight;
    public CssPaddingTop paddingTop;
    public CssPaddingBottom paddingBottom;
    public CssPaddingLeft paddingLeft;

    public boolean shorthand = false;

    /**
     * Create a new CssPadding
     */
    public CssPadding() {
    }


    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssPadding(ApplContext ac, CssExpression expression)
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
    public CssPadding(ApplContext ac, CssExpression expression,
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
        return "padding";
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
        CssPadding cssPadding = ((Css1Style) style).cssPadding;
        cssPadding.byUser = byUser;
        cssPadding.shorthand = shorthand;
        paddingBottom.addToStyle(ac, style);
        paddingLeft.addToStyle(ac, style);
        paddingTop.addToStyle(ac, style);
        paddingRight.addToStyle(ac, style);
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
        paddingLeft.setInfo(line, source);
        paddingRight.setInfo(line, source);
        paddingTop.setInfo(line, source);
        paddingBottom.setInfo(line, source);
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getPadding();
        } else {
            return ((Css1Style) style).cssPadding;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        try {
            CssPadding other = (CssPadding) property;
            return (value != null && value.equals(other.value)) || (value == null && other.value == null);
        } catch (ClassCastException cce) {
            return false; // FIXME
        }
    }
}
