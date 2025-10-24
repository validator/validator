// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import java.util.HashMap;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/syndata.html#color-units
 */
public class CssColorCSS21 {
    protected static final HashMap<String, RGB> definedRGBColorsCSS21;

    public static RGB getRGB(String ident) {
        return definedRGBColorsCSS21.get(ident);
    }

    // those are the same as CSS2, let's use that
    public static String getSystem(String ident) {
        return CssColorCSS2.definedSystemColorsCSS2.get(ident);
    }

    static {
        // http://www.w3.org/TR/2011/REC-CSS2-20110607/syndata.html#color-units
        definedRGBColorsCSS21 = new HashMap<String, RGB>();
        definedRGBColorsCSS21.put("orange", new RGB(255, 165, 0));
        definedRGBColorsCSS21.put("black", new RGB(0, 0, 0));
        definedRGBColorsCSS21.put("silver", new RGB(192, 192, 192));
        definedRGBColorsCSS21.put("gray", new RGB(128, 128, 128));
        definedRGBColorsCSS21.put("white", new RGB(255, 255, 255));
        definedRGBColorsCSS21.put("maroon", new RGB(128, 0, 0));
        definedRGBColorsCSS21.put("red", new RGB(255, 0, 0));
        definedRGBColorsCSS21.put("purple", new RGB(128, 0, 128));
        definedRGBColorsCSS21.put("fuchsia", new RGB(255, 0, 255));
        definedRGBColorsCSS21.put("green", new RGB(0, 128, 0));
        definedRGBColorsCSS21.put("lime", new RGB(0, 255, 0));
        definedRGBColorsCSS21.put("olive", new RGB(128, 128, 0));
        definedRGBColorsCSS21.put("yellow", new RGB(255, 255, 0));
        definedRGBColorsCSS21.put("navy", new RGB(0, 0, 128));
        definedRGBColorsCSS21.put("blue", new RGB(0, 0, 255));
        definedRGBColorsCSS21.put("teal", new RGB(0, 128, 128));
        definedRGBColorsCSS21.put("aqua", new RGB(0, 255, 255));
    }
}
