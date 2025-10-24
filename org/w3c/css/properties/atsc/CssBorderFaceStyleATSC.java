//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.atsc;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 */
public class CssBorderFaceStyleATSC {

    int value;

    private static String[] BORDERSTYLE = {
            "none", "hidden", "dotted", "dashed", "solid", "double", "groove",
            "ridge", "inset", "outset", "inherit"};

    private static int[] hash_values;

    /**
     * Create a new CssBorderFaceStyleATSC
     */
    public CssBorderFaceStyleATSC() {
        // nothing to do
    }

    /**
     * Create a new CssBorderFaceStyleATSC with an another CssBorderFaceStyle
     *
     * @param another An another side.
     */
    public CssBorderFaceStyleATSC(CssBorderFaceStyleATSC another) {
        value = another.value;
    }

    /**
     * Create a new CssBorderFaceStyleATSC
     *
     * @param expression The expression for this face
     * @throws InvalidParamException The expression is incorrect
     */
    public CssBorderFaceStyleATSC(ApplContext ac, CssExpression expression,
                                  boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        if (val instanceof CssIdent) {
            int hash = val.hashCode();
            for (int i = 0; i < BORDERSTYLE.length; i++)
                if (hash_values[i] == hash) {
                    value = i;
                    expression.next();
                    return;
                }
        }

        throw new InvalidParamException("value", val.toString(), "style", ac);
    }

    public CssBorderFaceStyleATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value equals inherit
     */
    public boolean isSoftlyInherited() {
        return value == (BORDERSTYLE.length - 1);
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return BORDERSTYLE[value];
    }

    /**
     * Returns the value
     */
    public String getStyle() {
        return BORDERSTYLE[value];
    }

    /**
     * Compares two side for equality.
     *
     * @param value The another side.
     */
    public boolean equals(CssBorderFaceStyleATSC style) {
        return value == style.value;
    }

    static {
        hash_values = new int[BORDERSTYLE.length];
        for (int i = 0; i < BORDERSTYLE.length; i++)
            hash_values[i] = BORDERSTYLE[i].hashCode();
    }

}
