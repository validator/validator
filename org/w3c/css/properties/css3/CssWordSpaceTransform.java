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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-word-space-transform
 */
public class CssWordSpaceTransform extends org.w3c.css.properties.css.CssWordSpaceTransform {

    private static CssIdent[] allowed_action_values;
    private static CssIdent autoPhrase;


    static {
        autoPhrase = CssIdent.getIdent("auto-phrase");

        String id_values[] = {"space", "ideographic-space"};
        allowed_action_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_action_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingMainIdent(CssIdent ident) {
        for (CssIdent id : allowed_action_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssWordSpaceTransform
     */
    public CssWordSpaceTransform() {
        value = initial;
    }

    /**
     * Creates a new CssWordSPaceTransform
     *
     * @param expression The expression for this property
     * @throws InvalidParamException
     *          Expressions are incorrect
     */
    public CssWordSpaceTransform(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();
        char op;
        ArrayList<CssValue> values = new ArrayList<>();
        boolean got_main = false;
        boolean got_auto = false;

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            // ident, so inherit, or allowed value
            if (CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
            } else if (none.equals(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
            } else if (autoPhrase.equals(val.getIdent()) && !got_auto) {
                got_auto = true;
                values.add(val);
            } else if (!got_main) {
                if (getMatchingMainIdent(val.getIdent()) == null) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_main = true;
                values.add(val);
            } else {
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

    public CssWordSpaceTransform(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

