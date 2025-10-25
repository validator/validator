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
 * @spec https://www.w3.org/TR/2021/WD-css-values-4-20211016/
 * @spec http://www.w3.org/TR/2012/CR-css3-speech-20120320/
 * @spec https://www.w3.org/TR/2016/CR-css-grid-1-20160929/#typedef-flex
 * @spec https://github.com/w3c/csswg-drafts/blob/642e0f0b369c95619e6398788a943301656a89b8/css-values-4/Overview.bs
 * (for dimension 'x')
 */
public class CssUnitsCSS3 {
    private static final String[] relative_length_units = {
            "em", "rem", "ex", "rex", "cap", "rcap", "ch", "rch", "ic", "ric", "lh", "rlh",
            "vw", "svw", "lvw", "dvw", "vh", "svh", "lvh", "dvh",
            "vi", "svi", "lvi", "dvi", "vb", "svb", "lvb", "dvb",
            "vmin", "svmin", "lvmin", "dvnim", "vmax", "svmax", "lvmax", "dvmax"
    };

    private static final String[] absolute_length_units = {
            "in", "cm", "mm", "pt", "pc", "px", "q"
    };

    public static final String[] angle_units = {
            "deg", "grad", "rad", "turn"
    };


    public static final String flex_unit = "fr";

    public static final String volume_unit = "db";

    public static final String semitone_unit = "st";

    private static final BigDecimal[] angle_mult;

    static {
        angle_mult = new BigDecimal[angle_units.length];
        angle_mult[0] = BigDecimal.ONE;
        angle_mult[1] = BigDecimal.valueOf(9.0 / 10.0);
        angle_mult[2] = BigDecimal.valueOf(180.0 / Math.PI);
        angle_mult[3] = BigDecimal.valueOf(360);
    }

    public static final String[] time_units = {
            "ms", "s"
    };
    private static BigDecimal[] time_mult;

    static {
        time_mult = new BigDecimal[time_units.length];
        time_mult[0] = BigDecimal.valueOf(0.001);
        time_mult[1] = BigDecimal.ONE;
    }

    public static final String[] frequency_units = {
            "khz", "hz"
    };
    private static BigDecimal[] frequency_mult;

    static {
        frequency_mult = new BigDecimal[frequency_units.length];
        frequency_mult[0] = BigDecimal.valueOf(1000);
        frequency_mult[1] = BigDecimal.ONE;
    }

    public static final String[] resolution_units = {
            "dpi", "dpcm", "dppx", "x"
    };

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

    protected static void parseFlexibleLengthUnit(String unit, CssFlexibleLength length, ApplContext ac)
            throws InvalidParamException {
        if (flex_unit.equals(unit)) {
            length.unit = flex_unit;
            return;
        }
        throw new InvalidParamException("unit", unit, ac);
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

    protected static void parseResolutionUnit(String unit, CssResolution resolution, ApplContext ac)
            throws InvalidParamException {
        String matchedUnit = null;
        for (String s : resolution_units) {
            if (s.equals(unit)) {
                resolution.unit = s;
                return;
            }
        }
        throw new InvalidParamException("unit", unit, ac);
    }

    protected static void parseVolumeUnit(String unit, CssVolume vol, ApplContext ac)
            throws InvalidParamException {
        if (volume_unit.equals(unit)) {
            vol.unit = volume_unit;
            return;
        }
        throw new InvalidParamException("unit", unit, ac);
    }

    protected static void parseSemitoneUnit(String unit, CssSemitone semitone, ApplContext ac)
            throws InvalidParamException {
        if (semitone_unit.equals(unit)) {
            semitone.unit = semitone_unit;
            return;
        }
        throw new InvalidParamException("unit", unit, ac);
    }
}
