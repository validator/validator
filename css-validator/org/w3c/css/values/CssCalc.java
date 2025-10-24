//
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * A CSS calc().
 *
 * @spec https://www.w3.org/TR/2015/CR-css-values-3-20150611/#calc-notation
 */
public class CssCalc extends CssCheckableValue {

    public static final int type = CssTypes.CSS_CALC;

    public final int getRawType() {
        return type;
    }

    public final int getType() {
        if (computed_type == CssTypes.CSS_CALC) {
            return type;
        }
        return computed_type;
    }

    ApplContext ac;
    int computed_type = CssTypes.CSS_UNKNOWN;
    CssValue val1 = null;
    CssValue val2 = null;
    char operator;
    boolean hasParen = false;
    String _toString = null;
    boolean implicit_function = true;

    /**
     * Create a new CssCalc
     */
    public CssCalc() {
    }

    public CssCalc(ApplContext ac) {
        this(ac, null);
    }

    public CssCalc(CssValue value) {
        this(null, value);
    }

    public CssCalc(ApplContext ac, CssValue value) {
        if (ac != null) {
            this.ac = ac;
        }
        if (value != null) {
            if (value.getRawType() == CssTypes.CSS_CALC) {
                CssCalc c = (CssCalc) value;
                contains_variable = c.hasCssVariable();
            } else if (value.getType() == CssTypes.CSS_VARIABLE) {
                contains_variable = true;
            }
        }
        computed_type = value.getType();
        val1 = value;
    }

    public void setImplicitFunction(boolean v) {
        implicit_function = v;
        _toString = null;
    }

    public void set(String s, ApplContext ac) throws InvalidParamException {
        // we don't support this way of setting the value
        // as we rely on the parsing to create it incrementally
        throw new InvalidParamException("unrecognize", s, ac);
    }

    public void setValue(BigDecimal d) {
        // we don't support this way of setting the value
        // as we rely on the parsing to create it incrementally
    }

    /**
     * Add one operand, if we already got one we will... Add one operand.
     *
     * @param value
     * @return
     */
    public CssCalc setLeftSide(CssValue value)
            throws InvalidParamException {
        if (val1 != null) {
            throw new InvalidParamException("unrecognized", val1, ac);
        }
        val1 = value;
        if (val1.getType() == CssTypes.CSS_VARIABLE) {
            contains_variable = true;
        }
        _toString = null;
        return this;
    }

    public CssCalc addRightSide(String oper, CssValue value) throws InvalidParamException {
        _toString = null;
        switch (oper) {
            case "+":
                operator = CssOperator.PLUS;
                break;
            case "-":
                operator = CssOperator.MINUS;
                break;
            case "*":
                operator = CssOperator.MUL;
                break;
            case "/":
                operator = CssOperator.DIV;
                break;
            default:
                throw new InvalidParamException("operator", oper, ac);
        }
        val2 = value;
        if (val2.getType() == CssTypes.CSS_VARIABLE) {
            contains_variable = true;
        }
        _computeResultingType(false);
        return this;
    }

    public CssCalc setParenthesis() {
        hasParen = true;
        return this;
    }

    public void validate() throws InvalidParamException {
        _computeResultingType(true);
    }

    private void _checkAcceptableType(int type)
            throws InvalidParamException {
        //  <length>, <frequency>, <angle>, <time>, <number>, or <integer>
        if (type != CssTypes.CSS_PERCENTAGE &&
                type != CssTypes.CSS_LENGTH &&
                type != CssTypes.CSS_NUMBER &&
                type != CssTypes.CSS_ANGLE &&
                type != CssTypes.CSS_FREQUENCY &&
                type != CssTypes.CSS_TIME) {
            throw new InvalidParamException("invalidtype", toStringUnprefixed(), ac);
        }
    }

    private void _computeResultingType(boolean end)
            throws InvalidParamException {
        int valtype;

        if (contains_variable) {
            // nothing to check as we may not have anything yet
            return;
        }
        if (val2 == null) {
            // we only have val1 to check.
            valtype = val1.getType();
            _checkAcceptableType(valtype);
            computed_type = valtype;
        } else {
            // TODO sanity check... ensure that val1 is not null
            switch (operator) {
                case CssOperator.MUL:
                    // one operator must be a number.
                    if (val1.getType() == CssTypes.CSS_NUMBER) {
                        valtype = val2.getType();
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    } else if (val2.getType() == CssTypes.CSS_NUMBER) {
                        valtype = val1.getType();
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    } else {
                        // none of them is a number...
                        throw new InvalidParamException("operandnumber", toStringUnprefixed(), ac);
                    }
                    break;
                case CssOperator.DIV:
                    // 2nd operator must be a NUMBER (and not 0).
                    if (val2.getType() != CssTypes.CSS_NUMBER) {
                        throw new InvalidParamException("divisortype", toStringUnprefixed(), ac);
                    }
                    if (val2.getNumber().isZero()) {
                        throw new InvalidParamException("divzero", toStringUnprefixed(), ac);
                    }
                    valtype = val1.getType();
                    _checkAcceptableType(valtype);
                    computed_type = valtype;
                    break;
                case CssOperator.PLUS:
                case CssOperator.MINUS:
                    // the case for PLUS and MINUS
                    valtype = val1.getType();
                    if (valtype == val2.getType()) {
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    }
                    // if not the same type... one of them must be a percentage... or number zero
                    else if (valtype == CssTypes.CSS_PERCENTAGE) {
                        valtype = val2.getType();
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    } else if (val2.getType() == CssTypes.CSS_PERCENTAGE) {
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    } else if (valtype == CssTypes.CSS_NUMBER && val1.getNumber().isZero()) {
                        valtype = val2.getType();
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    } else if (val2.getType() == CssTypes.CSS_NUMBER && val2.getNumber().isZero()) {
                        _checkAcceptableType(valtype);
                        computed_type = valtype;
                    } else {
                        throw new InvalidParamException("incompatibletypes", toStringUnprefixed(), ac);
                    }
                    break;
                default:
                    // we have only one value.
                    valtype = val1.getType();
                    _checkAcceptableType(valtype);
                    computed_type = valtype;
            }
        }
    }

    /**
     * Returns the value
     */

    public Object get() {
        return toString();
    }

    protected String toStringUnprefixed() {
        StringBuilder sb = new StringBuilder();
        if (hasParen) {
            sb.append('(');
        }
        if (val1.getRawType() == CssTypes.CSS_CALC && ((CssCalc) val1).implicit_function) {
            sb.append(((CssCalc) val1).toStringUnprefixed());
        } else {
            sb.append(val1);
        }
        if (val2 != null) {
            sb.append(' ').append(operator).append(' ');
            if (val2.getRawType() == CssTypes.CSS_CALC && ((CssCalc) val2).implicit_function) {
                sb.append(((CssCalc) val2).toStringUnprefixed());
            } else {
                sb.append(val2);
            }
        }
        if (hasParen) {
            sb.append(')');
        }
        return sb.toString();
    }

    public String toString() {
        if (_toString == null) {
            if (!implicit_function) {
                StringBuilder sb = new StringBuilder();

                sb.append("calc(").append(toStringUnprefixed()).append(')');
                _toString = sb.toString();
            } else {
                _toString = toStringUnprefixed();
            }
        }
        return _toString;
    }


    public boolean isInteger() {
        return false;
    }

    /**
     * Returns true is the value is positive of null
     *
     * @return a boolean
     */
    public boolean isPositive() {
        // TODO do our best...
        return true;
    }

    /**
     * Returns true is the value is positive of null
     *
     * @return a boolean
     */
    public boolean isStrictlyPositive() {
        return true;
        // TODO do our best...
    }

    /**
     * Returns true is the value is zero
     *
     * @return a boolean
     */
    public boolean isZero() {
        // TODO do our best...
        return true;
    }


    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        if (value instanceof CssCalc) {
            CssCalc c = (CssCalc) value;
            if (this.val1 != null && !this.val1.equals(c.val1)) {
                return false;
            } else if ((this.val1 == null) && (c.val1 != null)) {
                return false;
            }
            // val1 and c.val2 are equal even if null
            if (this.val2 != null && !this.val2.equals(c.val2)) {
                return false;
            } else if ((this.val2 == null) && (c.val2 != null)) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * check if the value is positive or null
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws org.w3c.css.util.InvalidParamException
     */
    public void checkPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        // TODO do our best...
        if (false /*!isPositive()*/) {
            throw new InvalidParamException("negative-value",
                    toString(), callername, ac);
        }
    }

    /**
     * check if the value is strictly positive
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws org.w3c.css.util.InvalidParamException
     */
    public void checkStrictPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        // TODO do our best...
        if (false/*!isStrictlyPositive()*/) {
            throw new InvalidParamException("strictly-positive",
                    toString(), callername, ac);
        }
    }

    /**
     * check if the value is an integer
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws org.w3c.css.util.InvalidParamException
     */
    public void checkInteger(ApplContext ac, String callername)
            throws InvalidParamException {
        // TODO do our best...
        if (false/*!isInteger()*/) {
            throw new InvalidParamException("integer",
                    toString(), callername, ac);
        }
    }

    /**
     * warn if the value is not positive or null
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     */
    public boolean warnPositiveness(ApplContext ac, String callername) {
        // TODO do our best...
        if (false/*!isPositive()*/) {
            ac.getFrame().addWarning("negative", toString());
            return false;
        }
        return true;
    }

    public CssLength getLength() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_LENGTH) {
            if (val1.getType() == CssTypes.CSS_LENGTH) {
                return val1.getLength();
            }
            if (val2.getType() == CssTypes.CSS_LENGTH) {
                return val2.getLength();
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssPercentage getPercentage() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_PERCENTAGE) {
            if (val1.getType() == CssTypes.CSS_PERCENTAGE) {
                return val1.getPercentage();
            }
            if (val2.getType() == CssTypes.CSS_PERCENTAGE) {
                return val2.getPercentage();
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssNumber getNumber() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_NUMBER) {
            if (val1.getType() == CssTypes.CSS_NUMBER) {
                return val1.getNumber();
            }
            if (val2.getType() == CssTypes.CSS_NUMBER) {
                return val2.getNumber();
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssTime getTime() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_TIME) {
            if (val1.getType() == CssTypes.CSS_TIME) {
                return val1.getTime();
            }
            if (val2.getType() == CssTypes.CSS_TIME) {
                return val2.getTime();
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssAngle getAngle() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_ANGLE) {
            if (val1.getType() == CssTypes.CSS_ANGLE) {
                return val1.getAngle();
            }
            if (val2.getType() == CssTypes.CSS_ANGLE) {
                return val2.getAngle();
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssFrequency getFrequency() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_FREQUENCY) {
            if (val1.getType() == CssTypes.CSS_FREQUENCY) {
                return val1.getFrequency();
            }
            if (val2.getType() == CssTypes.CSS_FREQUENCY) {
                return val2.getFrequency();
            }
        }
        throw new ClassCastException("unknown");
    }

    /**
     * check if the value is equal to zero
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws InvalidParamException
     */
    public void checkEqualsZero(ApplContext ac, String callername)
            throws InvalidParamException {
        // we can't check so we only warn.
        // TODO should we do that only for CSS_NUMBER type?
        warnEqualsZero(ac, callername);
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        // TODO should we do that only for CSS_NUMBER type?
        if (!isZero()) {
            ac.getFrame().addWarning("dynamic", toString());
            return false;
        }
        return true;
    }
}
