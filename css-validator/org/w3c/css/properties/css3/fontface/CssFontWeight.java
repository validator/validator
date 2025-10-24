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
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.properties.css3.CssFontWeight.getAllowedAbsoluteValue;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-font-weight
 * @see org.w3c.css.properties.css3.CssFontWeight
 */
public class CssFontWeight extends org.w3c.css.properties.css.fontface.CssFontWeight {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssFontWeight
     */
    public CssFontWeight() {
        value = initial;
    }

    /**
     * Creates a new CssFontWeight
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontWeight(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        char op;
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    if (val.getRawType() == CssTypes.CSS_NUMBER) {
                        CssNumber num = val.getNumber();
                        num.checkGreaterEqualThan(ac, 1, this);
                        num.checkLowerEqualThan(ac, 1000, this);
                    } else {
                        // can at least check it is positive
                        val.getCheckableValue().checkStrictPositiveness(ac, getPropertyName());
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (auto.equals(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        values.add(val);
                        break;
                    }
                    if (getAllowedAbsoluteValue(id) != null) {
                        values.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (values.isEmpty()) {
            throw new InvalidParamException("few-value", getPropertyName(), ac);
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssFontWeight(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

