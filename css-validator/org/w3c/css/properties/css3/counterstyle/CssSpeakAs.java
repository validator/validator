//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.counterstyle;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssIdent.isCssWide;

/**
 * @spec https://www.w3.org/TR/2015/CR-css-counter-styles-3-20150611/#descdef-counter-style-speak-as
 */
public class CssSpeakAs extends org.w3c.css.properties.css.counterstyle.CssSpeakAs {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"auto", "bullets", "numbers", "words", "spell-out"};
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
     * Create a new CssSpeakAs
     */
    public CssSpeakAs() {
        value = initial;
    }

    /**
     * Creates a new CssSpeakAs
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssSpeakAs(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = (CssIdent) val;
            value = getAllowedIdent(ident);
            if (value == null) {
                // reserved values are excluded
                if (isCssWide(ident)) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                value = ident;
            }

        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();

    }

    public CssSpeakAs(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

