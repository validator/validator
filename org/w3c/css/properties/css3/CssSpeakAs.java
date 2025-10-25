//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#speak-as
 */
public class CssSpeakAs extends org.w3c.css.properties.css.CssSpeakAs {

    public static final CssIdent normal, spell_out, digits;
    public static final CssIdent[] punctuation_values;

    static {
        normal = CssIdent.getIdent("normal");
        spell_out = CssIdent.getIdent("spell-out");
        digits = CssIdent.getIdent("digits");

        String[] _punctuation_values = {"literal-punctuation", "no-punctuation"};

        punctuation_values = new CssIdent[_punctuation_values.length];
        int i = 0;
        for (String s : _punctuation_values) {
            punctuation_values[i++] = CssIdent.getIdent(s);
        }
    }

    static CssIdent getPunctuation(CssIdent ident) {
        for (CssIdent id : punctuation_values) {
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
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssSpeakAs(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;
        CssValue spellVal = null;
        CssValue digitVal = null;
        CssValue punctVal = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        break;
                    }
                    if (normal.equals(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        break;
                    }
                    if (spellVal == null) {
                        if (spell_out.equals(id)) {
                            spellVal = val;
                            value = val;
                            break;
                        }
                    }
                    if (digitVal == null) {
                        if (digits.equals(id)) {
                            digitVal = val;
                            value = val;
                            break;
                        }
                    }
                    if (punctVal == null) {
                        if (getPunctuation(id) != null) {
                            punctVal = val;
                            value = val;
                            break;
                        }
                    }
                    // let it fail if unrecognized
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
        // now construct the value...
        if (expression.getCount() > 1) {
            ArrayList<CssValue> v = new ArrayList<CssValue>(4);
            if (spellVal != null) {
                v.add(spellVal);
            }
            if (digitVal != null) {
                v.add(digitVal);
            }
            if (punctVal != null) {
                v.add(punctVal);
            }
            value = new CssValueList(v);
        }
    }

    public CssSpeakAs(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

