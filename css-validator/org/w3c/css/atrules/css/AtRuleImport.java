//
// $Id$
//
// (c) COPYRIGHT MIT, Keio University and ERCIM, 2009.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 * AtRuleMedia.java
 * $Id$
 */
package org.w3c.css.atrules.css;

import org.w3c.css.parser.AtRule;

/**
 * This class manages all imports
 *
 * @author Philippe Le Hegaret
 * @version $Revision$
 */
public class AtRuleImport extends AtRule {

    boolean is_url = false;
    String linkname = null;
    AtRuleLayer layer = null;
    AtRuleMedia media = null;

    public String keyword() {
        return "import";
    }

    public boolean isEmpty() {
        return true;
    }

    /**
     * The second must be exactly the same of this one
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


    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append('@');
        ret.append(keyword());
        ret.append(' ');
        if (is_url) {
            ret.append("url(\'");
            ret.append(linkname);
            ret.append("\')");
        } else {
            ret.append('\"');
            ret.append(linkname);
            ret.append('\"');
        }
        if (layer != null) {
            ret.append(' ');
            ret.append(layer.getNameString());
        }
        if (media != null && !media.isEmpty()) {
            ret.append(' ');
            ret.append(media.getValueString());
        }
        ret.append(';');
        return ret.toString();
    }

    public AtRuleImport(String linkname, boolean is_url,
                        AtRuleLayer layer, AtRuleMedia media) {
        this.linkname = linkname;
        this.is_url = is_url;
        this.layer = layer;
        this.media = media;
    }

	public String getLinkname() {
		return linkname;
	}

	public AtRuleMedia getMedia() {
		return media;
	}
    
    
}

