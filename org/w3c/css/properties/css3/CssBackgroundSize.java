// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2010.
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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background-size
 */
public class CssBackgroundSize extends org.w3c.css.properties.css.CssBackgroundSize {

    private static CssIdent auto;
    private static CssIdent[] allowed_values;

    static {
        auto = CssIdent.getIdent("auto");
        allowed_values = new CssIdent[3];
        allowed_values[0] = auto;
        allowed_values[1] = CssIdent.getIdent("cover");
        allowed_values[2] = CssIdent.getIdent("contain");
    }

    public static boolean isMatchingIdent(CssIdent ident) {
        return (getMatchingIdent(ident) != null);
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
     * Create a new CssBackgroundSize
     */
    public CssBackgroundSize() {
        value = auto;
    }

    /**
     * Create a new CssBackgroundSize
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      if arguments count must be checked.
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssBackgroundSize(ApplContext ac, CssExpression expression,
                             boolean check) throws InvalidParamException {

        setByUser();

        value = checkBackgroundSize(ac, expression, this);
    }

    public static CssValue checkBackgroundSize(ApplContext ac, CssExpression ex,
                                               CssProperty caller)
            throws InvalidParamException {
        return checkBackgroundSize(ac, ex, caller.getPropertyName());

    }

    public static CssValue checkBackgroundSize(ApplContext ac, CssExpression expression,
                                               String caller) throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        char op;
        CssValue val;
        CssValueList vl = null;
        boolean is_complete = true;


        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    val.getCheckableValue().checkPositiveness(ac, caller);
                    if (is_complete) {
                        vl = new CssValueList();
                        vl.add(val);
                    } else {
                        vl.add(val);
                        values.add(vl);
                    }
                    is_complete = !is_complete;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        // if we got inherit after other values, fail
                        // if we got more than one value... fail
                        if ((values.size() > 0) || (expression.getCount() > 1)) {
                            throw new InvalidParamException("value", val,
                                    caller, ac);
                        }
                        values.add(val);
                        break;
                    } else if (auto.equals(id)) {
                        if (is_complete) {
                            vl = new CssValueList();
                            vl.add(val);
                        } else {
                            vl.add(val);
                            values.add(vl);
                        }
                        is_complete = !is_complete;
                        break;
                    } else {
                        // if ok, and if we are not in a middle of a compound
                        // value...
                        if ((getMatchingIdent(id) != null) && is_complete) {
                            values.add(val);
                            break;
                        }
                    }
                default:
                    throw new InvalidParamException("value", val,
                            caller, ac);

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
        if (values.size() == 1) {
            return values.get(0);
        } else {
            return new CssLayerList(values);
        }
    }

    public CssBackgroundSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (auto == value);
    }

}
