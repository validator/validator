//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2020/CRD-css-grid-1-20201218/#propdef-grid-auto-rows
 */
public class CssGridAutoRows extends org.w3c.css.properties.css.CssGridAutoRows {

    public static final CssIdent[] allowed_values;
    public static final String minmax = "minmax";
    public static final String fit_content = "fit-content";

    protected enum ArgType {INFLEXIBLE_BREADTH, TRACK_BREADTH, FIXED_BREADTH}

    static {
        String[] _allowed_values = {"min-content", "max-content", "auto"};
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
     * Create a new CssGridAutoRows
     */
    public CssGridAutoRows() {
        value = initial;
    }

    /**
     * Creates a new CssGridAutoRows
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssGridAutoRows(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;

        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT && CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                values.add(val);
            } else {
                parseTrackSize(ac, val, this);
                values.add(val);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    protected static CssValue parseTrackSize(ApplContext ac, CssValue value,
                                             CssProperty caller)
            throws InvalidParamException {
        switch (value.getType()) {
            case CssTypes.CSS_IDENT:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_FLEX:
                return parseTrackBreadth(ac, value, caller);
            case CssTypes.CSS_FUNCTION:
                CssFunction function = value.getFunction();
                String fname = function.getName().toLowerCase();
                if (minmax.equals(fname)) {
                    return parseMinmaxFunction(ac, function,
                            ArgType.INFLEXIBLE_BREADTH,
                            ArgType.TRACK_BREADTH, caller);
                } else if (fit_content.equals(fname)) {
                    return parseFitContent(ac, function, caller);
                }
            default:
                throw new InvalidParamException("value",
                        value.toString(),
                        caller.getPropertyName(), ac);
        }
    }

    protected static CssValue parseFixedSize(ApplContext ac, CssValue value,
                                             CssProperty caller)
            throws InvalidParamException {
        switch (value.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                return parseFixedBreadth(ac, value, caller);
            case CssTypes.CSS_FUNCTION:
                CssFunction function = value.getFunction();
                String fname = function.getName().toLowerCase();
                if (minmax.equals(fname)) {
                    try {
                        return parseMinmaxFunction(ac, function,
                                ArgType.FIXED_BREADTH,
                                ArgType.TRACK_BREADTH, caller);
                    } catch (InvalidParamException ex) {
                        // we failed with the first option
                        // ignore and try the second one.
                        function.getParameters().starts();
                    }
                    return parseMinmaxFunction(ac, function,
                            ArgType.INFLEXIBLE_BREADTH,
                            ArgType.FIXED_BREADTH, caller);

                } else if (fit_content.equals(fname)) {
                    return parseFitContent(ac, function, caller);
                }
            default:
                throw new InvalidParamException("value",
                        value.toString(),
                        caller.getPropertyName(), ac);
        }

    }

    protected static CssValue parseTrackBreadth(ApplContext ac, CssValue value,
                                                CssProperty caller)
            throws InvalidParamException {

        switch (value.getType()) {
            case CssTypes.CSS_NUMBER:
                value.getCheckableValue().checkEqualsZero(ac, caller);
                return value;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_FLEX:
                value.getCheckableValue().checkPositiveness(ac, caller);
                return value;
            case CssTypes.CSS_IDENT:
                if (getAllowedIdent(value.getIdent()) != null) {
                    return value;
                }
                // else fail
            default:
                throw new InvalidParamException("value",
                        value.toString(),
                        caller.getPropertyName(), ac);
        }
    }

    protected static CssValue parseInflexibleBreadth(ApplContext ac, CssValue value,
                                                     CssProperty caller)
            throws InvalidParamException {
        CssIdent ident;

        switch (value.getType()) {
            case CssTypes.CSS_NUMBER:
                value.getCheckableValue().checkEqualsZero(ac, caller);
                return value;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                value.getCheckableValue().checkPositiveness(ac, caller);
                return value;
            case CssTypes.CSS_IDENT:
                if (getAllowedIdent(value.getIdent()) != null) {
                    return value;
                }
                // else fail
            default:
                throw new InvalidParamException("value",
                        value.toString(),
                        caller.getPropertyName(), ac);
        }
    }

    protected static CssValue parseFixedBreadth(ApplContext ac, CssValue value,
                                                CssProperty caller)
            throws InvalidParamException {
        switch (value.getType()) {
            case CssTypes.CSS_NUMBER:
                value.getCheckableValue().checkEqualsZero(ac, caller);
                return value;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                value.getCheckableValue().checkPositiveness(ac, caller);
                return value;
            default:
                throw new InvalidParamException("value",
                        value.toString(),
                        caller.getPropertyName(), ac);
        }
    }

    protected static CssFunction parseMinmaxFunction(ApplContext ac, CssFunction func,
                                                     ArgType type1, ArgType type2,
                                                     CssProperty caller)
            throws InvalidParamException {
        CssExpression exp = func.getParameters();
        CssExpression nex;
        CssValue val;
        char op;

        if (exp.getCount() != 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        nex = new CssExpression();
        val = exp.getValue();
        op = exp.getOperator();

        switch (type1) {
            case INFLEXIBLE_BREADTH:
                nex.addValue(parseInflexibleBreadth(ac, val, caller));
                break;
            case TRACK_BREADTH:
                nex.addValue(parseTrackBreadth(ac, val, caller));
                break;
            case FIXED_BREADTH:
                nex.addValue(parseFixedBreadth(ac, val, caller));
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
        }
        if (op != COMMA) {
            throw new InvalidParamException("operator", op,
                    caller.getPropertyName(), ac);
        }
        nex.setOperator(op);
        exp.next();
        val = exp.getValue();
        switch (type2) {
            case INFLEXIBLE_BREADTH:
                nex.addValue(parseInflexibleBreadth(ac, val, caller));
                break;
            case TRACK_BREADTH:
                nex.addValue(parseTrackBreadth(ac, val, caller));
                break;
            case FIXED_BREADTH:
                nex.addValue(parseFixedBreadth(ac, val, caller));
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
        }
        func.set(minmax, nex);
        return func;
    }

    protected static CssFunction parseFitContent(ApplContext ac, CssFunction func,
                                                 CssProperty caller)
            throws InvalidParamException {
        CssExpression exp = func.getParameters();
        CssValue val;
        char op;

        if (exp.getCount() != 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        val = exp.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, caller);
                break;
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                val.getCheckableValue().checkPositiveness(ac, caller);
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
        }
        func.set(minmax, exp);
        return func;
    }


    public CssGridAutoRows(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

