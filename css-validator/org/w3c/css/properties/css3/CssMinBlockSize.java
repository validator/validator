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
 * @spec  https://drafts.csswg.org/css-logical-1/#propdef-min-block-size retrieved 20210723 (no comment...)
 */
public class CssMinBlockSize extends org.w3c.css.properties.css.CssMinBlockSize {

    /**
     * Create a new CssMinBlockSize
     */
    public CssMinBlockSize() {
        value = initial;
    }

    /**
     * Creates a new CssMinBlockSize
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssMinBlockSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = CssMinWidth.parseMinWidth(ac, expression, this);

    }

    public CssMinBlockSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

