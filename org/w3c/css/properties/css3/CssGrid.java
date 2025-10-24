//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.properties.css3.CssGridTemplate.parseGridTemplate;
import static org.w3c.css.properties.css3.CssGridTemplate.parseTemplateRows;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/CRD-css-grid-1-20201218/#propdef-grid
 */
public class CssGrid extends org.w3c.css.properties.css.CssGrid {

    public static final CssIdent auto_flow;

    static {
        auto_flow = CssIdent.getIdent("auto-flow");
    }

    private CssGridTemplateRows cssGridTemplateRows;
    private CssGridTemplateColumns cssGridTemplateColumns;
    private CssGridTemplateAreas cssGridTemplateAreas;
    private CssGridAutoColumns cssGridAutoColumns;
    private CssGridAutoFlow cssGridAutoFlow;
    private CssGridAutoRows cssGridAutoRows;

    /**
     * Create a new CssGridArea
     */
    public CssGrid() {
        value = initial;
        cssGridTemplateAreas = new CssGridTemplateAreas();
        cssGridTemplateColumns = new CssGridTemplateColumns();
        cssGridTemplateRows = new CssGridTemplateRows();
        cssGridAutoColumns = new CssGridAutoColumns();
        cssGridAutoFlow = new CssGridAutoFlow();
        cssGridAutoRows = new CssGridAutoRows();
    }

    /**
     * Creates a new CssGridArea
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssGrid(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssExpression exp;

        CssValue v, val;
        char op;

        cssGridTemplateAreas = new CssGridTemplateAreas();
        cssGridTemplateColumns = new CssGridTemplateColumns();
        cssGridTemplateRows = new CssGridTemplateRows();
        cssGridAutoColumns = new CssGridAutoColumns();
        cssGridAutoFlow = new CssGridAutoFlow();
        cssGridAutoRows = new CssGridAutoRows();

        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssValue> autoFlowValues = new ArrayList<>();
        ArrayList<CssValue> templateRows = new ArrayList<>();
        ArrayList<CssValue> templateColumns = new ArrayList<>();
        ArrayList<CssValue> autoRows = new ArrayList<>();
        ArrayList<CssValue> autoColumns = new ArrayList<>();

        boolean got_auto_flow = false;
        boolean got_switch = false;
        boolean auto_flow_first = false;


        expression.mark();
        while (!expression.end() && !(got_auto_flow && got_switch)) {
            val = expression.getValue();
            if (val.getType() == CssTypes.CSS_SWITCH) {
                got_switch = true;
                if (got_auto_flow) {
                    auto_flow_first = true;
                } // else defaults to false
            } else if (val.getType() == CssTypes.CSS_IDENT) {
                got_auto_flow = auto_flow.equals(val.getIdent());
            }
            expression.next();
        }
        expression.reset();

        // getting auto-flow implies '/'
        if (got_auto_flow && !got_switch) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if (got_auto_flow) {
            if (auto_flow_first) {
                // [ auto-flow && dense? ] <?grid-auto-rows?>? / <?grid-template-columns?>
                val = expression.getValue();
                op = expression.getOperator();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                CssIdent id = val.getIdent();
                if (auto_flow.equals(id)) {
                    values.add(val);
                    autoFlowValues.add(CssGridAutoFlow.row);
                    // optional 'dense'
                    if (op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                getPropertyName(), ac);
                    }
                    expression.next();
                    val = expression.getValue();
                    op = expression.getOperator();
                    if (val.getType() == CssTypes.CSS_IDENT && CssGridAutoFlow.dense.equals(val.getIdent())) {
                        values.add(val);
                        autoFlowValues.add(val);
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                    }
                } else {
                    if (CssGridAutoFlow.dense.equals(id)) {
                        values.add(val);
                        autoFlowValues.add(val);
                        // mandatory 'auto-flow'
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                        val = expression.getValue();
                        op = expression.getOperator();
                        if (val.getType() == CssTypes.CSS_IDENT && auto_flow.equals(val.getIdent())) {
                            values.add(val);
                            autoFlowValues.add(CssGridAutoFlow.row);
                        } else {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                    } else {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }

                }
                // grid-auto-rows
                do {
                    val = expression.getValue();
                    op = expression.getOperator();
                    if (val.getType() != CssTypes.CSS_SWITCH) {
                        v = CssGridAutoRows.parseTrackSize(ac, val, this);
                        values.add(v);
                        autoRows.add(v);
                    }
                    if (op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                getPropertyName(), ac);
                    }
                    expression.next();
                } while (val.getType() != CssTypes.CSS_SWITCH);
                values.add(val);
                // grid-template-columns
                v = parseTemplateRows(ac, expression, this);
                values.add(v);
                templateColumns.add(v);
            } else {
                // <?grid-template-rows?> / [ auto-flow && dense? ] <?grid-auto-columns?>?
                exp = new CssExpression();
                do {
                    val = expression.getValue();
                    op = expression.getOperator();
                    if (val.getType() != CssTypes.CSS_SWITCH) {
                        exp.addValue(val);
                        exp.setOperator(op);
                    }
                    expression.next();
                } while (val.getType() != CssTypes.CSS_SWITCH);
                v = parseTemplateRows(ac, exp, this);
                values.add(v);
                templateRows.add(v);
                values.add(val); // the '/'
                // auto-flow && dense?
                val = expression.getValue();
                op = expression.getOperator();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                CssIdent id = val.getIdent();
                if (auto_flow.equals(id)) {
                    values.add(val);
                    autoFlowValues.add(CssGridAutoFlow.row);
                    // optional 'dense'
                    if (op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                getPropertyName(), ac);
                    }
                    expression.next();
                    val = expression.getValue();
                    op = expression.getOperator();
                    if (val.getType() == CssTypes.CSS_IDENT && CssGridAutoFlow.dense.equals(val.getIdent())) {
                        values.add(val);
                        autoFlowValues.add(CssGridAutoFlow.dense);
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                    }
                } else {
                    if (CssGridAutoFlow.dense.equals(id)) {
                        values.add(val);
                        autoFlowValues.add(CssGridAutoFlow.dense);
                        // mandatory 'auto-flow'
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                        val = expression.getValue();
                        op = expression.getOperator();
                        if (val.getType() == CssTypes.CSS_IDENT && auto_flow.equals(val.getIdent())) {
                            values.add(val);
                            autoFlowValues.add(CssGridAutoFlow.row);
                        } else {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        if (op != SPACE) {
                            throw new InvalidParamException("operator", op,
                                    getPropertyName(), ac);
                        }
                        expression.next();
                    } else {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }

                }
                // grid-auto-columns
                while (!expression.end()) {
                    val = expression.getValue();
                    op = expression.getOperator();
                    v = CssGridAutoRows.parseTrackSize(ac, val, this);
                    values.add(v);
                    autoColumns.add(v);
                    if (op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                getPropertyName(), ac);
                    }
                    expression.next();
                }
            }
        } else {
            // inherit ?
            val = expression.getValue();
            if (expression.getCount() == 1 && (val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                value = val;
                expression.next();
                return;
            }
            // <grid-template>
            values.add(parseGridTemplate(ac, expression, this,
                    cssGridTemplateAreas, cssGridTemplateColumns,
                    cssGridTemplateRows));
        }
        // now set all the different values
        if (!autoFlowValues.isEmpty()) {
            cssGridAutoFlow.value = (autoFlowValues.size() == 1) ? autoFlowValues.get(0) : new CssValueList(autoFlowValues);
        }
        if (!templateColumns.isEmpty()) {
            cssGridTemplateColumns.value = (templateColumns.size() == 1) ? templateColumns.get(0) : new CssValueList(templateColumns);
        }
        if (!templateRows.isEmpty()) {
            cssGridTemplateRows.value = (templateRows.size() == 1) ? templateRows.get(0) : new CssValueList(templateRows);

        }
        if (!autoColumns.isEmpty()) {
            cssGridAutoColumns.value = (autoColumns.size() == 1) ? autoColumns.get(0) : new CssValueList(autoColumns);
        }
        if (!autoRows.isEmpty()) {
            cssGridAutoRows.value = (autoRows.size() == 1) ? autoRows.get(0) : new CssValueList(autoRows);
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);

    }

    public CssGrid(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
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
        cssGridAutoColumns.addToStyle(ac, style);
        cssGridAutoFlow.addToStyle(ac, style);
        cssGridAutoRows.addToStyle(ac, style);
    }

}

