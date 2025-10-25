// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#propdef-text-emphasis-color
 */
public class CssTextEmphasisColor extends org.w3c.css.properties.css.CssTextEmphasisColor {

    /**
     * Create a new CssTextEmphasisColor
     */
    public CssTextEmphasisColor() {
        value = initial;
    }

    /**
     * Creates a new CssTextEmphasisColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextEmphasisColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if ((val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
            value = val;
            expression.next();
        } else {
            try {
                CssColor tcolor = new CssColor(ac, expression, check);
                value = tcolor.getValue();
            } catch (InvalidParamException e) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
        }
    }

    public CssTextEmphasisColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

