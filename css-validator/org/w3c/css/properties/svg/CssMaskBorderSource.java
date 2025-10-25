//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#propdef-mask-border-source
 */
public class CssMaskBorderSource extends org.w3c.css.properties.css.CssMaskBorderSource {

    public static CssIdent getAllowedIdent(CssIdent ident) {
        if (none.equals(ident)) {
            return none;
        }
        return null;
    }


    /**
     * Create a new CssMaskBorderSource
     */
    public CssMaskBorderSource() {
        value = initial;
    }

    /**
     * Creates a new CssMaskBorderSource
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssMaskBorderSource(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        CssValue val;
        char op;

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_URL:
            case CssTypes.CSS_IMAGE:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    value = val;
                    break;
                }
                if (getAllowedIdent(id) != null) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
        if (op != SPACE) {
            throw new InvalidParamException("operator",
                    Character.toString(op),
                    ac);
        }
    }

    public CssMaskBorderSource(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

