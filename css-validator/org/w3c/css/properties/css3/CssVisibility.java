// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-display-3-20210903/#propdef-visibility
 */
public class CssVisibility extends org.w3c.css.properties.css.CssVisibility {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"visible", "hidden", "collapse"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssVisibility
     */
    public CssVisibility() {
        value = initial;
    }

    /**
     * Creates a new CssVisibility
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssVisibility(ApplContext ac, CssExpression expression, boolean check)
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

    public CssVisibility(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

