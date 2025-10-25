// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * @spec https://www.w3.org/TR/2017/CR-mediaqueries-4-20170905/
 * @since CSS3
 */
public class CssComparator extends CssValue {

    public static final int type = CssTypes.CSS_COMPARATOR;

    public enum Comp {
        GE(">="),
        LE("<="),
        GT(">"),
        LT("<"),
        EQ("=");

        private String comp;

        Comp(String val) {
            comp = val;
        }

        public static Comp resolve(String s)
        //          throws InvalidParamException {
        {
            for (Comp v : Comp.values()) {
                if (v.toString().equals(s)) {
                    return v;
                }
            }
            return null; // consider thworing instead, note that it should never happen
        }

        public String toString() {
            return comp;
        }
    }

    public static boolean checkCompatibility(CssComparator first, CssComparator second) {
        if ((first.value == Comp.EQ) || (second.value == Comp.EQ)) {
            return false;
        }
        return true;
    }

    public static boolean checkUsefulness(CssComparator first, CssComparator second) {
        switch (first.value) {
            case GE:
            case GT:
                if (second.value == Comp.GE || second.value == Comp.GT)
                    return true;
                return false;
            case LE:
            case LT:
                if (second.value == Comp.LE || second.value == Comp.LT)
                    return true;
            default:
                return false;
        }
    }

    public final int getType() {
        return type;
    }

    private Comp value;
    CssExpression parameters;

    /**
     * Create a new CssComparator.
     */
    public CssComparator() {

    }

    /**
     * Set the value of this comparator.
     *
     * @param s  the string representation of the switch .
     * @param ac For errors and warnings reports.
     * @throws org.w3c.css.util.InvalidParamException
     *          (incorrect format)
     */
    public void set(String s, CssExpression ex, ApplContext ac) throws InvalidParamException {
        Comp c = Comp.resolve(s.trim());
        if (c == null) {
            throw new InvalidParamException("value",
                    s, ac);
        }
        value = c;
        parameters = ex;
    }

    /**
     * Set the value of this comparator.
     *
     * @param s  the string representation of the switch .
     * @param ac For errors and warnings reports.
     * @throws org.w3c.css.util.InvalidParamException
     *          (incorrect format)
     */
    public void set(String s, ApplContext ac) throws InvalidParamException {
        Comp c = Comp.resolve(s.trim());
        if (c == null) {
            throw new InvalidParamException("value",
                    s, ac);
        }
        value = c;
    }

    /**
     * Returns the parameters expression
     */
    public CssExpression getParameters() {
        return parameters;
    }

    public void setParameters(CssExpression nex) {
        parameters = nex;
    }

    /**
     * Returns the current value
     */
    public Object get() {
        return toString();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return String.valueOf(value);
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        try {
            CssComparator other = (CssComparator) value;
            // check that the ratio are the same
            return (value == other.value);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}

