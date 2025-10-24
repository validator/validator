//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-cascade-4-20210319/#propdef-all
 */
public class CssAll extends org.w3c.css.properties.css.CssAll {


    /**
     * Create a new CssAll
     */
    public CssAll() {
        value = initial;
    }

    /**
     * Creates a new CssAll
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssAll(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val = expression.getValue();
        char op = expression.getOperator();

        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if ((val.getType() != CssTypes.CSS_IDENT) || !CssIdent.isCssWide(val.getIdent())) {
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssAll(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

