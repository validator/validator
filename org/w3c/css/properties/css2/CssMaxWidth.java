// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/visudet.html#propdef-max-width
 */
public class CssMaxWidth extends org.w3c.css.properties.css.CssMaxWidth {

    /**
     * Create a new CssMaxWidth
     */
    public CssMaxWidth() {
    }

    /**
     * Creates a new CssMaxWidth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMaxWidth(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_LENGTH:
                CssLength length = val.getLength();
                length.checkPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_PERCENTAGE:
                CssPercentage percentage = val.getPercentage();
                percentage.checkPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (inherit.equals(val)) {
                    value = inherit;
                    break;
                }
                if (none.equals(val)) {
                    value = none;
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssMaxWidth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

