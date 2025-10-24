//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */
package org.w3c.css.properties.atsc;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssUnicodeRange;
import org.w3c.css.values.CssValue;

import java.util.Vector;

/**
 */
public class WidthsATSC extends CssProperty implements CssOperator {

    Vector values = new Vector();

    /**
     * Create a new WidthsATSC
     */
    public WidthsATSC() {
        // nothing to do
    }

    /**
     * Creates a new WidthsATSC
     *
     * @param expression the unicode em
     * @throws InvalidParamException values are incorrect
     */
    public WidthsATSC(ApplContext ac, CssExpression expression,
                      boolean check) throws InvalidParamException {

        boolean manyValues = expression.getCount() > 1;

        CssValue val;
        char op;
        //int i = 0;
        setByUser();
        {
            val = expression.getValue();
            ac.getFrame().addWarning("atsc", val.toString());
        }

        do {
            val = expression.getValue();
            op = expression.getOperator();

            if (manyValues && val.equals(inherit)) {
                throw new InvalidParamException("unrecognize", ac);
            }

            if (val instanceof CssUnicodeRange) {
                values.addElement(val);
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op),
                            getPropertyName(), ac);
                }
                if (expression.end()) {
                    throw new InvalidParamException("few-value",
                            getPropertyName(), ac);
                }
                expression.next();
            }
            do {
                op = expression.getOperator();
                val = expression.getValue();
                if (val instanceof CssNumber) {
                    values.addElement(" ");
                    values.addElement(val);
                } else {
                    throw new InvalidParamException("value",
                            val,
                            getPropertyName(), ac);
                }
                expression.next();
            } while ((op == SPACE) && !expression.end());
            values.addElement(", ");
        } while (op == CssOperator.COMMA);

    }

    public WidthsATSC(ApplContext ac, CssExpression expression)
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
        while (i < (values.size() - 2)) {
            ret += values.elementAt(i);
            i++;
        }
        return ret;
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "widths";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        ATSCStyle style0 = (ATSCStyle) style;
        if (style0.widthsATSC != null) {
            style0.addRedefinitionWarning(ac, this);
        }
        style0.widthsATSC = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((ATSCStyle) style).getWidthsATSC();
        } else {
            return ((ATSCStyle) style).widthsATSC;
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
