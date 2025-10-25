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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-border-image-source
 */
public class CssBorderImageSource extends org.w3c.css.properties.css.CssBorderImageSource {


    public static boolean isMatchingIdent(CssIdent ident) {
        return none.equals(ident);
    }

    /**
     * Create a new CssBorderImageSource
     */
    public CssBorderImageSource() {
        value = initial;
    }

    /**
     * Creates a new CssBorderImageSource
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssBorderImageSource(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);

        }
        CssValue val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_URL:
            case CssTypes.CSS_IMAGE:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id) || isMatchingIdent(id)) {
                    value = val;
                    break;
                }
                // unrecognized ident... let it fail
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();

    }

    public CssBorderImageSource(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

