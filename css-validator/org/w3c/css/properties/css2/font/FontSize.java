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
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssValue;

import java.util.Vector;

/**
 */
public class FontSize extends CssProperty implements FontConstant {

    Vector values = new Vector();

    private static CssIdent all = new CssIdent("all");

    /**
     * Create a new FontSize
     */
    public FontSize() {
        // nothing to do
    }

    /**
     * Creates a new FontSize
     *
     * @param expression the font size
     * @throws InvalidParamException values are incorrect
     */
    public FontSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        char op = expression.getOperator();
        CssValue val = expression.getValue();
        setByUser();

        if (val.equals(all)) {
            values.addElement(all);
            expression.next();
            return;
        }

        do {
            if (val instanceof CssLength) {
                // nothing
            } else if (val instanceof CssNumber) {
                values.addElement(((CssNumber) val).getLength());
            } else {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }
            values.addElement(val);
            op = expression.getOperator();
            expression.next();
        } while (op == CssOperator.COMMA);

    }

    public FontSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the current value
     */
    public Object get() {
        return values.elementAt(0);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        String ret = "";
        int i = 0;

        while (i < values.size()) {
            ret += ", " + values.elementAt(i);
            i++;
        }

        return ret.substring(2);
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "font-size";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.fontSize != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.fontSize = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getFaceFontSize();
        } else {
            return ((Css2Style) style).fontSize;
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
