//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.counterstyle;

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
 * @spec https://www.w3.org/TR/2015/CR-css-counter-styles-3-20150611/#descdef-counter-style-system
 */
public class CssSystem extends org.w3c.css.properties.css.counterstyle.CssSystem {

    public static final CssIdent[] allowed_values;
    public static final CssIdent fixed;
    public static final CssIdent id_extends;

    static {
        fixed = CssIdent.getIdent("fixed");
        id_extends = CssIdent.getIdent("extends");

        String[] _allowed_values = {"cyclic", "numeric", "alphabetic", "symbolic", "additive"};
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
     * Create a new CssSystem
     */
    public CssSystem() {
        value = initial;
    }

    /**
     * Creates a new CssSystem
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssSystem(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();
        CssIdent id, ident;
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>(2);
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            ident = (CssIdent) val;
            id = getAllowedIdent(ident);
            if (id == null) {
                // now try special parsed values
                if (fixed.equals(ident)) {
                    values.add(fixed);
                    // optional integer
                    if (expression.getRemainingCount() > 1) {
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                        val = expression.getValue();
                        if (val.getType() == CssTypes.CSS_NUMBER) {
                            val.getCheckableValue().checkInteger(ac, this);
                            values.add(val);
                        } else {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                    }
                } else if (id_extends.equals(ident)) {
                    values.add(id_extends);
                    // we must have two values
                    expression.next();
                    if (expression.end()) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    if (op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                getPropertyName(), ac);
                    }
                    val = expression.getValue();
                    if (val.getType() != CssTypes.CSS_IDENT) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    ident = (CssIdent) val;
                    // unreserved ident only
                    if (CssIdent.isCssWide(ident)) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    values.add(ident);
                } else {
                    // not a valid value
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
            } else {
                if (expression.getCount() > 1) {
                    val = expression.getNextValue();
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                values.add(id);
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);

    }

    public CssSystem(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

