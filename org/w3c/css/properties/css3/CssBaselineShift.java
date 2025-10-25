// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-inline-3-20200827/#propdef-baseline-shift
 */

public class CssBaselineShift extends org.w3c.css.properties.css.CssBaselineShift {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"sub", "super", "top", "center", "bottom"};
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
     * Creates a new CssBaselineShift
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssBaselineShift(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;

        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                // zero is a valid length. otherwise it will fail.
                val.getCheckableValue().checkEqualsZero(ac, this);
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    value = val;
                    break;
                }
                if (getAllowedIdent(id) != null) {
                    value = val;
                    break;
                }
                // unrecognized ident -> fail.
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }

    }


    public CssBaselineShift(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}
