//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

/**
 * @version $Revision$
 */
public interface CssBackgroundConstants {
    // TODO REMOVE (and clear other background classes)
    static String[] REPEAT = {"repeat", "repeat-x", "repeat-y", "no-repeat", "inherit"};

    static String[] ATTACHMENT = {"scroll", "fixed", "inherit"};
    static String[] ATTACHMENTMOB = {"scroll", "inherit"};

    static String[] POSITION = {"top", "center", "bottom", "left", "right", "inherit"};

    /**
     * The top position
     */
    static int POSITION_TOP = 0;

    /**
     * The center position
     */
    static int POSITION_CENTER = 1;

    /**
     * The bottom position
     */
    static int POSITION_BOTTOM = 2;

    /**
     * The left position
     */
    static int POSITION_LEFT = 3;

    /**
     * The right position
     */
    static int POSITION_RIGHT = 4;

    /**
     * Inherit
     */
    static int POSITION_INHERIT = 5;
}
