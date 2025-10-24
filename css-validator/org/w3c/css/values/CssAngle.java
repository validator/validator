//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <H3>Angle</H3>
 * <p/>
 * <P>Angle units are used with aural cascading style sheets.
 * <p/>
 * <P>These are the legal angle units:
 * <p/>
 * <UL>
 * <LI>deg: degrees
 * <LI>grad: gradians
 * <LI>rad: radians
 * </UL>
 * <p/>
 * <p>Values in these units may be negative. They should be normalized to the
 * range 0-360deg by the UA. For example, -10deg and 350deg are equivalent.
 *
 * @version $Revision$
 */
public class CssAngle extends CssCheckableValue implements CssValueFloat {

    public static final int type = CssTypes.CSS_ANGLE;

    public final int getType() {
        return type;
    }

    protected static final BigDecimal deg360;

    static {
        deg360 = BigDecimal.valueOf(360);
    }

    private BigDecimal value;
    protected BigDecimal factor = BigDecimal.ONE;
    String unit;

    /**
     * Create a new CssAngle.
     */
    public CssAngle() {
        this(BigDecimal.ZERO);
    }

    /**
     * Create a new CssAngle
     */
    public CssAngle(float v) {
        this(new BigDecimal(v));
    }

    /**
     * Create a new CssAngle
     */
    public CssAngle(BigDecimal angle) {
        value = angle;
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
     * set the native value
     *
     * @param s, the unit
     */
    public void setUnit(String s) {
        unit = s;
    }

    /**
     * Set the value of this angle.
     *
     * @param s  The string representation of the angle
     * @param ac For errors and warnings reports
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
                CssUnitsCSS2.parseAngleUnit(unit_str, this, ac);
                break;
            case CSS21:
                CssUnitsCSS21.parseAngleUnit(unit_str, this, ac);
                break;
            case CSS3:
                CssUnitsCSS3.parseAngleUnit(unit_str, this, ac);
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
     */
    public Object get() {
        return value;
    }

    public float getValue() {
        return value.floatValue();
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
        return value.toPlainString() + ((BigDecimal.ZERO.compareTo(value) == 0) ? "deg" : unit);
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return (value instanceof CssAngle &&
                this.value.equals(((CssAngle) value).value) &&
                unit.equals(((CssAngle) value).unit));
    }

    private BigDecimal normalize(BigDecimal value) {
        BigDecimal degree = value.multiply(factor);
        if ((degree.compareTo(BigDecimal.ZERO) >= 0) && (degree.compareTo(deg360) <= 0)) {
            // no need to normalize
            return value;
        }
        degree = degree.remainder(deg360);
        if (degree.compareTo(BigDecimal.ZERO) < 0) {
            degree.add(deg360);
        }
        return degree.divide(factor, 9, RoundingMode.HALF_DOWN).stripTrailingZeros();
    }

    public void normalizeValue() {
        value = normalize(value);
    }

    public CssAngle getAngle() {
        return this;
    }

    //@@FIXME I should return the remainder for all ...

    public float getDegree() {
        return normalize(value).multiply(factor).floatValue();
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
        return BigDecimal.ZERO.equals(normalize(value));
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
        checkEqualsZero(ac, new String[]{"angle", toString(), callername});
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        return warnEqualsZero(ac, new String[]{"angle", callername});
    }

}

