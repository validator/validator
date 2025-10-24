// $Id$
// @author Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/box.html#border-color-properties
 */
public class CssBorderColor extends org.w3c.css.properties.css.CssBorderColor {

    /**
     * Create a new CssBorderColor
     */
    public CssBorderColor() {
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderColor(ApplContext ac, CssExpression expression,
                          boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        CssValue val;
        char op;

        ArrayList<CssValue> res = new ArrayList<CssValue>();
        while (res.size() < 4 && !expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_HASH_IDENT:
                    org.w3c.css.values.CssColor c = new org.w3c.css.values.CssColor();
                    c.setShortRGBColor(ac, val.toString());
                    res.add(c);
                    break;
                case CssTypes.CSS_COLOR:
                    res.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        res.add(inherit);
                        break;
                    }
                    if (transparent.equals(val)) {
                        res.add(transparent);
                        break;
                    }
                    res.add(new org.w3c.css.values.CssColor(ac, (String) val.get()));
                    break;
                default:
                    throw new InvalidParamException("unrecognize", ac);
            }
            expression.next();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        // check that inherit is alone
        if (res.size() > 1 && res.contains(inherit)) {
            throw new InvalidParamException("unrecognize", ac);
        }
        value = (res.size() == 1) ? res.get(0) : new CssValueList(res);

        // now assign the computed values...
        top = new CssBorderTopColor();
        right = new CssBorderRightColor();
        bottom = new CssBorderBottomColor();
        left = new CssBorderLeftColor();

        switch (res.size()) {
            case 1:
                top.value = left.value = right.value = bottom.value = res.get(0);
                break;
            case 2:
                top.value = bottom.value = res.get(0);
                right.value = left.value = res.get(1);
                break;
            case 3:
                top.value = res.get(0);
                right.value = left.value = res.get(1);
                bottom.value = res.get(2);
                break;
            case 4:
                top.value = res.get(0);
                right.value = res.get(1);
                bottom.value = res.get(2);
                left.value = res.get(3);
                break;
            default:
                // can't happen
                throw new InvalidParamException("unrecognize", ac);
        }
        shorthand = true;
    }

    /**
     * Check the border-*-color and returns a value.
     * It makes sense to do it only once for all the sides, so by having the code here.
     */
    protected static CssValue checkBorderSideColor(ApplContext ac, CssProperty caller, CssExpression expression,
                                                   boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue retval = null;
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_HASH_IDENT:
                org.w3c.css.values.CssColor c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.toString());
                retval = c;
                break;
            case CssTypes.CSS_COLOR:
                retval = val;
                break;
            case CssTypes.CSS_IDENT:
                // strangely enough border-color can be transparent but not border-*-color
                // CSS21 is not that way.
                if (inherit.equals(val)) {
                    retval = inherit;
                    break;
                }
                retval = new org.w3c.css.values.CssColor(ac,
                        (String) val.get());
                break;
            default:
                throw new InvalidParamException("value", val.toString(),
                        caller.getPropertyName(), ac);
        }
        expression.next();
        return retval;
    }
}
