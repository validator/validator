//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-anchor-name retrieved 2026-11-03
 */
public class CssAnchorName extends org.w3c.css.properties.css.CssAnchorName {

    /**
     * Create a new CssAnchorName
     */
    public CssAnchorName() {
        value = initial;
    }

    /**
     * Creates a new CssAnchorName
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssAnchorName(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        value = parseAnchorNames(ac, expression, new CssIdent[]{none},
                getPropertyName());
    }

    public CssAnchorName(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Parses a value that's either a CSS-wide keyword or one of the given
     * keywords alone, or a comma-separated list of anchor names
     * (dashed idents).
     */
    static CssValue parseAnchorNames(ApplContext ac, CssExpression expression,
                                     CssIdent[] keywords, String propertyName)
            throws InvalidParamException {
        CssValue val = expression.getValue();
        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (expression.getCount() > 1
                    || !(CssIdent.isCssWide(id) || isOneOf(id, keywords))) {
                throw new InvalidParamException("value", val,
                        propertyName, ac);
            }
            expression.next();
            return val;
        }
        ArrayList<CssValue> values = new ArrayList<>();
        while (!expression.end()) {
            val = expression.getValue();
            char op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_DASHED_IDENT) {
                throw new InvalidParamException("value", val,
                        propertyName, ac);
            }
            values.add(val);
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        return (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    static boolean isOneOf(CssIdent ident, CssIdent[] idents) {
        for (CssIdent id : idents) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }
}
