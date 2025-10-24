// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2008/CR-css3-marquee-20081205/#marquee-play-count
 * @deprecated
 */
@Deprecated
public class CssMarqueePlayCount extends org.w3c.css.properties.css.CssMarqueePlayCount {

    private static CssIdent infinite;

    static {
        infinite = CssIdent.getIdent("infinite");
    }

    /**
     * Create a new CssMarqueeDirection
     */
    public CssMarqueePlayCount() {
        value = initial;
    }

    /**
     * Creates a new CssMarqueeDirection
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMarqueePlayCount(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                CssCheckableValue num = val.getCheckableValue();
                num.checkInteger(ac, this);
                num.checkPositiveness(ac, this);
                value = num;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (inherit.equals(ident)) {
                    value = inherit;
                    break;
                }
                if (infinite.equals(ident)) {
                    value = infinite;
                    break;
                }
                // unrecognized, let it flow.
            default:
                throw new InvalidParamException("value",
                        val, getPropertyName(), ac);
        }
        expression.next();
    }

    public CssMarqueePlayCount(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

