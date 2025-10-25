//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2020.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-color-adjust-1-20210616/#propdef-forced-color-adjust
 */
public class CssForcedColorAdjust extends org.w3c.css.properties.css.CssForcedColorAdjust {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"auto", "none", "preserve-parent-color"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssForcedColorAdjust
     */
    public CssForcedColorAdjust() {
        value = initial;
    }

    /**
     * Creates a new CssForcedColorAdjust
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssForcedColorAdjust(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        // ident, so inherit, or allowed value
        CssIdent ident = val.getIdent();
        if (CssIdent.isCssWide(ident)) {
            value = val;
        } else {
            if (getMatchingIdent(ident) == null) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            value = val;
        }
        expression.next();
    }

    public CssForcedColorAdjust(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

