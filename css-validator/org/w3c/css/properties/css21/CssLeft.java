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
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/visuren.html#propdef-left
 * @see org.w3c.css.properties.css21.CssTop
 */
public class CssLeft extends org.w3c.css.properties.css.CssLeft {

    /**
     * Create a new CssLeft
     */
    public CssLeft() {
    }

    /**
     * Creates a new CssLeft
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     * @see org.w3c.css.properties.css21.CssTop
     */
    public CssLeft(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssTop.checkValue(ac, expression, check, this);

    }

    public CssLeft(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

