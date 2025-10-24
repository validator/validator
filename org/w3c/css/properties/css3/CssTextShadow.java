// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#propdef-text-shadow
 */
public class CssTextShadow extends org.w3c.css.properties.css.CssTextShadow {

    /**
     * Create a new CssTextShadow
     */
    public CssTextShadow() {
        value = initial;
    }

    /**
     * Creates a new CssTextShadow
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextShadow(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();
        CssValue val;
        ArrayList<CssValue> values;
        CssExpression singleExpr = null;
        CssValue b_val = null;
        char op;

        values = new ArrayList<CssValue>();
        // we just accumulate values and check at validation
        // unless it's inherit or none.
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if ((val.getType() == CssTypes.CSS_IDENT) &&
                    (none.equals(val.getIdent()) || CssIdent.isCssWide(val.getIdent()))) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
                return;
            }
            if (singleExpr == null) {
                singleExpr = new CssExpression();
            }
            // we will check later
            singleExpr.addValue(val);
            singleExpr.setOperator(op);
            expression.next();

            if (!expression.end()) {
                // incomplete value followed by a comma... it's complete!
                if (op == COMMA) {
                    singleExpr.setOperator(SPACE);
                    b_val = check(ac, singleExpr);
                    values.add(b_val);
                    singleExpr = null;
                } else if ((op != SPACE)) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        // if we reach the end in a value that can come in pair
        if (singleExpr != null) {
            b_val = check(ac, singleExpr);
            values.add(b_val);
        }
        if (values.size() == 1) {
            value = values.get(0);
        } else {
            value = new CssLayerList(values);
        }
    }

    public CssValue check(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        CssValue val;
        CssValue color = null;
        ArrayList<CssValue> values = new ArrayList<CssValue>(4);

        int count = exp.getCount();
        if (count < 2 || count > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        while (!exp.end()) {
            val = exp.getValue();
            // color is last, so if we reach this, we are in error
            if (val.getType() == CssTypes.CSS_NUMBER) {
                // case of 0, a number and a length
                val = val.getLength();
            }
            if (val.getType() == CssTypes.CSS_LENGTH) {
                values.add(val);
                exp.next();
            } else {
                // we can't have two colors
                if (color != null) {
                    throw new InvalidParamException("value",
                            val, getPropertyName(), ac);
                }
                // FIXME rewrite potential exceptions
                CssColor c = new CssColor(ac, exp, false);
                color = val;
                // color can be first or last
                if (values.size() > 0 && exp.getRemainingCount() != 0) {
                    throw new InvalidParamException("value",
                            val, getPropertyName(), ac);
                }
                // no need for exp.next() as CssColor parsing is
                // already doing that.
            }
        }
        int lcount = values.size();
        // sanity check third length if present must not be negative
        if (lcount == 3) {
            CssCheckableValue l = values.get(2).getCheckableValue();
            l.checkPositiveness(ac, this);
        }
        // sanity check we need two to three length
        if (lcount < 2 || lcount > 3) {
            throw new InvalidParamException("value",
                    (new CssValueList(values)).toString(),
                    getPropertyName(), ac);
        }
        // add the color if set, create the value and return
        if (color != null) {
            values.add(color);
        }
        return new CssValueList(values);
    }

    public CssTextShadow(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

