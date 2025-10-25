//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/CR-css-speech-1-20200310/#cue
 */
public class CssCue extends org.w3c.css.properties.css.CssCue {

    /**
     * Create a new CssCue
     */
    public CssCue() {
        value = initial;
        cssCueAfter = new CssCueAfter();
        cssCueBefore = new CssCueBefore();
    }

    /**
     * Creates a new CssCue
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssCue(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        char op;

        cssCueBefore = new CssCueBefore();
        cssCueBefore.value = checkCueValue(ac, expression, this);
        if (expression.end()) {
            cssCueAfter = new CssCueAfter();
            cssCueAfter.value = cssCueBefore.value;
            value = cssCueBefore.value;
        } else {
            op = expression.getOperator();
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            cssCueAfter = new CssCueAfter();
            cssCueAfter.value = checkCueValue(ac, expression, this);
            // FIXME check for double reserved keyword instead of just inherit
            // as a value
            if (cssCueBefore.value == inherit || cssCueAfter.value == inherit) {
                throw new InvalidParamException("value",
                        inherit, getPropertyName(), ac);
            }
            if (!expression.end()) {
                throw new InvalidParamException("value",
                        cssCueAfter.value, getPropertyName(), ac);
            }
            ArrayList<CssValue> values = new ArrayList<CssValue>(2);
            values.add(cssCueBefore.value);
            values.add(cssCueAfter.value);
            value = new CssValueList(values);
        }
    }

    public CssCue(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    protected static CssValue checkCueValue(ApplContext ac, CssExpression expression,
                                            CssProperty caller)
            throws InvalidParamException {
        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_URL:
                // now let's check for a volume...
                if (expression.getRemainingCount() > 1) {
                    CssValue vnext = expression.getNextValue();
                    if (vnext.getType() == CssTypes.CSS_VOLUME) {
                        // we got a volume, so let's do extra checks, then
                        // construct the value...
                        if (op != SPACE) {
                            throw new InvalidParamException("operator",
                                    Character.toString(op), ac);
                        }
                        expression.next();
                        ArrayList<CssValue> values = new ArrayList<CssValue>(2);
                        values.add(val);
                        values.add(vnext);
                        expression.next();
                        return new CssValueList(values);
                    }
                }
                expression.next();
                return val;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                val, caller.getPropertyName(), ac);
                    }
                    expression.next();
                    return val;
                }
                if (none.equals(val.getIdent())) {
                    expression.next();
                    return val;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
        }
    }
}

