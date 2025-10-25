// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-variant-alternates
 */
public class CssFontVariantAlternates extends org.w3c.css.properties.css.CssFontVariantAlternates {

    public static final CssIdent normal;
    public static final CssIdent historicalForms;

    static {
        normal = CssIdent.getIdent("normal");
        historicalForms = CssIdent.getIdent("historical-forms");
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        if (historicalForms.equals(ident)) {
            return historicalForms;
        }
        return null;
    }

    /**
     * Create a new CssFontVariantAlternates
     */
    public CssFontVariantAlternates() {
        value = initial;
    }

    // here we just check that we got identifiers, comma separated for
    // the multipleValues case

    private void checkFuncExpression(ApplContext ac, CssExpression expression,
                                     boolean multipleValues)
            throws InvalidParamException {
        CssValue val;
        char op;
        if (expression.getCount() == 0 || (!multipleValues && expression.getCount() > 1)) {
            throw new InvalidParamException("unrecognize", ac);
        }
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end()) {
                if (op != CssOperator.COMMA) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
    }

    /**
     * Creates a new CssFontVariantAlternates
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontVariantAlternates(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 7) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        CssValue stylistic = null;
        CssValue histValue = null;
        CssValue styleSet = null;
        CssValue charVariant = null;
        CssValue swash = null;
        CssValue ornaments = null;
        CssValue annotation = null;
        boolean match;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident) || normal.equals(ident)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                    } else {
                        // no inherit, nor normal, test the up-to-(now one) values
                        match = false;
                        if (histValue == null) {
                            match = historicalForms.equals(ident);
                            if (match) {
                                histValue = val;
                                value = val;
                            }
                        }
                        if (!match) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                    }
                    break;
                case CssTypes.CSS_FUNCTION:
                    match = false;
                    CssFunction func = val.getFunction();
                    String funcname = func.getName().toLowerCase();
                    if (stylistic == null) {
                        if ("stylistic".equals(funcname)) {
                            checkFuncExpression(ac, func.getParameters(), false);
                            stylistic = val;
                            value = val;
                            match = true;
                        }
                    }
                    if (!match && styleSet == null) {
                        if ("styleset".equals(funcname)) {
                            checkFuncExpression(ac, func.getParameters(), true);
                            styleSet = val;
                            value = styleSet;
                            match = true;
                        }
                    }
                    if (!match && charVariant == null) {
                        if ("character-variant".equals(funcname)) {
                            checkFuncExpression(ac, func.getParameters(), true);
                            charVariant = val;
                            value = charVariant;
                            match = true;
                        }
                    }
                    if (!match && swash == null) {
                        if ("swash".equals(funcname)) {
                            checkFuncExpression(ac, func.getParameters(), false);
                            swash = val;
                            value = swash;
                            match = true;
                        }
                    }
                    if (!match && ornaments == null) {
                        if ("ornaments".equals(funcname)) {
                            checkFuncExpression(ac, func.getParameters(), false);
                            ornaments = val;
                            value = ornaments;
                            match = true;
                        }
                    }
                    if (!match && annotation == null) {
                        if ("annotation".equals(funcname)) {
                            checkFuncExpression(ac, func.getParameters(), false);
                            annotation = val;
                            value = annotation;
                            match = true;
                        }
                    }
                    if (match) {
                        break;
                    }
                    // let if fail
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        // now set the right value
        if (expression.getCount() > 1) {
            // do this to keep the same order for comparisons
            ArrayList<CssValue> v = new ArrayList<CssValue>();
            if (stylistic != null) {
                v.add(stylistic);
            }
            if (histValue != null) {
                v.add(histValue);
            }
            if (styleSet != null) {
                v.add(styleSet);
            }
            if (charVariant != null) {
                v.add(charVariant);
            }
            if (swash != null) {
                v.add(swash);
            }
            if (ornaments != null) {
                v.add(ornaments);
            }
            if (annotation != null) {
                v.add(annotation);
            }
            value = new CssValueList(v);
        }
    }

    public CssFontVariantAlternates(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

