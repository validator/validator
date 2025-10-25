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
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec http://www.w3.org/TR/2012/WD-css3-transitions-20120403/#transition-delay
 */
public class CssTransitionDelay extends org.w3c.css.properties.css.CssTransitionDelay {

    /**
     * Create a new CssTransitionDelay
     */
    public CssTransitionDelay() {
        value = initial;
    }

    /**
     * Creates a new CssTransitionDelay
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTransitionDelay(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        boolean gotinherit = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_TIME:
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        gotinherit = true;
                        values.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (gotinherit && values.size() > 1) {
            throw new InvalidParamException("value",
                    inherit.toString(),
                    getPropertyName(), ac);
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssTransitionDelay(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

