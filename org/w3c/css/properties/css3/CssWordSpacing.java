//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, INRIA and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-word-spacing
 */
public class CssWordSpacing extends org.w3c.css.properties.css.CssWordSpacing {

    private static CssIdent normal = CssIdent.getIdent("normal");

    /**
     * Create a new CssWordSpacing.
     */
    public CssWordSpacing() {
        value = initial;
    }

    /**
     * Create a new CssWordSpacing with an expression
     *
     * @param expression The expression
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssWordSpacing(ApplContext ac, CssExpression expression,
                          boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        ArrayList<CssValue> v = new ArrayList<CssValue>(4);
        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, this);
            case CssTypes.CSS_LENGTH:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent()) ||
                        normal.equals(val.getIdent())) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        if (op != CssOperator.SPACE) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        expression.next();
    }

    public CssWordSpacing(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}
