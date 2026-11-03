//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
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
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-position-try retrieved 2026-11-03
 */
public class CssPositionTry extends org.w3c.css.properties.css.CssPositionTry {

    private CssPositionTryOrder _longhand_order;
    private CssPositionTryFallbacks _longhand_fallbacks;

    /**
     * Create a new CssPositionTry
     */
    public CssPositionTry() {
        value = initial;
        _longhand_order = new CssPositionTryOrder();
        _longhand_fallbacks = new CssPositionTryFallbacks();
    }

    /**
     * Creates a new CssPositionTry
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssPositionTry(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        _longhand_order = new CssPositionTryOrder();
        CssValue val = expression.getValue();
        CssValue order = null;

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                _longhand_order.value = val;
                _longhand_fallbacks = new CssPositionTryFallbacks();
                _longhand_fallbacks.value = val;
                expression.next();
                return;
            }
            // <'position-try-order'>? <'position-try-fallbacks'>
            if (CssPositionTryOrder.getAllowedIdent(id) != null) {
                char op = expression.getOperator();
                expression.next();
                // the position-try-fallbacks part isn't optional
                if (expression.end()) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                CssValue next = expression.getValue();
                if ((next.getType() == CssTypes.CSS_IDENT)
                        && CssIdent.isCssWide(next.getIdent())) {
                    throw new InvalidParamException("value", next,
                            getPropertyName(), ac);
                }
                order = val;
                _longhand_order.value = val;
            }
        }
        _longhand_fallbacks = new CssPositionTryFallbacks(ac, expression, check);
        if (order == null) {
            value = _longhand_fallbacks.value;
        } else {
            ArrayList<CssValue> values = new ArrayList<>();
            values.add(order);
            values.add(_longhand_fallbacks.value);
            value = new CssValueList(values);
        }
    }

    public CssPositionTry(ApplContext ac, CssExpression expression)
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
        _longhand_order.addToStyle(ac, style);
        _longhand_fallbacks.addToStyle(ac, style);
    }
}
