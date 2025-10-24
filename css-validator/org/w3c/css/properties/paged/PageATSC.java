//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */
package org.w3c.css.properties.paged;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 */
public class PageATSC extends CssProperty
        implements CssOperator {

    CssValue value;
    CssValue pseudo;

    private static CssIdent auto = new CssIdent("auto");


    /**
     * Create a new CssPageATSC
     */
    public PageATSC() {
        value = auto;
    }

    /**
     * Create a new CssPageATSC
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public PageATSC(ApplContext ac, CssExpression expression,
                    boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();
        char op = expression.getOperator();

        setByUser();

        ac.getFrame().addWarning("atsc", val.toString());

        if (val.equals(auto)) {
            if (expression.getCount() > 1) {
                throw new InvalidParamException("unrecognize", ac);
            }
            value = val;
            expression.next();
        } else if (val instanceof CssIdent) {
            value = val;
            expression.next();
            if (!expression.end()) {
                val = expression.getValue();
                if ((op == SPACE) && (val instanceof CssIdent)) {
                    pseudo = val;
                    expression.next();
                    return;
                } else {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(), getPropertyName(), ac);
        }
    }

    public PageATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return null;
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "page";
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (pseudo != null) {
            return value + " " + pseudo;
        } else {
            return value.toString();
        }
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css2Style style0 = (Css2Style) style;
        if (style0.pageATSC != null)
            style0.addRedefinitionWarning(ac, this);
        style0.pageATSC = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css2Style) style).getPageATSC();
        } else {
            return ((Css2Style) style).pageATSC;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof PageATSC
                && value == ((PageATSC) property).value
                && ((pseudo == null)
                || pseudo.equals(((PageATSC) property).pseudo)));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return value == auto;
    }

}
