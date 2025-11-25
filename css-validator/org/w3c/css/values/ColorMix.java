//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
//
package org.w3c.css.values;

import org.w3c.css.properties.css3.CssColor;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;

public class ColorMix {
    public final static CssIdent in = CssIdent.getIdent("in");
    public final static CssIdent hue = CssIdent.getIdent("hue");
    public final static CssIdent[] rectangularColorSpaceValues;
    public final static CssIdent[] polarColorSpaceValues;
    public final static CssIdent[] hueInterpolationMethodModifiers;

    static {
        String[] _rectangularColorSpaceValues = {"srgb", "srgb-linear", "display-p3",
                "a98-rgb", "prophoto-rgb", "rec2020", "lab",
                "oklab", "xyz", "xyz-d50", "xyz-d65"};
        rectangularColorSpaceValues = new CssIdent[_rectangularColorSpaceValues.length];
        for (int i = 0; i < _rectangularColorSpaceValues.length; i++) {
            rectangularColorSpaceValues[i] = CssIdent.getIdent(_rectangularColorSpaceValues[i]);
        }
        String[] _polarColorSpaceValues = {"hsl", "hwb", "lch", "oklch"};
        polarColorSpaceValues = new CssIdent[_polarColorSpaceValues.length];
        for (int i = 0; i < _polarColorSpaceValues.length; i++) {
            polarColorSpaceValues[i] = CssIdent.getIdent(_polarColorSpaceValues[i]);
        }
        String[] _hueInterpolationMethodModifiers = {"shorter", "longer",
                "increasing", "decreasing"};
        hueInterpolationMethodModifiers = new CssIdent[_hueInterpolationMethodModifiers.length];
        for (int i = 0; i < _hueInterpolationMethodModifiers.length; i++) {
            hueInterpolationMethodModifiers[i] = CssIdent.getIdent(_hueInterpolationMethodModifiers[i]);
        }
    }

    public static final CssIdent getAllowedValue(CssIdent ident, CssIdent[] allowedValues) {
        for (CssIdent id : allowedValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    String output = null;
    ArrayList<CssValue> color_percentages = new ArrayList<>();
    CssValue color_interpolation_method = null;
    boolean has_css_variable = false;

    /**
     * Create a new LightDark
     */
    public ColorMix() {
    }

    public static CssValue parseColorInterpolationMethod(ApplContext ac, CssExpression exp, CssValue caller)
            throws InvalidParamException {
        ArrayList<CssValue> values;
        CssValue val, res;
        CssIdent id;
        char op;
        if (exp.getCount() < 2 || exp.getCount() > 4) {
            throw new InvalidParamException("unrecognize", ac);
        }
        val = exp.getValue();
        op = exp.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(), caller, ac);
        }
        id = val.getIdent();
        if (!in.equals(id)) {
            throw new InvalidParamException("value",
                    val.toString(), caller, ac);
        }
        // we got the first token!
        values = new ArrayList<>(4);
        values.add(val);
        if (op != CssOperator.SPACE) {
            throw new InvalidParamException("operator",
                    Character.toString(op), ac);
        }
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(), caller, ac);
        }
        id = val.getIdent();
        res = getAllowedValue(id, rectangularColorSpaceValues);
        if (res != null) {
            values.add(val);
            exp.next();
            // must be last
            if (!exp.end()) {
                throw new InvalidParamException("unrecognize", ac);
            }
            return new CssValueList(values);
        }
        res = getAllowedValue(id, polarColorSpaceValues);
        if (res != null) {
            // good but need to check next token as well
            values.add(val);
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            exp.next();
            if (exp.end()) {
                return new CssValueList(values);
            } else {
                val = exp.getValue();
                op = exp.getOperator();
                if (val.getType() == CssTypes.CSS_IDENT) {
                    id = val.getIdent();
                    if (getAllowedValue(id, hueInterpolationMethodModifiers) != null) {
                        values.add(val);
                    }
                    if (op != CssOperator.SPACE) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                    exp.next();
                    val = exp.getValue();
                    if (hue.equals(val.getIdent())) {
                        values.add(val);
                        exp.next();
                        // must be last
                        if (!exp.end()) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        return new CssValueList(values);
                    }
                    // if not -> bail out
                }
            }
            throw new InvalidParamException("value",
                    val.toString(), caller, ac);
        }
        if (id.toString().startsWith("--")) {
            // TODO check it is a declared value
            values.add(val);
            if (!exp.end()) {
                throw new InvalidParamException("unrecognize", ac);
            }
            return new CssValueList(values);
        }
        throw new InvalidParamException("unrecognize", ac);
    }

    static public CssValue parseColorPercentageValue(ApplContext ac, CssExpression exp, CssValue caller)
            throws InvalidParamException {
        if (exp.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        ArrayList<CssValue> values = new ArrayList<>(2);
        CssValue val;
        org.w3c.css.properties.css3.CssColor css3Color;
        char op;
        boolean gotPercentage = false;

        val = exp.getValue();
        op = exp.getOperator();
        if (val.getType() == CssTypes.CSS_PERCENTAGE) {
            gotPercentage = true;
            values.add(val);
        } else {
            CssExpression e = new CssExpression();
            e.addValue(val);
            css3Color = new CssColor(ac, e);
            values.add(css3Color.getColor());
        }
        exp.next();
        if (!exp.end()) {
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            val = exp.getValue();
            if (val.getType() == CssTypes.CSS_PERCENTAGE) {
                if (gotPercentage) {
                    throw new InvalidParamException("value", val.toString(), caller, ac);
                }
                values.add(val);
            } else {
                if (!gotPercentage) {
                    // two colors is forbidden here
                    throw new InvalidParamException("value", val.toString(), caller, ac);
                }
                CssExpression e = new CssExpression();
                e.addValue(val);
                css3Color = new CssColor(ac, e);
                values.add(css3Color.getColor());
            }
            exp.next();
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public final void addColorPercentageValue(ApplContext ac, CssExpression exp, CssValue caller)
            throws InvalidParamException {
        color_percentages.add(parseColorPercentageValue(ac, exp, caller));
    }

    public final void setColorInterpolationMethod(ApplContext ac, CssExpression exp, CssValue caller)
            throws InvalidParamException {
        color_interpolation_method = parseColorInterpolationMethod(ac, exp, caller);
    }


    public boolean equals(ColorMix other) {
        if (other != null) {
            // FIXME interpolation-method
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder("color-mix(");
            boolean isFirst = true;
            if (color_interpolation_method != null) {
                sb.append(color_interpolation_method);
                isFirst = false;
            }
            for (CssValue v : color_percentages) {
                if (!isFirst) {
                    sb.append(", ");
                } else {
                    isFirst = false;
                }
                sb.append(v);
            }
            sb.append(')');
            output = sb.toString();
        }
        return output;
    }
}
