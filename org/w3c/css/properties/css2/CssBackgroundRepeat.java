//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/colors.html#propdef-background-repeat
 */
public class CssBackgroundRepeat extends org.w3c.css.properties.css.CssBackgroundRepeat {

    private static CssIdent[] allowed_values;

    static {
        String[] REPEAT = {"repeat", "repeat-x", "repeat-y", "no-repeat"};

        allowed_values = new CssIdent[REPEAT.length];
        int i = 0;
        for (String aREPEAT : REPEAT) {
            allowed_values[i++] = CssIdent.getIdent(aREPEAT);
        }
    }

    public static boolean checkMatchingIdent(CssIdent ident) {
        return (getMatchingIdent(ident) != null);
    }

    protected static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }


    /**
     * Create a new CssBackgroundRepeat
     */
    public CssBackgroundRepeat() {
        value = repeat;
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @throws InvalidParamException The expression is incorrect
     */
    public CssBackgroundRepeat(ApplContext ac, CssExpression expression,
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
        if (inherit.equals(val)) {
            value = inherit;
        } else {
            value = getMatchingIdent((CssIdent) val);
            if (value == null) {
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
            }
        }
        expression.next();
    }

    public CssBackgroundRepeat(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (repeat == value);
    }
}



