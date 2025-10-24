// $Id$
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2011
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.atrules.css1;

import org.w3c.css.atrules.css.media.MediaFeature;
import org.w3c.css.parser.AtRule;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/media.html#media-types
 * @since CSS2
 */
public class AtRuleMedia extends org.w3c.css.atrules.css.AtRuleMedia {

    /**
     * Adds a medium.
     *
     * @throws org.w3c.css.util.InvalidParamException
     *          the medium doesn't exist
     */
    public org.w3c.css.atrules.css.AtRuleMedia addMedia(String restrictor, String medium,
                                                  ApplContext ac) throws InvalidParamException {
        throw new InvalidParamException("media", medium, ac);
    }

    /**
     * Mediafeatures are not supported in CSS1
     *
     * @param feature
     * @param ac
     * @throws org.w3c.css.util.InvalidParamException
     *
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
        return false;
    }

    /**
     * The second must only match this one
     */
    public boolean canMatch(AtRule atRule) {
        return false;
    }

    public String getValueString() {
        return null;
    }
}

