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
import org.w3c.css.values.CssValue;

/**
 * <H4>
 * &nbsp;&nbsp; 'border-right-width'
 * </H4>
 * <p/>
 * <EM>Value:</EM> thin | medium | thick | &lt;length&gt;<BR>
 * <EM>Initial:</EM> 'medium'<BR>
 * <EM>Applies to:</EM> all elements<BR>
 * <EM>Inherited:</EM> no<BR>
 * <EM>Percentage values:</EM> N/A<BR>
 * <P> This property sets the width of an element's right border. The width of
 * the keyword values are UA dependent, but the following holds: 'thin' &lt;=
 * 'medium' &lt;= 'thick'.
 * <p/>
 * The keyword widths are constant throughout a document:
 * <PRE>
 * H1 { border: solid thick red }
 * P  { border: solid thick blue }
 * </PRE>
 * <p/>
 * In the example above, 'H1' and 'P' elements will have the same border width
 * regardless of font size. To achieve relative widths, the 'em' unit can be
 * used:
 * <PRE>
 * H1 { border: solid 0.5em }
 * </PRE>
 * <p/>
 * Border widths cannot be negative.
 *
 * @version $Revision$
 */
public class CssBorderRightWidthATSC extends CssProperty {

    CssBorderFaceWidthATSC face;

    /**
     * Create a new CssBorderRightWidth
     */
    public CssBorderRightWidthATSC() {
        face = new CssBorderFaceWidthATSC();
    }

    /**
     * Create a new CssBorderRightWidthATSC with an another CssBorderFaceWidthATSC
     *
     * @param another The another side.
     */
    public CssBorderRightWidthATSC(CssBorderFaceWidthATSC another) {
        setByUser();

        face = another;
    }

    /**
     * Create a new CssBorderRightWidthATSC
     *
     * @param expression The expression for this property.
     * @throws InvalidParamException Values are incorrect
     */
    public CssBorderRightWidthATSC(ApplContext ac, CssExpression expression,
                                   boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        face = new CssBorderFaceWidthATSC(ac, expression);
    }

    public CssBorderRightWidthATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return face;
    }

    /**
     * Return the value of this property
     */
    public CssValue getValue() {
        return face.getValue();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return face.toString();
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "border-right-width";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBorderRightATSC right = ((ATSCStyle) style).cssBorderATSC.right;
        if (right.width != null)
            style.addRedefinitionWarning(ac, this);
        right.width = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((ATSCStyle) style).getBorderRightWidthATSC();
        } else {
            return ((ATSCStyle) style).cssBorderATSC.getRight().width;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBorderRightWidthATSC &&
                face.equals(((CssBorderRightWidthATSC) property).face));
    }

}
