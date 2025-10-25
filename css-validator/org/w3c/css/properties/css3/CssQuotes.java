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
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2019/WD-css-content-3-20190802/#propdef-quotes
 */
public class CssQuotes extends org.w3c.css.properties.css.CssQuotes {

    static final CssIdent auto = CssIdent.getIdent("auto");
    
    /**
     * Create a new CssQuotes
     */
    public CssQuotes() {
        value = initial;
    }

    /**
     * Creates a new CssQuotes
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssQuotes(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        CssValue val;
        char op;

        switch (expression.getCount()) {
            case 1:
                val = expression.getValue();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    expression.next();
                    break;
                }
                if (none.equals(ident) || auto.equals(ident)) {
                    value = val;
                    expression.next();
                    break;
                }
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            default:
                if (expression.getCount() % 2 == 1) {
                    // odd number, one missing value
                    throw new InvalidParamException("few-value", getPropertyName(), ac);
                }
                ArrayList<CssValue> v = new ArrayList<CssValue>();

                while (!expression.end()) {
                    for (int i = 0; i < 2; i++) {
                        val = expression.getValue();
                        op = expression.getOperator();
                        if (val.getType() != CssTypes.CSS_STRING) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        if (op != SPACE) {
                            throw new InvalidParamException("operator",
                                    Character.toString(op), ac);
                        }
                        expression.next();
                    }
                }
                value = new CssValueList(v);
        }
    }

    public CssQuotes(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

