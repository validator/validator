//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import java.util.HashMap;

/**
 * @version $Revision$
 * @spec http://www.w3.org/TR/2008/REC-CSS1-20080411/#color-units
 */
public class CssColorCSS1 {

    protected static final HashMap<String, RGB> definedRGBColorsCSS1;

    static RGB getRGB(String ident) {
        return definedRGBColorsCSS1.get(ident);
    }

    static {
        definedRGBColorsCSS1 = new HashMap<String, RGB>();
        definedRGBColorsCSS1.put("black", new RGB(0, 0, 0));
        definedRGBColorsCSS1.put("silver", new RGB(192, 192, 192));
        definedRGBColorsCSS1.put("gray", new RGB(128, 128, 128));
        definedRGBColorsCSS1.put("white", new RGB(255, 255, 255));
        definedRGBColorsCSS1.put("maroon", new RGB(128, 0, 0));
        definedRGBColorsCSS1.put("red", new RGB(255, 0, 0));
        definedRGBColorsCSS1.put("purple", new RGB(128, 0, 128));
        definedRGBColorsCSS1.put("fuchsia", new RGB(255, 0, 255));
        definedRGBColorsCSS1.put("green", new RGB(0, 128, 0));
        definedRGBColorsCSS1.put("lime", new RGB(0, 255, 0));
        definedRGBColorsCSS1.put("olive", new RGB(128, 128, 0));
        definedRGBColorsCSS1.put("yellow", new RGB(255, 255, 0));
        definedRGBColorsCSS1.put("navy", new RGB(0, 0, 128));
        definedRGBColorsCSS1.put("blue", new RGB(0, 0, 255));
        definedRGBColorsCSS1.put("teal", new RGB(0, 128, 128));
        definedRGBColorsCSS1.put("aqua", new RGB(0, 255, 255));
    }

}
