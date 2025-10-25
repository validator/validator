//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-3-20210317/#propdef-width
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-4-20210520/#sizing-values
 */
public class CssWidth extends org.w3c.css.properties.css.CssWidth {

    public static final CssIdent[] allowed_values;
    public static final String fit_content_func = "fit-content";

    static {
        String[] _allowed_values = {"auto", "max-content", "min-content",
                // following from sizing-4
                "stretch", "fit-content", "contain"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssWidth
     */
    public CssWidth() {
        value = initial;
    }

    /**
     * Create a new CssWidth.
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssWidth(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        value = parseWidth(ac, expression, this);
    }

    public CssWidth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return ((value == auto) || (value == initial));
    }

    public static final CssValue parseWidth(ApplContext ac, CssExpression expression,
                                            CssProperty caller)
            throws InvalidParamException {
        CssValue v = null;
        CssValue val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    v = val;
                } else {
                    if (getAllowedIdent(id) != null) {
                        v = val;
                    } else {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                }
                break;
            case CssTypes.CSS_FUNCTION:
                v = parseFunctionValue(ac, val, caller);
                break;
            case CssTypes.CSS_NUMBER:
                // only 0 can be a length...
                CssCheckableValue p = val.getCheckableValue();
                p.checkEqualsZero(ac, caller);
                v = val;
                break;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                p = val.getCheckableValue();
                p.checkPositiveness(ac, caller);
                v = val;
                break;
            default:
                throw new InvalidParamException("value", val,
                        caller.getPropertyName(), ac);
        }
        expression.next();
        return v;
    }

    protected static CssValue parseFunctionValue(ApplContext ac, CssValue value,
                                                 CssProperty caller)
            throws InvalidParamException {
        // FIXME cast
        CssFunction function = value.getFunction();
        if (!fit_content_func.equalsIgnoreCase(function.getName())) {
            throw new InvalidParamException("value", value.toString(),
                    caller.getPropertyName(), ac);
        }
        CssExpression expression = function.getParameters();
        // don't check in that case
        if (expression.hasCssVariable()) {
            return value;
        }
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, caller);
                break;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                CssCheckableValue l = val.getCheckableValue();
                l.checkPositiveness(ac, caller);
                break;
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        caller.getPropertyName(), ac);
        }
        return value;
    }
}
