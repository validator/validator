// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/tables.html#propdef-border-spacing
 */
public class CssBorderSpacing extends org.w3c.css.properties.css.CssBorderSpacing {

    /**
     * Create a new CssBorderSpacing
     */
    public CssBorderSpacing() {
    }

    /**
     * Creates a new CssBorderSpacing
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssBorderSpacing(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;


        ArrayList<CssValue> v = new ArrayList<CssValue>();
        int i = 0;

        while (!expression.end() && i < 2) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                    CssLength l = val.getLength();
                    l.checkPositiveness(ac, this);
                    v.add(l);
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val) && expression.getCount() == 1) {
                        value = inherit;
                        break;
                    }
                    // unrecognized ident => fail
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
            i++;
        }
        if (value != inherit) {
            value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
        }
    }

    public CssBorderSpacing(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

