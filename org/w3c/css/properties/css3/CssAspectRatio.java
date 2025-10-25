//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssRatio;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-4-20210520/#propdef-aspect-ratio
 */
public class CssAspectRatio extends org.w3c.css.properties.css.CssAspectRatio {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssAspectRatio
     */
    public CssAspectRatio() {
        value = initial;
    }

    /**
     * Creates a new CssAspectRatio
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssAspectRatio(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        ArrayList<CssValue> v = new ArrayList<>();
        char op;
        int ratio_state = 0;
        setByUser();
        CssValue dividend = null;
        CssValue divisor = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                // so we are cheating and create a CssRatio when needed.
                case CssTypes.CSS_NUMBER:
                    if (ratio_state == 0) {
                        dividend = val;
                        ratio_state++;
                        break;
                    } else if (ratio_state == 2) {
                        divisor = val;
                        ratio_state++;
                        v.add(new CssRatio(dividend, divisor)) ;
                        break;
                    }
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_SWITCH:
                    if (ratio_state == 1) {
                        ratio_state++;
                        break;
                    }
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (inherit.equals(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    if (auto.equals(id)) {
                        v.add(val);
                        break;
                    }
                    // unrecognize ident, let it fail
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
        // if things are not entirely parsed
        if (ratio_state == 1) {
            // so we got only one number
            v.add(new CssRatio(dividend));
        } else if (v.isEmpty() || (ratio_state != 0 && ratio_state != 3)) {
            throw new InvalidParamException("value",
                    expression.toStringFromStart(),
                    getPropertyName(), ac);
        }
        if (v.size() > 1) {
            if (v.get(0).getType() == v.get(1).getType()) {
                throw new InvalidParamException("value", v.get(1).toString(),
                        getPropertyName(), ac);
            }
            value = new CssValueList(v);
        } else {
            value = v.get(0);
        }
    }


    public CssAspectRatio(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

