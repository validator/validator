// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
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
 * @spec http://www.w3.org/TR/2012/WD-css3-text-20120814/#text-emphasis0
 */
public class CssTextEmphasis extends org.w3c.css.properties.css.CssTextEmphasis {

    CssTextEmphasisColor colorValue = new CssTextEmphasisColor();
    CssTextEmphasisStyle styleValue = new CssTextEmphasisStyle();

    /**
     * Create a new CssTextEmphasis
     */
    public CssTextEmphasis() {
        value = initial;
    }

    /**
     * Creates a new CssTextEmphasis
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextEmphasis(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        CssValue color = null;
        CssExpression styleExp = null;

        CssValue val;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_STRING:
                    if (styleExp == null) {
                        styleExp = new CssExpression();
                    }
                    styleExp.addValue(val);
                    expression.next();
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        value = val;
                        expression.next();
                        return;
                    }
                    CssIdent id = CssTextEmphasisStyle.getAllowedValue(ident);
                    if (id != null) {
                        if (styleExp == null) {
                            styleExp = new CssExpression();
                        }
                        styleExp.addValue(val);
                        expression.next();
                        break;
                    }
                    // or else, it should be a color...
                default:
                    // we can't have two colors
                    if (color != null) {
                        throw new InvalidParamException("value",
                                val, getPropertyName(), ac);
                    }
                    CssColor c = new CssColor(ac, expression, false);
                    color = c.getColor();
                    // color can be first or last
                    if (styleExp != null && expression.getRemainingCount() != 0) {
                        if (color != null) {
                            throw new InvalidParamException("value",
                                    val, getPropertyName(), ac);
                        }
                    }
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        // parse the style exp
        if (styleExp != null) {
            styleValue = new CssTextEmphasisStyle(ac, styleExp, check);
            value = styleValue.value;
        }
        if (color != null) {
            colorValue.value = color;
            if (styleExp == null) {
                value = color;
            } else {
                ArrayList<CssValue> v = new ArrayList<CssValue>(2);
                v.add(styleValue.value);
                v.add(color);
                value = new CssValueList(v);
            }
        }
    }

    public CssTextEmphasis(ApplContext ac, CssExpression expression)
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
        // and the individual...
        colorValue.addToStyle(ac, style);
        styleValue.addToStyle(ac, style);
    }
}

