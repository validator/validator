// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/syndata.html#values
 */
public class CssUnitsCSS2 {
    private static final String[] relative_length_units = {
            "em", "ex", "px"
    };

    private static final String[] absolute_length_units = {
            "in", "cm", "mm", "pt", "pc"
    };

    private static final String[] angle_units = {
            "deg", "grad", "rad"
    };
    private static final BigDecimal[] angle_mult;

    static {
        angle_mult = new BigDecimal[angle_units.length];
        angle_mult[0] = BigDecimal.ONE;
        angle_mult[1] = BigDecimal.valueOf(9.0 / 10.0);
        angle_mult[2] = BigDecimal.valueOf(180.0 / Math.PI);
    }

    private static final String[] time_units = {
            "ms", "s"
    };
    private static BigDecimal[] time_mult;

    static {
        time_mult = new BigDecimal[time_units.length];
        time_mult[0] = BigDecimal.valueOf(0.001);
        time_mult[1] = BigDecimal.ONE;
    }

    private static final String[] frequency_units = {
            "khz", "hz"
    };
    private static BigDecimal[] frequency_mult;

    static {
        frequency_mult = new BigDecimal[frequency_units.length];
        frequency_mult[0] = BigDecimal.valueOf(1000);
        frequency_mult[1] = BigDecimal.ONE;
    }

    protected static String getRelativeLengthUnit(String unit) {
        for (String s : relative_length_units) {
            if (s.equals(unit)) {
                return s;
            }
        }
        return null;
    }

    protected static String getAbsoluteLengthUnit(String unit) {
        for (String s : absolute_length_units) {
            if (s.equals(unit)) {
                return s;
            }
        }
        return null;
    }

    protected static void parseLengthUnit(String unit, CssLength length, ApplContext ac)
            throws InvalidParamException {
        String matchedUnit = getRelativeLengthUnit(unit);
        if (matchedUnit != null) {
            length.absolute = false;
        } else {
            matchedUnit = getAbsoluteLengthUnit(unit);
            if (matchedUnit == null) {
                throw new InvalidParamException("unit", unit, ac);
            }
            length.absolute = true;
        }
        length.unit = matchedUnit;
    }

    protected static void parseAngleUnit(String unit, CssAngle angle, ApplContext ac)
            throws InvalidParamException {
        String matchedUnit = null;
        for (int i = 0; i < angle_units.length; i++) {
            if (angle_units[i].equals(unit)) {
                matchedUnit = angle_units[i];
                angle.factor = angle_mult[i];
                break;
            }
        }
        if (matchedUnit == null) {
            throw new InvalidParamException("unit", unit, ac);
        }
        angle.unit = matchedUnit;
    }

    protected static void parseFrequencyUnit(String unit, CssFrequency frequency, ApplContext ac)
            throws InvalidParamException {
        String matchedUnit = null;
        for (int i = 0; i < frequency_units.length; i++) {
            if (frequency_units[i].equals(unit)) {
                matchedUnit = frequency_units[i];
                frequency.factor = frequency_mult[i];
                break;
            }
        }
        if (matchedUnit == null) {
            throw new InvalidParamException("unit", unit, ac);
        }
        frequency.unit = matchedUnit;
    }


    protected static void parseTimeUnit(String unit, CssTime time, ApplContext ac)
            throws InvalidParamException {
        String matchedUnit = null;
        for (int i = 0; i < time_units.length; i++) {
            if (time_units[i].equals(unit)) {
                matchedUnit = time_units[i];
                time.factor = time_mult[i];
                break;
            }
        }
        if (matchedUnit == null) {
            throw new InvalidParamException("unit", unit, ac);
        }
        time.unit = matchedUnit;
    }
}
