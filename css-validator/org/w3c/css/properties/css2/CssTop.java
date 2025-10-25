// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/visuren.html#propdef-top
 * @see CssBottom
 * @see CssLeft
 * @see CssRight
 */
public class CssTop extends org.w3c.css.properties.css.CssTop {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssTop
     */
    public CssTop() {
    }

    /**
     * Creates a new CssTop
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssTop(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = checkValue(ac, expression, check, this);
    }

    public CssTop(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * for use by other box properties
     *
     * @see CssBottom
     * @see CssLeft
     * @see CssRight
     */
    protected static CssValue checkValue(ApplContext ac,
                                         CssExpression expression,
                                         boolean check, CssProperty caller)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        caller.setByUser();
        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                CssLength l = val.getLength();
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                expression.next();
                return val;
            case CssTypes.CSS_IDENT:
                if (inherit.equals(val)) {
                    expression.next();
                    return inherit;
                }
                if (auto.equals(val)) {
                    expression.next();
                    return auto;
                }
                // if not inherit, or not an ident
                // let it flow to the exception
        }
        throw new InvalidParamException("value",
                val.toString(),
                caller.getPropertyName(), ac);
    }

}

