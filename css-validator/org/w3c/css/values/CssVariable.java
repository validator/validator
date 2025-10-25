//
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.css.StyleSheet;
import org.w3c.css.properties.css.CssCustomProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

/**
 * A CSS var().
 *
 * @spec https://www.w3.org/TR/2015/CR-css-variables-1-20151203/#funcdef-var
 */
public class CssVariable extends CssCheckableValue {

    public static final int type = CssTypes.CSS_VARIABLE;

    public final int getRawType() {
        return type;
    }

    public final int getType() {
        if (computed_type == CssTypes.CSS_UNKNOWN) {
            if (ac != null) {
                StyleSheet s = ac.getStyleSheet();
                if (s != null) {
                    CssCustomProperty cp = s.getCustomProperty(variable_name);
                    if (cp != null) {
                        CssVariableDefinition vd = (CssVariableDefinition) cp.value;
                        if (vd != null) {
                            if (vd.size() == 1) {
                                _exp_value = vd.expression.getValue();
                                computed_type = _exp_value.getType();
                                return computed_type;
                            }
                        }
                    }
                }
            }
            return type;
        }
        return computed_type;
    }

    public final CssExpression getVariableExpression() {
        if (ac != null) {
            StyleSheet s = ac.getStyleSheet();
            if (s != null) {
                CssCustomProperty cp = s.getCustomProperty(variable_name);
                if (cp != null) {
                    CssVariableDefinition vd = (CssVariableDefinition) cp.value;
                    if (vd != null) {
                        if (vd.size() > 0) {
                            return vd.expression;
                        }
                    }
                }
            }
        }
        return null;
    }

    String variable_name = null;
    ApplContext ac;
    int computed_type = CssTypes.CSS_UNKNOWN;
    CssExpression exp = null;
    CssValue _exp_value = null;
    String _toString = null;

    /**
     * Create a new CssVariable
     */
    public CssVariable() {
    }

    public CssVariable(ApplContext ac, String varname) {
        this(ac, varname, null);
    }

    public CssVariable(String varname, CssExpression expression) {
        this(null, varname, expression);
    }

    public CssVariable(ApplContext ac, String varname, CssExpression expression) {
        if (ac != null) {
            this.ac = ac;
        }
        if (varname != null) {
            variable_name = varname;
        }
        if (expression != null) {
            if (expression.getCount() == 1) {
                _exp_value = expression.getValue();
                computed_type = _exp_value.getType();
            }
        }
        this.exp = expression;
    }

    public void set(String s, ApplContext ac) throws InvalidParamException {
        // we don't support this way of setting the value
        // as we rely on the parsing to create it incrementally
        throw new InvalidParamException("unrecognize", s, ac);
    }

    public void set(CssExpression expression) {
        if (expression != null) {
            if (expression.getCount() == 1) {
                _exp_value = expression.getValue();
                computed_type = _exp_value.getType();
            }
        }
        this.exp = expression;
    }

    /**
     * Returns the value
     */

    public Object get() {
        return toString();
    }


    public String toString() {
        if (_toString == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("var(").append(variable_name);
            if (exp != null) {
                sb.append(", ").append(exp.toStringFromStart());
            }
            sb.append(')');
            _toString = sb.toString();
        }
        return _toString;
    }

    private boolean _isCheckableType(int type) {
        switch (type) {
            case CssTypes.CSS_ANGLE:
            case CssTypes.CSS_MATH_FUNCTION:
            case CssTypes.CSS_FLEX:
            case CssTypes.CSS_FREQUENCY:
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_SEMITONE:
            case CssTypes.CSS_TIME:
            case CssTypes.CSS_VOLUME:
                return true;
            default:
                return false;
        }
    }

    @Override
    public CssCheckableValue getCheckableValue() {
        if (_isCheckableType(computed_type)) {
            return this;
        }
        throw new ClassCastException("unknown");
    }

    /**
     * check if the value is positive or null
     *
     * @param ac         the validation context
     * @param callername the String value of the caller (property / media query / ...)
     * @throws InvalidParamException
     */
    @Override
    public void checkPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        if (_isCheckableType(computed_type)) {
            _exp_value.getCheckableValue().checkPositiveness(ac, callername);
        }
    }

    /**
     * check if the value is strictly positive
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws org.w3c.css.util.InvalidParamException
     */
    @Override
    public void checkStrictPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        if (_isCheckableType(computed_type)) {
            _exp_value.getCheckableValue().checkStrictPositiveness(ac, callername);
        }
    }

    /**
     * check if the value is equal to zero
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws InvalidParamException
     */
    @Override
    public void checkEqualsZero(ApplContext ac, String callername)
            throws InvalidParamException {
        if (_isCheckableType(computed_type)) {
            _exp_value.getCheckableValue().checkEqualsZero(ac, callername);
        }
    }

    /**
     * warn if the value is not positive or null
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     */
    @Override
    public boolean warnPositiveness(ApplContext ac, String callername) {
        if (_isCheckableType(computed_type)) {
            try {
                return _exp_value.getCheckableValue().warnPositiveness(ac, callername);
            } catch (InvalidParamException e) {
            }
        }
        return false;
    }

    /**
     * warn if the value is not zero
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     */
    public boolean warnEqualsZero(ApplContext ac, String callername) {
        if (_isCheckableType(computed_type)) {
            try {
                return _exp_value.getCheckableValue().warnEqualsZero(ac, callername);
            } catch (InvalidParamException e) {
            }
        }
        return false;
    }

    @Override
    public boolean isPositive() {
        if (_isCheckableType(computed_type)) {
            try {
                return _exp_value.getCheckableValue().isPositive();
            } catch (Exception ignored) {
            }
            ;
        }
        return false;
    }

    @Override
    public boolean isStrictlyPositive() {
        if (_isCheckableType(computed_type)) {
            try {
                return _exp_value.getCheckableValue().isStrictlyPositive();
            } catch (Exception ignored) {
            }
            ;
        }
        return false;
    }

    @Override
    public boolean isZero() {
        if (_isCheckableType(computed_type)) {
            try {
                return _exp_value.getCheckableValue().isZero();
            } catch (Exception ignored) {
            }
            ;
        }
        return false;
    }

    @Override
    public void checkInteger(ApplContext ac, String callername)
            throws InvalidParamException {
        if (_isCheckableType(computed_type)) {
            _exp_value.getCheckableValue().checkInteger(ac, callername);
        }
    }

    @Override
    public void setValue(BigDecimal v) {
        // do nothing
    }

    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        return (value instanceof CssVariable &&
                this.variable_name.equals(((CssVariable) value).variable_name));
    }

    // extras for conflict resolution
    @Override
    public CssLength getLength() throws InvalidParamException {
        if ((computed_type == CssTypes.CSS_LENGTH)
                || (computed_type == CssTypes.CSS_NUMBER)) {
            return _exp_value.getLength();
        }
        throw new ClassCastException("unknown");
    }

    @Override
    public CssPercentage getPercentage() throws InvalidParamException {
        if (computed_type != CssTypes.CSS_PERCENTAGE) {
            throw new ClassCastException("unknown");
        }
        return _exp_value.getPercentage();
    }

    @Override
    public CssNumber getNumber() throws InvalidParamException {
        if (computed_type != CssTypes.CSS_NUMBER) {
            throw new ClassCastException("unknown");
        }
        return _exp_value.getNumber();
    }

    @Override
    public CssTime getTime() throws InvalidParamException {
        if ((computed_type == CssTypes.CSS_TIME)
                || (computed_type == CssTypes.CSS_NUMBER)) {
            return _exp_value.getTime();
        }
        throw new ClassCastException("unknown");
    }

    @Override
    public CssAngle getAngle() throws InvalidParamException {
        if ((computed_type == CssTypes.CSS_ANGLE)
                || (computed_type == CssTypes.CSS_NUMBER)) {
            return _exp_value.getAngle();
        }
        throw new ClassCastException("unknown");
    }

    @Override
    public CssFrequency getFrequency() throws InvalidParamException {
        if ((computed_type == CssTypes.CSS_FREQUENCY)
                || (computed_type == CssTypes.CSS_NUMBER)) {
            return _exp_value.getFrequency();
        }
        throw new ClassCastException("unknown");
    }

    @Override
    public CssIdent getIdent() throws InvalidParamException {
        if (computed_type != CssTypes.CSS_IDENT) {
            throw new ClassCastException("unknown");
        }
        return _exp_value.getIdent();
    }

    @Override
    public CssHashIdent getHashIdent() throws InvalidParamException {
        if (computed_type != CssTypes.CSS_HASH_IDENT) {
            throw new ClassCastException("unknown");
        }
        return _exp_value.getHashIdent();
    }

    @Override
    public CssString getString() throws InvalidParamException {
        if (computed_type != CssTypes.CSS_STRING) {
            throw new ClassCastException("unknown");
        }
        return _exp_value.getString();
    }

    @Override
    public CssFunction getFunction() throws InvalidParamException {
        if (computed_type != CssTypes.CSS_FUNCTION) {
            throw new ClassCastException("unknown");
        }
        CssFunction f = _exp_value.getFunction();
        if (f.getParameters() != null) {
            f.getParameters().starts();
        }
        return _exp_value.getFunction();
    }
}
