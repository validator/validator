// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
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
 * @spec https://www.w3.org/TR/2021/WD-css-transforms-2-20211109/#propdef-perspective-origin
 */
public class CssPerspectiveOrigin extends org.w3c.css.properties.css.CssPerspectiveOrigin {

    public static CssIdent[] allowed_values;
    public static CssIdent center, top, bottom, left, right;

    // FIXME this should be a generic 'position' parsing
    
    static {
        top = CssIdent.getIdent("top");
        bottom = CssIdent.getIdent("bottom");
        left = CssIdent.getIdent("left");
        right = CssIdent.getIdent("right");
        center = CssIdent.getIdent("center");
        allowed_values = new CssIdent[5];
        allowed_values[0] = top;
        allowed_values[1] = bottom;
        allowed_values[2] = left;
        allowed_values[3] = right;
        allowed_values[4] = center;
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static boolean isVerticalIdent(CssIdent ident) {
        return top.equals(ident) || bottom.equals(ident);
    }

    public static boolean isHorizontalIdent(CssIdent ident) {
        return left.equals(ident) || right.equals(ident);
    }

    /**
     * Create a new CssPerspectiveOrigin
     */
    public CssPerspectiveOrigin() {
        value = initial;
    }

    /**
     * Creates a new CssPerspectiveOrigin
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssPerspectiveOrigin(ApplContext ac, CssExpression expression,
                                boolean check) throws InvalidParamException {

        int nb_val = expression.getCount();

        if (check && nb_val > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val;
        char op;
        ArrayList<CssValue> values = new ArrayList<CssValue>();

        // we just accumulate values and check at validation
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if ((val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
                return;
            }
            // we will check later
            values.add(val);
            expression.next();

            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        // if we reach the end in a value that can come in pair
        parsePerspectiveOrigin(ac, values, getPropertyName());
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    // check the value
    // TODO FIXME do it right using the epxression instead
    protected static void parsePerspectiveOrigin(ApplContext ac, ArrayList<CssValue> values,
                                                 String caller)
            throws InvalidParamException {
        int nb_keyword = 0;
        int nb_values = values.size();

        if (nb_values > 2 || nb_values == 0) {
            throw new InvalidParamException("unrecognize", ac);
        }
        // basic check
        for (CssValue aValue : values) {
            switch (aValue.getType()) {
                case CssTypes.CSS_NUMBER:
                    aValue.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    break;
                case CssTypes.CSS_IDENT:
                    nb_keyword++;
                    break;
                default:
                    throw new InvalidParamException("value", aValue,
                            caller, ac);
            }
        }
        // then we need to ckeck the values if we got two values and
        // at least one keyword (as restrictions may occur)
        if (nb_keyword > 0 && nb_values == 2) {
            boolean gothorizontal = false;
            boolean gotvertical = false;
            CssValue v = values.get(0);
            if (v.getType() == CssTypes.CSS_IDENT) {
                CssIdent id = v.getIdent();
                // strictly horizontal or vertical
                gothorizontal = isHorizontalIdent(id);
                if (!gothorizontal) {
                    gotvertical = isVerticalIdent(id);
                }
            }
            v = values.get(1);
            if (v.getType() == CssTypes.CSS_IDENT) {
                CssIdent id = v.getIdent();
                // yeah, it can be a single ugly test.
                if (gothorizontal && isHorizontalIdent(id)) {
                    throw new InvalidParamException("value", id,
                            caller, ac);
                }
                if (gotvertical && isVerticalIdent(id)) {
                    throw new InvalidParamException("value", id,
                            caller, ac);
                }
            }
        }
    }

    public CssPerspectiveOrigin(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
