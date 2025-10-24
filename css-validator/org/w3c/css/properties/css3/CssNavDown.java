//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-nav-up
 * @see CssNavUp
 */
public class CssNavDown extends org.w3c.css.properties.css.CssNavDown {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssNavDown
     */
    public CssNavDown() {
        value = initial;
    }

    /**
     * Create a new CssNavDown
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      true will test the number of parameters
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssNavDown(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();
        value = CssNavUp.parseNav(ac, expression, check, this);
    }

    /**
     * Create a new CssNavDown
     *
     * @param ac,        the Context
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssNavDown(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return (auto == value) || (value == initial);
    }
}
