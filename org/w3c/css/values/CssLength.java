// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
// Updated September 25th 2000 Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Updated 2012 by Yves Lafon <yves@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * <H3>
 * &nbsp;&nbsp; Length units
 * </H3>
 * <p/>
 * The format of a length value is an optional sign character ('+' or '-', with
 * '+' being the default) immediately followed by a number (with or without
 * a decimal point) immediately followed by a unit identifier (a two-letter
 * abbreviation). After a '0' number, the unit identifier is optional.
 * <p/>
 * Some properties allow negative length units, but this may complicate the
 * formatting model and there may be implementation-specific limits. If a negative
 * length value cannot be supported, it should be clipped to the nearest value
 * that can be supported.
 * <p/>
 * There are two types of length units: relative and absolute. Relative units
 * specify a length relative to another length property. Style sheets that use
 * relative units will more easily scale from one medium to another (e.g. from
 * a computer display to a laser printer). Percentage
 * units (described below) and keyword values (e.g. 'x-large') offer similar
 * advantages.
 * <p/>
 * These relative units are supported:
 * <PRE>
 * H1 { margin: 0.5em }      /* ems, the height of the element's font * /
 * H1 { margin: 1ex }        /* x-height, ~ the height of the letter 'x' * /
 * P  { font-size: 12px }    /* pixels, relative to canvas * /
 * P  { layout-grid: strict both 20 pt 15 pt; margin 1gd 3gd 1gd 2gd } /* grid units * /
 * </PRE>
 * <p/>
 * The relative units 'em' and 'ex' are relative to the font size of the element
 * itself. The only exception to this rule in CSS1 is the 'font-size' property
 * where 'em' and 'ex' values refer to the font size of the parent element.
 * <p/>
 * The existence of a grid in an element makes it possible and very useful to express various
 * measurements in that element in terms of grid units. Grid units are used very frequently
 * in East Asian typography, especially for the left, right, top and bottom element margins.
 * Therefore a new length unit is necessary: gd to enable the author to specify the various
 * measurements in terms of the grid.
 * <p/>
 * Pixel units, as used in the last rule, are relative to the resolution of
 * the canvas, i.e. most often a computer display. If the pixel density of the
 * output device is very different from that of a typical computer display,
 * the UA should rescale pixel values. The suggested <EM>reference pixel</EM>
 * is the visual angle of one pixel on a device with a pixel density of 90dpi
 * and a distance from the reader of an arm's length. For a nominal arm's length
 * of 28 inches, the visual angle is about 0.0227 degrees.
 * <p/>
 * Child elements inherit the computed value, not the relative value:
 * <PRE>
 * BODY {
 * font-size: 12pt;
 * text-indent: 3em;  /* i.e. 36pt * /
 * }
 * H1 { font-size: 15pt }
 * </PRE>
 * <p/>
 * In the example above, the 'text-indent' value of 'H1' elements will be 36pt,
 * not 45pt.
 * <p/>
 * Absolute length units are only useful when the physical properties of the
 * output medium are known. These absolute units are supported:
 * <PRE>
 * H1 { margin: 0.5in }      /* inches, 1in = 2.54cm * /
 * H2 { line-height: 3cm }   /* centimeters * /
 * H3 { word-spacing: 4mm }  /* millimeters * /
 * H4 { font-size: 12pt }    /* points, 1pt = 1/72 in * /
 * H4 { font-size: 1pc }     /* picas, 1pc = 12pt * /
 * </PRE>
 * <p/>
 * In cases where the specified length cannot be supported, UAs should try to
 * approximate. For all CSS1 properties, further computations and inheritance
 * should be based on the approximated value.
 *
 * @version $Revision$
 * @see CssPercentage
 */
public class CssLength extends CssCheckableValue {

    public static final int type = CssTypes.CSS_LENGTH;

    public final int getType() {
        return type;
    }


    private BigDecimal value;
    protected String unit;
    protected boolean absolute = false;

    /**
     * Create a new CssLength
     */
    public CssLength() {
        value = BigDecimal.ZERO;
    }

    /**
     * Set the value of this length.
     *
     * @param s the string representation of the length.
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

        // TODO check the  if (!BigDecimal.ZERO.equals(value))) test
        // that was here earlier
        // seems legit to always test the unit no matter the value
        switch (ac.getCssVersion()) {
            case CSS1:
                CssUnitsCSS1.parseLengthUnit(unit_str, this, ac);
                break;
            case CSS2:
                CssUnitsCSS2.parseLengthUnit(unit_str, this, ac);
                break;
            case CSS21:
                CssUnitsCSS21.parseLengthUnit(unit_str, this, ac);
                break;
            case CSS3:
                CssUnitsCSS3.parseLengthUnit(unit_str, this, ac);
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
     * set the native value
     *
     * @param v the BigDecimal
     */
    public void setValue(BigDecimal v) {
        value = v;
    }

    // return self
    public CssLength getLength() throws InvalidParamException {
        return this;
    }

    /**
     * Returns the current value
     */
    public Object get() {
        // TODO this is old ugly crap, needed for not breaking everything
        // remove as soon as reference to get is removed...
        return value.floatValue();
    }

    /**
     * return the float value
     */
    public float floatValue() {
        return value.floatValue();
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
     * Returns the current value
     */
    public String getUnit() {
        return unit;
    }

    /**
     * tells if it is relative or not
     */
    public boolean isRelative() {
        return !absolute;
    }

    /**
     * tells if it is absolute or not
     */
    public boolean isAbsolute() {
        return absolute;
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
        return (value instanceof CssLength &&
                this.value.equals(((CssLength) value).value) &&
                unit.equals(((CssLength) value).unit));
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
        checkEqualsZero(ac, new String[]{"length", toString(), callername});
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        return warnEqualsZero(ac, new String[]{"length", callername});
    }

}

