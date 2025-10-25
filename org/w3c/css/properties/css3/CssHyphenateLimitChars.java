//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2024.
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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-hyphenate-limit-chars
 */
public class CssHyphenateLimitChars extends org.w3c.css.properties.css.CssHyphenateLimitChars {

    private final static CssIdent[] allowed_values;

    static {
        String[] id_values = {"auto"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
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
     * Create a new CssHyphenateLimitChars
     */
    public CssHyphenateLimitChars() {
        value = initial;
    }

    /**
     * Creates a new CssHyphenateLimitChars
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssHyphenateLimitChars(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        CssValue val;
        CssIdent id;
        char op;

        setByUser();

        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    expression.getValue(),
                                    getPropertyName(), ac);
                        }
                        values.add(val);
                        break;
                    }
                    if (getAllowedIdent(val.getIdent()) != null) {
                        values.add(val);
                        break;
                    }
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkInteger(ac, this);
                    values.add(val);
                    break;
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

    public CssHyphenateLimitChars(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

