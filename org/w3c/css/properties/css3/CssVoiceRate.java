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
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#voice-rate
 */
public class CssVoiceRate extends org.w3c.css.properties.css.CssVoiceRate {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"normal", "x-slow", "slow", "medium", "fast", "x-fast"};
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
     * Create a new CssVoiceRate
     */
    public CssVoiceRate() {
        value = initial;
    }

    /**
     * Creates a new CssVoiceRate
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssVoiceRate(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssValue pctValue = null;
        CssValue ideValue = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_PERCENTAGE:
                    CssCheckableValue p = val.getCheckableValue();
                    p.checkPositiveness(ac, this);
                    if (pctValue != null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    pctValue = val;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        ideValue = val;
                        break;
                    } else {
                        if ((ideValue == null) && (getAllowedIdent(id) != null)) {
                            ideValue = val;
                            break;
                        }
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        // now check what we have...
        ArrayList<CssValue> v = new ArrayList<CssValue>(2);
        if (ideValue != null) {
            v.add(ideValue);
        }
        if (pctValue != null) {
            v.add(pctValue);
        }
        value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }

    public CssVoiceRate(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

