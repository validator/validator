//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-4-20210520/#intrinsic-size-override
 * @spec https://drafts.csswg.org/css-sizing-4/#intrinsic-size-override may 12, 2026
 * @see org.w3c.css.properties.css3.CssContainIntrinsicHeight
 */
public class CssContainIntrinsicHeight extends org.w3c.css.properties.css.CssContainIntrinsicHeight {

    /**
     * Create a new CssContainIntrinsicHeight
     */
    public CssContainIntrinsicHeight() {
        value = initial;
    }

    /**
     * Creates a new CssContainIntrinsicHeight
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssContainIntrinsicHeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        value = CssContainIntrinsicSize.parseContainIntrinsic(ac, expression, this);
    }

    public CssContainIntrinsicHeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

