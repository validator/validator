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
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-rule-style
 */

public class CssColumnRuleStyle extends org.w3c.css.properties.css.CssColumnRuleStyle {

    /**
     * Create a new CssColumnRuleStyle
     */
    public CssColumnRuleStyle() {
        value = initial;
    }

    /**
     * Create a new CssColumnRuleStyle
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      if check on length is required
     * @throws org.w3c.css.util.InvalidParamException
     *          Incorrect value
     */
    public CssColumnRuleStyle(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        setByUser();
        value = CssBorderStyle.parseBorderSideStyle(ac, expression, check, this);
    }

    public CssColumnRuleStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return value == initial;
    }
}
