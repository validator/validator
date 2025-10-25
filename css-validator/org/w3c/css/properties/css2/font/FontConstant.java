//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2.font;

/**
 * @version $Revision$
 */
public interface FontConstant {

    /**
     * Array of font-style values
     */
    static String[] FONTSTYLE = {"normal", "italic", "oblique"};

    /**
     * Array of font-variant values
     */
    static String[] FONTVARIANT = {"normal", "small-caps"};

    /**
     * Array of font-weight values
     */
    static String[] FONTWEIGHT = {"normal", "bold"};

    /**
     * Array of font-stretch values
     */
    static String[] FONTSTRETCH = {"normal", "wider", "narrower",
            "ultra-condensed", "extra-condensed",
            "condensed", "semi-condensed",
            "semi-expanded", "expanded", "extra-expanded",
            "ultra-expanded", "inherit"};

}
