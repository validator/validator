// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
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
 * @spec http://www.w3.org/TR/2012/WD-css3-transitions-20120403/#transition
 */
public class CssTransition extends org.w3c.css.properties.css.CssTransition {

    CssTransitionProperty cssTransitionProperty = null;
    CssTransitionTimingFunction cssTransitionTimingFunction = null;
    CssTransitionDelay cssTransitionDelay = null;
    CssTransitionDuration cssTransitionDuration = null;

    /**
     * Create a new CssTransition
     */
    public CssTransition() {
        value = initial;
        cssTransitionDelay = new CssTransitionDelay();
        cssTransitionDuration = new CssTransitionDuration();
        cssTransitionProperty = new CssTransitionProperty();
        cssTransitionTimingFunction = new CssTransitionTimingFunction();
    }

    /**
     * Creates a new CssTransition
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssTransition(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val;
        ArrayList<CssValue> values;
        CssExpression single_layer = null;
        char op;

        values = new ArrayList<CssValue>();
        // we just accumulate values and check at validation
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if ((val.getType() == CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                expression.next();
                return;
            }
            if (single_layer == null) {
                single_layer = new CssExpression();
            }
            // we will check later
            single_layer.addValue(val);
            single_layer.setOperator(op);
            expression.next();

            if (!expression.end()) {
                // incomplete value followed by a comma... it's complete!
                if (op == COMMA) {
                    single_layer.setOperator(SPACE);
                    values.add(checkLayer(ac, single_layer, this));
                    single_layer = null;
                } else if ((op != SPACE)) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        // if we reach the end in a value that can come in pair
        if (single_layer != null) {
            values.add(checkLayer(ac, single_layer, this));
        }
        if (values.size() == 1) {
            value = values.get(0);
            CssTransitionValue v = (CssTransitionValue) value;
            if (v.delay != null) {
                cssTransitionDelay = new CssTransitionDelay();
                cssTransitionDelay.value = v.delay;
            }
            if (v.duration != null) {
                cssTransitionDuration = new CssTransitionDuration();
                cssTransitionDuration.value = v.duration;
            }
            if (v.property != null) {
                cssTransitionProperty = new CssTransitionProperty();
                cssTransitionProperty.value = v.property;
            }
            if (v.timingfunc != null) {
                cssTransitionTimingFunction = new CssTransitionTimingFunction();
                cssTransitionTimingFunction.value = v.timingfunc;
            }
        } else {
            // if we have multiple layers, none can't be present
            for (CssValue tv : values) {
                CssTransitionValue rtv = (CssTransitionValue) tv;
                if (rtv.property == none) {
                    throw new InvalidParamException("value", none,
                            getPropertyName(), ac);
                }
            }
            // TODO explode the layers for addToStyle...
            value = new CssLayerList(values);
        }
    }

    public CssTransition(ApplContext ac, CssExpression expression)
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
        if (cssTransitionDelay != null) {
            cssTransitionDelay.addToStyle(ac, style);
        }
        if (cssTransitionDuration != null) {
            cssTransitionDuration.addToStyle(ac, style);
        }
        if (cssTransitionProperty != null) {
            cssTransitionProperty.addToStyle(ac, style);
        }
        if (cssTransitionTimingFunction != null) {
            cssTransitionTimingFunction.addToStyle(ac, style);
        }
    }

    private CssTransitionValue checkLayer(ApplContext ac,
                                          CssExpression expression,
                                          CssProperty caller)
            throws InvalidParamException {
        CssTransitionValue v = new CssTransitionValue();
        // here we know we have the right operator, we just need to check
        // everything else.
        if (expression.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        while (!expression.end()) {
            val = expression.getValue();
            switch (val.getType()) {
                case CssTypes.CSS_TIME:
                    if (v.duration == null) {
                        // we got a duration (first parsable time)
                        CssCheckableValue t = val.getCheckableValue();
                        t.warnPositiveness(ac, this);
                        v.duration = val;
                        break;
                    }
                    if (v.delay == null) {
                        v.delay = val;
                        break;
                    }
                    // we already got two times => exit
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
                case CssTypes.CSS_FUNCTION:
                    if (v.timingfunc == null) {
                        CssTransitionTimingFunction.parseFunctionValues(ac, val, this);
                        v.timingfunc = val;
                        break;
                    }
                    // unknown function
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        v.property = val;
                        break;
                    }
                    if (v.timingfunc == null) {
                        CssIdent match = CssTransitionTimingFunction.getAllowedIdent(ident);
                        if (match != null) {
                            v.timingfunc = val;
                            break;
                        }
                    }
                    if (v.property == null) {
                        v.property = CssTransitionProperty.getAllowedIdent(ac, ident);
                        break;
                    }
                    // already set, let it fail
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);
            }
            expression.next();
        }
        return v;
    }

    private class CssTransitionValue extends CssValueList {

        CssValue delay = null;
        CssValue duration = null;
        CssValue property = null;
        CssValue timingfunc = null;

        public String toString() {
            boolean doneFirst = false;
            StringBuilder sb = new StringBuilder();
            if (property != null) {
                sb.append(property);
                doneFirst = true;
            }
            if (duration != null) {
                if (doneFirst) {
                    sb.append(' ');
                }
                sb.append(duration);
                doneFirst = true;
                if (delay != null) {
                    sb.append(' ').append(delay);
                }
            }
            if (timingfunc != null) {
                if (doneFirst) {
                    sb.append(' ');
                }
                sb.append(timingfunc);
            }
            return sb.toString();
        }
    }
}

