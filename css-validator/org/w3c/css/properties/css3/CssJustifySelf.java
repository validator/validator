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
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-align-3-20200421/#propdef-justify-self
 * @see CssAlignSelf
 */
public class CssJustifySelf extends org.w3c.css.properties.css.CssJustifySelf {

    public static final CssIdent[] self_position_extras;

    static {
        String[] _self_position_extra_values = {"left", "right"};
        self_position_extras = new CssIdent[_self_position_extra_values.length];
        int i = 0;
        for (String s : _self_position_extra_values) {
            self_position_extras[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getSelfPositionAddExtras(CssIdent ident) {
        for (CssIdent id : self_position_extras) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return CssAlignSelf.getSelfPosition(ident);
    }

    /**
     * Create a new CssAlignSelf
     */
    public CssJustifySelf() {
        value = initial;
    }

    /**
     * Creates a new CssAlignSelf
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssJustifySelf(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = parseJustifySelf(ac, expression, this);
        if (!expression.end()) {
            throw new InvalidParamException("unrecognize", ac);
        }
    }

    public static CssValue parseJustifySelf(ApplContext ac, CssExpression expression,
                                            CssProperty caller)
            throws InvalidParamException {
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                expression.next();
                return val;
            }
            if (CssAlignSelf.getSingleAlignSelfValue(ident) != null) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                expression.next();
                return val;
            }
            // now try the two-values position, starting first with the "only one value" case
            if (CssAlignContent.baseline.equals(ident)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                expression.next();
                return val;
            }
            if (getSelfPositionAddExtras(ident) != null) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                expression.next();
                return val;
            }
            // ok, at that point we really need two values.
            if (CssAlignContent.getBaselineQualifier(ident) != null) {
                values.add(val);
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                expression.next();
                if (expression.end()) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                val = expression.getValue();
                if (val.getType() != CssTypes.CSS_IDENT || !CssAlignContent.baseline.equals(val.getIdent())) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                return new CssValueList(values);
            }
            if (CssAlignContent.getOverflowPosition(ident) != null) {
                values.add(val);
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                expression.next();
                if (expression.end()) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                val = expression.getValue();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                if ((CssAlignSelf.getSelfPosition(val.getIdent()) == null)
                        && (getSelfPositionAddExtras(val.getIdent()) == null)) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                return new CssValueList(values);
            } else {
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                if (CssAlignSelf.getSelfPosition(val.getIdent()) == null) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                return new CssValueList(values);
            }

        }
        throw new InvalidParamException("value",
                val.toString(),
                caller.getPropertyName(), ac);
    }


    public CssJustifySelf(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

