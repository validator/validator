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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-wrap-after
 */
public class CssWrapAfter extends org.w3c.css.properties.css.CssWrapAfter {

    private static final CssIdent[] allowed_idents;


    static {
        String[] id_values = {"auto", "avoid", "avoid-line", "avoid-flex", "line", "flex"};
        allowed_idents = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_idents[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_idents) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssWrapAfter
     */
    public CssWrapAfter() {
        value = initial;
    }

    /**
     * Creates a new CssWrapAfter
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssWrapAfter(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", val,
                    getPropertyName(), ac);
        }
        CssIdent id = val.getIdent();
        if (!CssIdent.isCssWide(id) && (getAllowedIdent(id) == null)) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssWrapAfter(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

