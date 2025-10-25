//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.mobile;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/colors.html#propdef-background-image
 * @spec http://www.w3.org/TR/2008/CR-css-mobile-20081210/#properties
 */
public class CssBackgroundImage extends org.w3c.css.properties.css.CssBackgroundImage {

    public CssValue url = null;


    public static boolean checkMatchingIdent(CssIdent ident) {
        return none.equals(ident);
    }

    /**
     * Create a new CssBackgroundImage
     */
    public CssBackgroundImage() {
        url = none;
    }

    /**
     * Creates a new CssBackgroundImage
     *
     * @param ac         The context
     * @param expression The expression for this property
     * @param check      if count check must be performed
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public CssBackgroundImage(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val = expression.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_URL:
                url = val;
                break;
            case CssTypes.CSS_IDENT:
                if (inherit.equals(val)) {
                    url = inherit;
                    break;
                }
                if (none.equals(val)) {
                    url = none;
                    break;
                }
            default:
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssBackgroundImage(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return url;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return (url == inherit);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return url.toString();
    }


    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        org.w3c.css.properties.css.CssBackground cssBackground = ((Css1Style) style).cssBackground;
        if (cssBackground.image != null) {
            style.addRedefinitionWarning(ac, this);
        }
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
        return ((property == null && url == null)
                || (property instanceof CssBackgroundImage &&
                url != null &&
                url.equals(((CssBackgroundImage) property).url)));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (url == none);
    }

}
