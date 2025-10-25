// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio, 2010.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS1
 */
public class CssBackgroundImage extends CssProperty {

    /**
     * Create a new CssBackgroundImage
     */
    public CssBackgroundImage() {
    }

    /**
     * Creates a new CssBackgroundImage
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      boolean
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundImage(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        throw new InvalidParamException("unrecognize", ac);

    }

    public CssBackgroundImage(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */

    public Object get() {
        return value;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return (inherit == value);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return "background-image";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBackground cssBackground = ((Css1Style) style).cssBackground;
        if (cssBackground.image != null)
            style.addRedefinitionWarning(ac, this);
        cssBackground.image = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getBackgroundImage();
        } else {
            return ((Css1Style) style).cssBackground.image;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBackgroundImage && value != null &&
                value.equals(((CssBackgroundImage) property).value));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }

}
