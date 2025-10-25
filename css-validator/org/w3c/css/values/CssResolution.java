//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Updated September 25th 2000 Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * @spec http://www.w3.org/TR/2010/CR-css3-mediaqueries-20100727/#values
 * @since CSS3
 */
public class CssResolution extends CssValue {

    public static final int type = CssTypes.CSS_RESOLUTION;

    public final int getType() {
        return type;
    }

    private BigDecimal value;
    protected String unit;
    private boolean isInt = false;

    /**
     * Create a new CssResolution
     */
    public CssResolution() {
    }

    private void setValue(String s) {
        value = new BigDecimal(s);
        try {
            value.toBigIntegerExact();
            isInt = true;
        } catch (ArithmeticException e) {
            isInt = false;
        }
    }

    /**
     * Set the value of this Resolution.
     *
     * @param s  the string representation of the Resolution.
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
            case CSS3:
                CssUnitsCSS3.parseResolutionUnit(unit_str, this, ac);
                break;
            default:
                throw new InvalidParamException("unit", s, ac);
        }
        try {
            String num_s = low_s.substring(0, unitIdx);
            if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
                // check for scientific notation in CSS < 3
                if (num_s.indexOf('e') >= 0 || num_s.indexOf('E') >= 0) {
                    throw new InvalidParamException("invalid-number", s, ac);
                }
            }
            setValue(num_s);
        } catch (NumberFormatException nex) {
            throw new InvalidParamException("invalid-number",
                    low_s.substring(0, unitIdx), ac);
        }
    }

    /**
     * Returns the current value
     */
    public Object get() {
        if (isInt) {
            return Integer.valueOf(value.intValue());
        }
        return value;
    }

    /**
     * @return a float
     */
    public float getFloatValue() {
        return value.floatValue();
    }

    /**
     * @return the current value
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toPlainString() + ((BigDecimal.ZERO.compareTo(value) == 0) ? "dpi" : unit);
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return (value instanceof CssResolution &&
                this.value.equals(((CssResolution) value).value) &&
                unit.equals(((CssResolution) value).unit));
    }
}

