//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */
package org.w3c.css.properties.css2.font;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssValue;

/**
 */
public class Panose1 extends CssProperty {

    CssValue[] value = new CssValue[10];

    /**
     * Create a new Panose1
     */
    public Panose1() {
        // nothing to do
    }

    /**
     * Creates a new Panose1
     *
     * @param expression the unicode em
     * @throws InvalidParamException values are incorrect
     */
    public Panose1(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op;
        int i = 0;
        setByUser();

        do {
            val = expression.getValue();
            op = expression.getOperator();
            if (val instanceof CssNumber) {
                value[i++] = val;
                expression.next();
            } else {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }
        } while (!expression.end()
                && (op == CssOperator.SPACE)
                && (i < 10));

        if (i != 10) {
            throw new InvalidParamException("few-value",
                    getPropertyName(), ac);
        }
    }

    public Panose1(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the current value
     */
    public Object get() {
        return value[0];
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        String ret = "";
        for (int i = 0; i < 10; i++) {
            ret += " " + value[i];
        }
        return ret.substring(1);
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "panose-1";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.panose1 != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.panose1 = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getPanose1();
        } else {
            return ((Css2Style) style).panose1;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        // @@TODO
        return false;
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }

}
