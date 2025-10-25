// $Id$
//
// (c) COPYRIGHT 1995-2012  World Wide Web Consortium (MIT, ERCIM and Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssSwitch;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-border-radius
 */
public class CssBorderRadius extends org.w3c.css.properties.css.CssBorderRadius {


    /**
     * Create new CssBorderRadius
     */
    public CssBorderRadius() {
        value = initial;
        topLeft = new CssBorderTopLeftRadius();
        topRight = new CssBorderTopRightRadius();
        bottomLeft = new CssBorderBottomLeftRadius();
        bottomRight = new CssBorderBottomRightRadius();
    }

    /**
     * Create new CssBorderRadius
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBorderRadius(ApplContext ac, CssExpression expression,
                           boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 9) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        CssValue val;
        char op;
        boolean switched = false;

        ArrayList<CssValue> h_radius = new ArrayList<CssValue>();
        ArrayList<CssValue> v_radius = new ArrayList<CssValue>();
        ArrayList<CssValue> cur_radius = h_radius;

        while (!expression.end() && h_radius.size() < 4 && v_radius.size() < 4) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, getPropertyName());
                    cur_radius.add(val);
                    break;
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    val.getCheckableValue().checkPositiveness(ac, getPropertyName());
                    cur_radius.add(val);
                    break;
                case CssTypes.CSS_SWITCH:
                    if (switched) {
                        throw new InvalidParamException("operator",
                                val.toString(), ac);
                    }
                    switched = true;
                    cur_radius = v_radius;
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        cur_radius.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            expression.next();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        if (v_radius.size() == 0) {
            value = new CssValueList(h_radius);
        } else {
            CssValueList vlist = new CssValueList();
            for (CssValue h : h_radius) {
                vlist.add(h);
            }
            vlist.add(new CssSwitch());
            for (CssValue v : v_radius) {
                vlist.add(v);
            }
            value = vlist;
        }
        // now assign the computed values...
        topLeft = new CssBorderTopLeftRadius();
        topRight = new CssBorderTopRightRadius();
        bottomLeft = new CssBorderBottomLeftRadius();
        bottomRight = new CssBorderBottomRightRadius();

        switch (h_radius.size()) {
            case 1:
                topLeft.h_radius = topRight.h_radius = bottomLeft.h_radius = bottomRight.h_radius = h_radius.get(0);
                break;
            case 2:
                topLeft.h_radius = bottomRight.h_radius = h_radius.get(0);
                topRight.h_radius = bottomLeft.h_radius = h_radius.get(1);
                break;
            case 3:
                topLeft.h_radius = h_radius.get(0);
                topRight.h_radius = bottomLeft.h_radius = h_radius.get(1);
                bottomRight.h_radius = h_radius.get(2);
                break;
            case 4:
                topLeft.h_radius = h_radius.get(0);
                topRight.h_radius = h_radius.get(1);
                bottomRight.h_radius = h_radius.get(2);
                bottomLeft.h_radius = h_radius.get(3);
                break;
            default:
                // can't happen
                throw new InvalidParamException("unrecognize", ac);
        }
        switch (v_radius.size()) {
            case 0:
                // v = h
                topLeft.v_radius = topRight.v_radius = bottomLeft.v_radius = bottomRight.v_radius = h_radius.get(0);
                break;
            case 1:
                topLeft.v_radius = topRight.v_radius = bottomLeft.v_radius = bottomRight.v_radius = v_radius.get(0);
                break;
            case 2:
                topLeft.v_radius = bottomRight.v_radius = v_radius.get(0);
                topRight.v_radius = bottomLeft.v_radius = v_radius.get(1);
                break;
            case 3:
                topLeft.v_radius = v_radius.get(0);
                topRight.v_radius = bottomLeft.v_radius = v_radius.get(1);
                bottomRight.v_radius = v_radius.get(2);
                break;
            case 4:
                topLeft.v_radius = v_radius.get(0);
                topRight.v_radius = v_radius.get(1);
                bottomRight.v_radius = v_radius.get(2);
                bottomLeft.v_radius = v_radius.get(3);
                break;
            default:
                // can't happen
                throw new InvalidParamException("unrecognize", ac);
        }
        shorthand = true;
    }

    public CssBorderRadius(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Check the border-*-radius and returns a value.
     * It makes sense to do it only once for all the corners, so by having the code here.
     */
    public static CssValue parseBorderCornerRadius(ApplContext ac, CssExpression expression, boolean check, CssProperty caller)
            throws InvalidParamException {

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val, cssWideVal = null;
        CssValueList res = new CssValueList();
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                    res.add(val);
                    break;
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    val.getCheckableValue().checkPositiveness(ac, caller);
                    res.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        cssWideVal = val;
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val, caller, ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
            expression.next();
        }
        // sanity check
        if ((cssWideVal != null) && (res.size() != 0)) {
            throw new InvalidParamException("value", cssWideVal, caller, ac);
        }
        if (cssWideVal != null) {
            return cssWideVal;
        }
        return (res.size() == 1) ? res.get(0) : res;
    }
}
