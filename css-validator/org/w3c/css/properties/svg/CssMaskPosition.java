//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.properties.css3.CssBackgroundPosition;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#the-mask-position
 */
public class CssMaskPosition extends org.w3c.css.properties.css.CssMaskPosition {

    /**
     * Create a new CssMaskPosition
     */
    public CssMaskPosition() {
        value = initial;
    }

    /**
     * Creates a new CssMaskPosition
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMaskPosition(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        value = CssBackgroundPosition.checkBackgroundPosition(ac, expression, this);
    }

    public CssMaskPosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

