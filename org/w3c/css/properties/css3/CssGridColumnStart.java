//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2020/CRD-css-grid-1-20201218/#propdef-grid-column-start
 */
public class CssGridColumnStart extends org.w3c.css.properties.css.CssGridColumnStart {

    /**
     * Create a new CssGridColumnStart
     */
    public CssGridColumnStart() {
        value = initial;
    }

    /**
     * Creates a new CssGridColumnStart
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssGridColumnStart(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssGridRowStart.checkGridLine(ac, expression, check, this);
    }

    public CssGridColumnStart(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

