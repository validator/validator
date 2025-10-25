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
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#propdef-speak-numeral
 * @deprecated
 */
@Deprecated
public class CssSpeakNumeral extends org.w3c.css.properties.css.CssSpeakNumeral {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"digits", "continuous"};
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
     * Create a new CssSpeakNumeral
     */
    public CssSpeakNumeral() {
        value = initial;
    }

    /**
     * Creates a new CssSpeakNumeral
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssSpeakNumeral(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        // same as CSS21 plus a warning
        ac.getFrame().addWarning("deprecatedproperty", getPropertyName());

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (inherit.equals(id)) {
                value = inherit;
            } else {
                value = getAllowedIdent(id);
                if (value == null) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssSpeakNumeral(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

