// $Id$
//
// (c) COPYRIGHT MIT, ECRIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css3.media;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.atrules.css.media.MediaRangeFeature;
import org.w3c.css.util.ApplContext;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2017/CR-mediaqueries-4-20170905/#mf-deprecated
 */
@Deprecated
public class MediaDeviceHeight extends MediaRangeFeature {

    /**
     * Create a new MediaHeight
     */
    public MediaDeviceHeight() {
    }

    /**
     * Create a new MediaHeight.
     *
     * @param expression The expression for this media feature
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public MediaDeviceHeight(ApplContext ac, String modifier,
                             CssExpression expression, boolean check) {
        reportDeprecatedMediaFeature(ac, modifier);
    }

    public MediaDeviceHeight(ApplContext ac, String modifier, CssExpression expression)
            {
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
        return "device-height";
    }

    /**
     * Compares two media features for equality.
     *
     * @param other The other media features.
     */
    public boolean equals(MediaFeature other) {
        try {
            MediaDeviceHeight mdh = (MediaDeviceHeight) other;
            return (((value == null) && (mdh.value == null)) || ((value != null) && value.equals(mdh.value)))
                    && (((modifier == null) && (mdh.modifier == null)) || ((modifier != null) && modifier.equals(mdh.modifier)));
        } catch (ClassCastException cce) {
            return false;
        }

    }
}
