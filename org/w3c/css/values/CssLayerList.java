// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2010.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;

/**
 * A comma separated value list.
 */
public class CssLayerList extends CssValue {

    public static final int type = CssTypes.CSS_LAYER_LIST;

    public ArrayList<CssValue> value;

    public final int getType() {
        return type;
    }

    public int size() {
        return value.size();
    }

    /**
     * Create a new CssLayerList
     */
    public CssLayerList() {
        value = new ArrayList<CssValue>();
    }

    /**
     * Create a new CssLayerList
     *
     * @param val the <EM>ArrayList</EM> of CssValue
     */
    public CssLayerList(ArrayList<CssValue> val) {
        value = val;
    }

    /**
     * Set the value of this string.
     *
     * @param s  the ArrayList of CSS values
     * @param ac For errors and warnings reports.
     * @throws org.w3c.css.util.InvalidParamException
     *          The unit is incorrect
     */
    public void set(String s, ApplContext ac)
            throws InvalidParamException {
        throw new InvalidParamException("invalid-class", s, ac);
    }

    public void add(CssValue val) {
        value.add(val);
    }

    /**
     * Returns the value
     */
    public Object get() {
        return value;
    }

    /**
     * return a stored value
     */
    public CssValue get(int idx) {
        return value.get(idx);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CssValue aCssValue : value) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append(aCssValue.toString());
        }
        return sb.toString();
    }

    /**
     * Get the hash code of the internal string.
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return (value instanceof CssLayerList &&
                this.value.equals(((CssLayerList) value).value));
    }

}