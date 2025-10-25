//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec https://www.w3.org/TR/2015/CR-css-will-change-1-20151203/#propdef-will-change
 */
public class CssWillChange extends org.w3c.css.properties.css.CssWillChange {

    public static final CssIdent[] allowed_values;
    public static final CssIdent[] excluded_values;
    public static final CssIdent auto = CssIdent.getIdent("auto");

    static {
        String[] _allowed_values = {"scroll-position", "contents"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        String[] _excluded_values = {"will-change", "none", "all", "auto",
                "scroll-position", "contents"};
        i = 0;
        excluded_values = new CssIdent[_excluded_values.length];
        for (String s : _excluded_values) {
            excluded_values[i++] = CssIdent.getIdent(s);
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

    public static boolean isExcludedIdent(CssIdent ident) {
        for (CssIdent id : excluded_values) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new CssWillChange
     */
    public CssWillChange() {
        value = initial;
    }

    /**
     * Creates a new CssWillChange
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssWillChange(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op;
        CssIdent id, ident;

        setByUser();

        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            id = val.getIdent();
            if (auto.equals(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                values.add(val);
            } else {
                ident = getAllowedIdent(id);
                if (ident != null) {
                    values.add(val);
                } else {
                    // custom-ident
                    if (CssIdent.isCssWide(id) || isExcludedIdent(id)) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                }
            }
            if (op != COMMA && expression.getRemainingCount() > 1) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssWillChange(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}



