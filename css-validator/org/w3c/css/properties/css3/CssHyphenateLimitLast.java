//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2024.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-hyphenate-limit-zone
 */
public class CssHyphenateLimitLast extends org.w3c.css.properties.css.CssHyphenateLimitZone {
    private final static CssIdent[] allowed_values;

    static {
        String[] id_values = {"none", "always", "column", "page", "spread"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssHyphenateLimitZone
     */
    public CssHyphenateLimitLast() {
        value = initial;
    }

    /**
     * Creates a new CssHyphenateLimitZone
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssHyphenateLimitLast(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }

        CssIdent ident = val.getIdent();
        if (CssIdent.isCssWide(ident)) {
            value = val;
        } else if (getAllowedIdent(ident) != null) {
            value = val;
        } else {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssHyphenateLimitLast(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

