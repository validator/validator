// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * @spec http://www.w3.org/TR/2012/WD-css3-background-20120214/
 * @since CSS3
 */
public class CssSwitch extends CssValue {

    public static final int type = CssTypes.CSS_SWITCH;

    public static final char SLASH = '/';

    public final int getType() {
        return type;
    }

    char switch_char;

    /**
     * Create a new CssSwitch.
     */
    public CssSwitch() {
        switch_char = SLASH;
    }

    /**
     * Set the value of this ratio.
     *
     * @param s  the string representation of the switch .
     * @param ac For errors and warnings reports.
     * @throws org.w3c.css.util.InvalidParamException
     *          (incorrect format)
     */
    public void set(String s, ApplContext ac) throws InvalidParamException {
        String _spec = s;
        if (_spec.length() != 1) {
            _spec.trim();
            if (_spec.length() != 1) {
                throw new InvalidParamException("value",
                        s, ac);
            }
        }
        switch_char = _spec.charAt(0);
        // currently, only '/' is defined for this.
        if (switch_char != SLASH) {
            throw new InvalidParamException("value",
                    s, ac);
        }
    }

    /**
     * Returns the current value
     */
    public Object get() {
        return toString();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return String.valueOf(switch_char);
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        try {
            CssSwitch other = (CssSwitch) value;
            // check that the ratio are the same
            return (switch_char == other.switch_char);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}

