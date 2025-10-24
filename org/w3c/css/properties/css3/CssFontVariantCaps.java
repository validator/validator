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
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.Arrays;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-variant-caps
 */
public class CssFontVariantCaps extends org.w3c.css.properties.css.CssFontVariantCaps {

    public static final CssIdent normal;
    public static final CssIdent[] capsValues;
    public static final String _capsValues[] = {"small-caps", "all-small-caps",
            "petite-caps", "all-petite-caps", "titling-caps", "unicase"};

    static {
        normal = CssIdent.getIdent("normal");
        capsValues = new CssIdent[_capsValues.length];
        int i = 0;
        for (String s : _capsValues) {
            capsValues[i++] = CssIdent.getIdent(s);
        }
        Arrays.sort(capsValues);
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        int idx = Arrays.binarySearch(capsValues, ident);
        if (idx >= 0) {
            return capsValues[idx];
        }
        return null;
    }

    /**
     * Create a new CssFontVariantCaps
     */
    public CssFontVariantCaps() {
        value = initial;
    }

    /**
     * Creates a new CssFontVariantCaps
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontVariantCaps(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        CssIdent ident = val.getIdent();
        if (!CssIdent.isCssWide(ident) &&
                !normal.equals(ident) &&
                (getAllowedValue(ident) == null)) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssFontVariantCaps(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

