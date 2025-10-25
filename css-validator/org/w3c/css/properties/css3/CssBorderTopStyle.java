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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-border-top-style
 * @see CssBorderStyle
 */
public class CssBorderTopStyle extends org.w3c.css.properties.css.CssBorderTopStyle {

    /**
     * Create a new CssBorderTopStyle
     */
    public CssBorderTopStyle() {
        value = initial;
    }

    /**
     * Creates a new CssBorderTopStyle
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderTopStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        // here we delegate to BorderWidth implementation
        value = CssBorderStyle.parseBorderSideStyle(ac, expression, check, this);
    }

    public CssBorderTopStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

