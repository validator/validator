//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-caret-color
 */
public class CssCaretColor extends org.w3c.css.properties.css.CssCaretColor {

    private static CssIdent auto = CssIdent.getIdent("auto");


    public static CssIdent getMatchingIdent(CssIdent ident) {
        if (auto.equals(ident)) {
            return auto;
        }
        return null;
    }

    /**
     * Create a new CssCaretColor
     */
    public CssCaretColor() {
        value = initial;
    }

    /**
     * Creates a new CssCaretColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssCaretColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    break;
                }
                if (getMatchingIdent(ident) != null) {
                    value = val;
                    break;
                }
                // if not recognized... it can be a color.
            default:
                try {
                    CssColor tcolor = new CssColor(ac, expression, check);
                    // instead of using getColor, we get the value directly
                    // as we can have idents
                    value = tcolor.getValue();
                } catch (InvalidParamException e) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
        }
        expression.next();
    }

    public CssCaretColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

