//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.viewport;

import org.w3c.css.atrules.css.AtRuleViewport;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2016/WD-css-device-adapt-1-20160329/#descdef-viewport-max-height
 */
public class CssMaxHeight extends org.w3c.css.properties.css.viewport.CssMaxHeight {

    /**
     * Create a new CssMaxHeight
     */
    public CssMaxHeight() {
        value = initial;  // this is wrong...
    }

    /**
     * Creates a new CssMaxHeight
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMaxHeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        setByUser();

        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        val = expression.getValue();
        value = AtRuleViewport.checkViewportLenght(val, this, ac);

        expression.next();

    }

    public CssMaxHeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

