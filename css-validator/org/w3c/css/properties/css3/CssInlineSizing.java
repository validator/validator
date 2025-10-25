//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-inline-3-20200827/#propdef-inline-sizing
 */
public class CssInlineSizing extends org.w3c.css.properties.css.CssInlineSizing {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"normal", "stretch"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
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
     * Create a new CssInlineSizing
     */
    public CssInlineSizing() {
        value = initial;
    }

    /**
     * Creates a new CssInlineSizing
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssInlineSizing(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val = expression.getValue();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = val.getIdent();
            if (!CssIdent.isCssWide(ident) && getAllowedIdent(ident) == null) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            value = val;
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();

    }

    public CssInlineSizing(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

