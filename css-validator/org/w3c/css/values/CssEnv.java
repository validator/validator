//
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.parser.CssError;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * A CSS env().
 *
 * @spec https://www.w3.org/TR/2025/WD-css-env-1-20250923/
 */
public class CssEnv extends CssCheckableValue {

    public static final int type = CssTypes.CSS_ENV;

    private static final HashMap<String, Integer> defaultTypes;
    private static final HashMap<String, Integer> defaultNumValues;

    static {
        String[] _two_dimensions = {"viewport-segment-width", "viewport-segment-height",
                "viewport-segment-top", "viewport-segment-left", "viewport-segment-bottom",
                "viewport-segment-right"};
        String[] _zero_dimensions = {"safe-area-inset-top", "safe-area-inset-left",
                "safe-area-inset-bottom", "safe-area-inset-right"};
        defaultTypes = new HashMap<>();
        defaultNumValues = new HashMap<>();
        for (String s : _zero_dimensions) {
            defaultNumValues.put(s, 0);
            defaultTypes.put(s, CssTypes.CSS_LENGTH);
        }
        for (String s : _two_dimensions) {
            defaultNumValues.put(s, 2);
            defaultTypes.put(s, CssTypes.CSS_LENGTH);
        }
    }

    public final int getRawType() {
        return type;
    }

    public final int getType() {
        if (computed_type == CssTypes.CSS_UNKNOWN) {
            if (defaultTypes.containsKey(env_name)) {
                computed_type = defaultTypes.get(env_name);
                // check
                if (declaration != null && declaration.getCount() == 1) {
                    _exp_value = declaration.getValue();
                    int declaration_type = _exp_value.getType();
                    if (computed_type != declaration_type) {
                        // raise an error unless we need a length and get a 0
                        if (computed_type == CssTypes.CSS_LENGTH) {
                            if (_exp_value.getType() == CssTypes.CSS_NUMBER) {
                                try {
                                    if (_exp_value.getCheckableValue().isZero()) {
                                        return computed_type;
                                    }
                                } catch (InvalidParamException ignored) {
                                }
                            }
                        }
                        CssError error;
                        error = new CssError(new InvalidParamException("value",
                                _exp_value, toStringEmpty(), ac));
                        ac.getFrame().addError(error);
                    }
                }
                return computed_type;
            } else {
                if (declaration != null && declaration.getCount() == 1) {
                    _exp_value = declaration.getValue();
                    computed_type = _exp_value.getType();
                    return computed_type;
                }
            }
            return type;
        }
        return computed_type;
    }

    String env_name = null;
    ApplContext ac;
    int computed_type = CssTypes.CSS_UNKNOWN;
    CssExpression declaration = null;
    CssExpression num_exp = null;
    CssValue _exp_value = null;
    String _toString = null;

    /**
     * Create a new CssVariable
     */
    public CssEnv() {
    }

    public CssEnv(ApplContext ac, String env_name) {
        this(ac, env_name, null, null);
    }

    public CssEnv(String env_name, CssExpression num_exp) {
        this(null, env_name, num_exp, null);
    }

    public CssEnv(ApplContext ac, String env_name, CssExpression num_exp, CssExpression declaration) {
        if (ac != null) {
            this.ac = ac;
        }
        if (env_name != null) {
            this.env_name = env_name;
        }
        if ((num_exp != null) && (num_exp.getCount() != 0)) {
            this.num_exp = num_exp;
        }
        this.declaration = declaration;
    }


    public void set(String s, ApplContext ac) throws InvalidParamException {
        // we don't support this way of setting the value
        // as we rely on the parsing to create it incrementally
        throw new InvalidParamException("unrecognize", s, ac);
    }


    public void setDeclaration(CssExpression exp) {
        this.declaration = exp;
        computed_type = CssTypes.CSS_UNKNOWN;
        _toString = null;
    }

    public void setNumberExp(CssExpression exp)
            throws InvalidParamException {
        this.num_exp = exp;
        computed_type = CssTypes.CSS_UNKNOWN;
        if (num_exp != null) {
            num_exp.starts();
            while (!num_exp.end()) {
                CssValue v = num_exp.getValue();
                if (v.getType() != CssTypes.CSS_NUMBER) {
                    throw new InvalidParamException("value", v.toString(), "env()", ac);
                }
                num_exp.next();
            }
            num_exp.starts();
        }
        _toString = null;
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
            sb.append("env(").append(env_name);
            if (num_exp != null && (num_exp.getCount() > 0)) {
                sb.append(" ").append(num_exp.toStringFromStart());
            }
            if (declaration != null) {
                sb.append(", ").append(declaration.toStringFromStart());
            }
            sb.append(')');
            _toString = sb.toString();
        }
        return _toString;
    }

    public String toStringEmpty() {
        StringBuilder sb = new StringBuilder();
        sb.append("env(").append(env_name).append(')');
        return sb.toString();
    }

    private boolean _isCheckableType(int type) {
        switch (type) {
            case CssTypes.CSS_ANGLE:
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

    private void checkDefinedVariableSyntax()
            throws InvalidParamException {
        if (defaultNumValues.containsKey(env_name)) {
            int num_values = defaultNumValues.get(env_name);
            if (num_values == 0 && (num_exp != null && num_exp.getCount() > 0)) {
                throw new InvalidParamException("value", num_exp.getValue(), toStringEmpty(), ac);
            }
            if (num_values != 0 && ((num_exp == null) || (num_exp.getCount() != num_values))) {
                throw new InvalidParamException("value", num_exp.getLastValue(), toStringEmpty(), ac);
            }
            // now check both values are integer
            if (num_values > 0) {
                num_exp.starts();
                while (!num_exp.end()) {
                    CssValue v = num_exp.getValue();
                    // should be numbers
                    v.getNumber().checkInteger(ac, env_name);
                    num_exp.next();
                }
            }

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
            if (_exp_value != null) {
                _exp_value.getCheckableValue().checkPositiveness(ac, callername);
            } else {
                checkDefinedVariableSyntax();
            }
        }
    }

    /**
     * check if the value is strictly positive
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws InvalidParamException
     */
    @Override
    public void checkStrictPositiveness(ApplContext ac, String callername)
            throws InvalidParamException {
        if (_isCheckableType(computed_type)) {
            if (_exp_value != null) {
                _exp_value.getCheckableValue().checkStrictPositiveness(ac, callername);
            } else {
                checkDefinedVariableSyntax();
            }
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
            if (_exp_value != null) {
                _exp_value.getCheckableValue().checkEqualsZero(ac, callername);
            } else {
                checkDefinedVariableSyntax();
            }
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
                if (_exp_value != null) {
                    return _exp_value.getCheckableValue().warnPositiveness(ac, callername);
                }
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
                if (_exp_value != null) {
                    return _exp_value.getCheckableValue().warnEqualsZero(ac, callername);
                }
            } catch (InvalidParamException e) {
            }
        }
        return false;
    }

    @Override
    public boolean isPositive() {
        if (_isCheckableType(computed_type)) {
            try {
                if (_exp_value != null) {
                    return _exp_value.getCheckableValue().isPositive();
                }
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
                if (_exp_value != null) {
                    return _exp_value.getCheckableValue().isStrictlyPositive();
                }
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
                if (_exp_value != null) {
                    return _exp_value.getCheckableValue().isZero();
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public void checkInteger(ApplContext ac, String callername)
            throws InvalidParamException {
        if (_isCheckableType(computed_type)) {
            if (_exp_value != null) {
                _exp_value.getCheckableValue().checkInteger(ac, callername);
            } else {
                checkDefinedVariableSyntax();
            }
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
        return (value instanceof CssEnv &&
                this.env_name.equals(((CssEnv) value).env_name));
    }

    // extras for conflict resolution
    @Override
    public CssLength getLength() throws InvalidParamException {
        if ((computed_type == CssTypes.CSS_LENGTH)
                || (computed_type == CssTypes.CSS_NUMBER)) {
            if (_exp_value != null) {
                return _exp_value.getLength();
            } else {
                // FIXME get something better here
                // length is currently the only "weird" case
                // as the only pre-defined type used
                CssLength l = new CssLength();
                return l;
            }
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
