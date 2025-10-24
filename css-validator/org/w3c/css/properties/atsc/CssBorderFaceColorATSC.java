//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.atsc;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 */
public class CssBorderFaceColorATSC {

    CssValue face;

    /**
     * Create a new CssBorderFaceColor
     */
    public CssBorderFaceColorATSC() {
        face = new org.w3c.css.values.CssColor();
    }

    /**
     * Create a new CssBorderFaceColor with a color property.
     *
     * @param color A color property
     */
    public CssBorderFaceColorATSC(org.w3c.css.properties.css2.CssColor color) {
        face = color.getColor();
    }

    /**
     * Create a new CssBorderFaceColor with an another CssBorderFaceColor
     *
     * @param another An another face.
     */
    public CssBorderFaceColorATSC(CssBorderFaceColorATSC another) {
        face = another.face;
    }

    /**
     * Create a new CssBorderFaceColor with an expression
     *
     * @param expression The expression for this property.
     * @throws InvalidParamException color is not a color
     */
    public CssBorderFaceColorATSC(ApplContext ac, CssExpression expression,
                                  boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        if (val instanceof CssColor) {
            face = val;
        } else if (val.equals(CssProperty.inherit)) {
            face = CssProperty.inherit;
        } else if (val instanceof CssIdent) {
            face = new org.w3c.css.values.CssColor(ac, (String) val.get());
        } else {
            throw new InvalidParamException("value", val.toString(),
                    "border-color", ac);
        }
        expression.next();
    }

    public CssBorderFaceColorATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the internal color
     */
    public CssValue getColor() {
        return face;
    }

    /**
     * Is the value of this face is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return false; // @@ FIXME face.isDefault();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return face.toString();
    }

    /**
     * Compares two faces for equality.
     *
     * @param value The another faces.
     */
    public boolean equals(CssBorderFaceColorATSC color) {
        return this.face.equals(color.face);
    }
}
