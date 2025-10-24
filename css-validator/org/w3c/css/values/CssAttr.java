//
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * A CSS Attr.
 *
 * @spec https://www.w3.org/TR/2015/CR-css-values-3-20150611/#attr-notation
 */
public class CssAttr extends CssCheckableValue {

    public static HashMap<CssIdent, Integer> typeMap;

    static {
        /* @spec https://www.w3.org/TR/2015/CR-css-values-3-20150611/#typedef-type-or-unit */
        typeMap = new HashMap<>();
        typeMap.put(CssIdent.getIdent("color"), CssTypes.CSS_COLOR);
        typeMap.put(CssIdent.getIdent("string"), CssTypes.CSS_STRING);
        typeMap.put(CssIdent.getIdent("url"), CssTypes.CSS_URL);
        typeMap.put(CssIdent.getIdent("integer"), CssTypes.CSS_NUMBER);
        typeMap.put(CssIdent.getIdent("number"), CssTypes.CSS_NUMBER);
        typeMap.put(CssIdent.getIdent("%"), CssTypes.CSS_PERCENTAGE);
        // length
        String[] _length_tokens = {"length", "em", "ex", "ch", "rem",
                "vw", "vh", "vmin", "vmax",
                "mm", "cm", "q", "in", "pt", "pc", "px"};
        for (String s : _length_tokens) {
            typeMap.put(CssIdent.getIdent(s), CssTypes.CSS_LENGTH);
        }
        // angle
        String[] _angle_tokens = {"angle", "deg", "rad", "grad", "turn"};
        for (String s : _angle_tokens) {
            typeMap.put(CssIdent.getIdent(s), CssTypes.CSS_ANGLE);
        }
        // time
        String[] _time_tokens = {"time", "ms", "s"};
        for (String s : _time_tokens) {
            typeMap.put(CssIdent.getIdent(s), CssTypes.CSS_TIME);
        }
        // frequency
        String[] _frequency_tokens = {"frequency", "Hz", "kHz"};
        for (String s : _frequency_tokens) {
            typeMap.put(CssIdent.getIdent(s), CssTypes.CSS_FREQUENCY);
        }
    }

    public static final int type = CssTypes.CSS_ATTR;

    public final int getRawType() {
        return type;
    }

    public final int getType() {
        if (computed_type == CssTypes.CSS_UNKNOWN) {
            return type;
        }
        return computed_type;
    }

    ApplContext ac;
    int computed_type = CssTypes.CSS_UNKNOWN;
    CssValue value = null;
    CssValue value_type = null;
    CssValue fallback_value = null;
    String _ts = null;


    /**
     * Create a new CssAttr
     */
    public CssAttr() {
    }

    public void set(String s, ApplContext ac) throws InvalidParamException {
        // we don't support this way of setting the value
        // as we rely on the parsing a CssExpression
        throw new InvalidParamException("unrecognize", s, ac);
    }

    public void setValue(BigDecimal d) {
        // we don't support this way of setting the value
        // as we rely on the parsing a CssExpression
    }

    public void setValue(CssExpression exp, ApplContext ac)
            throws InvalidParamException {

        CssValue val;
        char op;
        int count = exp.getCount();
        // for levels before CSS3, attr only got a single value
        if (count > 3 || (count > 1 && ac.getCssVersion().compareTo(CssVersion.CSS3) < 0)) {
            throw new InvalidParamException("unrecognize", ac);
        }
        val = exp.getValue();
        op = exp.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(), "attr()", ac);
        }
        value = val;
        // by default we have a Css_String
        computed_type = CssTypes.CSS_STRING;
        exp.next();
        // check the
        while (!exp.end()) {
            val = exp.getValue();
            switch (op) {
                case SPACE:
                    // first we must ensure that we didn't get the fallback value first
                    if (fallback_value != null || value_type != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    if (val.getType() == CssTypes.CSS_IDENT) {
                        computed_type = _checkType(val.getIdent());
                        value_type = val;
                        break;
                    }
                    throw new InvalidParamException("value",
                            val.toString(), "attr()", ac);
                case COMMA:
                    fallback_value = val;
                    // we should have a computed_type by then.
                    // let's check the value.
                    if (fallback_value.getType() != computed_type) {
                        // deal with percentage and numbers
                        int fb_type = fallback_value.getType();
                        if (fb_type == CssTypes.CSS_NUMBER) {
                            // number can be another type only when its value is zero
                            fallback_value.getCheckableValue().checkEqualsZero(ac, "");
                        }
                        if (fb_type == CssTypes.CSS_PERCENTAGE || fb_type == CssTypes.CSS_NUMBER) {
                            if ((computed_type == CssTypes.CSS_LENGTH) ||
                                    (computed_type == CssTypes.CSS_ANGLE) ||
                                    (computed_type == CssTypes.CSS_FREQUENCY) ||
                                    (computed_type == CssTypes.CSS_NUMBER) ||
                                    (computed_type == CssTypes.CSS_TIME)) {
                                break;
                            }
                        }
                        // else, invalid type
                        throw new InvalidParamException("typevaluemismatch",
                                fallback_value, value_type, ac);
                    }
                    break;
                default:
                    throw new InvalidParamException("operator",
                            Character.toString(op),
                            ac);
            }
            op = exp.getOperator();
            exp.next();
        }
    }

    private int _checkType(CssIdent ident)
            throws InvalidParamException {
        Integer t = typeMap.get(ident);
        if (t == null) {
            throw new InvalidParamException("invalidtype", ident.toString(), "attr()", ac);
        }
        return t;
    }


    /**
     * Returns the value
     */

    public Object get() {
        return toString();
    }


    public String toString() {
        if (_ts == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("attr(").append(value);
            if (value_type != null) {
                sb.append(' ').append(value_type);
            }
            if (fallback_value != null) {
                sb.append(", ").append(fallback_value);
            }
            sb.append(')');
            _ts = sb.toString();
        }
        return _ts;
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
        return (value instanceof CssAttr &&
                toString().equals(((CssAttr) value).toString()));
    }

    /**
     * check if the value is positive or null
     *
     * @param ac         the validation context
     * @param callername the property the value is defined in
     * @throws org.w3c.css.util.InvalidParamException
     *
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
     *
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
     *
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
     * @param ac       the validation context
     * @param property the property the value is defined in
     */
    public boolean warnPositiveness(ApplContext ac, CssProperty property) {
        // TODO do our best...
        if (false/*!isPositive()*/) {
            ac.getFrame().addWarning("negative", toString());
            return false;
        }
        return true;
    }

    public CssLength getLength() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_LENGTH) {
            // TODO fixme...
            // we might change this to CssCheckableValue instead.
        }
        throw new ClassCastException("unknown");
    }

    public CssPercentage getPercentage() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_PERCENTAGE) {
            // TODO
        }
        throw new ClassCastException("unknown");
    }

    public CssNumber getNumber() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_NUMBER) {
            //TODO
        }
        throw new ClassCastException("unknown");
    }

    public CssTime getTime() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_TIME) {
            //TODO
        }
        throw new ClassCastException("unknown");
    }

    public CssAngle getAngle() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_ANGLE) {
            //TODO
        }
        throw new ClassCastException("unknown");
    }

    public CssFrequency getFrequency() throws InvalidParamException {
        if (computed_type == CssTypes.CSS_FREQUENCY) {
            //TODO
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
            ac.getFrame().addWarning("dynamic", new String[]{toString(), callername});
            return false;
        }
        return true;
    }
}
