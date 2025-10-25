//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * @version $Revision$
 */
public abstract class CssValue {

    public static int type = CssTypes.CSS_UNKNOWN;

    String cssversion;

    public int getType() {
        return type;
    }

    public int getRawType() {
        return getType();
    }

    /**
     * Set the value of this value.
     *
     * @param s  the string representation of the value.
     * @param ac For errors and warnings reports.
     * @throws InvalidParamException The unit is incorrect
     */
    public abstract void set(String s, ApplContext ac)
            throws InvalidParamException;

    /**
     * @return the internal value
     */
    public abstract Object get();

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return super.equals(value);
    }

    public void setCssVersion(String cssversion) {
        this.cssversion = cssversion;
    }

    public boolean isDefault() {
        return false;
    }

    public CssLength getLength() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssPercentage getPercentage() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssNumber getNumber() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssTime getTime() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssAngle getAngle() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssFrequency getFrequency() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssCheckableValue getCheckableValue() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssIdent getIdent() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssHashIdent getHashIdent() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssFunction getFunction() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    public CssString getString() throws InvalidParamException {
        throw new ClassCastException("unknown");
    }

    /**
     * Does this value contain a "\9" CSS declaration hack?
     */
    public boolean hasBackslash9Hack() {
        return false;
    }
}
