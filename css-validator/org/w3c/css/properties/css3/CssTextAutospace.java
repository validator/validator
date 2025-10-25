//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang, 2012.
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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#text-autospace-property
 */
public class CssTextAutospace extends org.w3c.css.properties.css.CssTextAutospace {

    private static CssIdent[] allowed_values;
    private static CssIdent[] ideograph_axis, behaviour_axis;
    private static CssIdent single_val;

    static {
        String[] id_values = {"normal", "auto", "no-autospace", "ideograph-alpha", "ideograph-numeric", "punctuation",
                "insert", "replace"};
        String[] id_axis = {"ideograph-alpha", "ideograph-numeric", "punctuation"};
        String[] be_axis = {"insert", "replace"};
        single_val = CssIdent.getIdent("no-autospace");
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        ideograph_axis = new CssIdent[id_axis.length];
        i = 0;
        for (String s : id_axis) {
            ideograph_axis[i++] = CssIdent.getIdent(s);
        }
        behaviour_axis = new CssIdent[be_axis.length];
        i = 0;
        for (String s : be_axis) {
            behaviour_axis[i++] = CssIdent.getIdent(s);
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

    public static CssIdent getAutospaceIdent(CssIdent ident) {
        if (single_val.equals(ident)) {
            return single_val;
        }
        for (CssIdent id : ideograph_axis) {
            if (id.equals(ident)) {
                return id;
            }
        }
        for (CssIdent id : behaviour_axis) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static boolean isIdeographAxis(CssIdent ident) {
        for (CssIdent id : ideograph_axis) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBehaviourAxis(CssIdent ident) {
        for (CssIdent id : behaviour_axis) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new CssTextAutospace
     */
    public CssTextAutospace() {
        value = initial;
    }

    /**
     * Creates a new CssTextAutospace
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTextAutospace(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssIdent id;

        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        id = val.getIdent();
        if (CssIdent.isCssWide(id)) {
            if (expression.getCount() > 1) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            value = val;
            expression.next();
        } else if (getAllowedIdent(id) != null) {
            // ident or <autospace>
            if (getAutospaceIdent(id) != null) {
                // <autospace>
                value = checkAutoSpace(ac, expression, this);
            } else {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
            }
        } else {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }

    }


    public CssTextAutospace(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    // this function only check if the expression is an <autospace>
    public static CssValue checkAutoSpace(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        CssValue val;
        CssIdent id;
        char op;
        boolean got_behaviour = false;

        if (expression.getCount() > 4) {
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
            id = val.getIdent();
            if (getAutospaceIdent(id) != null) {
                if (!isIdeographAxis(id) && !isBehaviourAxis(id)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                caller.getPropertyName(), ac);
                    }
                    values.add(val);
                } else {
                    // check we don't have two of those
                    if (isBehaviourAxis(id)) {
                        if (got_behaviour) {
                            throw new InvalidParamException("value",
                                    expression.getValue(),
                                    caller.getPropertyName(), ac);
                        } else {
                            got_behaviour = true;
                        }
                    }
                    // avoid duplicates. TODO is that a good enough check? See attr/var
                    if (values.contains(id)) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                caller.getPropertyName(), ac);
                    }
                    values.add(val);
                }
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

