// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/ui.html#propdef-outline-color
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
                break;
            case CssTypes.CSS_HASH_IDENT:
                org.w3c.css.values.CssColor c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.toString());
                value = c;
                break;
            case CssTypes.CSS_IDENT:
                if (invert.equals(val)) {
                    value = invert;
                    break;
                }
                if (inherit.equals(val)) {
                    value = inherit;
                    break;
                }
                // else, we must parse the ident as a color value
                value = new org.w3c.css.values.CssColor(ac,
                        (String) val.get());
                break;
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssOutlineColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

