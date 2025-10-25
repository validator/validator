// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/aural.html#propdef-pause
 */
public class CssPause extends org.w3c.css.properties.css.CssPause {

    /**
     * Create a new CssPause
     */
    public CssPause() {
    }

    /**
     * Creates a new CssPause
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssPause(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        char op;

        cssPauseBefore = new CssPauseBefore(ac, expression, false);
        if (expression.end()) {
            cssPauseAfter = new CssPauseAfter();
            cssPauseAfter.value = cssPauseBefore.value;
            value = cssPauseBefore.value;
        } else {
            op = expression.getOperator();
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            cssPauseAfter = new CssPauseAfter(ac, expression, false);
            if (cssPauseBefore.value == inherit || cssPauseAfter.value == inherit) {
                throw new InvalidParamException("value",
                        inherit, getPropertyName(), ac);
            }
            ArrayList<CssValue> values = new ArrayList<CssValue>(2);
            values.add(cssPauseBefore.value);
            values.add(cssPauseAfter.value);
            value = new CssValueList(values);
        }
    }

    public CssPause(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

