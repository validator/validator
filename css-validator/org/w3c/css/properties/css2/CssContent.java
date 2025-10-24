//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2008/REC-CSS2-20080411/generate.html#content
 */
public class CssContent extends org.w3c.css.properties.css.CssContent {

    protected static CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"open-quote", "close-quote", "no-open-quote", "no-close-quote"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
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
     * Create a new CssContent
     */
    public CssContent() {
    }

    /**
     * Creates a new CssContent
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssContent(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        ArrayList<CssValue> values;

        setByUser();
        values = new ArrayList<>();
        CssValue val;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_STRING:
                case CssTypes.CSS_URL:
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    checkCounterFunction(ac, this, val);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        values.add(inherit);
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        break;
                    }
                    value = getMatchingIdent((CssIdent) val);
                    if (value != null) {
                        values.add(val);
                        break;
                    }
                    // if not recognized... it can be a color.
                default:
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);

            }
            if ((op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    // check the value of counter and counters function
    // per https://www.w3.org/TR/2008/REC-CSS2-20080411/syndata.html#value-def-counter
    protected static void checkCounterFunction(ApplContext ac,
                                               CssProperty property,
                                               CssValue function)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;
        CssFunction f = (CssFunction) function;

        switch (f.getName()) {
            case "counter":
                exp = f.getParameters();
                // must be counter(name [,style?])
                if (exp.getCount() > 2) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                v = exp.getValue();
                op = exp.getOperator();
                if (v.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value", v,
                            property.getPropertyName(), ac);
                }
                exp.next();
                if (!exp.end()) {
                    // we have another item, it must be an ident matching list-style-type.
                    if (op != COMMA) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                    v = exp.getValue();
                    if (v.getType() == CssTypes.CSS_IDENT) {
                        if (null == CssListStyleType.getAllowedIdent((CssIdent) v)) {
                            throw new InvalidParamException("value", v,
                                    property.getPropertyName(), ac);
                        }
                    }
                }
                break;
            case "counters":
                exp = f.getParameters();
                // must be counter(name string[,style?])
                if (exp.getCount() < 2 || exp.getCount() > 3) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                v = exp.getValue();
                op = exp.getOperator();
                if (v.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value", v,
                            property.getPropertyName(), ac);
                }
                exp.next();
                if (op != COMMA) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                v = exp.getValue();
                op = exp.getOperator();
                if (v.getType() != CssTypes.CSS_STRING) {
                    throw new InvalidParamException("value", v,
                            property.getPropertyName(), ac);
                }
                exp.next();
                if (!exp.end()) {
                    // we have another item, it must be an ident matching list-style-type.
                    if (op != COMMA) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                    v = exp.getValue();
                    if (v.getType() == CssTypes.CSS_IDENT) {
                        if (null == CssListStyleType.getAllowedIdent((CssIdent) v)) {
                            throw new InvalidParamException("value", v,
                                    property.getPropertyName(), ac);
                        }
                    }
                }
                break;
            default:
                throw new InvalidParamException("value", function,
                        property.getPropertyName(), ac);
        }
    }

    public CssContent(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

