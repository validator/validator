// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/fonts.html#propdef-font-size-adjust
 */
public class CssFontSizeAdjust extends org.w3c.css.properties.css.CssFontSizeAdjust {

    /**
     * Create a new CssFontSizeAdjust
     */
    public CssFontSizeAdjust() {
    }

    /**
     * Creates a new CssFontSizeAdjust
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFontSizeAdjust(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent id = (CssIdent) val;
                if (inherit.equals(id)) {
                    value = inherit;
                } else if (none.equals(id)) {
                    value = none;
                } else {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                break;
            case CssTypes.CSS_NUMBER:
                value = val;
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();

    }

    public CssFontSizeAdjust(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

