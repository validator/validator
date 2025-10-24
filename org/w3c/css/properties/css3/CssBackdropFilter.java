// Initial Author: Chris Rebert <css.validator@chrisrebert.com>
//
// (c) COPYRIGHT World Wide Web Consortium (MIT, ERCIM, Keio University, and Beihang University), 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec https://drafts.fxtf.org/filter-effects-2/#BackdropFilterProperty retrieved 12 sept 2021
 * @see CssFilter
 */
public class CssBackdropFilter extends org.w3c.css.properties.css.CssBackdropFilter {


    /**
     * Create a new CssFilter
     */
    public CssBackdropFilter() {
        value = initial;
    }

    public CssBackdropFilter(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Create a new CssFilter
     *
     * @param expression The expressions for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssBackdropFilter(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        value = CssFilter.parseFilter(ac, expression, check, getPropertyName());
    }
}
