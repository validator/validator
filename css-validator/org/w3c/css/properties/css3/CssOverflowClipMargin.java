//
// Author: Yves Lafon <ylafon@w3.org>
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
 * @spec https://www.w3.org/TR/2025/WD-css-overflow-3-20251007/#propdef-overflow-clip-margin
 */
public class CssOverflowClipMargin extends org.w3c.css.properties.css.CssOverflowClipMargin {

    private static CssIdent[] visual_box_values;

    static {
        String id_values[] = {"content-box", "padding-box", "border-box"};
        visual_box_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            visual_box_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getVisualBoxIdent(CssIdent ident) {
        for (CssIdent id : visual_box_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssOverflowClipMargin
     */
    public CssOverflowClipMargin() {
        value = initial;
    }

    /**
     * Creates a new CssOverflowClipMargin
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssOverflowClipMargin(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        boolean got_length = false;
        boolean got_visualbox = false;
        CssValue val;
        char op;

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> v = new ArrayList<CssValue>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                    val.getCheckableValue().checkPositiveness(ac, this);
                    if (got_length) {
                        throw new InvalidParamException("value", val, getPropertyName(), ac);
                    }
                    v.add(val);
                    got_length = true;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    if (getVisualBoxIdent(id) != null) {
                        if (got_visualbox) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        got_visualbox = true;
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", val,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }

    public CssOverflowClipMargin(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

