// $Id$
//
// (c) COPYRIGHT MIT, ECRIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css3.media;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-mediaqueries-5-20200731/#descdef-media-grid
 */
public class MediaGrid extends MediaFeature {

    /**
     * Create a new MediaGrid
     */
    public MediaGrid() {
    }

    /**
     * Create a new MediaGrid.
     *
     * @param expression The expression for this media feature
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public MediaGrid(ApplContext ac, String modifier,
                     CssExpression expression, boolean check)
            throws InvalidParamException {

        if (modifier != null) {
            throw new InvalidParamException("nomodifiermedia",
                    getFeatureName(), ac);
        }

        if (expression != null) {
            if (expression.getCount() > 1) {
                throw new InvalidParamException("unrecognize", ac);
            }
            if (expression.getCount() == 0) {
                throw new InvalidParamException("few-value", getFeatureName(), ac);
            }
            CssValue val = expression.getValue();
            // it must be a >=0 integer only
            if (val.getType() == CssTypes.CSS_NUMBER) {
                val.getCheckableValue().checkInteger(ac, getFeatureName());
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    CssNumber valnum = (CssNumber) val;
                    int gridval = valnum.getInt();
                    if (gridval != 0 && gridval != 1) {
                        throw new InvalidParamException("grid",
                                val.toString(), ac);
                    }
                } else {
                    // best we can do is >=0
                    val.getCheckableValue().checkPositiveness(ac, getFeatureName());
                }
                value = val;
            } else {
                throw new InvalidParamException("unrecognize", ac);
            }
        }
    }

    // just in case someone wants to call it externally...
    public void setModifier(ApplContext ac, String modifier)
            throws InvalidParamException {
        throw new InvalidParamException("nomodifiermedia",
                getFeatureName(), ac);
    }

    public MediaGrid(ApplContext ac, String modifier, CssExpression expression)
            throws InvalidParamException {
        this(ac, modifier, expression, false);
    }

    /**
     * Returns the value of this media feature.
     */

    public Object get() {
        return value;
    }

    /**
     * Returns the name of this media feature.
     */
    public final String getFeatureName() {
        return "grid";
    }

    /**
     * Compares two media features for equality.
     *
     * @param other The other media features.
     */
    public boolean equals(MediaFeature other) {
        try {
            MediaGrid mg = (MediaGrid) other;
            return (((value == null) && (mg.value == null)) || ((value != null) && value.equals(mg.value)));
        } catch (ClassCastException cce) {
            return false;
        }

    }
}
