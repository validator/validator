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
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-accent-color
 */
public class CssAccentColor extends org.w3c.css.properties.css.CssAccentColor {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    public static final CssIdent getMatchingIdent(CssIdent ident) {
        if (auto.equals(ident)) {
            return ident;
        }
        return null;
    }

    /**
     * Create a new CssAccentColor
     */
    public CssAccentColor() {
        value = initial;
    }

    /**
     * Creates a new CssAccentColor
     *
     * @param expression The expression for this property
     * @throws InvalidParamException
     *          Expressions are incorrect
     */
    public CssAccentColor(ApplContext ac, CssExpression expression, boolean check)
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
                value = (val.getRawType() == CssTypes.CSS_HASH_IDENT)? c : value;
                expression.next();
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (auto.equals(id)) {
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
                    if (value == null) {
                        value = val;
                    }
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

    public CssAccentColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

