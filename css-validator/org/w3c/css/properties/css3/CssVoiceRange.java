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
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#voice-range
 */
public class CssVoiceRange extends org.w3c.css.properties.css.CssVoiceRange {

    public static final CssIdent[] allowed_values;
    public static final CssIdent absolute;

    static {
        String[] _allowed_values = {"x-low", "low", "medium", "high", "x-high"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        absolute = CssIdent.getIdent("absolute");
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
     * Create a new CssVoiceRange
     */
    public CssVoiceRange() {
        value = initial;
    }

    /**
     * Creates a new CssVoiceRange
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssVoiceRange(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;
        CssValue identVal = null;
        CssValue numVal = null;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_FREQUENCY:
                case CssTypes.CSS_SEMITONE:
                case CssTypes.CSS_PERCENTAGE:
                    if (numVal != null) {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                    numVal = val;
                    value = val;
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", inherit,
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    if (identVal == null) {
                        CssIdent id = val.getIdent();
                        if (absolute.equals(id)) {
                            identVal = val;
                            value = val;
                            break;
                        }
                        identVal = getAllowedIdent(id);
                        if (identVal != null) {
                            value = val;
                            break;
                        }
                        // unrecognized... let it fail
                    }
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (expression.getCount() > 1) {
            if (identVal == absolute) {
                if (numVal.getType() != CssTypes.CSS_FREQUENCY) {
                    throw new InvalidParamException("value", expression.toStringFromStart(),
                            getPropertyName(), ac);
                }
                CssCheckableValue freq = numVal.getCheckableValue();
                freq.warnPositiveness(ac, this);
            }
            ArrayList<CssValue> values = new ArrayList<CssValue>(2);
            values.add(numVal);
            values.add(identVal);
            value = new CssValueList(values);
        }
    }

    public CssVoiceRange(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

