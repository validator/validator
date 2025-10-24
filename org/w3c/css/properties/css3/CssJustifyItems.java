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
 * @spec https://www.w3.org/TR/2020/WD-css-align-3-20200421/#propdef-justify-items
 */
public class CssJustifyItems extends org.w3c.css.properties.css.CssJustifyItems {

    public static final CssIdent[] self_position_extras, legacy_qualifier,
            single_justify_items_values;
    public static final CssIdent legacy;

    static {
        legacy = CssIdent.getIdent("legacy");
        String[] _single_values = {"normal", "stretch"};
        single_justify_items_values = new CssIdent[_single_values.length];
        int i = 0;
        for (String s : _single_values) {
            single_justify_items_values[i++] = CssIdent.getIdent(s);
        }
        String[] _self_position_extra_values = {"left", "right"};
        self_position_extras = new CssIdent[_self_position_extra_values.length];
        i = 0;
        for (String s : _self_position_extra_values) {
            self_position_extras[i++] = CssIdent.getIdent(s);
        }
        String[] _legacy_qualifier_values = {"left", "right", "center"};
        legacy_qualifier = new CssIdent[_legacy_qualifier_values.length];
        i = 0;
        for (String s : _legacy_qualifier_values) {
            legacy_qualifier[i++] = CssIdent.getIdent(s);
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

    public static CssIdent getLegacyQualifier(CssIdent ident) {
        for (CssIdent id : legacy_qualifier) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getSingleJustifyItemsValue(CssIdent ident) {
        for (CssIdent id : single_justify_items_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssAlignSelf
     */
    public CssJustifyItems() {
        value = initial;
    }

    /**
     * Creates a new CssAlignSelf
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssJustifyItems(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = parseJustifyItems(ac, expression, this);
        if (!expression.end()) {
            throw new InvalidParamException("unrecognize", ac);
        }
    }

    public static CssValue parseJustifyItems(ApplContext ac, CssExpression expression,
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
            if (getSingleJustifyItemsValue(ident) != null) {
                expression.next();
                return val;
            }
            // now try the two-values position, starting first with only one.
            if (CssAlignContent.baseline.equals(ident)) {
                expression.next();
                return CssAlignContent.baseline;
            }
            // we must check the extras first
            // legacy qualifier are part of self-position, so we may have nothing
            if (getLegacyQualifier(ident) != null) {
                expression.next();
                if (expression.end()) {
                    return val;
                }
                values.add(val);
                val = expression.getValue();
                if (val.getType() != CssTypes.CSS_IDENT || !legacy.equals(val.getIdent())) {
                    // FIXME sreturn or throw???
                    return val;
                }
                // ok, we got a leagacy, operator check and return
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                values.add(val);
                expression.next();
                return new CssValueList(values);
            }

            if (getSelfPositionAddExtras(ident) != null) {
                expression.next();
                return val;
            }
            // ok, at that point we need two values.
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
                if (getSelfPositionAddExtras(val.getIdent()) == null) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                return new CssValueList(values);
            }
            // now we need to do guess work and possibly backtrack.
            if (legacy.equals(ident)) {
                // we can have nothing or a qualifier here.
                expression.next();
                if (expression.end()) {
                    return val;
                }
                val = expression.getValue();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    return legacy; // let the caller check and fail if necessary
                }
                if (getLegacyQualifier(val.getIdent()) == null) {
                    return legacy;
                }
                // so we got something, check the operator
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                values.add(legacy);
                values.add(val);
                expression.next();
                return new CssValueList(values);
            }

        }
        throw new InvalidParamException("value",
                val.toString(),
                caller.getPropertyName(), ac);
    }


    public CssJustifyItems(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

