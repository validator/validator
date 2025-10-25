//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2020.
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
 * @spec https://www.w3.org/TR/2020/WD-css-color-adjust-1-20200402/#propdef-color-scheme
 */
public class CssColorScheme extends org.w3c.css.properties.css.CssColorScheme {

    private static CssIdent[] allowed_values;
    private static CssIdent light, only, normal;

    static {
        String id_values[] = {"light", "dark"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        light = CssIdent.getIdent("light");
        only = CssIdent.getIdent("only");
        normal = CssIdent.getIdent("normal");
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssColorScheme
     */
    public CssColorScheme() {
        value = initial;
    }

    /**
     * Creates a new CssColorScheme
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssColorScheme(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val;
        char op;
        boolean gotLight = false;
        boolean gotOnly = false;

        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            // ident, so inherit, or allowed value
            CssIdent id = val.getIdent();
            if (inherit.equals(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                value = val;
            } else if (normal.equals(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                value = val;
            } else if (only.equals(id)) {
                if (gotOnly) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                if (expression.getCount() > 2) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                gotOnly = true;
                values.add(val);
            } else if (light.equals(id)) {
                gotLight = true;
                values.add(val);
            } else {
                values.add(val);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
            expression.next();
        }
        if (gotOnly && !gotLight) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (!values.isEmpty()) {
            value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        }
    }

    public CssColorScheme(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

