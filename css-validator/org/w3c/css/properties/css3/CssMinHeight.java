//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-3-20210317/#propdef-min-height
 */
public class CssMinHeight extends org.w3c.css.properties.css.CssMinHeight {

    /**
     * Create a new CssMinHeight
     */
    public CssMinHeight() {
        value = initial;
    }

    /**
     * Creates a new CssMinHeight
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMinHeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        value = CssMinWidth.parseMinWidth(ac, expression, this);
    }

    public CssMinHeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

