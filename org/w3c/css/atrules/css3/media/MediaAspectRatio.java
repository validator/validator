// $Id$
//
// (c) COPYRIGHT MIT, ECRIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css3.media;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.atrules.css.media.MediaRangeFeature;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssComparator;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-mediaqueries-5-20200731/#descdef-media-aspect-ratio
 */
public class MediaAspectRatio extends MediaRangeFeature {

    /**
     * Create a new MediaHeight
     */
    public MediaAspectRatio() {
    }

    /**
     * Create a new MediaHeight.
     *
     * @param expression The expression for this media feature
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public MediaAspectRatio(ApplContext ac, String modifier,
                            CssExpression expression, boolean check)
            throws InvalidParamException {

        if (expression != null) {
            if (expression.getCount() > 2) {
                throw new InvalidParamException("unrecognize", ac);
            }
            if (expression.getCount() == 0) {
                throw new InvalidParamException("few-value", getFeatureName(), ac);
            }
            CssValue val = expression.getValue();

            switch (val.getType()) {
                case CssTypes.CSS_RATIO:
                    value = checkValue(ac, expression, getFeatureName());
                    break;
                case CssTypes.CSS_COMPARATOR:
                    // mediaqueries-4 case, expand the comparator and check its value
                    if (modifier != null) {
                        throw new InvalidParamException("nomodifierrangemedia",
                                getFeatureName(), ac);
                    }
                    CssComparator p = (CssComparator) val;
                    value = checkValue(ac, p.getParameters(), getFeatureName());
                    comparator = p.toString();
                    expression.next();
                    if (!expression.end()) {
                        val = expression.getValue();
                        if (val.getType() != CssTypes.CSS_COMPARATOR) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        CssComparator p2;
                        p2 = (CssComparator) val;
                        otherValue = checkValue(ac, p2.getParameters(), getFeatureName());
                        otherComparator = p2.toString();
                        checkComparators(ac, p, p2, getFeatureName());
                    }
                    break;
                default:
                    throw new InvalidParamException("value", expression.getValue(),
                            getFeatureName(), ac);
            }
            expression.next();
            setModifier(ac, modifier);
        } else {
            if (modifier != null) {
                throw new InvalidParamException("nomodifiershortmedia",
                        getFeatureName(), ac);
            }
        }
    }

    public MediaAspectRatio(ApplContext ac, String modifier, CssExpression expression)
            throws InvalidParamException {
        this(ac, modifier, expression, false);
    }

    static CssValue checkValue(ApplContext ac, CssExpression expression, String caller)
            throws InvalidParamException {
        if (expression.getCount() == 0) {
            throw new InvalidParamException("few-value", caller, ac);
        }
        CssValue val = expression.getValue();
        CssValue value = null;

        switch (val.getType()) {
            case CssTypes.CSS_RATIO:
                // a bit stupid as the only value would be 0...
                value = val;
                break;
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        caller, ac);
        }
        return value;
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
        return "aspect-ratio";
    }

    /**
     * Compares two media features for equality.
     *
     * @param other The other media features.
     */
    public boolean equals(MediaFeature other) {
        try {
            MediaAspectRatio mar = (MediaAspectRatio) other;
            if (value == null) {
                return (other.value == null);
            }
            return value.equals(other.value);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}
