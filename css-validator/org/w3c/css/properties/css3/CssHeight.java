//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-3-20210317/#propdef-height
 */
public class CssHeight extends org.w3c.css.properties.css.CssHeight {

    /**
     * Create a new CssHeight
     */
    public CssHeight() {
        value = initial;
    }

    /**
     * Create a new CssHeight.
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssHeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        value = CssWidth.parseWidth(ac, expression, this);
    }

    public CssHeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return ((value == auto) || (value == initial));
    }

}
