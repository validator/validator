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
 * <H3> Frequencies</H3>
 * <p/>
 * <P>Frequency units are used with aural cascading style sheets.
 * <p/>
 * <p>There are two legal frequency units:
 * <p/>
 * <ul>
 * <li>Hz: Hertz
 * <li>kHz: kilo Hertz
 * </ul>
 * <p/>
 * <P> For example, 200Hz is a bass sound, and 6kHz is a treble sound.
 *
 * @version $Revision$
 */
public class CssFrequency extends CssCheckableValue {

    public static final int type = CssTypes.CSS_FREQUENCY;

    public final int getType() {
        return type;
    }

    private BigDecimal value;
    protected String unit;
    protected BigDecimal factor = BigDecimal.ONE;
    private static BigDecimal defaultValue = BigDecimal.ZERO;

    /**
     * Create a new CssFrequency
     */
    public CssFrequency() {
        value = defaultValue;
    }

    /**
     * Create a new CssFrequency with a float number.
     *
     * @param value the float number.
     */
    public CssFrequency(BigDecimal value) {
        this.value = value;
    }

    /**
     * Set the value of this frequency.
     *
     * @param s  the string representation of the frequency.
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
                CssUnitsCSS2.parseFrequencyUnit(unit_str, this, ac);
                break;
            case CSS21:
                CssUnitsCSS21.parseFrequencyUnit(unit_str, this, ac);
                break;
            case CSS3:
                CssUnitsCSS3.parseFrequencyUnit(unit_str, this, ac);
                break;
            case CSS1:
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
     */
    public Object get() {
        // TODO FIXME should not be a Float...
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
            return BigDecimal.ZERO.toPlainString();
        }
        return value.toPlainString() + unit;
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return (value instanceof CssFrequency
                && this.value.equals(((CssFrequency) value).value)
                && unit.equals(((CssFrequency) value).unit));
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


    public CssFrequency getFrequency() {
        return this;
    }


    // redefinitions for some error messages.

    /**
     * check if the value is equal to zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, String callername)
            throws InvalidParamException {
        checkEqualsZero(ac, new String[]{"frequency", toString(), callername});
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        return warnEqualsZero(ac, new String[]{"frequency", callername});
    }

}

