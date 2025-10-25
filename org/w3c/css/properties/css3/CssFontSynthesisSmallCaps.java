//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
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
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-synthesis-small-caps
 */
public class CssFontSynthesisSmallCaps extends org.w3c.css.properties.css.CssFontSynthesisSmallCaps {

    public static final CssIdent[] allowedValues;

    static {
        String[] _allowedValues = {"auto", "none"};
        allowedValues = new CssIdent[_allowedValues.length];
        for (int i = 0; i < allowedValues.length; i++) {
            allowedValues[i] = CssIdent.getIdent(_allowedValues[i]);
        }
        Arrays.sort(allowedValues);
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        for (CssIdent id : allowedValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssFontSynthesisSmallCaps
     */
    public CssFontSynthesisSmallCaps() {
        value = initial;
    }

    /**
     * Creates a new CssFontSynthesisSmallCaps
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontSynthesisSmallCaps(ApplContext ac, CssExpression expression, boolean check)
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
        if (!CssIdent.isCssWide(ident) && (getAllowedValue(ident) == null)) {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssFontSynthesisSmallCaps(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

