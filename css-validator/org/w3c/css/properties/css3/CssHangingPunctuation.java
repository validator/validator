//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-hanging-punctuation
 */
public class CssHangingPunctuation extends org.w3c.css.properties.css.CssHangingPunctuation {

    public static final CssIdent first, last;
    public static final CssIdent[] endValues;
    public static final CssIdent none;

    static {
        first = CssIdent.getIdent("first");
        last = CssIdent.getIdent("last");
        none = CssIdent.getIdent("none");

        String[] _endValues = {"force-end", "allow-end"};
        endValues = new CssIdent[_endValues.length];
        int i = 0;
        for (String s : _endValues) {
            endValues[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getEndValue(CssIdent ident) {
        for (CssIdent id : endValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssHangingPunctuation
     */
    public CssHangingPunctuation() {
    }

    /**
     * Creates a new CssHangingPunctuation
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssHangingPunctuation(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssValue firstValue = null;
        CssValue lastValue = null;
        CssValue endValue = null;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }

        CssIdent ident = val.getIdent();
        if (CssIdent.isCssWide(ident)) {
            value = val;
            if (check && expression.getCount() != 1) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
        } else if (none.equals(ident)) {
            value = val;
            if (check && expression.getCount() != 1) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
        } else {
            int nbgot = 0;
            do {
                boolean match = false;
                if (firstValue == null && first.equals(ident)) {
                    firstValue = val;
                    match = true;
                } else if (lastValue == null && last.equals(ident)) {
                    lastValue = val;
                    match = true;
                } else {
                    if ((endValue == null) && (getEndValue(ident) != null)) {
                        endValue = val;
                        match = true;
                    }
                }
                if (!match) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                nbgot++;
                if (expression.getRemainingCount() == 1 || (!check && nbgot == 3)) {
                    // if we have both, exit
                    // (needed only if check == false...
                    break;
                }
                if (op != CssOperator.SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                expression.next();
                val = expression.getValue();
                op = expression.getOperator();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                ident = val.getIdent();
            } while (!expression.end());
            // now construct the value
            ArrayList<CssValue> v = new ArrayList<CssValue>(nbgot);
            if (firstValue != null) {
                v.add(firstValue);
            }
            if (endValue != null) {
                v.add(endValue);
            }
            if (lastValue != null) {
                v.add(lastValue);
            }
            value = (nbgot > 1) ? new CssValueList(v) : v.get(0);
        }
        expression.next();
    }

    public CssHangingPunctuation(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

