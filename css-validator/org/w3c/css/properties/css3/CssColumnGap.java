//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM, Keio, Beihang)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-align-3-20200421/#propdef-column-gap
 * @see CssRowGap
 */

public class CssColumnGap extends org.w3c.css.properties.css.CssColumnGap {

    /**
     * Create a new CssColumnGap
     */
    public CssColumnGap() {
        value = initial;
    }

    /**
     * Create a new CssColumnGap
     */
    public CssColumnGap(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        value = CssRowGap.parseRowGap(ac, expression, this);
    }

    public CssColumnGap(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (value == initial);
    }

}
