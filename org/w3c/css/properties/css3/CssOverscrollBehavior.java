// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
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
 * @spec https://www.w3.org/TR/2019/WD-css-overscroll-1-20190606/#propdef-overscroll-behavior
 */
public class CssOverscrollBehavior extends org.w3c.css.properties.css.CssOverscrollBehavior {

    CssOverscrollBehaviorX cssOverscrollBehaviorX;
    CssOverscrollBehaviorY cssOverscrollBehaviorY;

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"contain", "none", "auto"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssOverscrollBehavior
     */
    public CssOverscrollBehavior() {
        value = initial;
        cssOverscrollBehaviorX = new CssOverscrollBehaviorX();
        cssOverscrollBehaviorY = new CssOverscrollBehaviorY();
    }

    /**
     * Creates a new CssOverscrollBehavior
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssOverscrollBehavior(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        cssOverscrollBehaviorX = new CssOverscrollBehaviorX();
        cssOverscrollBehaviorY = new CssOverscrollBehaviorY();

        switch (expression.getCount()) {
            case 1:
                value = checkOverscrollBehaviorAxis(ac, expression, check, this);
                cssOverscrollBehaviorX.value = value;
                cssOverscrollBehaviorY.value = value;
                break;
            case 2:
                ArrayList<CssValue> v = new ArrayList<CssValue>();
                CssValue val;
                char op = expression.getOperator();
                val = checkOverscrollBehaviorAxis(ac, expression, false, this);
                if (val.getType() == CssTypes.CSS_IDENT && CssIdent.isCssWide(val.getIdent())) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                v.add(val);
                cssOverscrollBehaviorX.value = val;
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                val = checkOverscrollBehaviorAxis(ac, expression, false, this);
                if (val.getType() == CssTypes.CSS_IDENT && CssIdent.isCssWide(val.getIdent())) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                v.add(val);
                cssOverscrollBehaviorY.value = val;
                value = new CssValueList(v);
                break;
            default:
                throw new InvalidParamException("unrecognize", ac);
        }
    }

    public CssOverscrollBehavior(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css3Style) style).cssOverscrollBehavior != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssOverscrollBehavior = this;
        cssOverscrollBehaviorX.addToStyle(ac, style);
        cssOverscrollBehaviorY.addToStyle(ac, style);
    }

    static CssValue checkOverscrollBehaviorAxis(ApplContext ac, CssExpression expression,
                                                boolean check, CssProperty caller)
            throws InvalidParamException {
        CssValue value;

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", val,
                    caller.getPropertyName(), ac);
        }
        CssIdent id = val.getIdent();
        if (!CssIdent.isCssWide(id) && getAllowedIdent(id) == null) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        value = val;
        expression.next();
        return value;
    }

    /**
     * Update the source file and the line.
     * Overrides this method for a macro
     *
     * @param line   The line number where this property is defined
     * @param source The source file where this property is defined
     */
    public void setInfo(int line, String source) {
        super.setInfo(line, source);
        cssOverscrollBehaviorX.setInfo(line, source);
        cssOverscrollBehaviorY.setInfo(line, source);
    }

    /**
     * Set this property to be important.
     * Overrides this method for a macro
     */
    public void setImportant() {
        super.setImportant();
        cssOverscrollBehaviorX.setImportant();
        cssOverscrollBehaviorY.setImportant();
    }

    /**
     * Set the context.
     * Overrides this method for a macro
     *
     * @see org.w3c.css.css.CssCascadingOrder#order
     * @see org.w3c.css.css.StyleSheetParser#handleRule
     */
    public void setSelectors(CssSelectors selector) {
        super.setSelectors(selector);
        cssOverscrollBehaviorX.setSelectors(selector);
        cssOverscrollBehaviorY.setSelectors(selector);
    }
}

