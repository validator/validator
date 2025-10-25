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

import java.util.Arrays;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/fonts.html#propdef-font-stretch
 */
public class CssFontStretch extends org.w3c.css.properties.css.CssFontStretch {

    static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"normal", "wider", "narrower",
                "ultra-condensed", "extra-condensed", "condensed", "semi-condensed",
                "semi-expanded", "expanded", "extra-expanded", "ultra-expanded"};
        allowed_values = new CssIdent[_allowed_values.length];
        for (int i = 0; i < allowed_values.length; i++) {
            allowed_values[i] = CssIdent.getIdent(_allowed_values[i]);
        }
        Arrays.sort(allowed_values);
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        int idx = Arrays.binarySearch(allowed_values, ident);
        if (idx >= 0) {
            return allowed_values[idx];
        }
        return null;
    }

    /**
     * Create a new CssFontStretch
     */
    public CssFontStretch() {
    }

    /**
     * Creates a new CssFontStretch
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFontStretch(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = (CssIdent) val;
            if (inherit.equals(ident)) {
                value = inherit;
            } else {
                value = getAllowedValue(ident);
                if (value == null) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssFontStretch(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

