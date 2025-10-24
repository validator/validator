// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.atsc;

import org.w3c.css.properties.css2.CssColor;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @origspec http://www.w3.org/TR/2008/REC-CSS2-20080411/text.html#propdef-text-shadow
 */
public class CssTextShadow extends org.w3c.css.properties.css.CssTextShadow {

    /**
     * Create a new CssTextShadow
     */
    public CssTextShadow() {
    }

    /**
     * Creates a new CssTextShadow
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssTextShadow(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();
        CssValue val;
        ArrayList<CssValue> values;
        CssExpression singleExpr = null;
        CssValue b_val = null;
        char op;

        // same as CSS2 but with a warning.
        ac.getFrame().addWarning("atsc", expression.toString());

        values = new ArrayList<CssValue>();
        // we just accumulate values and check at validation
        // unless it's inherit or none.
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (inherit.equals(val)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = inherit;
                expression.next();
                return;
            }
            if (none.equals(val)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = none;
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
                val = ((CssNumber) val).getLength();
            }
            if (val.getType() == CssTypes.CSS_LENGTH) {
                values.add(val);
                exp.next();
            } else {
                CssColor c = new CssColor(ac, exp, false);
                color = c.getColor();
                // color can be first or last in CSS2
                if (values.size() > 0 && exp.getRemainingCount() != 0) {
                    if (color != null) {
                        throw new InvalidParamException("value",
                                val, getPropertyName(), ac);
                    }
                }
                // no need for exp.next() as CssColor parsing is
                // already doing that.
            }
        }
        int lcount = values.size();
        // sanity check third length if present must not be negative
/*
        This check is not mandated explicitly in the spec, however
        a radius can't be negative so the code will be re-enabled
        if necessary (but it's CSS2...)

        if (lcount == 3) {
			CssLength l = (CssLength) values.get(2);
			if (!l.isPositive()) {
				throw new InvalidParamException("negative-value",
						l, getPropertyName(), ac);
			}
		}
*/
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

