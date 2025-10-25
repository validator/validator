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
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssValue;

/**
 * <H4>
 * &nbsp;&nbsp; 'text-indent'
 * </H4>
 * <p/>
 * <EM>Value:</EM> &lt;length&gt; | &lt;percentage&gt;<BR>
 * <EM>Initial:</EM> 0<BR>
 * <EM>Applies to:</EM> block-level elements<BR>
 * <EM>Inherited:</EM> yes<BR>
 * <EM>Percentage values:</EM> refer to parent element's width<BR>
 * <p/>
 * The property specifies the indentation that appears before the first formatted
 * line. The value of 'text-indent' may be negative, but there may be
 * implementation-specific limits. An indentation is not inserted in the middle
 * of an element that was broken by another (such as 'BR' in HTML).
 * <p/>
 * Example:
 * <PRE>
 * P { text-indent: 3em }
 * </PRE>
 *
 * @version $Revision$
 */
public class CssTextIndentMob extends CssProperty {

    CssValue value = new CssLength();

    /**
     * Create a new CssTextIndent
     */
    public CssTextIndentMob() {
    }

    /**
     * Create a new CssTextIndentMob
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssTextIndentMob(ApplContext ac, CssExpression expression,
                            boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();

        if (val.equals(inherit)) {
            value = inherit;
        } else if (val instanceof CssLength) {
            value = val;
        } else if (val instanceof CssNumber) {
            value = ((CssNumber) val).getLength();
        } else {
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);
        }

        expression.next();
    }

    public CssTextIndentMob(ApplContext ac, CssExpression expression)
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
        return "text-indent";
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
        if (style0.cssTextIndentMob != null)
            style0.addRedefinitionWarning(ac, this);
        style0.cssTextIndentMob = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css1Style) style).getTextIndentMob();
        } else {
            return ((Css1Style) style).cssTextIndentMob;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssTextIndentMob &&
                value.equals(((CssTextIndentMob) property).value));
    }
}
