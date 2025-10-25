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
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @pec http://www.w3.org/TR/2008/REC-CSS2-20080411/aural.html#volume-props
 */
public class CssVolume extends org.w3c.css.properties.css.CssVolume {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"silent", "x-soft", "soft", "medium", "loud", "x-loud"};
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
     * Create a new CssVolume
     */
    public CssVolume() {
    }

    /**
     * Creates a new CssVolume
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssVolume(ApplContext ac, CssExpression expression, boolean check)
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
                CssNumber n = val.getNumber();
                n.warnPositiveness(ac, this);
                n.warnLowerEqualThan(ac, 100, this);
                // FIXME clip to 100
                value = val;
                break;
            case CssTypes.CSS_PERCENTAGE:
                CssPercentage p = val.getPercentage();
                p.warnPositiveness(ac, this);
                p.warnLowerEqualThan(ac, 100, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = (CssIdent) val;
                if (inherit.equals(id)) {
                    value = inherit;
                    break;
                } else {
                    value = getAllowedIdent(id);
                    if (value != null) {
                        break;
                    }
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssVolume(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

