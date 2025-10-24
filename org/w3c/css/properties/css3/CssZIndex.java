// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 1997-2010.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2016/WD-css-position-3-20160517/#propdef-z-index
 */
public class CssZIndex extends org.w3c.css.properties.css.CssZIndex {

    /**
     * Create a new CssZIndex
     */
    public CssZIndex() {
        value = initial;
    }

    /**
     * Create a new CssZIndex
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      true will test the number of parameters
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssZIndex(ApplContext ac, CssExpression expression, boolean check)
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
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ide = val.getIdent();
                if (CssIdent.isCssWide(ide)) {
                    value = val;
                    break;
                } else if (auto.equals(ide)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    /**
     * Create a new CssZIndex
     *
     * @param ac,        the Context
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssZIndex(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return (auto == value) || (auto == initial);
    }

}
