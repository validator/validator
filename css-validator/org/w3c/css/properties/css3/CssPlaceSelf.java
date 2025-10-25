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
 * @spec https://www.w3.org/TR/2018/WD-css-align-3-20180423/#place-self-property
 */
public class CssPlaceSelf extends org.w3c.css.properties.css.CssPlaceSelf {

    private CssAlignSelf alignSelf;
    private CssJustifySelf justifySelf;

    /**
     * Create a new CssAlignSelf
     */
    public CssPlaceSelf() {
        value = initial;
        alignSelf = new CssAlignSelf();
        justifySelf = new CssJustifySelf();
    }

    /**
     * Creates a new CssAlignSelf
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssPlaceSelf(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        alignSelf = new CssAlignSelf();
        justifySelf = new CssJustifySelf();

        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val;

        val = CssAlignSelf.parseAlignSelf(ac, expression, this);
        if (expression.end()) {
            value = val;
            alignSelf.value = val;
            justifySelf.value = val;
        } else {
            char op = expression.getOperator();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            values.add(val);
            alignSelf.value = val;
            CssExpression ex = new CssExpression();
            while (!expression.end()) {
                ex.addValue(expression.getValue());
                ex.setOperator(expression.getOperator());
                expression.next();
            }
            val = CssJustifySelf.parseJustifySelf(ac, ex, this);
            if (!ex.end()) {
                throw new InvalidParamException("value", expression.getValue().toString(),
                        getPropertyName(), ac);
            }
            values.add(val);
            justifySelf.value = val;
            value = new CssValueList(values);
        }
    }

    public CssPlaceSelf(ApplContext ac, CssExpression expression)
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
        alignSelf.addToStyle(ac, style);
        justifySelf.addToStyle(ac, style);
    }
}

