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

/**
 * Be careful, this is not a CSS1 property !
 *
 * @version $Revision$
 */
public class CssBorderRightStyleATSC extends CssProperty {

    CssBorderFaceStyleATSC face;

    /**
     * Create a new CssBorderRightStyleATSC
     */
    public CssBorderRightStyleATSC() {
        setByUser();

        face = new CssBorderFaceStyleATSC();
    }

    /**
     * Create a new CssBorderRightStyle with an another CssBorderFaceStyle
     *
     * @param another The another side.
     */
    public CssBorderRightStyleATSC(CssBorderFaceStyleATSC another) {
        setByUser();

        face = another;
    }

    /**
     * Create a new CssBorderRightStyleATSC
     *
     * @param expression The expression for this property.
     * @throws InvalidParamException Values are incorrect
     */
    public CssBorderRightStyleATSC(ApplContext ac, CssExpression expression,
                                   boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();
        face = new CssBorderFaceStyleATSC(ac, expression);
    }

    public CssBorderRightStyleATSC(ApplContext ac, CssExpression expression)
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
     * Returns the value
     */
    public String getStyle() {
        return face.getStyle();
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
        return "border-right-style";
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        CssBorderRightATSC right = ((ATSCStyle) style).cssBorderATSC.right;
        if (right.style != null)
            style.addRedefinitionWarning(ac, this);
        right.style = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((ATSCStyle) style).getBorderRightStyleATSC();
        } else {
            return ((ATSCStyle) style).cssBorderATSC.getRight().style;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param value The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssBorderRightStyleATSC &&
                face.equals(((CssBorderRightStyleATSC) property).face));
    }

}
