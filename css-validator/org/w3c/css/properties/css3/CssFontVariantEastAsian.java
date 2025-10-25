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
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-variant-east-asian
 */
public class CssFontVariantEastAsian extends org.w3c.css.properties.css.CssFontVariantEastAsian {

    public static final CssIdent[] eastAsianVariantValues;
    public static final CssIdent[] eastAsianWidthValues;

    public static final CssIdent normal, ruby;

    static {
        normal = CssIdent.getIdent("normal");
        ruby = CssIdent.getIdent("ruby");

        String[] _eastAsianVariantValues = {"jis78", "jis83", "jis90", "jis04",
                "simplified", "traditional"};
        String[] _eastAsianWidthValues = {"full-width", "proportional-width"};

        eastAsianVariantValues = new CssIdent[_eastAsianVariantValues.length];
        for (int i = 0; i < eastAsianVariantValues.length; i++) {
            eastAsianVariantValues[i] = CssIdent.getIdent(_eastAsianVariantValues[i]);
        }
        Arrays.sort(eastAsianVariantValues);
        eastAsianWidthValues = new CssIdent[_eastAsianWidthValues.length];
        for (int i = 0; i < eastAsianWidthValues.length; i++) {
            eastAsianWidthValues[i] = CssIdent.getIdent(_eastAsianWidthValues[i]);
        }
        Arrays.sort(eastAsianWidthValues);
    }

    public static final CssIdent getEastAsianVariantValue(CssIdent ident) {
        int idx = Arrays.binarySearch(eastAsianVariantValues, ident);
        if (idx >= 0) {
            return eastAsianVariantValues[idx];
        }
        return null;
    }

    public static final CssIdent getEastAsianWidthValue(CssIdent ident) {
        for (CssIdent id : eastAsianWidthValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        if (ruby.equals(ident)) {
            return ruby;
        }
        CssIdent id = getEastAsianWidthValue(ident);
        if (id == null) {
            id = getEastAsianVariantValue(ident);
        }
        return id;
    }

    /**
     * Create a new CssFontVariantEastAsian
     */
    public CssFontVariantEastAsian() {
        value = initial;
    }

    /**
     * Creates a new CssFontVariantEastAsian
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontVariantEastAsian(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        char op;

        CssValue varValue = null;
        CssValue widValue = null;
        CssValue rubValue = null;
        boolean match;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT) {
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident) || normal.equals(ident)) {
                    if (expression.getCount() != 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    value = val;
                } else {
                    // no inherit, nor normal, test the up-to-three values
                    match = false;
                    if (varValue == null) {
                        match = (getEastAsianVariantValue(ident) != null);
                        if (match) {
                            varValue = val;
                            value = val;
                        }
                    }

                    if (!match && (widValue == null)) {
                        match = (getEastAsianWidthValue(ident) != null);
                        if (match) {
                            widValue = val;
                            value = val;

                        }
                    }
                    if (!match && (rubValue == null)) {
                        match = ruby.equals(ident);
                        if (match) {
                            rubValue = val;
                            value = val;
                        }
                    }
                    if (!match) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                }
            } else {
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
        if (expression.getCount() > 1) {
            // do this to keep the same order for comparisons
            ArrayList<CssValue> v = new ArrayList<CssValue>();
            if (varValue != null) {
                v.add(varValue);
            }
            if (widValue != null) {
                v.add(widValue);
            }
            if (rubValue != null) {
                v.add(rubValue);
            }
            value = new CssValueList(v);
        }
    }

    public CssFontVariantEastAsian(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

