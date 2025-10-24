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
public class CssBorderBlock extends CssProperty {

    /**
     * Create a new CssBorderBlock
     */
    public CssBorderBlock() {
    }

    /**
     * Creates a new CssBorderBlock
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssBorderBlock(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                expression.getValue().toString(),
                getPropertyName(), ac);
    }

    public CssBorderBlock(ApplContext ac, CssExpression expression)
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
        return "border-block";
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
        if (s.cssBorderBlock != null) {
            style.addRedefinitionWarning(ac, this);
        } else {
            if (s.cssBorderBlockStart != null) {
                style.addRedefinitionWarning(ac, s.cssBorderBlockStart);
            } else {
                if (s.cssBorderBlockStartColor != null) {
                    style.addRedefinitionWarning(ac, s.cssBorderBlockStartColor);
                }
                if (s.cssBorderBlockStartStyle != null) {
                    style.addRedefinitionWarning(ac, s.cssBorderBlockStartStyle);
                }
                if (s.cssBorderBlockStartWidth != null) {
                    style.addRedefinitionWarning(ac, s.cssBorderBlockStartWidth);
                }
            }
            if (s.cssBorderBlockEnd != null) {
                style.addRedefinitionWarning(ac, s.cssBorderBlockEnd); 
            } else {
                if (s.cssBorderBlockEndColor != null) {
                    style.addRedefinitionWarning(ac, s.cssBorderBlockEndColor);
                }
                if (s.cssBorderBlockEndStyle != null) {
                    style.addRedefinitionWarning(ac, s.cssBorderBlockEndStyle);
                }
                if (s.cssBorderBlockEndWidth != null) {
                    style.addRedefinitionWarning(ac, s.cssBorderBlockEndWidth);
                }
            }
        }
        s.cssBorderBlock = this;
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBorderBlock &&
                value.equals(((CssBorderBlock) property).value));
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getBorderBlock();
        } else {
            return ((Css3Style) style).cssBorderBlock;
        }
    }
}

