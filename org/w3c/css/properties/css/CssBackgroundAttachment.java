// $Id$
// @author Yves Lafon <ylafon@w3.org>

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
public class CssBackgroundAttachment extends CssProperty {

    /**
     * Create a new CssBackgroundAttachment
     */
    public CssBackgroundAttachment() {
    }

    /**
     * Creates a new CssBackgroundAttachment
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      if some length checking is required
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundAttachment(ApplContext ac, CssExpression expression,
                                   boolean check) throws InvalidParamException {


        throw new InvalidParamException("unrecognize", ac);

    }

    public CssBackgroundAttachment(ApplContext ac, CssExpression expression)
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
        return "background-attachment";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css1Style) style).cssBackground.attachment != null)
            style.addRedefinitionWarning(ac, this);
        ((Css1Style) style).cssBackground.attachment = this;

    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getBackgroundAttachment();
        } else {
            return ((Css1Style) style).cssBackground.attachment;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBackgroundAttachment &&
                value.equals(((CssBackgroundAttachment) property).value));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false;
    }
}
