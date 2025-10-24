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
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-weight
 */
public class CssFontWeight extends org.w3c.css.properties.css.CssFontWeight {

    public static final CssIdent[] allowed_values;
    public static final CssIdent[] absolute_values;

    static {
        String[] _absolute_values = {"normal", "bold"};
        String[] _non_absolute_values = {"bolder", "lighter"};
        int i = 0;
        absolute_values = new CssIdent[_absolute_values.length];
        for (String s : _absolute_values) {
            absolute_values[i++] = CssIdent.getIdent(s);
        }
        i = 0;
        // here we concatenate the two
        allowed_values = new CssIdent[_absolute_values.length + _non_absolute_values.length];
        for (String s : _absolute_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        for (String s : _non_absolute_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedAbsoluteValue(CssIdent ident) {
        for (CssIdent id : absolute_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssFontWeight
     */
    public CssFontWeight() {
        value = initial;
    }

    /**
     * Creates a new CssFontWeight
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontWeight(ApplContext ac, CssExpression expression, boolean check)
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
            case CssTypes.CSS_NUMBER:
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    CssNumber num = val.getNumber();
                    num.checkGreaterEqualThan(ac, 1, this);
                    num.checkLowerEqualThan(ac, 1000, this);
                } else {
                    // can at least check it is positive
                    val.getCheckableValue().checkStrictPositiveness(ac, getPropertyName());
                }
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id) || (getAllowedValue(id) != null)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(), getPropertyName(), ac);
        }
        expression.next();
    }

    public CssFontWeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

