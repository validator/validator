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
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-outline-color
 */
public class CssOutlineColor extends org.w3c.css.properties.css.CssOutlineColor {

    public static final CssIdent invert = CssIdent.getIdent("invert");

    public static final CssIdent getMatchingIdent(CssIdent ident) {
        if (invert.equals(ident)) {
            return ident;
        }
        return null;
    }

    /**
     * Create a new CssOutlineColor
     */
    public CssOutlineColor() {
        value = initial;
    }

    /**
     * Creates a new CssOutlineColor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssOutlineColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_COLOR:
                value = val;
                expression.next();
                break;
            case CssTypes.CSS_HASH_IDENT:
                org.w3c.css.values.CssColor c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.getHashIdent().toString());
                value = val;
                expression.next();
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (invert.equals(id)) {
                    value = val;
                    expression.next();
                    break;
                }
                if (CssIdent.isCssWide(id)) {
                    value = val;
                    expression.next();
                    break;
                }
                // else, we must parse as a color value
                // colors might be a function (for now)
            case CssTypes.CSS_FUNCTION:
                try {
                    CssColor tcolor = new CssColor(ac, expression, check);
                    value = tcolor.getColor();
                    break;
                } catch (InvalidParamException e) {
                    // we recreate the exception, as it will have
                    // the wrong property name otherwise
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
    }

    public CssOutlineColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

