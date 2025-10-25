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
 * @spec https://www.w3.org/TR/2018/WD-css-align-3-20180423/#place-content-property
 */
public class CssPlaceContent extends org.w3c.css.properties.css.CssPlaceContent {

    private CssAlignContent alignContent;
    private CssJustifyContent justifyContent;

    /**
     * Create a new CssAlignContent
     */
    public CssPlaceContent() {
        value = initial;
        alignContent = new CssAlignContent();
        justifyContent = new CssJustifyContent();
    }

    /**
     * Creates a new CssAlignContent
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssPlaceContent(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        alignContent = new CssAlignContent();
        justifyContent = new CssJustifyContent();

        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val;

        val = CssAlignContent.parseAlignContent(ac, expression, this);
        if (expression.end()) {
            value = val;
            alignContent.value = val;
            justifyContent.value = val;
        } else {
            char op = expression.getOperator();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            values.add(val);
            alignContent.value = val;

            val = CssJustifyContent.parseJustifyContent(ac, expression, this);
            if (!expression.end()) {
                throw new InvalidParamException("value", expression.getValue().toString(),
                        getPropertyName(), ac);
            }
            values.add(val);
            justifyContent.value = val;
            value = new CssValueList(values);
        }
    }

    public CssPlaceContent(ApplContext ac, CssExpression expression)
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
        alignContent.addToStyle(ac, style);
        justifyContent.addToStyle(ac, style);
    }
}

