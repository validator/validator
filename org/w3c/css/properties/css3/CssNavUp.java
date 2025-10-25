//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-nav-up
 */
public class CssNavUp extends org.w3c.css.properties.css.CssNavUp {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"current", "root"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingNonUniqueIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        if (auto.equals(ident)) {
            return auto;
        }
        return getMatchingNonUniqueIdent(ident);
    }

    /**
     * Create a new CssNavUp
     */
    public CssNavUp() {
        value = initial;
    }

    /**
     * Create a new CssNavUp
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      true will test the number of parameters
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssNavUp(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();
        value = parseNav(ac, expression, check, this);
    }

    /**
     * Create a new CssNavUp
     *
     * @param ac,        the Context
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssNavUp(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return (auto == value) || (auto == initial);
    }

    // the main check is here (to be shared with other
    // nav-<dir>
    protected static CssValue parseNav(ApplContext ac, CssExpression expression,
                                       boolean check, CssProperty caller)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue value;
        CssValue val = expression.getValue();
        char op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_HASH_IDENT:
                value = val;
                // we got it, we must check if there are other values
                if (expression.getRemainingCount() > 1) {
                    if (op != SPACE) {
                        throw new InvalidParamException("operator",
                                Character.toString(op),
                                ac);
                    }
                    ArrayList<CssValue> values = new ArrayList<CssValue>();
                    values.add(val);
                    expression.next();
                    val = expression.getValue();
                    switch (val.getType()) {
                        case CssTypes.CSS_IDENT:
                            if (getMatchingNonUniqueIdent(val.getIdent()) == null) {
                                throw new InvalidParamException("value", val,
                                        caller.getPropertyName(), ac);
                            }
                            values.add(val);
                            break;
                        case CssTypes.CSS_STRING:
                            CssString s = val.getString();
                            if (s.toString().charAt(1) == '_') {
                                // TODO better error (do not start with _)
                                throw new InvalidParamException("value", val,
                                        caller.getPropertyName(), ac);
                            }
                            values.add(val);
                        default:
                            throw new InvalidParamException("value", val,
                                    caller.getPropertyName(), ac);
                    }
                    value = new CssValueList(values);
                }
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ide = val.getIdent();
                if (CssIdent.isCssWide(ide)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value", inherit,
                                caller.getPropertyName(), ac);
                    }
                    value = val;
                    break;
                } else if (auto.equals(ide)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value", auto,
                                caller.getPropertyName(), ac);
                    }
                    value = val;
                    break;
                }
                // let it fail
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        caller.getPropertyName(), ac);
        }
        expression.next();
        return value;
    }
}
