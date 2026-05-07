//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://drafts.csswg.org/css-viewport/#zoom-property dated 2025-12-15
 */
public class CssZoom extends org.w3c.css.properties.css.CssZoom {

    /**
     * Create a new CssZoom
     */
    public CssZoom() {
        value = initial;
    }

    /**
     * Creates a new CssZoom
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssZoom(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
                val.getCheckableValue().checkPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                // TODO handle 'normal' and 'reset' per https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/zoom#values
                // deprecated? warning? error with specific message?
                if (CssIdent.isCssWide(val.getIdent())) {
                    value = val;
                }
                break;
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssZoom(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

