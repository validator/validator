// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT 1995-2010
// World Wide Web Consortium (MIT, ERCIM, Keio University)
//
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-span
 */

public class CssColumnSpan extends org.w3c.css.properties.css.CssColumnSpan {


    static CssIdent all;

    static {
        all = CssIdent.getIdent("all");
    }

    /**
     * Create a new CssColumnSpan
     */
    public CssColumnSpan() {
        value = initial;

    }

    /**
     * Create a new CssColumnSpan
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssColumnSpan(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {
        setByUser(); // tell this property is set by the user
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (all.equals(ident) || none.equals(ident)) {
                    value = val;
                    break;
                }
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
    }

    public CssColumnSpan(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssColumnSpan &&
                value.equals(((CssColumnSpan) property).value));
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns true if this property is "softly" inherited
     */
    public boolean isSoftlyInherited() {
        return (value == inherit);
    }

    /**
     * Returns a string representation of the object
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        // we only have 3 values
        return (initial == value);
    }

}
