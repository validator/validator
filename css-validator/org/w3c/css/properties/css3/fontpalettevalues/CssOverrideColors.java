//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontpalettevalues;

import org.w3c.css.properties.css3.CssColor;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2026/WD-css-fonts-4-20260303/#descdef-font-palette-values-base-palette
 */
public class CssOverrideColors extends org.w3c.css.properties.css.fontpalettevalues.CssBasePalette {

    /**
     * Create a new CssBasePalette
     */
    public CssOverrideColors() {
        value = initial;
    }

    /**
     * Creates a new CssBasePalette
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssOverrideColors(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        char op;
        CssValue val;
        CssExpression exp;
        ArrayList<CssValue> values;
        ArrayList<CssValue> layers = new ArrayList<>();

        while (!expression.end()) {
            values = new ArrayList<>(2);
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_NUMBER) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            values.add(val);

            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
            exp = new CssExpression();
            while (!expression.end() && op != CssOperator.COMMA) {
                val = expression.getValue();
                op = expression.getOperator();
                exp.addValue(val);
                exp.setOperator(op);
                expression.next();
            }
            CssColor css3Color = new CssColor(ac, exp, check);
            // TODO FIXME need to check that it is an absolute color
            if (exp.getCount() == 1) {
                values.add(val);
            } else {
                values.add(css3Color.getColor());
            }
            layers.add(new CssValueList(values));
        }
        value = (layers.size() == 1) ? layers.get(0) : new CssValueList(layers);
    }

    public CssOverrideColors(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

