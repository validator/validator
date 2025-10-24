//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssBracket;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.properties.css3.CssGridAutoRows.parseFixedSize;
import static org.w3c.css.properties.css3.CssGridAutoRows.parseTrackSize;
import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/CRD-css-grid-1-20201218/#propdef-grid-template
 */
public class CssGridTemplate extends org.w3c.css.properties.css.CssGridTemplate {

    public static final CssIdent[] allowed_repeat_values;
    public static final String repeat_func = "repeat";

    static {
        String[] _allowed_repeat_values = {"auto-fill", "auto-fit"};
        allowed_repeat_values = new CssIdent[_allowed_repeat_values.length];
        int i = 0;
        for (String s : _allowed_repeat_values) {
            CssGridTemplate.allowed_repeat_values[i++] = CssIdent.getIdent(s);
        }
    }

    private CssGridTemplateAreas cssGridTemplateAreas;
    private CssGridTemplateColumns cssGridTemplateColumns;
    private CssGridTemplateRows cssGridTemplateRows;

    /**
     * Create a new CssGridTemplate
     */
    public CssGridTemplate() {
        value = initial;
        cssGridTemplateAreas = new CssGridTemplateAreas();
        cssGridTemplateColumns = new CssGridTemplateColumns();
        cssGridTemplateRows = new CssGridTemplateRows();
    }

    /**
     * Creates a new CssGridTemplate
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssGridTemplate(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        cssGridTemplateAreas = new CssGridTemplateAreas();
        cssGridTemplateColumns = new CssGridTemplateColumns();
        cssGridTemplateRows = new CssGridTemplateRows();


        value = parseGridTemplate(ac, expression, this, cssGridTemplateAreas,
                cssGridTemplateColumns, cssGridTemplateRows);
    }

    protected static CssValue parseGridTemplate(ApplContext ac, CssExpression expression,
                                                CssProperty caller,
                                                CssGridTemplateAreas areas,
                                                CssGridTemplateColumns columns,
                                                CssGridTemplateRows rows)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssValue> areaValues = new ArrayList<>();
        ArrayList<CssValue> columnValues = new ArrayList<>();
        ArrayList<CssValue> rowValues = new ArrayList<>();

        CssValue val = null;
        CssValue v;
        char op;

        if (expression.getCount() == 1) {
            // can only be 'none' or 'inherit'
            val = expression.getValue();
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
            }
            CssIdent id = val.getIdent();
            if (none.equals(id) || CssIdent.isCssWide(id)) {
                values.add(val);
                areaValues.add(val);
                columnValues.add(val);
                rowValues.add(val);
            } else {
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
            }
            expression.next();
        } else {
            // check if there is a CssString element to decide which case we are in.
            boolean got_string = false;

            while (!expression.end() && !got_string) {
                val = expression.getValue();
                got_string = (val.getType() == CssTypes.CSS_STRING);
                expression.next();
            }
            expression.starts();

            if (!got_string) {
                // we should have  <?grid-template-rows?> / <?grid-template-columns?>
                CssExpression nex = new CssExpression();
                boolean got_slash = false;
                while (!got_slash && !expression.end()) {
                    val = expression.getValue();
                    op = expression.getOperator();
                    got_slash = (val.getType() == CssTypes.CSS_SWITCH);
                    if (got_slash) {
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    caller.getPropertyName(), ac);
                        }
                    } else {
                        nex.addValue(val);
                        nex.setOperator(op);
                    }
                    expression.next();
                }
                if (!got_slash) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                v = parseTemplateRows(ac, nex, caller);
                rowValues.add(v);
                values.add(v);
                values.add(val);
                nex = new CssExpression();
                while (!expression.end()) {
                    val = expression.getValue();
                    op = expression.getOperator();
                    nex.addValue(val);
                    nex.setOperator(op);
                    expression.next();
                }
                v = parseTemplateRows(ac, nex, caller);
                columnValues.add(v);
                values.add(v);
                areaValues.add(none);
            } else {
                // [ <line-names>? <string> <track-size>? <line-names>? ]+ [ / <explicit-track-list> ]?
                boolean got_slash = false;
                CssExpression nex = new CssExpression();
                boolean in_line_names = false;
                int got_line_names = 1;     // why 1? because we can have only 1 <list-name> first

                while (!got_slash && !expression.end()) {
                    val = expression.getValue();
                    op = expression.getOperator();

                    switch (val.getType()) {
                        case CssTypes.CSS_STRING:
                            if (in_line_names) {
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        caller.getPropertyName(), ac);
                            }
                            got_line_names = 0;
                            areaValues.add(val);
                            values.add(val);
                            break;
                        case CssTypes.CSS_BRACKET:
                            CssBracket bracket = (CssBracket) val;
                            if (bracket.isLeft()) {
                                if (in_line_names || (got_line_names > 2)) {
                                    throw new InvalidParamException("value",
                                            val.toString(),
                                            caller.getPropertyName(), ac);
                                }
                                in_line_names = true;
                            } else { // bracket.isRight() but it can't be anything else...
                                if (!in_line_names) {
                                    throw new InvalidParamException("value",
                                            val.toString(),
                                            caller.getPropertyName(), ac);
                                }
                                got_line_names++;
                                in_line_names = false;
                            }
                            values.add(val);
                            rowValues.add(val);
                            break;
                        case CssTypes.CSS_SWITCH:
                            got_slash = true;
                            values.add(val);
                            break;
                        case CssTypes.CSS_IDENT:
                            if (in_line_names) {
                                values.add(val);
                                rowValues.add(val);
                                break;
                            }
                        default:
                            v = parseTrackSize(ac, val, caller);
                            values.add(v);
                            rowValues.add(v);
                    }
                    if (op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                caller.getPropertyName(), ac);
                    }
                    expression.next();
                }
                if (got_slash) {
                    while (!expression.end()) {
                        val = expression.getValue();
                        op = expression.getOperator();
                        nex.addValue(val);
                        nex.setOperator(op);
                        expression.next();
                    }
                    v = parseExplicitTrackList(ac, nex, caller);
                    columnValues.add(v);
                    values.add(v);
                } else {
                    columnValues.add(none);
                }
            }
        }
        if (areas != null) {
            areas.value = (areaValues.size() == 1) ? areaValues.get(0) : new CssValueList(areaValues);
        }
        if (columns != null) {
            columns.value = (columnValues.size() == 1) ? columnValues.get(0) : new CssValueList(columnValues);
        }
        if (rows != null) {
            rows.value = (rowValues.size() == 1) ? rowValues.get(0) : new CssValueList(rowValues);
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssGridTemplate(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public static CssIdent getAllowedRepeatIdent(CssIdent ident) {
        for (CssIdent id : allowed_repeat_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    protected static CssValue parseTemplateRows(ApplContext ac, CssExpression exp, CssProperty caller)
            throws InvalidParamException {
        if (exp.getCount() == 1) {
            CssValue val = exp.getValue();
            if (val.getType() == CssTypes.CSS_IDENT && none.equals(val.getIdent())) {
                exp.next();
                return val;
            }
        }
        exp.mark();
        try {
            return parseTrackList(ac, exp, caller);
        } catch (InvalidParamException ex) {
            // perhaps an AutoTrackList?
            exp.reset();
            return parseAutoTrackList(ac, exp, caller);
        }
    }

    protected static CssValue parseTrackList(ApplContext ac, CssExpression exp, CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val;
        char op;

        boolean in_line_names = false;
        boolean got_line_names = false;
        boolean got_size = false;
        while (!exp.end()) {
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_BRACKET:
                    CssBracket bracket = (CssBracket) val;
                    if (bracket.isLeft()) {
                        if (in_line_names || got_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        in_line_names = true;
                    } else { // bracket.isRight() but it can't be anything else...
                        if (!in_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        got_line_names = true;
                        in_line_names = false;
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (in_line_names) {
                        // todo check unreserved words
                        values.add(val);
                        break;
                    }
                    values.add(parseTrackSize(ac, val, caller));
                    got_line_names = false;
                    got_size = true;
                    break;
                case CssTypes.CSS_FUNCTION:
                    CssFunction function = val.getFunction();
                    if (repeat_func.equals(function.getName())) {
                        parseRepeatFunction(ac, function, RepeatType.TRACK_REPEAT, caller);
                        values.add(val);
                        got_line_names = false;
                        got_size = true;
                        break;
                    }
                    // not a repeat function, let it flow.
                default:
                    // should be a tracksize, or fail.
                    values.add(parseTrackSize(ac, val, caller));
                    got_size = true;
                    got_line_names = false;

            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        caller.getPropertyName(), ac);
            }
            exp.next();
        }
        if (values.isEmpty() || !got_size) {
            throw new InvalidParamException("unrecognize", ac);
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    protected static CssValue parseExplicitTrackList(ApplContext ac, CssExpression exp,
                                                     CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val;
        char op;

        boolean in_line_names = false;
        boolean got_line_names = false;
        boolean got_size = false;
        while (!exp.end()) {
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_BRACKET:
                    CssBracket bracket = (CssBracket) val;
                    if (bracket.isLeft()) {
                        if (in_line_names || got_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        in_line_names = true;
                    } else { // bracket.isRight() but it can't be anything else...
                        if (!in_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        got_line_names = true;
                        in_line_names = false;
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (in_line_names) {
                        // todo check unreserved words
                        values.add(val);
                        break;
                    }
                default:
                    // should be a tracksize, or fail.
                    values.add(parseTrackSize(ac, val, caller));
                    got_size = true;
                    got_line_names = false;

            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        caller.getPropertyName(), ac);
            }
            exp.next();
        }
        if (values.isEmpty() || !got_size) {
            throw new InvalidParamException("unrecognize", ac);
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    protected static CssValue parseAutoTrackList(ApplContext ac, CssExpression exp, CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<>();
        CssValue val;
        char op;

        boolean in_line_names = false;
        boolean got_line_names = false;
        boolean got_auto = false;
        while (!exp.end()) {
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_BRACKET:
                    CssBracket bracket = (CssBracket) val;
                    if (bracket.isLeft()) {
                        if (in_line_names || got_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        in_line_names = true;
                    } else { // bracket.isRight() but it can't be anything else...
                        if (!in_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        got_line_names = true;
                        in_line_names = false;
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (in_line_names) {
                        // todo check unreserved words
                        values.add(val);
                        break;
                    }
                    // no other ident allowed.
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
                case CssTypes.CSS_FUNCTION:
                    CssFunction function = val.getFunction();
                    if (repeat_func.equals(function.getName())) {
                        if (exp.getRemainingCount() == 1) {
                            parseRepeatFunction(ac, function, RepeatType.AUTO_REPEAT, caller);
                            values.add(val);
                            got_auto = true;
                        } else {
                            parseRepeatFunction(ac, function, RepeatType.FIXED_REPEAT, caller);
                            values.add(val);
                        }
                        got_line_names = false;
                        break;
                    }
                    // not a repeat function, let it flow.
                default:
                    // should be a tracksize, or fail.
                    values.add(parseTrackSize(ac, val, caller));
                    got_line_names = false;

            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        caller.getPropertyName(), ac);
            }
            exp.next();
        }
        if (values.isEmpty() || !got_auto) {
            throw new InvalidParamException("unrecognize", ac);
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    /**
     * @spec https://www.w3.org/TR/2017/CR-css-grid-1-20170209/#funcdef-repeat
     */
    protected static CssFunction parseRepeatFunction(ApplContext ac, CssFunction func,
                                                     RepeatType type,
                                                     CssProperty caller)
            throws InvalidParamException {
        CssExpression exp = func.getParameters();
        CssExpression nex;
        CssValue val;
        char op;

        if (exp.getCount() < 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        nex = new CssExpression();
        val = exp.getValue();
        op = exp.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                if ((getAllowedRepeatIdent(val.getIdent()) == null) || (type != RepeatType.AUTO_REPEAT)) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
                }
                break;
            case CssTypes.CSS_NUMBER:
                if (type != RepeatType.TRACK_REPEAT && type != RepeatType.FIXED_REPEAT) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
                }
                CssCheckableValue v = val.getCheckableValue();
                v.checkInteger(ac, caller);
                v.checkPositiveness(ac, caller);
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
        exp.next();
        boolean got_line_names = false;
        boolean in_line_names = false;

        while (!exp.end()) {
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_BRACKET:
                    CssBracket bracket = (CssBracket) val;
                    if (bracket.isLeft()) {
                        if (in_line_names || got_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        in_line_names = true;
                    } else { // bracket.isRight() but it can't be anything else...
                        if (!in_line_names) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                        }
                        got_line_names = true;
                        in_line_names = false;
                    }
                    break;
                case CssTypes.CSS_IDENT:
                    if (in_line_names) {
                        // todo check unreserved words
                        break;
                    }
                    // same branch for FLEX as it can only be TRACK_REPEAT.
                case CssTypes.CSS_FLEX:
                    if (type == RepeatType.TRACK_REPEAT) {
                        parseTrackSize(ac, val, caller);
                        got_line_names = false;
                    }
                    break;
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_FUNCTION:
                    switch (type) {
                        case AUTO_REPEAT:
                        case FIXED_REPEAT:
                            parseFixedSize(ac, val, caller);
                            break;
                        case TRACK_REPEAT:
                            parseTrackSize(ac, val, caller);
                            break;
                        default:
                            // wrong type?
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    caller.getPropertyName(), ac);
                    }
                    // we made it! now wait for a possible line name
                    got_line_names = false;
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        caller.getPropertyName(), ac);
            }
            exp.next();
        }
        exp.starts();
        // we reached the end without closing the line-names...
        if (in_line_names) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        return func;
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        cssGridTemplateAreas.addToStyle(ac, style);
        cssGridTemplateColumns.addToStyle(ac, style);
        cssGridTemplateRows.addToStyle(ac, style);
    }

    protected enum RepeatType {TRACK_REPEAT, AUTO_REPEAT, FIXED_REPEAT}
}

