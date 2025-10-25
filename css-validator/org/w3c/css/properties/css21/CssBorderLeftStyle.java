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
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/box.html#propdef-border-left-style
 * @see CssBorderStyle
 */
public class CssBorderLeftStyle extends org.w3c.css.properties.css.CssBorderLeftStyle {

    /**
     * Create a new CssBorderLeftStyle
     */
    public CssBorderLeftStyle() {
    }

    /**
     * Creates a new CssBorderLeftStyle
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderLeftStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        // here we delegate to BorderWidth implementation
        value = CssBorderStyle.checkBorderSideStyle(ac, this, expression, check);
    }

    public CssBorderLeftStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

