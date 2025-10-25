//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2024.
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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-white-space-trim
 */
public class CssWhiteSpaceTrim extends org.w3c.css.properties.css.CssWhiteSpaceTrim {

    private final static CssIdent[] allowed_values;

    static {
        String[] id_values = {"discard-before", "discard-after", "discard-inner"};
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
     * Create a new CssWhiteSpaceTrim
     */
    public CssWhiteSpaceTrim() {
        value = initial;
    }

    /**
     * Creates a new CssWhiteSpaceTrim
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssWhiteSpaceTrim(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        value = checkWhiteSpaceTrim(ac, expression, this);
    }

    public CssWhiteSpaceTrim(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public static CssValue checkWhiteSpaceTrim(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        CssValue val;
        CssIdent id;
        char op;

        if (expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        caller.getPropertyName(), ac);
            }
            // ident, so inherit, or allowed value
            if (CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
            } else if (none.equals(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
            } else if ((id = getAllowedIdent(val.getIdent())) != null) {
                // check for duplicates.
                // TODO Should it be a warning instead?
                if (values.contains(id)) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
            } else {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        caller.getPropertyName(), ac);
            }
            expression.next();
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

}

