//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2018/WD-css-logical-1-20180827/#propdef-border-block-end-color
 */
public class CssBorderBlockEndColor extends org.w3c.css.properties.css.CssBorderBlockEndColor {

    /**
     * Create a new CssBorderBlockEndColor
     */
    public CssBorderBlockEndColor() {
        value = initial;
    }

    /**
     * Creates a new CssBorderBlockEndColor
     *
     * @param expression The expression for this property
     * @throws InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderBlockEndColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        value = CssBorderColor.parseBorderSideColor(ac, expression, check, this);
    }


    public CssBorderBlockEndColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

