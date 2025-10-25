//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#StrokeLinecapProperty
 */
public class CssStrokeLinecap extends org.w3c.css.properties.css.CssStrokeLinecap {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"butt", "round", "square"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
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
     * Create a new CssStrokeLinecap
     */
    public CssStrokeLinecap() {
        value = initial;
    }

    /**
     * Creates a new CssStrokeLinecap
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssStrokeLinecap(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        // ident, so inherit, or allowed value
        CssIdent id = val.getIdent();
        if (!CssIdent.isCssWide(id) && getAllowedIdent(id) == null) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssStrokeLinecap(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

