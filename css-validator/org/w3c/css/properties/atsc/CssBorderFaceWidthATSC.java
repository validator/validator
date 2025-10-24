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
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLength;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssValue;

/**
 * @version $Revision$
 */
public class CssBorderFaceWidthATSC {

    CssValue value;

    /**
     * Create a new CssBorderFaceWidthATSC
     */
    public CssBorderFaceWidthATSC() {
        value = medium;
    }

    /**
     * Create a new CssBorderFaceWidthATSC from an another CssBorderFaceWidthATSC
     *
     * @param another The another side.
     */
    public CssBorderFaceWidthATSC(CssBorderFaceWidthATSC another) {
        value = another.value;
    }

    /**
     * Create a new CssBorderFaceWidth
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssBorderFaceWidthATSC(ApplContext ac, CssExpression expression,
                                  boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        if (val instanceof CssLength) {
            CssLength l = (CssLength) val;
            if (l.isPositive()) {
                this.value = val;
            } else {
                throw new InvalidParamException("negative-value", val.toString(), ac);
            }
        } else if (val instanceof CssNumber) {
            value = ((CssNumber) val).getLength();
        } else if (val.equals(thin)) {
            value = thin;
        } else if (val.equals(medium)) {
            value = medium;
        } else if (val.equals(thick)) {
            value = thick;
        } else if (val.equals(CssProperty.inherit)) {
            value = CssProperty.inherit;
        } else {
            throw new InvalidParamException("value", val.toString(), "width", ac);
        }

        expression.next();
    }

    public CssBorderFaceWidthATSC(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the internal value
     */
    public CssValue getValue() {
        return value;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Compares two sides for equality.
     *
     * @param value The another side.
     */
    public boolean equals(CssBorderFaceWidthATSC another) {
        return value.equals(another.value);
    }

    private static CssIdent thin = new CssIdent("thin");
    private static CssIdent medium = new CssIdent("medium");
    private static CssIdent thick = new CssIdent("thick");

}



