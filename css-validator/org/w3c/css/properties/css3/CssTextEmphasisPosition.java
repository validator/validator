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
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#propdef-text-emphasis-position
 */
public class CssTextEmphasisPosition extends org.w3c.css.properties.css.CssTextEmphasisPosition {

    public static final CssIdent[] vertValues;
    public static final CssIdent[] horiValues;

    static {
        String[] _vertValues = {"over", "under"};
        String[] _horiValues = {"right", "left"};

        vertValues = new CssIdent[_vertValues.length];
        int i = 0;
        for (String s : _vertValues) {
            vertValues[i++] = CssIdent.getIdent(s);
        }
        horiValues = new CssIdent[_horiValues.length];
        i = 0;
        for (String s : _horiValues) {
            horiValues[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getVerticalValues(CssIdent ident) {
        for (CssIdent id : vertValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getHorizontalValues(CssIdent ident) {
        for (CssIdent id : horiValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        CssIdent v = getVerticalValues(ident);
        if (v == null) {
            v = getHorizontalValues(ident);
        }
        return v;
    }

    /**
     * Create a new CssTextEmphasisPosition
     */
    public CssTextEmphasisPosition() {
        value = initial;
    }

    /**
     * Creates a new CssTextEmphasisPosition
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextEmphasisPosition(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssIdent vValue = null;
        CssIdent hValue = null;

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
        } else {
            boolean match = false;
            int nbgot = 0;
            do {
                match = false;
                if (vValue == null) {
                    vValue = getVerticalValues(ident);
                    match = (vValue != null);
                }
                if (!match && hValue == null) {
                    hValue = getHorizontalValues(ident);
                    match = (hValue != null);
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
            if (hValue != null && vValue != null) {
                ArrayList<CssValue> v = new ArrayList<CssValue>(2);
                v.add(vValue);
                v.add(hValue);
                value = new CssValueList(v);
            } else {
                // TODO FIXME specific error (one value missing)
                throw new InvalidParamException("value",
                        expression.toString(),
                        getPropertyName(), ac);
            }
        }
        expression.next();
    }

    public CssTextEmphasisPosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

