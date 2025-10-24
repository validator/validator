// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * @spec https://www.w3.org/TR/2016/CR-css-grid-1-20160929/#typedef-line-names
 * @since CSS3
 */
public class CssBracket extends CssValue {

    public static final int type = CssTypes.CSS_BRACKET;

    public static final char LEFT_BRACKET = '[';
    public static final char RIGHT_BRACKET = ']';

    public final int getType() {
        return type;
    }

    char bracket_char;

    /**
     * Create a new CssBracket.
     */
    public CssBracket() {
        bracket_char = ' ';  // invalid
    }

    /**
     * Set the value.
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
        bracket_char = _spec.charAt(0);
        // currently, only '/' is defined for this.
        if (bracket_char != LEFT_BRACKET &&
                bracket_char != RIGHT_BRACKET) {
            throw new InvalidParamException("value",
                    s, ac);
        }
    }

    public boolean isLeft() {
        return LEFT_BRACKET == bracket_char;
    }

    public boolean isRight() {
        return RIGHT_BRACKET == bracket_char;
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
        return String.valueOf(bracket_char);
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        try {
            CssBracket other = (CssBracket) value;
            // check that the ratio are the same
            return (bracket_char == other.bracket_char);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}

