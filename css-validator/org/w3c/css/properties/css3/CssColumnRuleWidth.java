//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten Yves lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM and Keio)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-rule-width
 */

public class CssColumnRuleWidth extends org.w3c.css.properties.css.CssColumnRuleWidth {

    /**
     * Create a new CssColumnRuleWidth
     */
    public CssColumnRuleWidth() {
        value = initial;
        // nothing to do
    }

    /**
     * Create a new CssColumnRuleWidth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Incorrect value
     */
    public CssColumnRuleWidth(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        setByUser();
        value = CssBorderWidth.parseBorderSideWidth(ac, expression, check, this);
    }

    public CssColumnRuleWidth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (initial == value);
    }

}
