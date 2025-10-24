// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/box.html#propdef-padding
 */
public class CssPadding extends org.w3c.css.properties.css.CssPadding {

    /**
     * Create a new CssPadding
     */
    public CssPadding() {
    }


    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssPadding(ApplContext ac, CssExpression expression)
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
    public CssPadding(ApplContext ac, CssExpression expression,
                      boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;
        ArrayList<CssValue> v = new ArrayList<CssValue>();
        boolean gotInherit = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                    CssLength l = val.getLength();
                    l.checkPositiveness(ac, this);
                    v.add(val);
                    break;
                case CssTypes.CSS_PERCENTAGE:
                    CssPercentage p = val.getPercentage();
                    p.checkPositiveness(ac, this);
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        v.add(inherit);
                        gotInherit = true;
                        break;
                    }
                    // if not inherit, or not an ident
                    // let it flow to the exception
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
        // now we check the number of values...
        paddingBottom = new CssPaddingBottom();
        paddingLeft = new CssPaddingLeft();
        paddingTop = new CssPaddingTop();
        paddingRight = new CssPaddingRight();

        if (gotInherit) {
            if (v.size() > 1) {
                throw new InvalidParamException("unrecognize", ac);
            }
            value = inherit;
            paddingBottom.value = inherit;
            paddingTop.value = inherit;
            paddingLeft.value = inherit;
            paddingRight.value = inherit;
        } else {
            switch (v.size()) {
                case 1:
                    paddingTop.value = v.get(0);
                    paddingRight.value = v.get(0);
                    paddingBottom.value = v.get(0);
                    paddingLeft.value = v.get(0);
                    break;
                case 2:
                    paddingTop.value = v.get(0);
                    paddingRight.value = v.get(1);
                    paddingBottom.value = v.get(0);
                    paddingLeft.value = v.get(1);
                    break;
                case 3:
                    paddingTop.value = v.get(0);
                    paddingRight.value = v.get(1);
                    paddingBottom.value = v.get(2);
                    paddingLeft.value = v.get(1);
                    break;
                case 4:
                    paddingTop.value = v.get(0);
                    paddingRight.value = v.get(1);
                    paddingBottom.value = v.get(2);
                    paddingLeft.value = v.get(3);
                    break;
                default:
                    // can't happen unless we are not checking
                    // the size
                    throw new InvalidParamException("unrecognize", ac);
            }
        }
        value = new CssValueList(v);
    }

    // for use by individual padding-* properties

    protected static CssValue checkValue(ApplContext ac,
                                         CssExpression expression,
                                         boolean check, CssProperty caller)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_LENGTH:
                CssLength l = val.getLength();
                l.checkPositiveness(ac, caller);
                expression.next();
                return val;
            case CssTypes.CSS_PERCENTAGE:
                CssPercentage p = val.getPercentage();
                p.checkPositiveness(ac, caller);
                expression.next();
                return val;
            case CssTypes.CSS_IDENT:
                if (inherit.equals(val)) {
                    expression.next();
                    return inherit;
                }
                // if not inherit, or not an ident
                // let it flow to the exception
        }
        throw new InvalidParamException("value",
                val.toString(),
                caller.getPropertyName(), ac);
    }
}
