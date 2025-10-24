// 
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2024.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-tab-size
 */
public class CssTabSize extends org.w3c.css.properties.css.CssTabSize {

    /**
     * Create a new CssTabSize
     */
    public CssTabSize() {
        value = initial;
    }

    /**
     * Creates a new CssTabSize
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssTabSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                CssCheckableValue number = val.getCheckableValue();
                number.checkInteger(ac, this);
                number.checkPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_LENGTH:
                CssCheckableValue l = val.getCheckableValue();
                l.checkPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val, getPropertyName(), ac);
        }
        expression.next();
    }

    public CssTabSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

