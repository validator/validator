//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * <H3>Time</H3>
 * <p/>
 * <P>Time units are used with aural cascading style sheets.
 * <p/>
 * <P>These following are legal time units:
 * <p/>
 * <UL>
 * <LI>ms: milliseconds
 * <LI>s: seconds
 * </UL>
 * <p/>
 * <p>Time values may not be negative.
 *
 * @version $Revision$
 */
public class CssTime extends CssCheckableValue {

    public static final int type = CssTypes.CSS_TIME;

    public final int getType() {
        return type;
    }

    private BigDecimal value;
    protected String unit;
    protected BigDecimal factor = BigDecimal.ONE;

    /**
     * Create a new CssTime.
     */
    public CssTime() {
        value = BigDecimal.ZERO;
    }

    /**
     * Create a new CssTime with a Float object.
     *
     * @param value the Float object
     */
    public CssTime(Float value) {
        this.value = new BigDecimal(value);
    }

    /**
     * Set the value of this time.
     *
     * @param s  the string representation of the time.
     * @param ac For errors and warnings reports.
     * @throws InvalidParamException The unit is incorrect
     */
    public void set(String s, ApplContext ac) throws InvalidParamException {
        String low_s = s.toLowerCase();
        int length = low_s.length();
        int unitIdx = length - 1;
        char c = low_s.charAt(unitIdx);
        while (unitIdx > 0 && c <= 'z' && c >= 'a') {
            c = low_s.charAt(--unitIdx);
        }
        if (unitIdx == length - 1) {
            throw new InvalidParamException("unit", s, ac);
        }
        // we go back to the beginning of the unit
        unitIdx++;
        String unit_str = low_s.substring(unitIdx, length);
        // let's test the unit
        switch (ac.getCssVersion()) {
            case CSS2:
                CssUnitsCSS2.parseTimeUnit(unit_str, this, ac);
                break;
            case CSS21:
                CssUnitsCSS21.parseTimeUnit(unit_str, this, ac);
                break;
            case CSS3:
                CssUnitsCSS3.parseTimeUnit(unit_str, this, ac);
                break;
            default:
                throw new InvalidParamException("unit", s, ac);
        }
        try {
            String num_s = low_s.substring(0, unitIdx);
            if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
                // check for scientific notation in CSS < 3
                if (num_s.indexOf('e') >= 0 || num_s.indexOf('E') >= 0) {
                    throw new InvalidParamException("invalid-number", num_s, ac);
                }
            }
            value = new BigDecimal(num_s);
        } catch (NumberFormatException nex) {
            throw new InvalidParamException("invalid-number",
                    low_s.substring(0, unitIdx), ac);
        }

    }

    /**
     * Returns the current value
     * Float
     * TODO move to a BigDecimal
     */
    public Object get() {
        return value.multiply(factor).floatValue();

    }

    /**
     * Returns the current value
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (BigDecimal.ZERO.compareTo(value) == 0) {
            return value.toPlainString() + 's';
        }
        return value.toPlainString() + unit;
    }

    /**
     * Compares two values for equality.
     *
     * @param other The other value.
     */
    public boolean equals(Object other) {
        if (((CssValue) other).getType() == getType()) {
            return get().equals(((CssValue) other).get());
        }
        return false;
    }

    /**
     * set the native value
     *
     * @param v the BigDecimal
     */
    public void setValue(BigDecimal v) {
        value = v;
    }

    /**
     * Returns true is the value is positive of null
     *
     * @return a boolean
     */
    public boolean isPositive() {
        return (value.signum() >= 0);
    }

    /**
     * Returns true is the value is positive of null
     *
     * @return a boolean
     */
    public boolean isStrictlyPositive() {
        return (value.signum() == 1);
    }

    /**
     * Returns true is the value is zero
     *
     * @return a boolean
     */
    public boolean isZero() {
        return (BigDecimal.ZERO.compareTo(value) == 0);
    }

    /**
     * check if the value is equal to zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, String callername)
            throws InvalidParamException {
        checkEqualsZero(ac, new String[]{"time", toString(), callername});
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        return warnEqualsZero(ac, new String[]{"time", callername});
    }

    public CssTime getTime() throws InvalidParamException {
        return this;
    }
}

