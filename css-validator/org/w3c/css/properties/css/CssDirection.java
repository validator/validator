// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2011.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css2.Css2Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;

/**
 * @since CSS2
 */
public class CssDirection extends CssProperty {

    public static final CssIdent ltr;
    public static final CssIdent rtl;

    static {
        ltr = CssIdent.getIdent("ltr");
        rtl = CssIdent.getIdent("rtl");
    }

    /**
     * Create a new CssDirection
     */
    public CssDirection() {
    }

    /**
     * Create a new CssDirection
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssDirection(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
    }

    public CssDirection(ApplContext ac, CssExpression expression)
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
        return "direction";
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
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.cssDirection != null)
            style0.addRedefinitionWarning(ac, this);
        style0.cssDirection = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getDirection();
        } else {
            return ((Css2Style) style).cssDirection;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssDirection &&
                value.equals(((CssDirection) property).value));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }

}
