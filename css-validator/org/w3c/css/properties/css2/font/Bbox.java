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
public class Bbox extends CssProperty {

    CssValue[] value = new CssValue[4];

    /**
     * Create a new Bbox
     */
    public Bbox() {
        // nothing to do
    }

    /**
     * Creates a new Bbox
     *
     * @param expression the unicode em
     * @throws InvalidParamException values are incorrect
     */
    public Bbox(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op;
        int i = 0;
        setByUser();

        val = expression.getValue();
        op = expression.getOperator();
        if (val instanceof CssNumber) {
            value[i++] = val;
            expression.next();
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        if (op != CssOperator.COMMA || expression.end()) {
            throw new InvalidParamException("few-value",
                    getPropertyName(), ac);
        }

        val = expression.getValue();
        op = expression.getOperator();
        if (val instanceof CssNumber) {
            value[i++] = val;
            expression.next();
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        if (op != CssOperator.COMMA || expression.end()) {
            throw new InvalidParamException("few-value",
                    getPropertyName(), ac);
        }

        val = expression.getValue();
        op = expression.getOperator();
        if (val instanceof CssNumber) {
            value[i++] = val;
            expression.next();
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        if (op != CssOperator.COMMA || expression.end()) {
            throw new InvalidParamException("few-value",
                    getPropertyName(), ac);
        }

        val = expression.getValue();
        op = expression.getOperator();
        if (val instanceof CssNumber) {
            value[i++] = val;
            expression.next();
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
    }

    public Bbox(ApplContext ac, CssExpression expression)
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
        for (int i = 0; i < 4; i++) {
            ret += ", " + value[i];
        }
        return ret.substring(2);
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "bbox";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.bbox != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.bbox = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getBbox();
        } else {
            return ((Css2Style) style).bbox;
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
