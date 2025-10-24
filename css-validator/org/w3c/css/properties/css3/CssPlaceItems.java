//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2018/WD-css-align-3-20180423/#place-items-property
 */
public class CssPlaceItems extends org.w3c.css.properties.css.CssPlaceItems {

    private CssAlignItems alignItems;
    private CssJustifyItems justifyItems;

    /**
     * Create a new CssAlignItems
     */
    public CssPlaceItems() {
        value = initial;
        alignItems = new CssAlignItems();
        justifyItems = new CssJustifyItems();
    }

    /**
     * Creates a new CssAlignItems
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssPlaceItems(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        alignItems = new CssAlignItems();
        justifyItems = new CssJustifyItems();

        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val;

        val = CssAlignItems.parseAlignItems(ac, expression, this);
        if (expression.end()) {
            value = val;
            alignItems.value = val;
            justifyItems.value = val;
        } else {
            char op = expression.getOperator();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            values.add(val);
            alignItems.value = val;

            val = CssJustifyItems.parseJustifyItems(ac, expression, this);
            if (!expression.end()) {
                throw new InvalidParamException("value", expression.getValue().toString(),
                        getPropertyName(), ac);
            }
            values.add(val);
            justifyItems.value = val;
            value = new CssValueList(values);
        }
    }

    public CssPlaceItems(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        alignItems.addToStyle(ac, style);
        justifyItems.addToStyle(ac, style);
    }
}

