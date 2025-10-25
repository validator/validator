// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2013.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/page.html#propdef-orphans
 */
public class CssOrphans extends org.w3c.css.properties.css.CssOrphans {

    public static final CssNumber def = new CssNumber(2);

    /**
     * Create a new CssOrphans
     */
    public CssOrphans() {
    }

    /**
     * Create a new CssOrphans
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      true will test the number of parameters
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssOrphans(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                CssCheckableValue number = val.getCheckableValue();
                number.checkInteger(ac, this);
                number.checkStrictPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ide = (CssIdent) val;
                if (inherit.equals(ide)) {
                    value = inherit;
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    /**
     * Create a new CssOrphans
     *
     * @param ac,        the Context
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssOrphans(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return (def.equals(value));
    }

}
