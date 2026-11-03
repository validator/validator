//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-position-anchor retrieved 2026-11-03
 */
public class CssPositionAnchor extends org.w3c.css.properties.css.CssPositionAnchor {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"normal", "none", "auto", "match-parent"};
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
     * Create a new CssPositionAnchor
     */
    public CssPositionAnchor() {
        value = initial;
    }

    /**
     * Creates a new CssPositionAnchor
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssPositionAnchor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        // <anchor-name> is a dashed ident
        if (val.getType() != CssTypes.CSS_DASHED_IDENT) {
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            CssIdent id = val.getIdent();
            if (!CssIdent.isCssWide(id) && getAllowedIdent(id) == null) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
        }
        value = val;
        expression.next();
    }

    public CssPositionAnchor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
