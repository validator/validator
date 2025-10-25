// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 2010.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background-repeat
 */
public class CssBackgroundRepeat extends org.w3c.css.properties.css.CssBackgroundRepeat {

    public final static CssIdent repeat;
    private static CssIdent[] allowed_simple_values;
    private static CssIdent[] allowed_double_values;

    static {
        String[] REPEAT = {"repeat", "space", "round", "no-repeat"};

        allowed_simple_values = new CssIdent[2];

        allowed_simple_values[0] = CssIdent.getIdent("repeat-x");
        allowed_simple_values[1] = CssIdent.getIdent("repeat-y");

        allowed_double_values = new CssIdent[REPEAT.length];

        int i = 0;
        for (String aREPEAT : REPEAT) {
            allowed_double_values[i++] = CssIdent.getIdent(aREPEAT);
        }
        repeat = CssIdent.getIdent("repeat");
    }

    public static boolean isMatchingIdent(CssIdent ident) {
        return (getMatchingIdent(ident) != null);
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_simple_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        for (CssIdent id : allowed_double_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getSimpleValue(CssIdent ident) {
        for (CssIdent id : allowed_simple_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getDoubleValue(CssIdent ident) {
        for (CssIdent id : allowed_double_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssBackgroundRepeat
     */
    public CssBackgroundRepeat() {
        value = repeat;
    }

    /**
     * Set the value of the property
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      is length checking needed
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBackgroundRepeat(ApplContext ac, CssExpression expression,
                               boolean check)
            throws InvalidParamException {

        setByUser();

        value = checkBackgroundRepeat(ac, expression, this);
    }


    public static CssValue checkBackgroundRepeat(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        boolean is_complete = true;
        CssValue val;
        CssValueList vl = null;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            // not an ident? fail
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", expression.getValue(),
                        caller.getPropertyName(), ac);
            }

            CssIdent id_val = val.getIdent();
            if (CssIdent.isCssWide(id_val)) {
                // if we got inherit after other values, fail
                // if we got more than one value... fail
                if ((values.size() > 0) || (expression.getCount() > 1)) {
                    throw new InvalidParamException("value", val,
                            caller.getPropertyName(), ac);
                }
                return val;
            } else {
                // check values that must be alone
                if (getSimpleValue(id_val) != null) {
                    // if we already have a double value... it's an error
                    if (!is_complete) {
                        throw new InvalidParamException("value",
                                val, caller.getPropertyName(), ac);
                    }
                    values.add(val);
                    is_complete = true;
                } else {
                    // the the one that may come in pairs
                    // not an allowed value !
                    if (getDoubleValue(id_val) == null) {
                        throw new InvalidParamException("value",
                                val, caller.getPropertyName(), ac);
                    }
                    if (is_complete) {
                        vl = new CssValueList();
                        vl.add(val);
                    } else {
                        vl.add(val);
                        values.add(vl);
                    }
                    is_complete = !is_complete;
                }
            }

            expression.next();
            if (!expression.end()) {
                // incomplete value followed by a comma... it's complete!
                if (!is_complete && (op == COMMA)) {
                    values.add(vl);
                    is_complete = true;
                }
                // complete values are separated by a comma, otherwise space
                if ((is_complete && (op != COMMA)) ||
                        (!is_complete && (op != SPACE))) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        // if we reach the end in a value that can come in pair
        if (!is_complete) {
            values.add(vl);
        }
        return (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssBackgroundRepeat(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (repeat == value);
    }

}



