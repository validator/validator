//
// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
//   Rewritten entirely 2021 by Yves Lafon <ylafon@w3.org>
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

/**
 * @spec https://www.w3.org/TR/2021/WD-css-ruby-1-20210310/#propdef-ruby-overhang
 */

public class CssRubyOverhang extends org.w3c.css.properties.css.CssRubyOverhang {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"auto", "none"};
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
     * Create a new CssRubyOverhang
     */
    public CssRubyOverhang() {
        value = initial;
    }

    /**
     * Create a new CssRubyOverhang
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssRubyOverhang(ApplContext ac, CssExpression expression,
                           boolean check) throws InvalidParamException {
        CssValue val;
        setByUser();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        val = expression.getValue();
        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val, getPropertyName(), ac);
        }
        // ident, so inherit, or allowed value
        CssIdent ident = val.getIdent();
        if (!CssIdent.isCssWide(ident) && (getMatchingIdent(ident) == null)) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssRubyOverhang(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}
