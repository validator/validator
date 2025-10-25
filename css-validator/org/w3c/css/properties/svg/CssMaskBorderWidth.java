//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#propdef-mask-border-width
 */
public class CssMaskBorderWidth extends org.w3c.css.properties.css.CssMaskBorderWidth {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssMaskBorderWidth
     */
    public CssMaskBorderWidth() {
        value = initial;
    }

    /**
     * Creates a new CssMaskBorderWidth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssMaskBorderWidth(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        ArrayList<CssValue> v = new ArrayList<>();
        CssValue val;
        char op;
        int nb_width = 0;

        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_LENGTH:
                    val.getCheckableValue().checkPositiveness(ac, this);
                    if (nb_width >= 4) {
                        throw new InvalidParamException("unrecognize", val.toString(),
                                getPropertyName(), ac);
                    }
                    nb_width++;
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        break;
                    }
                    if (auto.equals(id)) {
                        if (nb_width >= 4) {
                            throw new InvalidParamException("unrecognize", val.toString(),
                                    getPropertyName(), ac);
                        }
                        nb_width++;
                        v.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        if (!v.isEmpty()) {
            value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
        }
    }

    public CssMaskBorderWidth(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

