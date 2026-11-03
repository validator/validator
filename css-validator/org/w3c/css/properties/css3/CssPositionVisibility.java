//
// (c) COPYRIGHT W3C, 2026.
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
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-position-visibility retrieved 2026-11-03
 */
public class CssPositionVisibility extends org.w3c.css.properties.css.CssPositionVisibility {

    public static final CssIdent always = CssIdent.getIdent("always");
    public static final CssIdent[] conditions;

    static {
        String[] _conditions = {"anchor-valid", "anchor-visible", "no-overflow"};
        conditions = new CssIdent[_conditions.length];
        int i = 0;
        for (String s : _conditions) {
            conditions[i++] = CssIdent.getIdent(s);
        }
    }

    /**
     * Create a new CssPositionVisibility
     */
    public CssPositionVisibility() {
        value = initial;
    }

    /**
     * Creates a new CssPositionVisibility
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssPositionVisibility(ApplContext ac, CssExpression expression,
                                 boolean check) throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > conditions.length) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id) || always.equals(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
                return;
            }
        }
        // anchor-valid || anchor-visible || no-overflow
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssIdent> seen = new ArrayList<>();
        while (!expression.end()) {
            val = expression.getValue();
            char op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            CssIdent id = val.getIdent();
            if (!CssAnchorName.isOneOf(id, conditions) || seen.contains(id)) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            seen.add(id);
            values.add(val);
            expression.next();
            if (!expression.end() && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssPositionVisibility(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
