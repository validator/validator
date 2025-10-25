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
 * @version $Revision$
 * @since CSS1
 */
public class CssFontFamily extends CssProperty {

    public boolean hasGenericFontFamily = false;

    /**
     * Create a new CssFontFamily
     */
    public CssFontFamily() {
    }

    /**
     * Creates a new CssFontFamily
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFontFamily(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                expression.getValue().toString(),
                getPropertyName(), ac);
    }

    public CssFontFamily(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    public boolean containsGenericFamily() {
        return hasGenericFontFamily;
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "font-family";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return value.equals(inherit);
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
        CssFont cssFont = ((Css1Style) style).cssFont;
        if (cssFont.fontFamily != null)
            style.addRedefinitionWarning(ac, this);
        cssFont.fontFamily = this;
    }


    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssFontFamily &&
                value.equals(((CssFontFamily) property).value));
    }


    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getFontFamily();
        } else {
            return ((Css1Style) style).cssFont.fontFamily;
        }
    }

    /**
     * Used to check that the value contains a generic font family
     *
     * @return a boolean, true if it contains one
     */
    public boolean hasGenericFamily() {
        return hasGenericFontFamily || (value == inherit);
    }
}

