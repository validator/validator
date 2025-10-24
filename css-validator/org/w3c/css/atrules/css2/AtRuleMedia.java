// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css2;

import org.w3c.css.atrules.css.media.Media;
import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.parser.AtRule;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/media.html#media-types
 * @since CSS2
 */
public class AtRuleMedia extends org.w3c.css.atrules.css.AtRuleMedia {

    static final String[] mediaCSS2 = {
            "all", "aural", "braille", "embossed", "handheld", "print",
            "projection", "screen", "tty", "tv"
    };

    /**
     * Adds a medium.
     *
     * @throws InvalidParamException the medium doesn't exist
     */
    public org.w3c.css.atrules.css.AtRuleMedia addMedia(String restrictor, String medium,
                                                  ApplContext ac)
            throws InvalidParamException {
        if (restrictor != null) {
            // media restrictor not supported in CSS2
            throw new InvalidParamException("nomediarestrictor", restrictor, ac);
        }
        medium = medium.toLowerCase();
        for (String s : mediaCSS2) {
            if (medium.equals(s)) {
                allMedia.add(new Media(s));
                return this;
            }
        }
        // FIXME we can check if media exists in other CSS versions
        throw new InvalidParamException("media", medium, ac);
    }

    /**
     * Mediafeatures are not supported in CSS2
     *
     * @param feature
     * @param ac
     * @throws InvalidParamException
     */
    public void addMediaFeature(MediaFeature feature, ApplContext ac)
            throws InvalidParamException {
        throw new InvalidParamException("nomediafeature",
                feature.toString(), ac);
    }

    /**
     * The second must be exactly the same as this one
     * so we check that each one match each other
     */
    public boolean canApply(AtRule atRule) {
        try {
            org.w3c.css.atrules.css.AtRuleMedia second = (org.w3c.css.atrules.css.AtRuleMedia) atRule;
            return (canMatch(second) && second.canMatch(this));
        } catch (ClassCastException cce) {
            return false;
        }
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        try {
            org.w3c.css.atrules.css.AtRuleMedia second = (org.w3c.css.atrules.css.AtRuleMedia) atRule;
            ArrayList<Media> otherMediaList = second.getMediaList();

            for (Media m : otherMediaList) {
                if (!allMedia.contains(m)) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public String getValueString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Media m : allMedia) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(m.toString());
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(keyword()).append(' ');
        sb.append(getValueString());
        return sb.toString();
    }
}

