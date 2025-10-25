// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-border-right-style
 * @see CssBorderStyle
 */
public class CssBorderRightStyle extends org.w3c.css.properties.css.CssBorderRightStyle {

    /**
     * Create a new CssBorderRightStyle
     */
    public CssBorderRightStyle() {
        value = initial;
    }

    /**
     * Creates a new CssBorderRightStyle
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderRightStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        // here we delegate to BorderWidth implementation
        value = CssBorderStyle.parseBorderSideStyle(ac, expression, check, this);
    }

    public CssBorderRightStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

