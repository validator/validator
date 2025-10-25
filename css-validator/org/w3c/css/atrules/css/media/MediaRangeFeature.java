package org.w3c.css.atrules.css.media;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssComparator;
import org.w3c.css.values.CssValue;

public abstract class MediaRangeFeature extends MediaFeature {
    public CssValue value;
    public CssValue otherValue;
    public String comparator;
    public String otherComparator;
    public String modifier;
    public boolean comparatorNameFirst = true;


    static public void checkComparators(ApplContext ac, CssComparator c1, CssComparator c2,
                                        String caller) throws InvalidParamException {
        if (!CssComparator.checkCompatibility(c1, c2)) {
            throw new InvalidParamException("comparator", c1, c2, ac);
        }
        if (!CssComparator.checkUsefulness(c1, c2)) {
            ac.getFrame().addWarning("comparator", new String[]{c1.toString(),
                    c2.toString()});
        }
    }

    // because of clashes in feature names / modifier, we can't check
// reliably unwanted modifiers, they are noy only unknown media features
    public void setModifier(ApplContext ac, String modifier)
            throws InvalidParamException {
//        if (modifier.equals("min") || modifier.equals("max")) {
        this.modifier = modifier;
//        } else {
//            throw new InvalidParamException("invalidmediafeaturemodifier",
//                    getFeatureName(), modifier, ac);
//        }
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (value == null) {
            return getFeatureName();
        }
        StringBuilder sb = new StringBuilder();
        if (modifier != null) {
            sb.append(modifier).append('-');
            sb.append(getFeatureName());
            sb.append(':').append(' ').append(value.toString());
        } else {
            if (comparator != null) {
                if (otherComparator == null) {
                    if (comparatorNameFirst) {
                        sb.append(getFeatureName()).append(' ').append(comparator).append(' ');
                        sb.append(value);
                    } else {
                        sb.append(value).append(' ').append(comparator).append(' ');
                        sb.append(getFeatureName());
                    }
                } else {
                    sb.append(value).append(' ').append(comparator).append(' ');
                    sb.append(getFeatureName());
                    sb.append(' ').append(otherComparator).append(' ');
                    sb.append(otherValue);
                }
            } else {
                sb.append(getFeatureName());
                sb.append(':').append(' ').append(value.toString());
            }
        }
        return sb.toString();
    }
}
