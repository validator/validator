//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.properties.css3.CssFontStyle.getMatchingIdent;
import static org.w3c.css.properties.css3.CssFontStyle.oblique;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-font-style
 * @see org.w3c.css.properties.css3.CssFontStyle
 */
public class CssFontStyle extends org.w3c.css.properties.css.fontface.CssFontStyle {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssFontStyle
     */
    public CssFontStyle() {
        value = initial;
    }

    /**
     * Creates a new CssFontStyle
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        char op;
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (auto.equals(id)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    value = val;
                    expression.next();
                    break;
                }
                if (getMatchingIdent(id) != null) {
                    if (oblique.equals(id)) {
                        values.add(val);
                        // check for extra angle values
                        expression.next();
                        while (!expression.end()) {
                            if (op != SPACE) {
                                throw new InvalidParamException("operator",
                                        Character.toString(op), ac);
                            }
                            val = expression.getValue();
                            op = expression.getOperator();
                            if (val.getType() != CssTypes.CSS_ANGLE) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        getPropertyName(), ac);
                            }
                            values.add(val);
                            expression.next();
                        }
                        if (values.size() > 3) {
                            throw new InvalidParamException("value",
                                    values.get(values.size() - 1).toString(),
                                    getPropertyName(), ac);
                        }
                        value = new CssValueList(values);
                    } else {
                        // single value
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        expression.next();
                    }
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }

    }

    public CssFontStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

