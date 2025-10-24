//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css1;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssValue;

/**
 * <H4>
 * &nbsp;&nbsp; 'height'
 * </H4>
 * <p/>
 * <EM>Value:</EM> &lt;length&gt; | auto <BR>
 * <EM>Initial:</EM> auto<BR>
 * <EM>Applies to:</EM> block-level and replaced elements<BR>
 * <EM>Inherited:</EM> no<BR>
 * <EM>Percentage values:</EM> N/A<BR>
 * <p/>
 * This property can be applied to text, but it is most useful with replaced
 * elements such as images. The height is to be enforced by scaling the image
 * if necessary. When scaling, the aspect ratio of the image is preserved if
 * the 'width' property is 'auto'.
 * <p/>
 * Example:
 * <PRE>
 * IMG.icon { height: 100px }
 * </PRE>
 * <p/>
 * If the 'width' and 'height' of a replaced element are both 'auto', these
 * properties will be set to the intrinsic dimensions of the element.
 * <p/>
 * If applied to a textual element, the height can be enforced with e.g. a
 * scrollbar.
 * <p/>
 * Negative values are not allowed.
 *
 * @version $Revision$
 */
public class CssHeightMob extends CssProperty {

    CssValue value;

    private static CssIdent auto = new CssIdent("auto");

    /**
     * Create a new CssHeightMob
     */
    public CssHeightMob() {
        value = auto;
    }

    /**
     * Create a new CssHeightMob
     *
     * @param expression The expression for this property
     * @throws InvalidParamException The expression is incorrect
     */
    public CssHeightMob(ApplContext ac, CssExpression expression,
                        boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();
        if (val.equals(inherit)) {
            value = inherit;
        } else if (val instanceof CssLength) {
            float f = ((Float) val.get()).floatValue();
            if (f < 0) {
                throw new InvalidParamException("negative-value",
                        val.toString(), ac);
            }
            value = val;
        } else if (val.equals(auto)) {
            value = auto;
        } else if (val instanceof CssNumber) {
            value = ((CssNumber) val).getLength();
        } else {
            throw new InvalidParamException("value", expression.getValue(),
                    getPropertyName(), ac);
        }

        expression.next();
    }

    public CssHeightMob(ApplContext ac, CssExpression expression)
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
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "height";
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return value == inherit;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css1Style style0 = (Css1Style) style;
        if (style0.cssHeightMob != null)
            style0.addRedefinitionWarning(ac, this);
        style0.cssHeightMob = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getHeightMob();
        } else {
            return ((Css1Style) style).cssHeightMob;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssHeightMob &&
                value.equals(((CssHeightMob) property).value));
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return value == auto;
    }

}
