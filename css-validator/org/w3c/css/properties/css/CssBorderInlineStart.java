//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css3.Css3Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS3
 */
public class CssBorderInlineStart extends CssProperty {

    /**
     * Create a new CssBorderInlineStart
     */
    public CssBorderInlineStart() {
    }

    /**
     * Creates a new CssBorderInlineStart
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssBorderInlineStart(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                expression.getValue().toString(),
                getPropertyName(), ac);
    }

    public CssBorderInlineStart(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
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
        return "border-inline-start";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return inherit.equals(value);
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
        Css3Style s = (Css3Style) style;
        if (s.cssBorderInlineStart != null) {
            style.addRedefinitionWarning(ac, this);
        } else {
            if (s.cssBorderInlineStartColor != null) {
                style.addRedefinitionWarning(ac, s.cssBorderInlineStartColor);
            }
            if (s.cssBorderInlineStartStyle != null) {
                style.addRedefinitionWarning(ac, s.cssBorderInlineStartStyle);
            }
            if (s.cssBorderInlineStartWidth != null) {
                style.addRedefinitionWarning(ac, s.cssBorderInlineStartWidth);
            }
        }
        s.cssBorderInlineStart = this;
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBorderInlineStart &&
                value.equals(((CssBorderInlineStart) property).value));
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getBorderInlineStart();
        } else {
            return ((Css3Style) style).cssBorderInlineStart;
        }
    }
}

