//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/CR-css-scroll-snap-1-20210311/#propdef-scroll-snap-stop
 */
public class CssScrollSnapStop extends org.w3c.css.properties.css.CssScrollSnapStop {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"normal", "always"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssScrollSnapStop
     */
    public CssScrollSnapStop() {
        value = initial;
    }

    /**
     * Creates a new CssScrollSnapStop
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssScrollSnapStop(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    break;
                }
                if (getMatchingIdent(ident) != null) {
                    value = val;
                    break;
                }
                // unrecognized... fail.
            default:
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssScrollSnapStop(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

