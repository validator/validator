//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec hhttp://www.w3.org/TR/2011/REC-SVG11-20110816/text.html#WritingModeProperty
 */
public class CssWritingMode extends org.w3c.css.properties.css.CssWritingMode {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"lr-tb", "rl-tb", "tb-rl", "lr", "rl", "tb"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
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

    /**
     * Create a new CssWritingMode
     */
    public CssWritingMode() {
        value = initial;
    }

    /**
     * Creates a new CssWritingMode
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssWritingMode(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            boolean isCss3 = (ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0);
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident)) {
                value = val;
            } else {
                CssIdent css3ident = null;
                if (isCss3) {
                    css3ident = org.w3c.css.properties.css3.CssWritingMode.getAllowedIdent(ident);
                }
                if (getAllowedIdent(ident) == null && css3ident == null) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                value = val;
                if (css3ident == null && isCss3) {
                    // Css3 and onward, the SVG values are deprecated, add a warning
                    ac.getFrame().addWarning("deprecated", getPropertyName());
                }
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();

    }

    public CssWritingMode(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

