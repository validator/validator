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
import org.w3c.css.values.CssValue;

/**
 * <H4>
 * &nbsp;&nbsp; 'background-color'
 * </H4>
 * <p/>
 * <EM>Value:</EM> &lt;color&gt; | transparent<BR>
 * <EM>Initial:</EM> transparent<BR>
 * <EM>Applies to:</EM> all elements<BR>
 * <EM>Inherited:</EM> no<BR>
 * <EM>Percentage values:</EM> N/A<BR>
 * <p/>
 * This property sets the background color of an element.
 * <PRE>
 * H1 { background-color: #F00 }
 * </PRE>
 *
 * @version $Revision$
 */
public class CssBackgroundColorATSC extends CssProperty {

    CssValue color;

    static CssIdent transparent = new CssIdent("transparent");

    /**
     * Create a new CssBackgroundColorATSC
     */
    public CssBackgroundColorATSC() {
        color = transparent;
    }

    /**
     * Create a new CssBackgroundColorATSC
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBackgroundColorATSC(ApplContext ac, CssExpression expression,
                                  boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        CssValue val = expression.getValue();

        if (val instanceof org.w3c.css.values.CssColor) {
            color = val;
            expression.next();
        } else if (val instanceof CssIdent) {
            if (val.equals(transparent)) {
                color = transparent;
                expression.next();
            } else if (val.equals(inherit)) {
                color = inherit;
                expression.next();
            } else {
                color = new org.w3c.css.values.CssColor(ac, (String) val.get());
                expression.next();
            }
        } else {
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);
        }
    }

    public CssBackgroundColorATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return color;
    }

    /**
     * Returns the color
     */
    public final CssValue getColor() {
        return color;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return color.equals(inherit);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return color.toString();
    }


    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBackgroundATSC cssBackground = ((ATSCStyle) style).cssBackgroundATSC;
        if (cssBackground.color != null)
            style.addRedefinitionWarning(ac, this);
        cssBackground.color = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((ATSCStyle) style).getBackgroundColorATSC();
        } else {
            return ((ATSCStyle) style).cssBackgroundATSC.color;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBackgroundColorATSC &&
                color.equals(((CssBackgroundColorATSC) property).color));
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "background-color";
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return color == transparent;
    }

}
