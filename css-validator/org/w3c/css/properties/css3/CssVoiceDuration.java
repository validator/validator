//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#voice-duration
 */
public class CssVoiceDuration extends org.w3c.css.properties.css.CssVoiceDuration {

    public static final CssIdent auto;

    static {
        auto = CssIdent.getIdent("auto");
    }

    /**
     * Create a new CssVoiceDuration
     */
    public CssVoiceDuration() {
        value = initial;
    }

    /**
     * Creates a new CssVoiceDuration
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssVoiceDuration(ApplContext ac, CssExpression expression, boolean check)
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
            case CssTypes.CSS_TIME:
                CssCheckableValue t = val.getCheckableValue();
                t.checkPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent()) || auto.equals(val.getIdent())) {
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

    public CssVoiceDuration(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

