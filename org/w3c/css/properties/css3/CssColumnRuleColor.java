//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 1995-2018  World Wide Web Consortium (MIT, ERCIM and Keio)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-rule-color
 */

public class CssColumnRuleColor extends org.w3c.css.properties.css.CssColumnRuleColor {

    /**
     * Create a new CssColumnRuleColor
     */
    public CssColumnRuleColor() {
        value = initial;
    }

    /**
     * Create a new CssColumnRuleColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Incorrect value
     */
    public CssColumnRuleColor(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident)) {
                value = val;
                expression.next();
                return;
            }
        }

        try {
            // we use the latest version of CssColor, aka CSS3
            // instead of using CSS21 colors + transparent per spec
            CssColor tcolor = new CssColor(ac, expression, check);
            value = tcolor.getValue();
        } catch (InvalidParamException e) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }

    }

    public CssColumnRuleColor(ApplContext ac, CssExpression expression)
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
