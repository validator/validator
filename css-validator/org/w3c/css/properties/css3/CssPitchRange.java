// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#propdef-pitch-range
 * @deprecated
 */
@Deprecated
public class CssPitchRange extends org.w3c.css.properties.css.CssPitchRange {

    /**
     * Create a new CssPitchRange
     */
    public CssPitchRange() {
        value = initial;
    }

    /**
     * Creates a new CssPitchRange
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssPitchRange(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        // same as CSS21 plus a warning
        ac.getFrame().addWarning("deprecatedproperty", getPropertyName());

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                CssNumber n = val.getNumber();
                n.checkPositiveness(ac, this);
                n.checkLowerEqualThan(ac, 100, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (inherit.equals(val)) {
                    value = inherit;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();

    }

    public CssPitchRange(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

