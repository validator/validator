// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/text.html#white-space-prop
 */
public class CssWhiteSpace extends org.w3c.css.properties.css.CssWhiteSpace {

    public static CssIdent[] allowed_values;

    static {
        String[] WHITESPACE = {
                "normal", "pre", "nowrap", "pre-wrap", "pre-line"
        };
        allowed_values = new CssIdent[WHITESPACE.length];
        int i = 0;
        for (String aWS : WHITESPACE) {
            allowed_values[i++] = CssIdent.getIdent(aWS);
        }
    }

    public static final CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssWhiteSpace
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          values are incorrect
     */
    public CssWhiteSpace(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();
        setByUser();

        if (val.getType() == CssTypes.CSS_IDENT) {
            if (inherit.equals(val)) {
                value = inherit;
            } else {
                value = getMatchingIdent((CssIdent) val);
            }
            if (value != null) {
                expression.next();
                return;
            }
        }
        throw new InvalidParamException("value", expression.getValue(),
                getPropertyName(), ac);
    }

    public CssWhiteSpace(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}
