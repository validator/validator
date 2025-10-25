//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.viewport;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2016/WD-css-device-adapt-1-20160329/#descdef-viewport-orientation
 */
public class CssOrientation extends org.w3c.css.properties.css.viewport.CssOrientation {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"auto", "portrait", "landscape"};
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
     * Create a new CssOrientation
     */
    public CssOrientation() {
        value = initial;  // this is wrong...
    }

    /**
     * Creates a new CssOrientation
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssOrientation(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        setByUser();

        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        val = expression.getValue();
        if (val.getType() == CssTypes.CSS_IDENT) {
            value = getAllowedIdent((CssIdent) val);
            if (value == null) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();

    }

    public CssOrientation(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

