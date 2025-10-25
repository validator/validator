// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2018/WD-filter-effects-1-20181218/#propdef-lighting-color
 */
public class CssLightingColor extends org.w3c.css.properties.css.CssLightingColor {

    /**
     * Create a new CssLightingColor
     */
    public CssLightingColor() {
        value = initial;
    }

    /**
     * Creates a new CssLightingColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssLightingColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val = expression.getValue();

        if ((val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
            value = val;
            expression.next();
        } else {
            try {
                CssColor tcolor = new CssColor(ac, expression, check);
                // instead of using getColor, we get the value directly
                // as we can have idents
                value = tcolor.getValue();
            } catch (InvalidParamException e) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
        }
    }

    public CssLightingColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

