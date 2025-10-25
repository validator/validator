//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-text-spacing-trim
 */
public class CssTextSpacingTrim extends org.w3c.css.properties.css.CssTextSpacingTrim {

    private static CssIdent[] allowed_values;
    private static CssIdent[] spacing_trim;

    static {
        String[] id_values = {"space-all", "normal", "trim-auto", "trim-start", "space-first", "trim-all"};
        String[] other_values = {"auto"};
        allowed_values = new CssIdent[id_values.length + other_values.length];
        spacing_trim = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            spacing_trim[i] = CssIdent.getIdent(s);
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        for (String s : other_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getSpacingTrimIdent(CssIdent ident) {
        for (CssIdent id : spacing_trim) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssTextSpacingTrim
     */
    public CssTextSpacingTrim() {
        value = initial;
    }

    /**
     * Creates a new CssTextSpacingTrim
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTextSpacingTrim(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        // ident, so inherit, or allowed value
        if (!CssIdent.isCssWide(val.getIdent()) && getAllowedIdent(val.getIdent()) == null) {
            throw new InvalidParamException("value",
                    expression.getValue(),
                    getPropertyName(), ac);
        }
        value = val;
        expression.next();
    }

    public CssTextSpacingTrim(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

