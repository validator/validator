//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten Yves lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM and Keio)
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

public class CssColumnRuleWidth extends CssProperty {

    /**
     * Create a new CssColumnRuleWidth
     */
    public CssColumnRuleWidth() {
        // nothing to do
    }

    /**
     * Create a new CssColumnRuleWidth
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect value
     */
    public CssColumnRuleWidth(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
    }

    public CssColumnRuleWidth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css3Style) style).cssColumnRuleWidth != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssColumnRuleWidth = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getColumnRuleWidth();
        } else {
            return ((Css3Style) style).cssColumnRuleWidth;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssColumnRuleWidth &&
                value.equals(((CssColumnRuleWidth) property).value));
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "column-rule-width";
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns true if this property is "softly" inherited
     */
    public boolean isSoftlyInherited() {
        return inherit == value;
    }

    /**
     * Returns a string representation of the object
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Is the value of this property a default value
     * It is used by alle macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }

}
