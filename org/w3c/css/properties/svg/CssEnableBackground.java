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
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/filters.html#EnableBackgroundProperty
 */
public class CssEnableBackground extends org.w3c.css.properties.css.CssEnableBackground {

    public CssIdent id_new = CssIdent.getIdent("new");
    public CssIdent accumulate = CssIdent.getIdent("accumulate");

    /**
     * Create a new CssEnableBackground
     */
    public CssEnableBackground() {
        value = initial;
    }

    /**
     * Creates a new CssEnableBackground
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssEnableBackground(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;
        int nbCoord = 0;
        boolean got_new = false;

        ArrayList<CssValue> values = new ArrayList<>();
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    nbCoord++;
                    if (!got_new || nbCoord > 4) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    // <x> <y> <width> <height> where <width> and <height> >= 0
                    if (nbCoord > 2) {
                        val.getCheckableValue().checkPositiveness(ac, this);
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    if (accumulate.equals(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    if (id_new.equals(id) && !got_new) {
                        values.add(val);
                        got_new = true;
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
            value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        }
    }

    public CssEnableBackground(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

