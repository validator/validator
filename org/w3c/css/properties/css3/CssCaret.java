//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-caret
 */
public class CssCaret extends org.w3c.css.properties.css.CssCaret {

    private static CssIdent auto = CssIdent.getIdent("auto");


    public static CssIdent getMatchingIdent(CssIdent ident) {
        if (auto.equals(ident)) {
            return auto;
        }
        return null;
    }

    /**
     * Create a new CssCaret
     */
    public CssCaret() {
        value = initial;
    }

    /**
     * Creates a new CssCaret
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssCaret(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val, v;
        char op;
        boolean gotColor = false;
        boolean gotShape = false;

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    values.add(val);
                    break;
                }
                if (getMatchingIdent(ident) != null) {
                    // auto can be used for both color and shape
                    values.add(val);
                    break;
                }
                if (CssCaretShape.getMatchingIdent(ident) != null) {
                    if (gotShape) {
                        throw new InvalidParamException("value",
                                val, getPropertyName(), ac);
                    }
                    gotShape = true;
                    values.add(val);
                    break;
                }
                // if not recognized... it can be a color.
                default:
                    try {
                        CssExpression nex = new CssExpression();
                        nex.addValue(val);
                        CssColor tcolor = new CssColor(ac, nex, check);
                        // instead of using getColor, we get the value directly
                        // as we can have idents
                        if (gotColor) {
                            throw new InvalidParamException("value",
                                    val, getPropertyName(), ac);
                        }
                        gotColor = true;
                        values.add(tcolor.getValue());
                    } catch (InvalidParamException e) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssCaret(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

