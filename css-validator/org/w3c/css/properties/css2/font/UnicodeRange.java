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
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

/**
 */
public class UnicodeRange extends CssProperty {

    ArrayList<CssValue> values = new ArrayList<CssValue>();

    /**
     * Create a new UnicodeRange
     */
    public UnicodeRange() {
        // nothing to do
    }

    /**
     * Creates a new UnicodeRange
     *
     * @param expression the unicode range
     * @throws InvalidParamException values are incorrect
     */
    public UnicodeRange(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {
        char op;
        CssValue val = expression.getValue();
        setByUser();

        do {
            if (val.getType() != CssTypes.CSS_UNICODE_RANGE) {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }
            values.add(val);
            op = expression.getOperator();
            expression.next();
        } while (op == CssOperator.COMMA);

    }

    public UnicodeRange(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the current value
     */
    public Object get() {
        return values.get(0);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CssValue val : values) {
            sb.append(val.toString());
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "unicode-range";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.unicodeRange != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.unicodeRange = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getFaceUnicodeRange();
        } else {
            return ((Css2Style) style).unicodeRange;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
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
