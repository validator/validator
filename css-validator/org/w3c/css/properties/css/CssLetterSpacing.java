// $Id$
//
// (c) COPYRIGHT MIT, INRIA and Keio University, 2011
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
public class CssLetterSpacing extends CssProperty {


    /**
     * Create a new CssLetterSpacing.
     */
    public CssLetterSpacing() {
    }

    /**
     * Create a new CssLetterSpacing with an expression
     *
     * @param expression The expression
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssLetterSpacing(ApplContext ac, CssExpression expression,
                            boolean check) throws InvalidParamException {

        throw new InvalidParamException("unrecognize", ac);
    }

    public CssLetterSpacing(ApplContext ac, CssExpression expression)
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
        return "letter-spacing";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return value == inherit;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Adds this property to a style.
     *
     * @param style The style.
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css1Style style0 = (Css1Style) style;
        if (style0.cssLetterSpacing != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.cssLetterSpacing = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getLetterSpacing();
        } else {
            return ((Css1Style) style).cssLetterSpacing;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssLetterSpacing) &&
                ((CssLetterSpacing) property).value.equals(value);
    }
}
