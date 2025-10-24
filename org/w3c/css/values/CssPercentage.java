//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2010.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * <H3>
 * &nbsp;&nbsp; Percentage units
 * </H3>
 * <p/>
 * The format of a percentage value is an optional sign character ('+' or '-',
 * with '+' being the default) immediately followed by a number (with or without
 * a decimal point) immediately followed by '%'.
 * <p/>
 * Percentage values are always relative to another value, for example a length
 * unit. Each property that allows percentage units also defines what value
 * the percentage value refer to. Most often this is the font size of the element
 * itself:
 * <PRE>
 * P { line-height: 120% }   / * 120% of the element's 'font-size' * /
 * </PRE>
 * <p/>
 * In all inherited CSS1 properties, if the value is specified as a percentage,
 * child elements inherit the resultant value, not the percentage value.
 *
 * @version $Revision$
 */
public class CssPercentage extends CssCheckableValue {

    public static final int type = CssTypes.CSS_PERCENTAGE;

    public final int getType() {
        return type;
    }

    private BigDecimal defaultValue = BigDecimal.ZERO;
    private BigDecimal value;

    /**
     * Create a new CssPercentage
     */
    public CssPercentage() {
        this.value = defaultValue;
    }

    /**
     * Create a new CssPercentage with a number
     *
     * @param value The value.
     */
    public CssPercentage(int value) {
        this(new BigDecimal(value));
    }

    /**
     * Create a new CssPercentage with a float
     *
     * @param value the float value.
     */
    public CssPercentage(float value) {
        this(new BigDecimal(value));
    }

    /**
     * Create a new CssPercentage with a Float value.
     *
     * @param value the Float object.
     */
    public CssPercentage(BigDecimal value) {
        this.value = value;
    }

    /**
     * Set the value of this percentage.
     *
     * @param s  the string representation of the percentage.
     * @param ac For errors and warnings reports.
     * @throws InvalidParamException The unit is incorrect
     */
    public void set(String s, ApplContext ac) throws InvalidParamException {
        int slength = s.length();
        if (s.charAt(slength - 1) != '%') {
            throw new InvalidParamException("percent", s, ac);
        }
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            // check for scientific notation in CSS < 3
            if (s.indexOf('e') >= 0 || s.indexOf('E') >= 0) {
                throw new InvalidParamException("percent", s, ac);
            }
        }
        this.value = new BigDecimal(s.substring(0, slength - 1));
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
     * Returns the current value
     */
    public Object get() {
        // TODO FIXME
        return value.floatValue();
    }

    /**
     * Returns the real value
     */
    public BigDecimal getValue() {
        return value;
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
     * Returns the value as a float
     */
    public float floatValue() {
        return value.floatValue();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value.toPlainString()).append('%');
        return sb.toString();
    }

    /**
     * Compares two values for equality.
     *
     * @param val The other value.
     */
    public boolean equals(Object val) {
        return ((val instanceof CssPercentage)
                && value.equals(((CssPercentage) val).value));
    }

    public CssPercentage getPercentage() throws InvalidParamException {
        return this;
    }

    /**
     * check if the value is lower or equal than...
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkLowerEqualThan(ApplContext ac, double d, CssProperty property)
            throws InvalidParamException {
        BigDecimal other = BigDecimal.valueOf(d);
        if (value.compareTo(other) > 0) {
            throw new InvalidParamException("lowerequal",
                    toString(), other.toPlainString(), ac);
        }
    }

    /**
     * check if the value is lower or equal than...
     *
     * @param ac       the validation context
     * @param property the property the value is defined in
     * @throws InvalidParamException
     */
    public void warnLowerEqualThan(ApplContext ac, double d, CssProperty property) {
        BigDecimal other = BigDecimal.valueOf(d);
        if (value.compareTo(other) > 0) {
            String[] s = new String[2];
            s[0] = toString();
            s[1] = other.toPlainString() + '%';
            ac.getFrame().addWarning("lowerequal", s);
        }
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
        checkEqualsZero(ac, new String[]{"percentage", toString(), callername});
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the String value of the object it is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        return warnEqualsZero(ac, new String[]{"percentage", callername});
    }

}
