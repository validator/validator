// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * The value of a variable definition
 */
public class CssVariableDefinition extends CssValue {

    public static final int type = CssTypes.CSS_VARIABLE_DEFINITION;

    public CssExpression expression = null;
    public String unparsable_value = null;

    public final int getType() {
        return type;
    }

    public int size() {
        if (expression != null) {
            return expression.getCount();
        }
        return 0;
    }

    /**
     * Create a new CssLayerList
     */
    public CssVariableDefinition() {
    }

    /**
     * Create a new CssVariableDefinition
     *
     * @param exp the <EM>CssExpression</EM>
     */
    public CssVariableDefinition(CssExpression exp) {
        if (exp.getCount() > 0) {
            expression = exp;
        } else {
            unparsable_value = "";
        }
    }

    /**
     * Create a new CssVariableDefinition
     *
     * @param s the <EM>String</EM> representing the unparsable definition
     */
    public CssVariableDefinition(String s) {
        unparsable_value = s;
    }

    public void set(CssExpression exp, ApplContext ac)
            throws InvalidParamException {
        expression = exp;
        // do something fancy
    }

    /**
     * Set the value of this string.
     *
     * @param s  the ArrayList of CSS values
     * @param ac For errors and warnings reports.
     * @throws InvalidParamException The unit is incorrect
     */
    public void set(String s, ApplContext ac)
            throws InvalidParamException {
        unparsable_value = s;
    }


    /**
     * Returns the value
     */
    public Object get() {
        return (expression == null) ? unparsable_value : expression;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (expression != null) {
            return expression.toString();
        }
        return unparsable_value;
    }

    /**
     * Get the hash code of the internal string.
     */
    public int hashCode() {
        return (get()).hashCode();
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        if (value instanceof CssVariableDefinition) {
            if (expression != null) {
                return expression.equals(((CssVariableDefinition) value).expression);
            }
            if (unparsable_value != null) {
                return unparsable_value.equals(((CssVariableDefinition) value).unparsable_value);
            } else {
                return (null == ((CssVariableDefinition) value).unparsable_value);
            }
        }
        return false;
    }

}