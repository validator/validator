//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
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
 * @spec https://www.w3.org/TR/2019/WD-css-content-3-20190802/#propdef-content
 */
public class CssContent extends org.w3c.css.properties.css.CssContent {

    private static CssIdent normal = CssIdent.getIdent("normal");
    private static CssIdent contents = CssIdent.getIdent("contents");
    protected static CssIdent[] allowed_quote_values, allowed_target_text_values;
    protected static CssIdent[] allowed_leader_values;

    static {
        String[] _allowed_values = {"open-quote", "close-quote", "no-open-quote", "no-close-quote"};
        int i = 0;
        allowed_quote_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_quote_values[i++] = CssIdent.getIdent(s);
        }

        String[] _target_text = {"content", "before", "after", "first-letter"};
        i = 0;
        allowed_target_text_values = new CssIdent[_target_text.length];
        for (String s : _target_text) {
            allowed_target_text_values[i++] = CssIdent.getIdent(s);
        }

        String[] _leader = {"dotted", "solid", "space"};
        i = 0;
        allowed_leader_values = new CssIdent[_leader.length];
        for (String s : _leader) {
            allowed_leader_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingQuoteIdent(CssIdent ident) {
        for (CssIdent id : allowed_quote_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getTargetTextIdent(CssIdent ident) {
        for (CssIdent id : allowed_target_text_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getLeaderIdent(CssIdent ident) {
        for (CssIdent id : allowed_leader_values) {
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
        value = initial;
    }

    /**
     * Creates a new CssContent
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssContent(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        ArrayList<CssValue> values;

        setByUser();
        values = new ArrayList<>();
        CssValue val;
        char op;
        boolean gotSlash = false;

        while (!gotSlash && !expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_SWITCH:
                    gotSlash = true;
                    values.add(val);
                    break;
                case CssTypes.CSS_IMAGE:
                case CssTypes.CSS_STRING:
                case CssTypes.CSS_URL:
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    CssFunction f = val.getFunction();
                    switch (f.getName()) {
                        case "counter":
                            checkCounterFunction(ac, f, this);
                            break;
                        case "counters":
                            checkCountersFunction(ac, f, this);
                            break;
                        case "target-counter":
                            checkTargetCounterFunction(ac, f, this);
                            break;
                        case "target-counters":
                            checkTargetCountersFunction(ac, f, this);
                            break;
                        case "target-text":
                            checkTargetTextFunction(ac, f, this);
                            break;
                        case "leader":
                            checkLeaderFunction(ac, f, this);
                            break;
                        default:
                            throw new InvalidParamException("value",
                                    val, getPropertyName(), ac);
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        values.add(val);
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        break;
                    }
                    if (normal.equals(id)) {
                        values.add(val);
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        break;
                    }
                    if (none.equals(id)) {
                        values.add(val);
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        break;
                    }
                    if (getMatchingQuoteIdent(id) != null) {
                        values.add(val);
                        break;
                    }
                    if (contents.equals(id)) {
                        values.add(val);
                        break;
                    }
                    // if not recognized... it can be a color.
                default:
                    throw new InvalidParamException("value",
                            val, getPropertyName(), ac);

            }
            if ((op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (gotSlash) {
            boolean gotOne = false;
            // second part of parsing
            while (!expression.end()) {
                val = expression.getValue();
                op = expression.getOperator();

                switch (val.getType()) {
                    case CssTypes.CSS_FUNCTION:
                        CssFunction f = val.getFunction();
                        switch (f.getName()) {
                            case "counter":
                                checkCounterFunction(ac, f, this);
                                break;
                            case "counters":
                                checkCountersFunction(ac, f, this);
                                break;
                            default:
                                throw new InvalidParamException("value",
                                        val, getPropertyName(), ac);
                        }
                        values.add(val);
                        break;
                    case CssTypes.CSS_STRING:
                        values.add(val);
                        break;
                }
                gotOne = true;
                if ((op != SPACE)) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                expression.next();
            }
            if (!gotOne) {
                throw new InvalidParamException("unrecognize", ac);
            }
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    // check the value of counter  function
    protected static void checkCounterFunction(ApplContext ac,
                                               CssFunction function, CssProperty property)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;

        if (!function.getName().equals("counter")) {
            throw new InvalidParamException("unrecognize", ac);
        }

        exp = function.getParameters();
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
                if (null == CssListStyleType.getAllowedIdent(v.getIdent())) {
                    // here we should check existing name counter-style.
                    // and release a warning if not
                    // we should first add a warning here to highlight the issue
                    // (non-existent style should behave like decimal)
                    // throw new InvalidParamException("value", v,
                    //                                  property.getPropertyName(), ac);
                }
            } else {
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
            }
        }
    }

    // check the value of counters function
    protected static void checkCountersFunction(ApplContext ac,
                                                CssFunction function, CssProperty property)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;

        if (!function.getName().equals("counters")) {
            throw new InvalidParamException("unrecognize", ac);
        }

        exp = function.getParameters();
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
                if (null == CssListStyleType.getAllowedIdent(v.getIdent())) {
                    throw new InvalidParamException("value", v,
                            property.getPropertyName(), ac);
                }
            }
        }

    }

    // https://www.w3.org/TR/2019/WD-css-content-3-20190802/#funcdef-target-counter
    protected static void checkTargetCounterFunction(ApplContext ac,
                                                     CssFunction function, CssProperty property)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;

        if (!function.getName().equals("target-counter")) {
            throw new InvalidParamException("unrecognize", ac);
        }

        exp = function.getParameters();
        if (exp.getCount() < 2 || exp.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        v = exp.getValue();
        op = exp.getOperator();
        switch (v.getType()) {
            case CssTypes.CSS_URL:
            case CssTypes.CSS_STRING:
                break;
            default:
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
        }
        if (op != COMMA) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        exp.next();

        v = exp.getValue();
        op = exp.getOperator();
        if (v.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", v,
                    property.getPropertyName(), ac);
        }
        if (CssIdent.isCssWide(v.getIdent())) {
            throw new InvalidParamException("value", v,
                    property.getPropertyName(), ac);
        }
        exp.next();
        if (!exp.end()) {
            if (op != COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            v = exp.getValue();
            CssExpression e = new CssExpression();
            e.addValue(v);
            try {
                CssListStyleType listStyleType = new CssListStyleType(ac, e, false);
            } catch (Exception ex) {
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
            }
        }
    }

    // https://www.w3.org/TR/2019/WD-css-content-3-20190802/#funcdef-target-counters
    protected static void checkTargetCountersFunction(ApplContext ac,
                                                      CssFunction function, CssProperty property)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;

        if (!function.getName().equals("target-counters")) {
            throw new InvalidParamException("unrecognize", ac);
        }

        exp = function.getParameters();
        if (exp.getCount() < 3 || exp.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        v = exp.getValue();
        op = exp.getOperator();
        switch (v.getType()) {
            case CssTypes.CSS_URL:
            case CssTypes.CSS_STRING:
                break;
            default:
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
        }
        if (op != COMMA) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        exp.next();

        v = exp.getValue();
        op = exp.getOperator();
        if (v.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", v,
                    property.getPropertyName(), ac);
        }
        if (CssIdent.isCssWide(v.getIdent())) {
            throw new InvalidParamException("value", v,
                    property.getPropertyName(), ac);
        }
        if (op != COMMA) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        exp.next();

        v = exp.getValue();
        op = exp.getOperator();
        if (v.getType() != CssTypes.CSS_STRING) {
            throw new InvalidParamException("value", v,
                    property.getPropertyName(), ac);
        }
        exp.next();

        if (!exp.end()) {
            if (op != COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            v = exp.getValue();
            CssExpression e = new CssExpression();
            e.addValue(v);
            try {
                CssListStyleType listStyleType = new CssListStyleType(ac, e, false);
            } catch (Exception ex) {
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
            }
        }
    }

    // https://www.w3.org/TR/2019/WD-css-content-3-20190802/#funcdef-target-text
    protected static void checkTargetTextFunction(ApplContext ac,
                                                  CssFunction function, CssProperty property)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;

        if (!function.getName().equals("target-text")) {
            throw new InvalidParamException("unrecognize", ac);
        }

        exp = function.getParameters();
        if (exp.getCount() < 1 || exp.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        v = exp.getValue();
        op = exp.getOperator();
        switch (v.getType()) {
            case CssTypes.CSS_URL:
            case CssTypes.CSS_STRING:
                break;
            default:
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
        }
        exp.next();

        if (!exp.end()) {
            if (op != COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            v = exp.getValue();
            if (v.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
            }
            if (getTargetTextIdent(v.getIdent()) == null) {
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
            }
        }
    }

    // https://www.w3.org/TR/2019/WD-css-content-3-20190802/#funcdef-target-text
    protected static void checkLeaderFunction(ApplContext ac,
                                              CssFunction function, CssProperty property)
            throws InvalidParamException {
        CssExpression exp;
        char op;
        CssValue v;

        if (!function.getName().equals("leader")) {
            throw new InvalidParamException("unrecognize", ac);
        }

        exp = function.getParameters();
        if (exp.getCount() != 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        v = exp.getValue();
        op = exp.getOperator();
        switch (v.getType()) {
            case CssTypes.CSS_STRING:
                break;
            case CssTypes.CSS_IDENT:
                if (getLeaderIdent(v.getIdent()) != null) {
                    break;
                }
            default:
                throw new InvalidParamException("value", v,
                        property.getPropertyName(), ac);
        }
        exp.next();
    }

    public CssContent(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

