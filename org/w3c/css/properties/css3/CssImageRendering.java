//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2019.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/CRD-css-images-3-20201217/#propdef-image-rendering
 */
public class CssImageRendering extends org.w3c.css.properties.css.CssImageRendering {

    public static final CssIdent[] allowed_values, deprecated_values;

    static {
        String[] _deprecated_values = {"optimizeSpeed", "optimizeQuality"};
        String[] _allowed_values = {"auto", "smooth", "high-quality", "crisp-edges", "pixelated"};

        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }

        deprecated_values = new CssIdent[_deprecated_values.length];
        i = 0;
        for (String s : _deprecated_values) {
            deprecated_values[i++] = CssIdent.getIdent(s);
        }

    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    // TODO should we add something specific for their replacement?
    public static final CssIdent getDeprecatedValue(CssIdent ident) {
        for (CssIdent id : deprecated_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssImageRendering
     */
    public CssImageRendering() {
        value = initial;
    }

    /**
     * Creates a new CssImageRendering
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssImageRendering(ApplContext ac, CssExpression expression, boolean check)
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
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident)) {
                value = val;
            } else {
                if (getAllowedValue(ident) == null) {
                    if (getDeprecatedValue(ident) == null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    ac.getFrame().addWarning("deprecated", value.toString());
                }
                value = val;
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssImageRendering(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

