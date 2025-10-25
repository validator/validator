// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 1995-2010  World Wide Web Consortium (MIT, ERCIM and Keio)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css3.Css3Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS3
 */

public class CssColumnRule extends CssProperty {

    private static final String propertyName = "column-rule";

    public CssColumnRuleWidth rule_width;
    public CssColumnRuleStyle rule_style;
    public CssColumnRuleColor rule_color;

    /**
     * Create a new CssColumnRule
     */
    public CssColumnRule() {
    }

    /**
     * Create a new CssColumnRule
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect values
     */
    public CssColumnRule(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
    }

    public CssColumnRule(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css3Style) style).cssColumnRule != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssColumnRule = this;
        if (rule_style != null) {
            rule_style.addToStyle(ac, style);
        }
        if (rule_color != null) {
            rule_color.addToStyle(ac, style);
        }
        if (rule_width != null) {
            rule_width.addToStyle(ac, style);
        }
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getColumnRule();
        } else {
            return ((Css3Style) style).cssColumnRule;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return false;
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns a string representation of the object
     */
    public String toString() {
        return value.toString();
    }
}
