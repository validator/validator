//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#the-mask-type
 */
public class CssMaskType extends org.w3c.css.properties.css.CssMaskType {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"luminance", "alpha"};
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
     * Create a new CssMaskType
     */
    public CssMaskType() {
        value = initial;
    }

    /**
     * Creates a new CssMaskType
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssMaskType(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        CssValue val;

        val = expression.getValue();

        switch (val.getType()) {
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
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();

    }

    public CssMaskType(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

