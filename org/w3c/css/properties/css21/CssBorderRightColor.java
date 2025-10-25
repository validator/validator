// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/box.html#propdef-border-right-color
 * @see CssBorderColor
 */
public class CssBorderRightColor extends org.w3c.css.properties.css.CssBorderRightColor {

    /**
     * Create a new CssBorderRightColor
     */
    public CssBorderRightColor() {
    }

    /**
     * Creates a new CssBorderRightColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderRightColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssBorderColor.checkBorderSideColor(ac, this, expression, check);
    }

    public CssBorderRightColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

