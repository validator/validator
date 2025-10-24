//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#rest
 */
public class CssRest extends org.w3c.css.properties.css.CssRest {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"none", "x-weak", "weak", "medium", "strong", "x-strong"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssRest
     */
    public CssRest() {
        cssRestAfter = new CssRestAfter();
        cssRestBefore = new CssRestBefore();
        value = initial;
    }

    /**
     * Creates a new CssRest
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssRest(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        char op;

        cssRestBefore = new CssRestBefore();
        cssRestBefore.value = checkRestValue(ac, expression, this);
        if (expression.end()) {
            cssRestAfter = new CssRestAfter();
            cssRestAfter.value = cssRestBefore.value;
            value = cssRestBefore.value;
        } else {
            op = expression.getOperator();
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            cssRestAfter = new CssRestAfter();
            cssRestAfter.value = checkRestValue(ac, expression, this);
            if (((cssRestBefore.value.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(cssRestBefore.value.getIdent())) ||
                    ((cssRestAfter.value.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(cssRestAfter.value.getIdent()))) {
                throw new InvalidParamException("value",
                        cssRestBefore.value, getPropertyName(), ac);
            }
            ArrayList<CssValue> values = new ArrayList<CssValue>(2);
            values.add(cssRestBefore.value);
            values.add(cssRestAfter.value);
            value = new CssValueList(values);
        }
    }

    public CssRest(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    protected static CssValue checkRestValue(ApplContext ac, CssExpression expression,
                                             CssProperty caller)
            throws InvalidParamException {
        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_TIME:
                CssCheckableValue t = val.getCheckableValue();
                t.checkPositiveness(ac, caller);
                expression.next();
                return val;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    expression.next();
                    return val;
                }
                if (getAllowedIdent(id) != null) {
                    expression.next();
                    return val;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
        }
    }
}

