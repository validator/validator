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

import java.util.Arrays;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-stretch
 */
public class CssFontStretch extends org.w3c.css.properties.css.CssFontStretch {

    static final String[] _allowed_values = {"normal", "ultra-condensed",
            "extra-condensed", "condensed", "semi-condensed", "semi-expanded",
            "expanded", "extra-expanded", "ultra-expanded"};
    static final CssIdent[] allowed_values;

    static {
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
        value = initial;
    }

    /**
     * Creates a new CssFontStretch
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
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

        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    value = val;
                    break;
                }
                if (getAllowedValue(id) != null) {
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

    public CssFontStretch(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

