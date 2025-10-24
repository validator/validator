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
 * @spec https://drafts.csswg.org/css-logical-1/#propdef-min-block-size retrieved 20210723 (no comment...)
 */
public class CssMinInlineSize extends org.w3c.css.properties.css.CssMinInlineSize {

    /**
     * Create a new CssMinInlineSize
     */
    public CssMinInlineSize() {
        value = initial;
    }

    /**
     * Creates a new CssMinInlineSize
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssMinInlineSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = CssMinWidth.parseMinWidth(ac, expression, this);

    }

    public CssMinInlineSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

