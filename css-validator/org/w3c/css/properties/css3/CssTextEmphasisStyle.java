// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#propdef-text-emphasis-style
 */
public class CssTextEmphasisStyle extends org.w3c.css.properties.css.CssTextEmphasisStyle {

    public static final CssIdent[] shapeStyle;
    public static final CssIdent[] shapeForm;

    static {
        String[] _shapeStyle = {"filled", "open"};
        String[] _shapeForm = {"dot", "circle", "double-circle", "triangle", "sesame"};

        shapeStyle = new CssIdent[_shapeStyle.length];
        int i = 0;
        for (String s : _shapeStyle) {
            shapeStyle[i++] = CssIdent.getIdent(s);
        }
        shapeForm = new CssIdent[_shapeForm.length];
        i = 0;
        for (String s : _shapeForm) {
            shapeForm[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getShapeStyle(CssIdent ident) {
        for (CssIdent id : shapeStyle) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getShapeForm(CssIdent ident) {
        for (CssIdent id : shapeForm) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        if (none.equals(ident)) {
            return none;
        }
        CssIdent v = getShapeStyle(ident);
        if (v == null) {
            v = getShapeForm(ident);
        }
        return v;
    }

    /**
     * Create a new CssTextEmphasisStyle
     */
    public CssTextEmphasisStyle() {
        value = initial;
    }

    /**
     * Creates a new CssTextEmphasisStyle
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssTextEmphasisStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssValue styleValue = null;
        CssValue formValue = null;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_STRING:
                CssString s = val.getString();
                // limit of 1 character + two surrounding quotes
                // TODO might be a warning only
                if (s.toString().length() != 3) {
                    throw new InvalidParamException("value",
                            s, getPropertyName(), ac);
                }
                if (check && expression.getCount() != 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    if (check && expression.getCount() != 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                } else if (none.equals(ident)) {
                    value = val;
                    if (check && expression.getCount() != 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                } else {
                    boolean match = false;
                    int nbgot = 0;
                    do {
                        match = false;
                        if (styleValue == null) {
                            match = (getShapeStyle(ident) != null);
                            if (match) {
                                styleValue = val;
                            }
                        }
                        if (!match && formValue == null) {
                            match = (getShapeForm(ident) != null);
                            if (match) {
                                formValue = val;
                            }
                        }
                        if (!match) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        nbgot++;
                        if (expression.getRemainingCount() == 1 || (nbgot == 2)) {
                            // if we have both, exit
                            // (needed only if check == false...
                            break;
                        }
                        if (op != CssOperator.SPACE) {
                            throw new InvalidParamException("operator",
                                    Character.toString(op), ac);
                        }
                        expression.next();
                        val = expression.getValue();
                        op = expression.getOperator();
                        if (val.getType() != CssTypes.CSS_IDENT) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        ident = val.getIdent();
                    } while (!expression.end());
                    // now construct the value
                    if (formValue != null && styleValue != null) {
                        ArrayList<CssValue> v = new ArrayList<CssValue>(2);
                        v.add(styleValue);
                        v.add(formValue);
                        value = new CssValueList(v);
                    } else {
                        value = (formValue == null) ? styleValue : formValue;
                    }
                }
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssTextEmphasisStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

