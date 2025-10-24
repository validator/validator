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
 * @spec https://drafts.csswg.org/css-logical-1/#propdef-max-inline-size retrieved 20210723 (no comment...)
 */
public class CssMaxInlineSize extends org.w3c.css.properties.css.CssMinInlineSize {

    /**
     * Create a new CssMaxInlineSize
     */
    public CssMaxInlineSize() {
        value = initial;
    }

    /**
     * Creates a new CssMaxInlineSize
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssMaxInlineSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = CssMaxWidth.parseMaxWidth(ac, expression, this);

    }

    public CssMaxInlineSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

