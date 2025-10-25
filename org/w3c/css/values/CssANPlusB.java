//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * @spec https://www.w3.org/TR/2019/CR-css-syntax-3-20190716/#anb-microsyntax
 * @since CSS3
 */
public class CssANPlusB extends CssValue {

    public static final int type = CssTypes.CSS_ANPLUSB;

    public final int getType() {
        return type;
    }

    BigDecimal a = null;
    BigDecimal b = null;
    boolean gotN = false;
    char operator;
    String ident = null;
    String representation = null;

    /**
     * Create a new CssANPlusB.
     */
    public CssANPlusB() {
    }

    public CssANPlusB(BigDecimal a, char operator, BigDecimal b) {
        this.a = a;
        this.b = b;
    }

    public void set(char op, String an, char nop, String bs, String caller, ApplContext ac)
            throws InvalidParamException {
        representation = null;
        if (bs != null) {
            try {
                b = new BigDecimal(bs);
            } catch (NumberFormatException ex) {
                throw new InvalidParamException("value", bs, caller, ac);
            }
            if (nop == '-' && an == null) {
                b = b.negate();
            }
        }
        if (an != null) {
            gotN = true;
            if (an.length() == 1 && (an.charAt(0) != 'n' && an.charAt(0) != 'N')) {
                throw new InvalidParamException("value", an, caller, ac);
            }
            if (an.length() > 1) {
                try {
                    a = new BigDecimal(an.substring(0, an.length() - 1));
                } catch (NumberFormatException ex) {
                    throw new InvalidParamException("value", bs, caller, ac);
                }
                if (op == '-') {
                    a = a.negate();
                }
                if (b != null) {
                    operator = nop;
                }
            }

        }
    }

    // special case... we got a DIMEN
    public void set(char op, String an, String bs, String caller, ApplContext ac)
            throws InvalidParamException {
        representation = null;
        if (bs != null) {
            try {
                b = new BigDecimal(bs);
            } catch (NumberFormatException ex) {
                throw new InvalidParamException("value", bs, caller, ac);
            }
            // bs != null so DIMEN should be ending with a single n
            if (!an.endsWith("-")) {
                throw new InvalidParamException("value", an + bs, caller, ac);
            }
            try {
                a = new BigDecimal(an.substring(0, an.length() - 2));
            } catch (NumberFormatException ex) {
                throw new InvalidParamException("value", bs, caller, ac);
            }
        } else {
            // we need to parse a full string
            int minuspos = an.indexOf('-');
            try {
                a = new BigDecimal(an.substring(0, minuspos - 1));
                b = new BigDecimal(an.substring(minuspos + 1));
            } catch (NumberFormatException ex) {
                throw new InvalidParamException("value", bs, caller, ac);
            }
        }
        operator = '-';
    }

    // special case... we got an IDENT
    public void set(String an, char op, String bs, String caller, ApplContext ac)
            throws InvalidParamException {
        representation = null;
        if (an.equalsIgnoreCase("odd") || an.equalsIgnoreCase("even")) {
            ident = an.toLowerCase(Locale.ENGLISH);
            return;
        }
        if (bs != null) {
            try {
                b = new BigDecimal(bs);
            } catch (NumberFormatException ex) {
                throw new InvalidParamException("value", bs, caller, ac);
            }
            if (op != ' ') {
                if (an.indexOf('-') > 0) {
                    throw new InvalidParamException("value", an + op + bs, caller, ac);
                }
                operator = op;
                if (an.indexOf('-') != an.lastIndexOf('-')) {
                    throw new InvalidParamException("value", an + op + bs, caller, ac);
                }
            }
            if (an.equalsIgnoreCase("-n")) {
                a = BigDecimal.ONE.negate();
            } else if (an.equalsIgnoreCase("n")) {
                a = BigDecimal.ONE;
            } else if (an.equalsIgnoreCase("n-") && op == ' ') {
                a = BigDecimal.ONE;
                operator = '-';
            } else if (an.equalsIgnoreCase("-n-")) {
                a = BigDecimal.ONE.negate();
                operator = '-';
            }
        } else {
            // we need to parse a full string
            int minuspos = an.lastIndexOf('-');
            if (minuspos == -1) {
                if (an.equalsIgnoreCase("n")) {
                    a = BigDecimal.ONE;
                }
            } else {
                try {
                    String astr = an.substring(0, minuspos - 1);
                    if (astr.equalsIgnoreCase("-")) {
                        a = BigDecimal.ONE.negate();
                    } else if (astr.length() == 0) {
                        // n
                        a = BigDecimal.ONE;
                    } else {
                        a = new BigDecimal(astr);
                    }
                    b = new BigDecimal(an.substring(minuspos + 1));
                    operator = '-';
                } catch (NumberFormatException ex) {
                    throw new InvalidParamException("value", bs, caller, ac);
                }
            }
        }
    }

    /**
     * Set the value of this ratio.
     *
     * @param s  the string representation of the ratio.
     * @param ac For errors and warnings reports.
     * @throws InvalidParamException (incorrect format)
     */
    public void set(String s, ApplContext ac) throws InvalidParamException {
        try {
            b = new BigDecimal(s);
        } catch (NumberFormatException ex) {
            throw new InvalidParamException("value", s, "An+B", ac);
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
        if (ident != null) {
            return ident;
        }
        if (representation == null) {
            StringBuilder sb = new StringBuilder();
            if (a != null) {
                sb.append(a.toPlainString()).append('n');
            } else if (gotN) {
                sb.append('n');
            }
            if (b != null) {
                if (operator != 0) {
                    sb.append(operator);
                }
                sb.append(b.toPlainString());
            }
            representation = sb.toString();
        }
        return representation;
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return false;
    }
}

