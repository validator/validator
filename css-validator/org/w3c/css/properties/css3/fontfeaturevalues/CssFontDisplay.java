//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontfeaturevalues;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2026/WD-css-fonts-4-20260422/#descdef-font-feature-values-font-display
 */
public class CssFontDisplay extends org.w3c.css.properties.css.fontfeaturevalues.CssFontDisplay {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"auto", "block", "swap", "fallback", "optional"};
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
     * Create a new CssFontDisplay
     */
    public CssFontDisplay() {
        value = initial;
    }

    /**
     * Creates a new CssFontDisplay
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontDisplay(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        char op;
        CssValue val;
        if (expression.getRemainingCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    value = val;
                    break;
                }
                if (getAllowedIdent(val.getIdent()) != null) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssFontDisplay(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

