//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;

/**
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-anchor-scope retrieved 2026-11-03
 */
public class CssAnchorScope extends org.w3c.css.properties.css.CssAnchorScope {

    public static final CssIdent all = CssIdent.getIdent("all");

    /**
     * Create a new CssAnchorScope
     */
    public CssAnchorScope() {
        value = initial;
    }

    /**
     * Creates a new CssAnchorScope
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssAnchorScope(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        value = CssAnchorName.parseAnchorNames(ac, expression,
                new CssIdent[]{none, all}, getPropertyName());
    }

    public CssAnchorScope(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
