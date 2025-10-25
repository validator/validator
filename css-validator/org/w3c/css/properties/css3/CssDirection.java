// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2011.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2019/REC-css-writing-modes-3-20191210/#propdef-direction
 */
public class CssDirection extends org.w3c.css.properties.css.CssDirection {

    /**
     * Create a new CssDirection
     */
    public CssDirection() {
        value = initial;
    }

    /**
     * Create a new CssDirection
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssDirection(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();
        if (val.getType() != CssTypes.CSS_IDENT) {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        CssIdent id = val.getIdent();
        if (CssIdent.isCssWide(id)) {
            value = val;
        } else if (id.equals(ltr)) {
            value = val;
        } else if (id.equals(rtl)) {
            value = val;
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
        expression.next();

    }

    public CssDirection(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (value == initial) || (value == ltr);
    }

}
