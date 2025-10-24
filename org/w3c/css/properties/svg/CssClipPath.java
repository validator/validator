//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css3.CssBackgroundClip;
import org.w3c.css.properties.css3.CssBackgroundPosition;
import org.w3c.css.properties.css3.CssBorderRadius;
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
 * @spec https://www.w3.org/TR/2014/CR-css-masking-1-20140826/#the-clip-path
 * @spec https://www.w3.org/TR/2014/CR-css-shapes-1-20140320/#basic-shape-functions
 * NOTE that ultimately, all function parsing should be moved to a specific directory
 */
public class CssClipPath extends org.w3c.css.properties.css.CssClipPath {

    public static final CssIdent[] geometry_box_allowed_values;
    public static final CssIdent[] shape_radius_allowed_values;

    public static final CssIdent inset_round;
    public static final CssIdent at_position;

    static {
        String[] _allowed_values = {"margin-box", "fill-box", "stroke-box", "view-box"};

        geometry_box_allowed_values = new CssIdent[_allowed_values.length];
        for (int i = 0; i < geometry_box_allowed_values.length; i++) {
            geometry_box_allowed_values[i] = CssIdent.getIdent(_allowed_values[i]);
        }

        String[] _shape_radius_values = {"closest-side", "farthest-side"};
        shape_radius_allowed_values = new CssIdent[_shape_radius_values.length];
        for (int i = 0; i < _shape_radius_values.length; i++) {
            shape_radius_allowed_values[i] = CssIdent.getIdent(_shape_radius_values[i]);
        }
        inset_round = CssIdent.getIdent("round");
        at_position = CssIdent.getIdent("at");
    }

    public static final CssIdent getGeometryBoxAllowedValue(CssIdent ident) {
        // <geometry-box> = <shape-box> | fill-box | stroke-box | view-box
        // <shape-box> = <box> | margin-box
        CssIdent idt = CssBackgroundClip.getMatchingIdent(ident);
        if (idt != null) {
            return idt;
        }
        for (CssIdent id : geometry_box_allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getShapeRadiusAllowedValue(CssIdent ident) {
        for (CssIdent id : shape_radius_allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }


    /**
     * Create a new CssClipPath
     */
    public CssClipPath() {
        value = initial;
    }

    /**
     * Creates a new CssClipPath
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssClipPath(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        ArrayList<CssValue> values = new ArrayList<CssValue>();
        boolean gotGeometryBox = false;
        boolean gotBasicShape = false;
        while (!expression.end()) {
            CssValue val;
            char op = expression.getOperator();
            val = expression.getValue();

            switch (val.getType()) {
                case CssTypes.CSS_FUNCTION:
                    if (!gotBasicShape) {
                        CssFunction func = val.getFunction();
                        String funcname = func.getName().toLowerCase();
                        switch (funcname) {
                            case "inset":
                                checkInsetFunction(ac, func.getParameters(), this);
                                break;
                            case "circle":
                                checkCircleFunction(ac, func.getParameters(), this);
                                break;
                            case "ellipse":
                                checkEllipseFunction(ac, func.getParameters(), this);
                                break;
                            case "polygon":
                                checkPolygonFunction(ac, func.getParameters(), this);
                                break;
                            default:
                                throw new InvalidParamException("value", val,
                                        getPropertyName(), ac);
                        }
                        gotBasicShape = true;
                        values.add(val);
                        break;
                    }
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                case CssTypes.CSS_URL:
                    value = val;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        value = val;
                        break;
                    }
                    if (none.equals(id)) {
                        value = val;
                        break;
                    }
                    if (!gotGeometryBox) {
                        if (getGeometryBoxAllowedValue(id) != null) {
                            gotGeometryBox = true;
                            values.add(val);
                            break;
                        }
                    }

                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (gotBasicShape || gotGeometryBox) {
            value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        }
    }

    public CssClipPath(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    protected static void checkInsetFunction(ApplContext ac, CssExpression expression,
                                             CssProperty caller) throws InvalidParamException {
        CssValue val;
        char op;
        int nb_shape_arg = 0;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    nb_shape_arg++;
                    if (nb_shape_arg > 4) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    break;
                case CssTypes.CSS_IDENT:
                    if (inset_round.equals(val.getIdent())) {
                        // the remainder must be a border-radius
                        CssExpression nex = new CssExpression();
                        expression.next();
                        while (!expression.end()) {
                            nex.addValue(expression.getValue());
                            nex.setOperator(expression.getOperator());
                            expression.next();
                        }
                        if (nex.getCount() == 0) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        CssBorderRadius.parseBorderCornerRadius(ac, nex, true, caller);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
    }

    protected static void checkCircleFunction(ApplContext ac, CssExpression expression,
                                              CssProperty caller) throws InvalidParamException {
        CssValue val;
        char op;
        boolean gotRadius = false;

        if (expression == null || expression.getCount() == 0) {
            // no expression allowed by grammar
            return;
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    if (gotRadius) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    val.getCheckableValue().checkPositiveness(ac, caller);
                    gotRadius = true;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (getShapeRadiusAllowedValue(ident) != null) {
                        if (gotRadius) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        gotRadius = true;
                        break;
                    }

                    if (at_position.equals(ident)) {
                        // the remainder must be a position
                        CssExpression nex = new CssExpression();
                        expression.next();
                        while (!expression.end()) {
                            nex.addValue(expression.getValue());
                            nex.setOperator(expression.getOperator());
                            expression.next();
                        }
                        if (nex.getCount() == 0) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        CssBackgroundPosition.checkSyntax(ac, nex, caller.getPropertyName());
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
    }

    protected static void checkEllipseFunction(ApplContext ac, CssExpression expression,
                                               CssProperty caller) throws InvalidParamException {
        CssValue val;
        char op;
        int nbRadius = 0;

        if (expression == null || expression.getCount() == 0) {
            // no expression allowed by grammar
            return;
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    if (nbRadius >= 2) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    val.getCheckableValue().checkPositiveness(ac, caller);
                    nbRadius++;
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (getShapeRadiusAllowedValue(ident) != null) {
                        if (nbRadius >= 2) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        nbRadius++;
                        break;
                    }

                    if (at_position.equals(ident)) {
                        // the remainder must be a position
                        CssExpression nex = new CssExpression();
                        expression.next();
                        while (!expression.end()) {
                            nex.addValue(expression.getValue());
                            nex.setOperator(expression.getOperator());
                            expression.next();
                        }
                        if (nex.getCount() == 0) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        CssBackgroundPosition.checkSyntax(ac, nex, caller.getPropertyName());
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
    }

    protected static void checkPolygonFunction(ApplContext ac, CssExpression expression,
                                               CssProperty caller) throws InvalidParamException {
        CssValue val;
        char op;
        int nbShapeArgs = 0;
        int nbPoints = 0;
        boolean gotFillRule = false;

        if (expression == null || expression.getCount() == 0) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    if (nbShapeArgs >= 2) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    nbShapeArgs++;
                    break;
                case CssTypes.CSS_IDENT:
                    // can only happen at the beginning.
                    if (!gotFillRule && nbPoints == 0 && nbShapeArgs == 0) {
                        if (CssFillRule.getAllowedIdent(val.getIdent()) != null) {
                            gotFillRule = true;
                            break;
                        }
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            // we need a COMMA after a possible fill-rule, and after two shape-args
            if (gotFillRule && nbPoints == 0 && nbShapeArgs == 0) {
                if (op != COMMA) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            } else if (nbShapeArgs == 2) {
                if (expression.getRemainingCount() > 1) {
                    // we don't need a COMMA at the end, so we check only before.
                    if (op != COMMA) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                }
                nbPoints++;
                nbShapeArgs = 0;
            } else {
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
            expression.next();
        }
        // we always needs two shape args, can't finish with only one
        if (nbShapeArgs == 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
    }

}

