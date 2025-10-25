//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-unicode-range
 */
public class CssUnicodeRange extends org.w3c.css.properties.css.fontface.CssUnicodeRange {

    /**
     * Create a new CssUnicodeRange
     */
    public CssUnicodeRange() {
        value = initial;
    }

    /**
     * Creates a new CssUnicodeRange
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssUnicodeRange(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        char op;
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();

        setByUser();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_UNICODE_RANGE:
                    values.add(val);
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();

            if (!expression.end() && op != COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (values.isEmpty()) {
            throw new InvalidParamException("few-value", getPropertyName(), ac);
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssUnicodeRange(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

