// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2012 by Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 1995-2012  World Wide Web Consortium (MIT, ERCIM, Keio University, Beihang)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2018/CR-css-break-3-20181204/#propdef-box-decoration-break
 */

public class CssBoxDecorationBreak extends org.w3c.css.properties.css.CssBoxDecorationBreak {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"slice", "clone"};
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
     * Create new CssBoxDecorationBreak
     */
    public CssBoxDecorationBreak() {
        value = initial;
    }

    /**
     * Create new CssBoxDecorationBreak
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssBoxDecorationBreak(ApplContext ac, CssExpression expression,
                                 boolean check) throws InvalidParamException {
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


    public CssBoxDecorationBreak(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}
