// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css;

import org.w3c.css.atrules.css.supports.SupportsFeature;
import org.w3c.css.parser.AtRule;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.util.ArrayList;


public class AtRuleSupports extends AtRule {

    public ArrayList<SupportsFeature> allSupportsRule = null;

    /**
     * Adds a medium.
     *
     * @throws org.w3c.css.util.InvalidParamException
     *          the medium doesn't exist
     */
    public void addFeature(SupportsFeature feature,
                                      ApplContext ac) throws InvalidParamException {
        if (allSupportsRule == null) {
            allSupportsRule = new ArrayList<>();
        }
        allSupportsRule.add(feature);
    }

    /**
     * Adds a media
     */
    //   public AtRuleSupports addMedia(Media m) {
    //      allMedia.add(m);
    //     return this;
    // }

    /**
     * Add a media feature to the current media, like (color:1)
     *
     * @param feature, the CssProperty
     * @since CSS3
     */
    //   public abstract void addMediaFeature(MediaFeature feature, ApplContext ac)
    //          throws InvalidParamException;

    /**
     * Returns the at rule keyword
     */
    public final String keyword() {
        return "supports";
    }


    public boolean isEmpty() {
        return false;
    }

//    public ArrayList<Media> getMediaList() {
    //       return allMedia;
    //   }

    //   public String getCurrentMedia() {
    //      if (!allMedia.isEmpty()) {
    //         return allMedia.get(allMedia.size()-1).getMedia();
    //    }
    //   return null;
    //  }

    /**
     * The second must be exactly the same of this one
     */
    public boolean canApply(AtRule atRule) {
        return false;
    }

    /**
     * See if two rules can match (ie: one have thing in common)
     */
    public boolean canMatch(AtRule atRule) {
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append('@');
        ret.append(keyword());
        if (allSupportsRule != null) {
            for (SupportsFeature feature : allSupportsRule) {
                ret.append(' ');
                ret.append(feature);
            }
        }
        return ret.toString();
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
    //   public abstract String getValueString();

}



