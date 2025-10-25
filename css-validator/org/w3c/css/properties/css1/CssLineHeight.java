// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css1;

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
 * @spec http://www.w3.org/TR/2008/REC-CSS1-20080411/#line-height
 */
public class CssLineHeight extends org.w3c.css.properties.css.CssLineHeight {

    public static final CssIdent normal = CssIdent.getIdent("normal");

    /**
     * Create a new CssLineHeight
     */
    public CssLineHeight() {
    }

    /**
     * Creates a new CssLineHeight
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssLineHeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                if (normal.equals(val)) {
                    value = normal;
                } else {
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
                }
                break;
            case CssTypes.CSS_LENGTH:
                CssLength length = (CssLength) val;
                if (!length.isPositive()) {
                    throw new InvalidParamException("negative-value",
                            val.toString(), ac);
                }
                value = val;
                break;
            case CssTypes.CSS_NUMBER:
                CssNumber number = (CssNumber) val;
                if (!number.isPositive()) {
                    throw new InvalidParamException("negative-value",
                            val.toString(), ac);
                }
                value = val;
                break;
            case CssTypes.CSS_PERCENTAGE:
                CssPercentage percent = (CssPercentage) val;
                if (!percent.isPositive()) {
                    throw new InvalidParamException("negative-value",
                            val.toString(), ac);
                }
                value = val;
                break;
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssLineHeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

