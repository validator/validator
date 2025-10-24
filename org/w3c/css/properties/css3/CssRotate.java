//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2025.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2021/WD-css-transforms-2-20211109/#propdef-rotate
 */
public class CssRotate extends org.w3c.css.properties.css.CssRotate {

    public static final CssIdent[] allowedValues;

    static {
        String[] _allowedValues = {"x", "y", "z"};
        allowedValues = new CssIdent[_allowedValues.length];
        for (int i = 0; i < allowedValues.length; i++) {
            allowedValues[i] = CssIdent.getIdent(_allowedValues[i]);
        }
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        for (CssIdent id : allowedValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }
    /**
     * Create a new CssRotate
     */
    public CssRotate() {
        value = initial;
    }

    /**
     * Creates a new CssRotate
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssRotate(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        ArrayList<CssValue> v = new ArrayList<>();
        char op;
        int nbNumber = 0;
        boolean gotIdent = false;
        boolean gotAngle = false;
        setByUser();


        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    // if we got an axis or more than 3 values, report error
                    if (gotIdent || (nbNumber == 3)) {
                        throw new InvalidParamException("value", val.toString(),
                                getPropertyName(), ac);
                    }
                    nbNumber++;
                    v.add(val);
                    break;
                case CssTypes.CSS_ANGLE:
                    // if we got numbers, we must get them all before the angle value
                    if (gotAngle || nbNumber == 1 || nbNumber == 2) {
                        throw new InvalidParamException("value", val.toString(),
                                getPropertyName(), ac);
                    }
                    gotAngle = true;
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (none.equals(id) || CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    // if wa got a number (ie: defining axis) or another ident axis, report error
                    if (!gotIdent && (nbNumber == 0) && getAllowedValue(id) != null) {
                        v.add(val);
                        gotIdent = true;
                        break;
                    }
                    // unrecognize ident, or unwanted one, let it fail
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
        // sanity check, if we got an axis, we should get an angle, if we got number we should get 3 of them
        if ((nbNumber > 0 && nbNumber != 3) || ((gotIdent || (nbNumber != 0)) && !gotAngle)) {
            throw new InvalidParamException("value",
                    v.toString(),
                    getPropertyName(), ac);
        }
        value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }


    public CssRotate(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

