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
 * @spec https://www.w3.org/TR/2025/WD-css-overflow-3-20251007/#propdef-overflow-block
 * @see CssOverflow
 */
public class CssOverflowBlock extends org.w3c.css.properties.css.CssOverflowBlock {

    /**
     * Create a new CssOverflowBlock
     */
    public CssOverflowBlock() {
        value = initial;
    }

    /**
     * Creates a new CssOverflowBlock
     *
     * @param expression The expression for this property
     * @throws InvalidParamException
     *          Expressions are incorrect
     */
    public CssOverflowBlock(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        value = CssOverflow.checkOverflowAxis(ac, expression, check, this);
    }

    public CssOverflowBlock(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

