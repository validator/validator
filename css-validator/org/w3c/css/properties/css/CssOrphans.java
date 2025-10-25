// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2013.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css2.Css2Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS2
 */
public class CssOrphans extends CssProperty {

    public static final String propertyName = "orphans";


    /**
     * Create a new CssOrphans
     */
    public CssOrphans() {
    }

    /**
     * Create a new CssOrphans
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      true will test the number of parameters
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssOrphans(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        throw new InvalidParamException("value", expression.getValue(),
                getPropertyName(), ac);

    }

    /**
     * Create a new CssOrphans
     *
     * @param ac,        the Context
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssOrphans(ApplContext ac, CssExpression expression)
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
        return propertyName;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return (value == inherit);
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
        Css2Style style0 = (Css2Style) style;
        if (style0.cssOrphans != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.cssOrphans = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getOrphans();
        } else {
            return ((Css2Style) style).cssOrphans;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     * @return boolean
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssOrphans &&
                value.equals(((CssOrphans) property).value));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }

}
