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
 * @spec https://www.w3.org/TR/2018/WD-css-logical-1-20180827/#propdef-border-start-start-radius
 */
public class CssBorderStartStartRadius extends org.w3c.css.properties.css.CssBorderStartStartRadius {

    /**
     * Create a new CssBorderStartStartRadius
     */
    public CssBorderStartStartRadius() {
        value = initial;
    }

    /**
     * Creates a new CssBorderStartStartRadius
     *
     * @param expression The expression for this property
     * @throws InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderStartStartRadius(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        value = CssBorderRadius.parseBorderCornerRadius(ac, expression, check, this);
    }


    public CssBorderStartStartRadius(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

