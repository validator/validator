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
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#text-underline-position-property
 */
public class CssTextUnderlinePosition extends org.w3c.css.properties.css.CssTextUnderlinePosition {

    public static final CssIdent auto;
    public static final CssIdent[] horizontalValues, verticalValues;

    static {
        String[] _horizontalValues = {"left", "right"};
        horizontalValues = new CssIdent[_horizontalValues.length];
        int i = 0;
        for (String s : _horizontalValues) {
            horizontalValues[i++] = CssIdent.getIdent(s);
        }
        String[] _verticalValues = {"under", "from-font"};
        verticalValues = new CssIdent[_verticalValues.length];
        i = 0;
        for (String s : _verticalValues) {
            verticalValues[i++] = CssIdent.getIdent(s);
        }
        auto = CssIdent.getIdent("auto");
    }

    public static CssIdent getHorizontalValue(CssIdent ident) {
        for (CssIdent id : horizontalValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getVerticalValue(CssIdent ident) {
        for (CssIdent id : verticalValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        if (auto.equals(ident)) {
            return auto;
        }
        CssIdent id = getHorizontalValue(ident);
        if (id != null) {
            return id;
        }
        return getVerticalValue(ident);
    }

    /**
     * Create a new CssTextUnderlinePosition
     */
    public CssTextUnderlinePosition() {
        value = initial;
    }

    /**
     * Creates a new CssTextUnderlinePosition
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextUnderlinePosition(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssValue horValue = null;
        CssValue verValue = null;

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
        } else if (auto.equals(ident)) {
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
                if (verValue == null) {
                    match = (getVerticalValue(ident) != null);
                    if (match) {
                        verValue = val;
                    }
                }
                if (!match && horValue == null) {
                    match = (getHorizontalValue(ident) != null);
                    if (match) {
                        horValue = val;
                    }
                }
                if (!match) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                nbgot++;
                if (expression.getRemainingCount() == 1 || (!check && nbgot == 2)) {
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
            if (horValue != null) {
                v.add(horValue);
            }
            if (verValue != null) {
                v.add(verValue);
            }
            value = (nbgot > 1) ? new CssValueList(v) : v.get(0);
        }
        expression.next();

    }

    public CssTextUnderlinePosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

