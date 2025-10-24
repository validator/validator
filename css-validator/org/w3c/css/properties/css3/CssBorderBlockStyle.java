//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2018/WD-css-logical-1-20180827/#propdef-border-block-style
 */
public class CssBorderBlockStyle extends org.w3c.css.properties.css.CssBorderBlockStyle {

    /**
     * Create a new CssBorderBlockStyle
     */
    public CssBorderBlockStyle() {
        value = initial;
    }

    /**
     * Creates a new CssBorderBlockStyle
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssBorderBlockStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        CssValue val;
        char op;
        ArrayList<CssValue> v = new ArrayList<>();

        op = expression.getOperator();
        val = CssBorderStyle.parseBorderSideStyle(ac, expression, false, this);
        v.add(val);
        if (op != SPACE) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        if (!expression.end()) {
            v.add(CssBorderStyle.parseBorderSideStyle(ac, expression,
                    false, this));
        }
        if (v.size() == 1) {
            value = v.get(0);
        } else {
            if (v.contains(inherit)) {
                throw new InvalidParamException("value", inherit.toString(),
                        getPropertyName(), ac);
            }
            value = new CssValueList(v);
        }
    }


    public CssBorderBlockStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

