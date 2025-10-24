// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/ui.html#propdef-outline-style
 * @see org.w3c.css.properties.css2.CssBorderStyle
 */
public class CssOutlineStyle extends org.w3c.css.properties.css.CssOutlineStyle {

    static final CssIdent hidden = CssIdent.getIdent("hidden");

    /**
     * Create a new CssOutlineStyle
     */
    public CssOutlineStyle() {
    }

    /**
     * Creates a new CssOutlineStyle
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssOutlineStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        // here we delegate to BorderStyle implementation
        value = CssBorderStyle.checkBorderSideStyle(ac, this, expression, check);
        // but hidden is not a valid value...
        if (hidden.equals(value)) {
            throw new InvalidParamException("value", hidden,
                    getPropertyName(), ac);
        }
    }

    public CssOutlineStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

