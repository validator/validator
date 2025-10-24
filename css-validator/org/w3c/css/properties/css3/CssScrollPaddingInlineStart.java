//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2019/CR-css-scroll-snap-1-20190319/#propdef-scroll-padding-inline-start
 */
public class CssScrollPaddingInlineStart extends org.w3c.css.properties.css.CssScrollPaddingInlineStart {

    /**
     * Create a new CssScrollPaddingInlineStart
     */
    public CssScrollPaddingInlineStart() {
        value = initial;
    }

    /**
     * Creates a new CssScrollPaddingInlineStart
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssScrollPaddingInlineStart(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        value = CssScrollPadding.checkPaddingValue(ac, expression, this);

    }

    public CssScrollPaddingInlineStart(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

