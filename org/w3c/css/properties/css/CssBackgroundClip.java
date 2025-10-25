// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 2010  World Wide Web Consortium (MIT, ERCIM, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css3.Css3Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS3
 */

public class CssBackgroundClip extends CssProperty {

    /**
     * Create a new CssBackgroundClip
     */
    public CssBackgroundClip() {
    }

    /**
     * Create a new CssBackgroundClip
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect value
     */
    public CssBackgroundClip(ApplContext ac, CssExpression expression,
                             boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognized", ac);

    }

    public CssBackgroundClip(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        // TODO FIXME -> in CssStyle
        if (((Css3Style) style).cssBackgroundClip != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssBackgroundClip = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getCssBackgroundClip();
        } else {
            return ((Css3Style) style).cssBackgroundClip;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBackgroundClip &&
                value.equals(((CssBackgroundClip) property).value));
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "background-clip";
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
        return inherit.equals(value);
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
        return false;
    }

}
