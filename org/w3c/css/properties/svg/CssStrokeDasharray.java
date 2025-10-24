//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

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
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#StrokeDasharrayProperty
 */
public class CssStrokeDasharray extends org.w3c.css.properties.css.CssStrokeDasharray {

    /**
     * Create a new CssStrokeDasharray
     */
    public CssStrokeDasharray() {
        value = initial;
    }

    /**
     * Creates a new CssStrokeDasharray
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssStrokeDasharray(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;

        ArrayList<CssValue> values = new ArrayList<>();
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_LENGTH:
                    // we need >=0 values
                    val.getCheckableValue().checkPositiveness(ac, this);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        value = val;
                        break;
                    }
                    if (none.equals(id)) {
                        value = val;
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            // both space and commas can happen...
            if (op != CssOperator.SPACE && op != CssOperator.COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (!values.isEmpty()) {
            value = new CssValueList(values);
        }
    }

    public CssStrokeDasharray(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

