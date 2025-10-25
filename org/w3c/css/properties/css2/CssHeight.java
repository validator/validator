// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/visudet.html#propdef-height
 */
public class CssHeight extends org.w3c.css.properties.css.CssHeight {

    /**
     * Create a new CssHeight
     */
    public CssHeight() {
    }

    /**
     * Create a new CssHeight.
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssHeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = (CssIdent) val;
                if (inherit.equals(val)) {
                    value = inherit;
                } else if (auto.equals(val)) {
                    value = auto;
                } else {
                    throw new InvalidParamException("unrecognize", ac);
                }
                break;
            case CssTypes.CSS_NUMBER:
                val = ((CssNumber) val).getLength();
            case CssTypes.CSS_LENGTH:
                CssLength l = (CssLength) val;
                if (!l.isPositive()) {
                    throw new InvalidParamException("negative-value",
                            val.toString(), ac);
                }
                value = l;
                break;
            case CssTypes.CSS_PERCENTAGE:
                CssPercentage p = (CssPercentage) val;
                if (!p.isPositive()) {
                    throw new InvalidParamException("negative-value",
                            val.toString(), ac);
                }
                value = p;
                break;
            default:
                throw new InvalidParamException("value", val, getPropertyName(), ac);
        }
        expression.next();
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
        return value == auto;
    }

}
