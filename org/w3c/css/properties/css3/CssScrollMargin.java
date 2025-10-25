//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2019/CR-css-scroll-snap-1-20190319/#propdef-scroll-margin
 */
public class CssScrollMargin extends org.w3c.css.properties.css.CssScrollMargin {

    private CssScrollMarginTop _longhand_top;
    private CssScrollMarginBottom _longhand_bottom;
    private CssScrollMarginLeft _longhand_left;
    private CssScrollMarginRight _longhand_right;

    /**
     * Create a new CssScrollMargin
     */
    public CssScrollMargin() {
        value = initial;
        _longhand_bottom = new CssScrollMarginBottom();
        _longhand_left = new CssScrollMarginLeft();
        _longhand_right = new CssScrollMarginRight();
        _longhand_top = new CssScrollMarginTop();
    }

    /**
     * Creates a new CssScrollMargin
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssScrollMargin(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val;
        char op;

        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        ArrayList<CssValue> values = new ArrayList<>();
        _longhand_bottom = new CssScrollMarginBottom();
        _longhand_left = new CssScrollMarginLeft();
        _longhand_right = new CssScrollMarginRight();
        _longhand_top = new CssScrollMarginTop();

        for (int i = 0; i < 4 && !expression.end(); i++) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                    values.add(val);
                    switch (i) {
                        case 0:
                            _longhand_top.value = val;
                            break;
                        case 1:
                            _longhand_right.value = val;
                            break;
                        case 2:
                            _longhand_bottom.value = val;
                            break;
                        case 3:
                            _longhand_left.value = val;
                            break;
                        default:
                            // can't happen by design
                    }
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    expression.getValue(),
                                    getPropertyName(), ac);
                        }
                        values.add(val);
                        _longhand_top.value = inherit;
                        _longhand_right.value = inherit;
                        _longhand_bottom.value = inherit;
                        _longhand_left.value = inherit;
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

    public CssScrollMargin(ApplContext ac, CssExpression expression)
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
}

