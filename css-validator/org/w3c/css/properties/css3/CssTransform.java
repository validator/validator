//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang 2012, 2019.
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
 * @spec https://www.w3.org/TR/2019/CR-css-transforms-1-20190214/#propdef-transform
 * @spec https://www.w3.org/TR/2021/WD-css-transforms-2-20211109/#transform-functions
 */
public class CssTransform extends org.w3c.css.properties.css.CssTransform {

    // 2d functions
    static final String matrix = "matrix";
    static final String translate = "translate";
    static final String translateX = "translatex"; // lowercase translateX
    static final String translateY = "translatey"; // lowercase translateY
    static final String scale = "scale";
    static final String scaleX = "scalex";  // lowercase scaleX
    static final String scaleY = "scaley";  // lowercase scaleY
    static final String rotate = "rotate";
    static final String skew = "skew";
    static final String skewX = "skewx";  // lowercase skewX
    static final String skewY = "skewy";  // lowercase skewY

    // 3d functions
    static final String matrix3d = "matrix3d";
    static final String translate3d = "translate3d";
    static final String translateZ = "translatez"; // lowercase translateZ
    static final String scale3d = "scale3d";
    static final String scaleZ = "scalez";   // lowercalse scaleZ
    static final String rotate3d = "rotate3d";
    static final String rotateX = "rotatex"; // lowercase rotateX
    static final String rotateY = "rotatey"; // lowercase rotateY
    static final String rotateZ = "rotatez"; // lowercase rotateZ
    static final String perspective = "perspective";

    /**
     * Create a new CssTransform
     */
    public CssTransform() {
        value = initial;
    }

    /**
     * Creates a new CssTransform
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTransform(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        boolean singleVal = false;
        CssValue sValue = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_FUNCTION:
                    parseFunctionValues(ac, val, this);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        singleVal = true;
                        sValue = val;
                        values.add(val);
                        break;
                    } else if (none.equals(id)) {
                        singleVal = true;
                        sValue = val;
                        values.add(val);
                        break;
                    }
                    // if not recognized, let it fail
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (singleVal && values.size() > 1) {
            throw new InvalidParamException("value",
                    sValue.toString(),
                    getPropertyName(), ac);
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssTransform(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    protected static void parseFunctionValues(ApplContext ac, CssValue func, CssProperty caller)
            throws InvalidParamException {
        CssFunction function = func.getFunction();
        String fname = function.getName().toLowerCase();
        // waiting for jdk7 for the string-based switch/case

        switch (fname) {
            // first, 2d functions
            case matrix:
                parseExactlyNX(ac, function.getParameters(), 6, CssTypes.CSS_NUMBER, caller);
                break;
            case translate:
                parseTranslateFunction(ac, function.getParameters(), caller);
                break;
            case translateX:
            case translateY:
                parseTranslateAxisFunction(ac, function.getParameters(), caller);
                break;
            case scale:
                parseAtMostX(ac, function.getParameters(), 2,
                        CssTypes.CSS_NUMBER, CssTypes.CSS_PERCENTAGE, caller);
                break;
            case scaleX:
            case scaleY:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_NUMBER, CssTypes.CSS_PERCENTAGE, caller);
                break;
            case rotate:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_ANGLE, caller);
                break;
            case skew:
                parseAtMostX(ac, function.getParameters(), 2, CssTypes.CSS_ANGLE, caller);
                break;
            case skewX:
            case skewY:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_ANGLE, caller);
                break;
            // 3d functions are only part of transform-2
            // theyr are listed here as part of the 20120911 WD, not the transform-1 20190214 CR
            case matrix3d:
                parseExactlyNX(ac, function.getParameters(), 16, CssTypes.CSS_NUMBER, caller);
                break;
            case translate3d:
                parseTranslate3dFunction(ac, function.getParameters(), caller);
                break;
            case translateZ:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_LENGTH, caller);
                break;
            case scale3d:
                parseExactlyNX(ac, function.getParameters(), 3, CssTypes.CSS_NUMBER, caller);
                break;
            case scaleZ:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_NUMBER, caller);
                break;
            case rotate3d:
                parseRotate3dFunction(ac, function.getParameters(), caller);
                break;
            case rotateX:
            case rotateY:
            case rotateZ:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_ANGLE, caller);
                break;
            case perspective:
                parseOneX(ac, function.getParameters(), CssTypes.CSS_LENGTH, caller);
                break;
            // unknown function
            default:
                throw new InvalidParamException("value",
                        func.toString(),
                        caller.getPropertyName(), ac);
        }
    }

    private static void parsePerspective(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (expression.getCount() == 0) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        CssValue val = expression.getValue();
        if (val.getType() == CssTypes.CSS_IDENT) {
            if (none.equals(val.getIdent())) {
                return;
            }
            // if not none, it will fail later
        }
        parseOneX(ac, expression, CssTypes.CSS_LENGTH, caller);
    }

    private static void parseExactlyNX(ApplContext ac, CssExpression expression,
                                       int n, int type, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() < n) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        parseAtMostX(ac, expression, n, CssTypes.CSS_NUMBER, caller);

    }

    // parse at most n values of type (CssTypes.XXX)
    private static void parseAtMostX(ApplContext ac, CssExpression expression,
                                     int atMost, int type, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > atMost) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            // special case, 0 can be a length or an angle...
            if (val.getType() == CssTypes.CSS_NUMBER) {
                switch (type) {
                    case CssTypes.CSS_LENGTH:
                        val.getLength();
                        break;
                    case CssTypes.CSS_ANGLE:
                        val.getAngle();
                    case CssTypes.CSS_NUMBER:
                        break;
                    default:
                        throw new InvalidParamException("value",
                                val.toString(),
                                caller.getPropertyName(), ac);
                }
            } else if (val.getType() != type) {
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
    }

    // parse at most n values of type (CssTypes.XXX)
    private static void parseAtMostX(ApplContext ac, CssExpression expression,
                                     int atMost, int type1, int type2, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > atMost) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            // special case, 0 can be a length or an angle...
            if (val.getType() == CssTypes.CSS_NUMBER) {
                switch (type1) {
                    case CssTypes.CSS_LENGTH:
                        val.getLength();
                        break;
                    case CssTypes.CSS_ANGLE:
                        val.getAngle();
                    case CssTypes.CSS_NUMBER:
                        break;
                    default:
                        switch (type2) {
                            case CssTypes.CSS_LENGTH:
                                val.getLength();
                                break;
                            case CssTypes.CSS_ANGLE:
                                val.getAngle();
                            case CssTypes.CSS_NUMBER:
                                break;
                            default:
                                throw new InvalidParamException("value",
                                        val.toString(),
                                        caller.getPropertyName(), ac);
                        }
                }
            } else if ((val.getType() != type1) && (val.getType() != type2)) {
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
    }

    // parse one value of type (CssTypes.XXX)
    private static void parseOneX(ApplContext ac, CssExpression expression,
                                  int type, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (expression.getCount() == 0) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        CssValue val;
        val = expression.getValue();
        // special case, 0 can be a length or an angle...
        if (val.getType() == CssTypes.CSS_NUMBER && type != CssTypes.CSS_NUMBER) {
            if (type == CssTypes.CSS_LENGTH || type == CssTypes.CSS_ANGLE) {
                // if not zero, it will fail
                val.getCheckableValue().checkEqualsZero(ac, caller.getPropertyName());
                expression.next();
                return;
            }
        }
        if (val.getType() != type) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        expression.next();
    }

    // parse one value of type (CssTypes.XXX)
    private static void parseOneX(ApplContext ac, CssExpression expression,
                                  int type1, int type2, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (expression.getCount() == 0) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        CssValue val;
        val = expression.getValue();
        // special case, 0 can be a length or an angle...
        if (val.getType() == CssTypes.CSS_NUMBER && type1 != CssTypes.CSS_NUMBER && type2 != CssTypes.CSS_NUMBER) {
            if (type1 == CssTypes.CSS_LENGTH || type1 == CssTypes.CSS_ANGLE ||
                    type1 == CssTypes.CSS_PERCENTAGE || type2 == CssTypes.CSS_PERCENTAGE ||
                    type2 == CssTypes.CSS_LENGTH || type2 == CssTypes.CSS_ANGLE) {
                // if not zero, it will fail
                val.getCheckableValue().checkEqualsZero(ac, caller.getPropertyName());
                expression.next();
                return;
            }
        }
        if (val.getType() != type1 && val.getType() != type2) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        expression.next();
    }

    // special cases


    // https://www.w3.org/TR/2019/CR-css-transforms-1-20190214/#funcdef-transform-translate
    private static void parseTranslateFunction(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (expression.getCount() == 0) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        CssValue val;
        char op;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getLength();
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
    }

    // https://www.w3.org/TR/2019/CR-css-transforms-1-20190214/#funcdef-transform-translatex
    private static void parseTranslateAxisFunction(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        if (expression.getCount() == 0) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        CssValue val;
        val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getLength();
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
        }
        expression.next();
    }

    // http://www.w3.org/TR/2012/WD-css3-transforms-20120911/#translate3d-function
    private static void parseTranslate3dFunction(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() < 3) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        if (expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;
        for (int i = 0; i < 2; i++) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getLength();
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            expression.next();
            if (op != COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        val = expression.getValue();
        if (val.getType() == CssTypes.CSS_NUMBER) {
            val.getLength();
        } else if (val.getType() != CssTypes.CSS_LENGTH) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        expression.next();
    }

    // http://www.w3.org/TR/2012/WD-css3-transforms-20120911/#rotate3d-function
    private static void parseRotate3dFunction(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() < 4) {
            throw new InvalidParamException("few-value", caller.getPropertyName(), ac);
        }
        if (expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;
        for (int i = 0; i < 3; i++) {
            val = expression.getValue();
            op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_NUMBER) {
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
            }
            expression.next();
            if (op != COMMA) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        val = expression.getValue();
        if (val.getType() != CssTypes.CSS_ANGLE) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        expression.next();
    }
}



