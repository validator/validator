//
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2020.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

/**
 * CSS mathfunction().
 *
 * @spec https://www.w3.org/TR/2019/WD-css-values-4-20190131/#funcdef-max
 */
public class CssMathFunction extends CssCheckableValue {

    public static final int type = CssTypes.CSS_MATH_FUNCTION;

    public final int getRawType() {
        return type;
    }

    public final int getType() {
        if (computed_type == CssTypes.CSS_MATH_FUNCTION) {
            return type;
        }
        return computed_type;
    }

    public static final CssIdent[] rounding_values;

    static {
        String[] _allowed_rounding_values = {"nearest", "up", "down",
                "to-zero"};
        int i = 0;
        rounding_values = new CssIdent[_allowed_rounding_values.length];
        for (String s : _allowed_rounding_values) {
            rounding_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static final boolean isAllowedRounding(CssIdent ident) {
        for (CssIdent id : rounding_values) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    ApplContext ac;
    int computed_type = CssTypes.CSS_UNKNOWN;
    ArrayList<CssValue> values = null;
    String prefix = null;
    String _toString = null;


    /**
     * Create a new CssCalc
     */
    public CssMathFunction(String prefix) {
        this.prefix = prefix.toLowerCase(Locale.ENGLISH);
    }

    public CssMathFunction(ApplContext ac, String prefix) {
        this(ac, prefix, null);
    }

    public CssMathFunction(String prefix, CssValue value) {
        this(null, prefix, value);
    }

    public CssMathFunction(ApplContext ac, String prefix, CssValue value) {
        if (ac != null) {
            this.ac = ac;
        }
        if (prefix != null) {
            this.prefix = prefix.toLowerCase(Locale.ENGLISH);
        }
        if (value != null) {
            computed_type = value.getType();
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(value);
            try {
                computed_type = _checkAcceptableType(value.getType());
            } catch (Exception ex) { // todo report error here or wait ?
            }
        }
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
    public CssMathFunction addValue(CssValue value)
            throws InvalidParamException {
        boolean first = false;
        if (values == null) {
            values = new ArrayList<>();
            first = true;
        }
        values.add(value);
        _computeResultingType(false);
        return this;
    }

    public void validate() throws InvalidParamException {
        _computeResultingType(true);
    }

    private int _checkAcceptableType(int type)
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
        return type;
    }

    private void _computeResultingType(boolean is_final)
            throws InvalidParamException {
        switch (prefix) {
            case "clamp(":
            case "min(":
            case "max(":
            case "hypot(":
                _computeResultingTypeList(is_final);
                break;
            case "sin(":
            case "cos(":
            case "tan(":
            case "asin(":
            case "acos(":
            case "atan(":
                if (is_final) {
                    _computeResultingTypeTrig(is_final);
                }
                break;
            case "exp(":
            case "sqrt(":
                _computeResultingTypeOneNum(is_final);
                break;
            case "pow(":
                if (is_final) {
                    _computeResultingTypeTwoNum(is_final);
                }
                break;
            case "log(":
                if (is_final) {
                    _computeResultingTypeTwoNumOpt(is_final);
                }
                break;
            case "mod(":
            case "rem(":
                if (is_final) {
                    _computeResultingTypeTwoAny(is_final);
                }
                break;
            case "abs(":
                _computeResultingTypeOneAny(is_final);
                break;
            case "atan2(":
                if (is_final) {
                    _computeResultingTypeAtan2(is_final);
                }
                break;
            case "sign(":
                if (is_final) {
                    _computeResultingTypeSign(is_final);
                }
                break;
            case "round(":
                if (is_final) {
                    _computeResultingTypeRound(is_final);
                }
                break;
            default:
                throw new InvalidParamException("unrecognize", ac);
        }

    }

    private void _computeResultingTypeOneNum(boolean is_final)
            throws InvalidParamException {
        int valtype;
        if (values.size() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype = values.get(0).getType();
        if (valtype == CssTypes.CSS_NUMBER) {
            computed_type = CssTypes.CSS_NUMBER;
        } else if (valtype == CssTypes.CSS_VARIABLE) {
            markCssVariable();
            computed_type = valtype;
        } else {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
    }

    private void _computeResultingTypeTwoNum(boolean is_final)
            throws InvalidParamException {
        int valtype1, valtype2;
        if (values.size() != 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype1 = values.get(0).getType();
        valtype2 = values.get(1).getType();
        if ((valtype1 == CssTypes.CSS_NUMBER) && (valtype2 == CssTypes.CSS_NUMBER)) {
            computed_type = CssTypes.CSS_NUMBER;
        } else if ((valtype1 == CssTypes.CSS_VARIABLE) || (valtype2 == CssTypes.CSS_VARIABLE)) {
            if (valtype1 == CssTypes.CSS_VARIABLE) {
                if ((valtype2 == CssTypes.CSS_NUMBER) || (valtype2 == CssTypes.CSS_VARIABLE)) {
                    computed_type = valtype2;
                } else {
                    // one type is not NUMBER
                    throw new InvalidParamException("incompatibletypes", toString(), ac);
                }
            } else if (valtype1 == CssTypes.CSS_NUMBER) {
                computed_type = valtype1;
            } else {
                throw new InvalidParamException("incompatibletypes", toString(), ac);
            }
        } else {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
    }

    // used for log(A, B?)
    private void _computeResultingTypeTwoNumOpt(boolean is_final)
            throws InvalidParamException {
        int valtype;
        if (values.size() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype = values.get(0).getType();
        if (valtype == CssTypes.CSS_NUMBER) {
            computed_type = valtype;
        } else {
            if (valtype == CssTypes.CSS_VARIABLE) {
                markCssVariable();
                computed_type = CssTypes.CSS_VARIABLE;
            } else {
                throw new InvalidParamException("incompatibletypes", toString(), ac);
            }
        }
        if (values.size() > 1) {
            valtype = values.get(1).getType();
            // computed type is set, just verify that it matches
            if (valtype != CssTypes.CSS_NUMBER) {
                if (valtype == CssTypes.CSS_VARIABLE) {
                    if (computed_type != valtype) {
                        markCssVariable();
                    }
                } else {
                    throw new InvalidParamException("incompatibletypes", toString(), ac);
                }
            }
        }
    }

    private void _computeResultingTypeAtan2(boolean is_final)
            throws InvalidParamException {
        int valtype1, valtype2;
        if (values.size() != 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype1 = values.get(0).getType();
        valtype2 = values.get(1).getType();
        if ((valtype1 == CssTypes.CSS_VARIABLE) || (valtype2 == CssTypes.CSS_VARIABLE)) {
            markCssVariable();
            if (valtype1 != CssTypes.CSS_VARIABLE) {
                if (valtype1 != CssTypes.CSS_PERCENTAGE &&
                        valtype1 != CssTypes.CSS_LENGTH &&
                        valtype1 != CssTypes.CSS_NUMBER) {
                    throw new InvalidParamException("incompatibletypes", toString(), ac);
                }
            } else if (valtype2 != CssTypes.CSS_VARIABLE) {
                if (valtype2 != CssTypes.CSS_PERCENTAGE &&
                        valtype2 != CssTypes.CSS_LENGTH &&
                        valtype2 != CssTypes.CSS_NUMBER) {
                    throw new InvalidParamException("incompatibletypes", toString(), ac);
                }
            } else {
                // both are variables.
                computed_type = valtype1;
            }
            computed_type = CssTypes.CSS_ANGLE;
            return;
        }
        if (valtype1 != CssTypes.CSS_PERCENTAGE &&
                valtype1 != CssTypes.CSS_LENGTH &&
                valtype1 != CssTypes.CSS_NUMBER) {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
        if (valtype1 == valtype2) {
            computed_type = CssTypes.CSS_ANGLE;
        } else {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
    }

    private void _computeResultingTypeRound(boolean is_final)
            throws InvalidParamException {
        int valtype1, valtype2;
        int vsize = values.size();
        if ((vsize < 2) || (vsize > 3)) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (vsize == 2) {
            valtype1 = values.get(0).getType();
            // parsing artefact
            if (valtype1 == CssTypes.CSS_IDENT) {
                try {
                    CssNumber n = new CssNumber();
                    n.set(values.get(0).getIdent().toString(), ac);
                    valtype1 = CssTypes.CSS_NUMBER;
                } catch (Exception ignored) {
                }
            } else if (valtype1 == CssTypes.CSS_VARIABLE) {
                markCssVariable();
                valtype2 = values.get(1).getType();
                if (valtype2 == CssTypes.CSS_VARIABLE) {
                    computed_type = valtype1;
                    return;
                } else {
                    if (valtype2 != CssTypes.CSS_PERCENTAGE &&
                            valtype2 != CssTypes.CSS_LENGTH &&
                            valtype2 != CssTypes.CSS_NUMBER) {
                        throw new InvalidParamException("incompatibletypes", toString(), ac);
                    }
                    computed_type = valtype2;
                    return;
                }
            } else if (valtype1 != CssTypes.CSS_PERCENTAGE &&
                    valtype1 != CssTypes.CSS_LENGTH &&
                    valtype1 != CssTypes.CSS_NUMBER) {
                throw new InvalidParamException("incompatibletypes", toString(), ac);
            }
            valtype2 = values.get(1).getType();
        } else {  // 3 values
            CssValue v = values.get(0);
            // FIXME TODO need to care about rounding type being an unresolved var() ?
            if (v.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("incompatibletypes", toString(), ac);
            }
            if (!isAllowedRounding(v.getIdent())) {
                throw new InvalidParamException("incompatibletypes", toString(), ac);
            }
            valtype1 = values.get(1).getType();
            valtype2 = values.get(2).getType();
        }
        if (valtype1 == CssTypes.CSS_VARIABLE) {
            markCssVariable();
            if (valtype2 == CssTypes.CSS_VARIABLE) {
                computed_type = valtype1;
                return;
            } else {
                if (valtype2 != CssTypes.CSS_PERCENTAGE &&
                        valtype2 != CssTypes.CSS_LENGTH &&
                        valtype2 != CssTypes.CSS_NUMBER) {
                    throw new InvalidParamException("incompatibletypes", toString(), ac);
                }
                computed_type = valtype2;
                return;
            }
        } else if (valtype2 == CssTypes.CSS_VARIABLE) {
            markCssVariable();
            if (valtype1 != CssTypes.CSS_PERCENTAGE &&
                    valtype1 != CssTypes.CSS_LENGTH &&
                    valtype1 != CssTypes.CSS_NUMBER) {
                throw new InvalidParamException("incompatibletypes", toString(), ac);
            }
            computed_type = valtype1;
            return;
        }
        if ((valtype1 == valtype2) && (valtype1 == CssTypes.CSS_PERCENTAGE ||
                valtype1 == CssTypes.CSS_LENGTH ||
                valtype1 == CssTypes.CSS_NUMBER)) {
            computed_type = valtype1;
        } else {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
    }

    private void _computeResultingTypeSign(boolean is_final)
            throws InvalidParamException {
        if (values.size() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (values.get(0).getType() == CssTypes.CSS_VARIABLE) {
            markCssVariable();
        }
        computed_type = CssTypes.CSS_NUMBER;
    }

    private void _computeResultingTypeOneAny(boolean is_final)
            throws InvalidParamException {
        int valtype;
        if (values.size() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype = values.get(0).getType();
        if (valtype == CssTypes.CSS_VARIABLE) {
            markCssVariable();
        }
        computed_type = valtype;
    }

    private void _computeResultingTypeTwoAny(boolean is_final)
            throws InvalidParamException {
        int valtype1, valtype2;
        if (values.size() != 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype1 = values.get(0).getType();
        valtype2 = values.get(1).getType();
        if ((valtype1 == CssTypes.CSS_VARIABLE) || (valtype2 == CssTypes.CSS_VARIABLE)) {
            markCssVariable();
            if (valtype1 != CssTypes.CSS_VARIABLE) {
                computed_type = valtype1;
            } else if (valtype2 != CssTypes.CSS_VARIABLE) {
                computed_type = valtype2;
            } else {
                // default to CssVariable
                computed_type = valtype1;
            }
            return;
        }
        if (valtype1 == valtype2) {
            computed_type = valtype1;
        } else {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
    }

    private void _computeResultingTypeTrig(boolean is_final)
            throws InvalidParamException {
        int valtype;
        if (values.size() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        valtype = values.get(0).getType();
        if (valtype == CssTypes.CSS_VARIABLE) {
            markCssVariable();
            if (prefix.startsWith("a")) {
                computed_type = CssTypes.CSS_ANGLE;
            } else {
                computed_type = CssTypes.CSS_NUMBER;
            }
        } else if ((valtype == CssTypes.CSS_NUMBER) || (valtype == CssTypes.CSS_ANGLE)) {
            // FIXME should check cos(angle | 0) and acos(number) to be more precise
            if (prefix.startsWith("a")) {
                computed_type = CssTypes.CSS_ANGLE;
            } else {
                computed_type = CssTypes.CSS_NUMBER;
            }
        } else {
            throw new InvalidParamException("incompatibletypes", toString(), ac);
        }
    }

    private void _computeResultingTypeList(boolean is_final)
            throws InvalidParamException {
        int valtype = CssTypes.CSS_MATH_FUNCTION;
        boolean firstVal = true;
        CssValue prevVal = null;

        for (CssValue v : values) {
            if (firstVal) {
                valtype = v.getType();
                // Variable? defer to the next type
                if (valtype == CssTypes.CSS_VARIABLE) {
                    markCssVariable();
                    continue;
                }
                _checkAcceptableType(valtype);
                computed_type = valtype;
                firstVal = false;
                prevVal = v;
            } else {
                if (valtype == v.getType()) {
                    prevVal = v;
                    continue;
                }
                if (valtype == CssTypes.CSS_PERCENTAGE) {
                    valtype = _checkAcceptableType(v.getType());
                    prevVal = v;
                    continue;
                }
                if (v.getType() == CssTypes.CSS_PERCENTAGE) {
                    continue;
                }
                if (valtype == CssTypes.CSS_NUMBER && prevVal.getNumber().isZero()) {
                    valtype = _checkAcceptableType(v.getType());
                    prevVal = v;
                    continue;
                }
                if (v.getType() == CssTypes.CSS_NUMBER && v.getNumber().isZero()) {
                    continue;
                }
                // if it is a variable without a computed type, skip it
                if ((v.getType() == CssTypes.CSS_VARIABLE) || (v.getRawType() == CssTypes.CSS_VARIABLE)) {
                    markCssVariable();
                    continue;
                }
                throw new InvalidParamException("incompatibletypes", toStringUnprefixed(), ac);
            }
        }
        computed_type = valtype;
    }

    /**
     * Returns the value
     */

    public Object get() {
        return toString();
    }

    protected String toStringUnprefixed() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (CssValue v : values) {
            if (!isFirst) {
                sb.append(", ");
            } else {
                isFirst = false;
            }
            sb.append(v);
        }
        return sb.toString();
    }

    public String toString() {
        if (_toString == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(prefix).append(toStringUnprefixed()).append(')');
            _toString = sb.toString();
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
        return false;
    }

    /**
     * Returns true is the value is positive of null
     *
     * @return a boolean
     */
    public boolean isStrictlyPositive() {
        return false;
        // TODO do our best...
    }

    /**
     * Returns true is the value is zero
     *
     * @return a boolean
     */
    public boolean isZero() {
        // TODO do our best...
        return false;
    }


    /**
     * Compares two values for equality.
     *
     * @param value The other value.
     */
    public boolean equals(Object value) {
        if (!(value instanceof CssMathFunction)) {
            return false;
        }
        CssMathFunction other = (CssMathFunction) value;
        boolean match;
        // this is inherently wrong, as we should check only the min value, but in that case we
        // would need to explicitly compute them which is not done.
        for (CssValue v : this.values) {
            match = false;
            for (CssValue ov : other.values) {
                if (v.equals(ov)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if the value is positive or null
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws InvalidParamException
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
     * @throws InvalidParamException
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
     * @throws InvalidParamException
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
            for (CssValue v : values) {
                if (v.getType() == CssTypes.CSS_LENGTH) {
                    return v.getLength();
                }
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssPercentage getPercentage() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_PERCENTAGE) {
            for (CssValue v : values) {
                if (v.getType() == CssTypes.CSS_PERCENTAGE) {
                    return v.getPercentage();
                }
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssNumber getNumber() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_NUMBER) {
            for (CssValue v : values) {
                if (v.getType() == CssTypes.CSS_NUMBER) {
                    return v.getNumber();
                }
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssTime getTime() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_TIME) {
            for (CssValue v : values) {
                if (v.getType() == CssTypes.CSS_TIME) {
                    return v.getTime();
                }
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssAngle getAngle() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_ANGLE) {
            for (CssValue v : values) {
                if (v.getType() == CssTypes.CSS_ANGLE) {
                    return v.getAngle();
                }
            }
        }
        throw new ClassCastException("unknown");
    }

    public CssFrequency getFrequency() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_FREQUENCY) {
            for (CssValue v : values) {
                if (v.getType() == CssTypes.CSS_FREQUENCY) {
                    return v.getFrequency();
                }
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
