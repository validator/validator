// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css1;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 * @spec http://www.w3.org/TR/2008/REC-CSS1-20080411/#height
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
     * @throws InvalidParamException Values are incorrect
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
                if (auto.equals(val)) {
                    value = auto;
                } else {
                    throw new InvalidParamException("unrecognize", ac);
                }
                break;
            case CssTypes.CSS_NUMBER:
                val.getLength();
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                CssCheckableValue l = val.getCheckableValue();
                l.checkPositiveness(ac, this);
                value = val;
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
