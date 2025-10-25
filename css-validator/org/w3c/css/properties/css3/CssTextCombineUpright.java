//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio and Beihang University, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2019/CR-css-writing-modes-4-20190730/#propdef-text-combine-upright
 */
public class CssTextCombineUpright extends org.w3c.css.properties.css.CssTextCombineUpright {

    public static final CssIdent[] allowed_values;
    public static final CssIdent digits;

    static {
        digits = CssIdent.getIdent("digits");
        String[] _allowed_values = {"all", "none", "digits"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    static CssIdent all;

    static {
        all = CssIdent.getIdent("all");
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
     * Create a new CssTextCombineUpright
     */
    public CssTextCombineUpright() {
        value = initial;
    }

    /**
     * Creates a new CssTextCombineUpright
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextCombineUpright(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();
        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        if (CssIdent.isCssWide(val.getIdent())) {
            value = val;
        } else {
            value = getAllowedIdent(val.getIdent());
            if (value == null) {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }
            if ((digits != value) && (expression.getCount() > 1)) {
                throw new InvalidParamException("unrecognize", ac);
            }
            if (digits == value) {
                if (expression.getCount() > 1) {
                    char op = expression.getOperator();
                    if (op != CssOperator.SPACE) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                    expression.next();
                    val = expression.getValue();
                    if (val.getType() != CssTypes.CSS_NUMBER) {
                        throw new InvalidParamException("value", expression.getValue(),
                                getPropertyName(), ac);
                    }
                    // we got a number.
                    val.getCheckableValue().checkInteger(ac, this);
                    val.getCheckableValue().checkStrictPositiveness(ac, this);
                    // in fact the restricted set of values is smaller than that.
                    // TODO check the interval.
                    // now rewrite the value
                    ArrayList<CssValue> v = new ArrayList<>();
                    v.add(value);
                    v.add(val);
                    value = new CssValueList(v);
                }
            }
        }
        expression.next();
    }

    public CssTextCombineUpright(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

