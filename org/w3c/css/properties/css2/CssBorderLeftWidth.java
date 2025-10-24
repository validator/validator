// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/box.html#propdef-border-left-width
 * @see CssBorderWidth
 */
public class CssBorderLeftWidth extends org.w3c.css.properties.css.CssBorderLeftWidth {

    /**
     * Create a new CssBorderLeftWidth
     */
    public CssBorderLeftWidth() {
    }

    /**
     * Creates a new CssBorderLeftWidth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderLeftWidth(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        // here we delegate to BorderWidth implementation
        value = CssBorderWidth.checkBorderSideWidth(ac, this, expression, check);
    }

    public CssBorderLeftWidth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

