//
// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssAngle;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#propdef-elevation
 */
public class CssElevation extends org.w3c.css.properties.css.CssElevation {
    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"below", "level", "above",
                "higher", "lower"};
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
     * Create a new CssElevation
     */
    public CssElevation() {
    }

    /**
     * Creates a new ACssElevation
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssElevation(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();
        setByUser();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_ANGLE:
                CssAngle a = val.getAngle();
                float v = a.getDegree();
                if (v > 90 && v < 270) {
                    throw new InvalidParamException("elevation.range", ac);
                }
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ident = (CssIdent) val;
                if (inherit.equals(ident)) {
                    value = inherit;
                    break;
                }
                value = getAllowedIdent(ident);
                if (value != null) {
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssElevation(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

