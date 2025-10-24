//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.atsc;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssURL;
import org.w3c.css.values.CssValue;

/**
 * <H4>
 * &nbsp;&nbsp; 'background-image'
 * </H4>
 * <p/>
 * <EM>Value:</EM> &lt;url&gt; | none<BR>
 * <EM>Initial:</EM> none<BR>
 * <EM>Applies to:</EM> all elements<BR>
 * <EM>Inherited:</EM> no<BR>
 * <EM>Percentage values:</EM> N/A<BR>
 * <P> This property sets the background image of an element. When setting a
 * background image, one should also set a background color that will be used
 * when the image is unavailable. When the image is available, it is overlaid
 * on top of the background color.
 * <PRE>
 * BODY { background-image: url(marble.gif) }
 * P { background-image: none }
 * </PRE>
 *
 * @version $Revision$
 */
public class CssBackgroundImageATSC extends CssProperty {

    CssValue url;

    private static CssIdent none = new CssIdent("none");

    /**
     * Create a new CssBackgroundImageATSC
     */
    public CssBackgroundImageATSC() {
        url = none;
    }

    /**
     * Creates a new CssBackgroundImageATSC
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundImageATSC(ApplContext ac, CssExpression expression,
                                  boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val = expression.getValue();
        if (val instanceof CssURL) {
            url = val;
            expression.next();
        } else if (val.equals(inherit)) {
            url = inherit;
            expression.next();
        } else if (val.equals(none)) {
            url = none;
            expression.next();
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }
    }

    public CssBackgroundImageATSC(ApplContext ac, CssExpression expression)
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
        return url.equals(inherit);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return url.toString();
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "background-image";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBackgroundATSC cssBackground = ((ATSCStyle) style).cssBackgroundATSC;
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
            return ((ATSCStyle) style).getBackgroundImageATSC();
        } else {
            return ((ATSCStyle) style).cssBackgroundATSC.image;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBackgroundImageATSC &&
                url.equals(((CssBackgroundImageATSC) property).url));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return url == none;
    }

}
