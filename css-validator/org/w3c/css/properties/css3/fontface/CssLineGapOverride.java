//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

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
 * @spec https://www.w3.org/TR/2026/WD-css-fonts-5-20260303/#descdef-font-face-line-gap-override
 */
public class CssLineGapOverride extends org.w3c.css.properties.css.fontface.CssLineGapOverride {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"normal"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssLineGapOverride
     */
    public CssLineGapOverride() {
        value = initial;
    }

    /**
     * Creates a new CssLineGapOverride
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssLineGapOverride(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        char op;
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_PERCENTAGE:
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (getAllowedIdent(val.getIdent()) != null) {
                        values.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && op != SPACE) {
                throw new InvalidParamException("operator", op, ac);
            }
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssLineGapOverride(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

