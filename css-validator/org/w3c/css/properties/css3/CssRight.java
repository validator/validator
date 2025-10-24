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
 * @spec https://www.w3.org/TR/2020/WD-css-position-3-20200519/#propdef-right
 * @see org.w3c.css.properties.css3.CssTop
 */
public class CssRight extends org.w3c.css.properties.css.CssRight {

    /**
     * Create a new CssRight
     */
    public CssRight() {
        value = initial;
    }

    /**
     * Creates a new CssRight
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     * @see org.w3c.css.properties.css3.CssTop
     */
    public CssRight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssTop.parseTop(ac, expression, check, this);

    }

    public CssRight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

