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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-hyphenate-limit-zone
 */
public class CssHyphenateLimitZone extends org.w3c.css.properties.css.CssHyphenateLimitZone {

    /**
     * Create a new CssHyphenateLimitZone
     */
    public CssHyphenateLimitZone() {
        value = initial;
    }

    /**
     * Creates a new CssHyphenateLimitZone
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssHyphenateLimitZone(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    break;
                }
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            case CssTypes.CSS_NUMBER:
                CssCheckableValue p = val.getCheckableValue();
                p.checkEqualsZero(ac, this);
                // flow for other possible checks
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                value = val;
                break;
            default:
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);

        }
        expression.next();
    }

    public CssHyphenateLimitZone(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

