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
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-variant-emoji
 */
public class CssFontVariantEmoji extends org.w3c.css.properties.css.CssFontVariantEmoji {

    public static final CssIdent[] emojiValues;
    public static final String _emojiValues[] = {"auto", "text", "emoji", "unicode"};

    static {
        emojiValues = new CssIdent[_emojiValues.length];
        int i = 0;
        for (String s : _emojiValues) {
            emojiValues[i++] = CssIdent.getIdent(s);
        }
        Arrays.sort(emojiValues);
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        int idx = Arrays.binarySearch(emojiValues, ident);
        if (idx >= 0) {
            return emojiValues[idx];
        }
        return null;
    }

    /**
     * Create a new CssFontVariantEmoji
     */
    public CssFontVariantEmoji() {
        value = initial;
    }

    /**
     * Creates a new CssFontVariantEmoji
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontVariantEmoji(ApplContext ac, CssExpression expression, boolean check)
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

    public CssFontVariantEmoji(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

