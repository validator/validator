//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, INRIA, Keio University, Beihang, 2011
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-letter-spacing
 */
public class CssLetterSpacing extends org.w3c.css.properties.css.CssLetterSpacing {

    private static CssIdent normal = CssIdent.getIdent("normal");

    /**
     * Create a new CssLetterSpacing.
     */
    public CssLetterSpacing() {
        value = initial;
    }

    /**
     * Create a new CssLetterSpacing with an expression
     *
     * @param expression The expression
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssLetterSpacing(ApplContext ac, CssExpression expression,
                            boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();


        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, this);
            case CssTypes.CSS_LENGTH:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    value = val;
                    break;
                } else if (normal.equals(id)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();

    }

    public CssLetterSpacing(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}
