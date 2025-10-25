//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.properties.css3.CssBackgroundRepeat;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#the-mask-repeat
 */
public class CssMaskRepeat extends org.w3c.css.properties.css.CssMaskRepeat {

    /**
     * Create a new CssMaskRepeat
     */
    public CssMaskRepeat() {
        value = initial;
    }

    /**
     * Creates a new CssMaskRepeat
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMaskRepeat(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        value = CssBackgroundRepeat.checkBackgroundRepeat(ac, expression, this);
    }

    public CssMaskRepeat(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

