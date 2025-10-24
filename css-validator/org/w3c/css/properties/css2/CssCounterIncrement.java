// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/generate.html#propdef-counter-increment
 */
public class CssCounterIncrement extends org.w3c.css.properties.css.CssCounterIncrement {

    /**
     * Create a new CssCounterIncrement
     */
    public CssCounterIncrement() {
    }

    /**
     * Creates a new CssCounterIncrement
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssCounterIncrement(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;
        boolean intallowed = false;
        ArrayList<CssValue> v = new ArrayList<CssValue>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        value = inherit;
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    if (none.equals(val)) {
                        value = none;
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        break;
                    }
                    v.add(val);
                    intallowed = true;
                    break;
                case CssTypes.CSS_NUMBER:
                    if (intallowed) {
                        // we allow int only after an ident
                        intallowed = false;
                        CssCheckableValue n = val.getCheckableValue();
                        n.checkInteger(ac, this);
                        v.add(val);
                        break;
                    }
                    // if int wasn't allowed, let it fail
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
        // check it's not inherit or none
        if (v.size() > 0) {
            value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
        }
    }

    public CssCounterIncrement(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

