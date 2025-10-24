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
public class MediaDeviceAspectRatio extends MediaRangeFeature {

    /**
     * Create a new MediaHeight
     */
    public MediaDeviceAspectRatio() {
    }

    /**
     * Create a new MediaHeight.
     *
     * @param expression The expression for this media feature
     * @throws org.w3c.css.util.InvalidParamException
     *          Values are incorrect
     */
    public MediaDeviceAspectRatio(ApplContext ac, String modifier,
                                  CssExpression expression, boolean check) {
        reportDeprecatedMediaFeature(ac, modifier);
    }

    public MediaDeviceAspectRatio(ApplContext ac, String modifier, CssExpression expression)
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
        return "device-aspect-ratio";
    }

    /**
     * Compares two media features for equality.
     *
     * @param other The other media features.
     */
    public boolean equals(MediaFeature other) {
        try {
            MediaDeviceAspectRatio mar = (MediaDeviceAspectRatio) other;
            if (value == null) {
                return (other.value == null);
            }
            return value.equals(other.value);
        } catch (ClassCastException cce) {
            return false;
        }
    }

}
