//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2019/CR-css-scroll-snap-1-20190319/#propdef-scroll-padding
 */
public class CssScrollPadding extends org.w3c.css.properties.css.CssScrollPadding {

    private CssScrollPaddingTop _longhand_top;
    private CssScrollPaddingRight _longhand_right;
    private CssScrollPaddingBottom _longhand_bottom;
    private CssScrollPaddingLeft _longhand_left;

    protected static CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssScrollPaddingInline
     */
    public CssScrollPadding() {
        value = initial;
        _longhand_top = new CssScrollPaddingTop();
        _longhand_right = new CssScrollPaddingRight();
        _longhand_bottom = new CssScrollPaddingBottom();
        _longhand_left = new CssScrollPaddingLeft();
    }

    /**
     * Creates a new CssScrollPaddingInline
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssScrollPadding(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val;
        char op;

        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        ArrayList<CssValue> values = new ArrayList<>();
        _longhand_top = new CssScrollPaddingTop();
        _longhand_right = new CssScrollPaddingRight();
        _longhand_bottom = new CssScrollPaddingBottom();
        _longhand_left = new CssScrollPaddingLeft();


        for (int i = 0; i < 4 && !expression.end(); i++) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    val.getCheckableValue().checkPositiveness(ac, this);
                    values.add(val);
                    setValue(i, val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    expression.getValue(),
                                    getPropertyName(), ac);
                        }
                        values.add(val);
                        _longhand_top.value = val;
                        _longhand_right.value = val;
                        _longhand_bottom.value = val;
                        _longhand_left.value = val;
                        break;
                    }
                    if (auto.equals(val.getIdent())) {
                        values.add(val);
                        setValue(i, val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    private void setValue(int index, CssValue value) {
        switch (index) {
            case 0:
                _longhand_top.value = value;
                break;
            case 1:
                _longhand_right.value = value;
                break;
            case 2:
                _longhand_bottom.value = value;
                break;
            case 3:
                _longhand_left.value = value;
                break;
            default:
                // can't happen by design
        }
    }

    public CssScrollPadding(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        _longhand_top.addToStyle(ac, style);
        _longhand_right.addToStyle(ac, style);
        _longhand_bottom.addToStyle(ac, style);
        _longhand_left.addToStyle(ac, style);

    }

    protected static CssValue checkPaddingValue(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, caller);
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                val.getCheckableValue().checkPositiveness(ac, caller);
                expression.next();
                return val;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    expression.next();
                    return val;
                }
                if (auto.equals(val)) {
                    expression.next();
                    return val;
                }
            default:
                throw new InvalidParamException("value",
                        expression.getValue(),
                        caller.getPropertyName(), ac);
        }
    }
}

