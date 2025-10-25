// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
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
 * @spec http://www.w3.org/TR/2007/WD-css3-box-20070809/#margin1
 */
public class CssMargin extends org.w3c.css.properties.css.CssMargin {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssMargin
     */
    public CssMargin() {
        value = initial;
        marginBottom = new CssMarginBottom();
        marginLeft = new CssMarginLeft();
        marginTop = new CssMarginTop();
        marginRight = new CssMarginRight();
    }


    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssMargin(ApplContext ac, CssExpression expression)
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
    public CssMargin(ApplContext ac, CssExpression expression,
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
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        v.add(val);
                        gotInherit = true;
                        break;
                    }
                    if (auto.equals(id)) {
                        v.add(val);
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
        marginBottom = new CssMarginBottom();
        marginLeft = new CssMarginLeft();
        marginTop = new CssMarginTop();
        marginRight = new CssMarginRight();

        if (gotInherit) {
            if (v.size() > 1) {
                throw new InvalidParamException("unrecognize", ac);
            }
            value = v.get(0);
            marginBottom.value = value;
            marginTop.value = value;
            marginLeft.value = value;
            marginRight.value = value;
        } else {
            switch (v.size()) {
                case 1:
                    marginTop.value = v.get(0);
                    marginRight.value = v.get(0);
                    marginBottom.value = v.get(0);
                    marginLeft.value = v.get(0);
                    break;
                case 2:
                    marginTop.value = v.get(0);
                    marginRight.value = v.get(1);
                    marginBottom.value = v.get(0);
                    marginLeft.value = v.get(1);
                    break;
                case 3:
                    marginTop.value = v.get(0);
                    marginRight.value = v.get(1);
                    marginBottom.value = v.get(2);
                    marginLeft.value = v.get(1);
                    break;
                case 4:
                    marginTop.value = v.get(0);
                    marginRight.value = v.get(1);
                    marginBottom.value = v.get(2);
                    marginLeft.value = v.get(3);
                    break;
                default:
                    // can't happen unless we are not checking
                    // the size
                    throw new InvalidParamException("unrecognize", ac);
            }
        }
        value = new CssValueList(v);
    }

    // for use by individual margin-* properties

    protected static CssValue parseMargin(ApplContext ac,
                                          CssExpression expression,
                                          boolean check, CssProperty caller)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;

        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, caller);
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                expression.next();
                return val;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    expression.next();
                    return val;
                }
                if (auto.equals(id)) {
                    expression.next();
                    return val;
                }
                // if not inherit, or not an ident
                // let it flow to the exception
        }
        throw new InvalidParamException("value",
                val.toString(),
                caller.getPropertyName(), ac);
    }
}
