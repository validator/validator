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
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssValue;

import java.util.Vector;

/**
 */
public class FontStretch extends CssProperty implements FontConstant {

    Vector values = new Vector();

    private static CssIdent all = new CssIdent("all");

    /**
     * Create a new FontStretch
     */
    public FontStretch() {
        // nothing to do
    }

    /**
     * Creates a new FontStretch
     *
     * @param expression the font stretch
     * @throws InvalidParamException values are incorrect
     */
    public FontStretch(ApplContext ac, CssExpression expression, boolean check)
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
            if (expression.getValue() instanceof CssIdent) {
                int hash = ((CssIdent) expression.getValue()).hashCode();
                int i = 0;
                for (; i < hash_values.length; i++) {
                    if (hash_values[i] == hash) {
                        values.addElement(FONTSTRETCH[i]);
                        break;
                    }
                }
                if (i == FONTSTRETCH.length) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
            } else {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }
            op = expression.getOperator();
            expression.next();
        } while (op == CssOperator.COMMA);

    }

    public FontStretch(ApplContext ac, CssExpression expression)
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
        return "font-stretch";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.fontStretch != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.fontStretch = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getFaceFontStretch();
        } else {
            return ((Css2Style) style).fontStretch;
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

    private static int[] hash_values;

    static {
        hash_values = new int[FONTSTRETCH.length];
        for (int i = 0; i < FONTSTRETCH.length; i++)
            hash_values[i] = FONTSTRETCH[i].hashCode();
    }
}
