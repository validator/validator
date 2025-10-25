//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2024.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-wrap-before
 */
public class CssWrapBefore extends org.w3c.css.properties.css.CssWrapBefore {
    /* See @CssWrapAfter */
    public static CssIdent getAllowedIdent(CssIdent ident) {
        return CssWrapAfter.getAllowedIdent(ident);
    }

    /**
     * Create a new CssWrapBefore
     */
    public CssWrapBefore() {
        value = initial;
    }

    /**
     * Creates a new CssWrapBefore
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssWrapBefore(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", val,
                    getPropertyName(), ac);
        }
        CssIdent id = val.getIdent();
        if (!CssIdent.isCssWide(id) && (getAllowedIdent(id) == null)) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssWrapBefore(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

