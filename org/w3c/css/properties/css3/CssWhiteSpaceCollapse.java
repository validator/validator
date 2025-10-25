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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-white-space-collapse
 */
public class CssWhiteSpaceCollapse extends org.w3c.css.properties.css.CssWhiteSpaceCollapse {

    private static final CssIdent[] allowed_idents;


    static {
        String[] id_values = {"collapse", "discard", "preserve", "preserve-breaks", "preserve-spaces", "break-spaces"};
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
     * Create a new CssWhiteSpaceCollapse
     */
    public CssWhiteSpaceCollapse() {
        value = initial;
    }

    /**
     * Creates a new CssWhiteSpaceCollapse
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssWhiteSpaceCollapse(ApplContext ac, CssExpression expression, boolean check)
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

    public CssWhiteSpaceCollapse(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

