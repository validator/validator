// $Id$
//
// (c) COPYRIGHT MIT, ECIM and Keio University, 2011.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css;

import org.w3c.css.atrules.css.media.Media;
import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.parser.AtRule;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;


public abstract class AtRuleMedia extends AtRule {

    public ArrayList<Media> allMedia = new ArrayList<Media>();

    /**
     * Adds a medium.
     *
     * @throws InvalidParamException the medium doesn't exist
     */
    public abstract AtRuleMedia addMedia(String restrictor, String medium,
                                         ApplContext ac) throws InvalidParamException;

    /**
     * Adds a media
     */
    public AtRuleMedia addMedia(Media m) {
        if (m != null) {
            allMedia.add(m);
        }
        return this;
    }

    public void removeLastMedia() {
        if (!allMedia.isEmpty()) {
            allMedia.remove(allMedia.size() - 1);
        }
    }

    /**
     * Add a media feature to the current media, like (color:1)
     *
     * @param feature, the CssProperty
     * @since CSS3
     */
    public abstract void addMediaFeature(MediaFeature feature, ApplContext ac)
            throws InvalidParamException;

    /**
     * Returns the at rule keyword
     */
    public final String keyword() {
        return "media";
    }


    public boolean isEmpty() {
        return false;
    }

    public ArrayList<Media> getMediaList() {
        return allMedia;
    }

    public String getCurrentMedia() {
        if (!allMedia.isEmpty()) {
            return allMedia.get(allMedia.size() - 1).getMedia();
        }
        return null;
    }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRuleMedia atRule) {
        return false;
    }

    /**
     * See if two rules can match (ie: one have thing in common)
     */
    public boolean canMatch(AtRuleMedia atRule) {
        return false;
    }

    public String lookupPrefix() {
        return "";

    }

    /**
     * Use to display the value part of the @media rule
     * used where the value is used, like as an option in @import
     *
     * @return a String
     */
    public abstract String getValueString();

    public static final AtRuleMedia getInstance(CssVersion version) {
        switch (version) {
            case CSS1:
                return new org.w3c.css.atrules.css1.AtRuleMedia();
            case CSS2:
                return new org.w3c.css.atrules.css2.AtRuleMedia();
            case CSS21:
                return new org.w3c.css.atrules.css21.AtRuleMedia();
            case CSS3:
            case CSS:
            case CSS_2015:
                return new org.w3c.css.atrules.css3.AtRuleMedia();
            default:
                throw new IllegalArgumentException(
                        "AtRuleMedia.getInstance called with unhandled"
                                + " CssVersion \"" + version.toString() + "\".");
        }
    }
}



