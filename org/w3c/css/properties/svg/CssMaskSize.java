//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.properties.css3.CssBackgroundSize;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#the-mask-size
 */
public class CssMaskSize extends org.w3c.css.properties.css.CssMaskSize {

    /**
     * Create a new CssMaskSize
     */
    public CssMaskSize() {
        value = initial;
    }

    /**
     * Creates a new CssMaskSize
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMaskSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        value = CssBackgroundSize.checkBackgroundSize(ac, expression, this);
    }

    public CssMaskSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

