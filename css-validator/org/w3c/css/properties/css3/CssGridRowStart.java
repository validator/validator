//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
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
 * @spec https://www.w3.org/TR/2020/CRD-css-grid-1-20201218/#propdef-grid-row-start
 */
public class CssGridRowStart extends org.w3c.css.properties.css.CssGridRowStart {

    public static CssIdent span = CssIdent.getIdent("span");
    public static CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssGridRowEnd
     */
    public CssGridRowStart() {
        value = initial;
    }

    /**
     * Creates a new CssGridRowEnd
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssGridRowStart(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = checkGridLine(ac, expression, check, this);
    }

    // as it is ued in other places, use a static checker function.
    public static CssValue checkGridLine(ApplContext ac, CssExpression expression, boolean check,
                                         CssProperty caller)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val, value;
        ArrayList<CssValue> v = new ArrayList<>();
        boolean gotNumber = false;
        boolean gotCustomIdent = false;

        char op;


        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    if (gotNumber || ((val.getRawType() == CssTypes.CSS_NUMBER) && val.getCheckableValue().isZero())) {
                        // TODO add a specific exception, value can't be zero.
                        throw new InvalidParamException("value",
                                val.toString(),
                                caller.getPropertyName(), ac);

                    }
                    gotNumber = true;
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        v.add(val);
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        break;
                    }
                    if (auto.equals(id)) {
                        v.add(val);
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        break;
                    }
                    if (span.equals(id)) {
                        // span cannot be in the middle...
                        if (v.size() > 0 && expression.getRemainingCount() > 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    if (gotCustomIdent) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                caller.getPropertyName(), ac);
                    }
                    v.add(val);
                    gotCustomIdent = true;
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);

            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        expression.next();
        return (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }

    public CssGridRowStart(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

