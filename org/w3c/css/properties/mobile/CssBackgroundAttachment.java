//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.mobile;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/colors.html#propdef-background-attachment
 * @spec http://www.w3.org/TR/2008/CR-css-mobile-20081210/#properties
 */
public class CssBackgroundAttachment extends org.w3c.css.properties.css.CssBackgroundAttachment {

    public static boolean checkMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    private static CssIdent[] allowed_values;

    static {
        allowed_values = new CssIdent[2];
        allowed_values[0] = CssIdent.getIdent("scroll");
        allowed_values[1] = CssIdent.getIdent("fixed");
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssBackgroundAttachment
     */
    public CssBackgroundAttachment() {
    }

    /**
     * Creates a new CssBackgroundAttachment
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssBackgroundAttachment(ApplContext ac, CssExpression expression,
                                   boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val = expression.getValue();

        if (val.getType() == CssTypes.CSS_IDENT) {
            if (inherit.equals(val)) {
                value = inherit;
                expression.next();
                return;
            }
            CssIdent new_val = getMatchingIdent((CssIdent) val);
            if (new_val != null) {
                value = new_val;
                expression.next();
                return;
            }
        }


        throw new InvalidParamException("value", expression.getValue(),
                getPropertyName(), ac);
    }

    public CssBackgroundAttachment(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
