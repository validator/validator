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
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/syndata.html#color-units
 */
public class CssColorCSS2 {

    protected static final HashMap<String, RGB> definedRGBColorsCSS2;
    protected static final HashMap<String, String> definedSystemColorsCSS2;

    public static RGB getRGB(String ident) {
        return definedRGBColorsCSS2.get(ident);
    }

    public static String getSystem(String ident) {
        return definedSystemColorsCSS2.get(ident);
    }

    static {
        // http://www.w3.org/TR/2008/REC-CSS2-20080411/syndata.html#color-units
        definedRGBColorsCSS2 = new HashMap<String, RGB>();
        definedRGBColorsCSS2.put("black", new RGB(0, 0, 0));
        definedRGBColorsCSS2.put("silver", new RGB(192, 192, 192));
        definedRGBColorsCSS2.put("gray", new RGB(128, 128, 128));
        definedRGBColorsCSS2.put("white", new RGB(255, 255, 255));
        definedRGBColorsCSS2.put("maroon", new RGB(128, 0, 0));
        definedRGBColorsCSS2.put("red", new RGB(255, 0, 0));
        definedRGBColorsCSS2.put("purple", new RGB(128, 0, 128));
        definedRGBColorsCSS2.put("fuchsia", new RGB(255, 0, 255));
        definedRGBColorsCSS2.put("green", new RGB(0, 128, 0));
        definedRGBColorsCSS2.put("lime", new RGB(0, 255, 0));
        definedRGBColorsCSS2.put("olive", new RGB(128, 128, 0));
        definedRGBColorsCSS2.put("yellow", new RGB(255, 255, 0));
        definedRGBColorsCSS2.put("navy", new RGB(0, 0, 128));
        definedRGBColorsCSS2.put("blue", new RGB(0, 0, 255));
        definedRGBColorsCSS2.put("teal", new RGB(0, 128, 128));
        definedRGBColorsCSS2.put("aqua", new RGB(0, 255, 255));

        // http://www.w3.org/TR/2008/REC-CSS2-20080411/ui.html#system-colors
        definedSystemColorsCSS2 = new HashMap<String, String>();
        definedSystemColorsCSS2.put("activeborder", "ActiveBorder");
        definedSystemColorsCSS2.put("activecaption", "ActiveCaption");
        definedSystemColorsCSS2.put("appworkspace", "AppWorkspace");
        definedSystemColorsCSS2.put("background", "Background");
        definedSystemColorsCSS2.put("buttonface", "ButtonFace");
        definedSystemColorsCSS2.put("buttonhighlight", "ButtonHighlight");
        definedSystemColorsCSS2.put("buttonshadow", "ButtonShadow");
        definedSystemColorsCSS2.put("buttontext", "ButtonText");
        definedSystemColorsCSS2.put("captiontext", "CaptionText");
        definedSystemColorsCSS2.put("graytext", "GrayText");
        definedSystemColorsCSS2.put("highlight", "Highlight");
        definedSystemColorsCSS2.put("highlighttext", "HighlightText");
        definedSystemColorsCSS2.put("inactiveborder", "InactiveBorder");
        definedSystemColorsCSS2.put("inactivecaption", "InactiveCaption");
        definedSystemColorsCSS2.put("inactivecaptiontext", "InactiveCaptionText");
        definedSystemColorsCSS2.put("infobackground", "InfoBackground");
        definedSystemColorsCSS2.put("infotext", "InfoText");
        definedSystemColorsCSS2.put("menu", "Menu");
        definedSystemColorsCSS2.put("menutext", "MenuText");
        definedSystemColorsCSS2.put("scrollbar", "Scrollbar");
        definedSystemColorsCSS2.put("threeddarkshadow", "ThreeDDarkShadow");
        definedSystemColorsCSS2.put("threedface", "ThreeDFace");
        definedSystemColorsCSS2.put("threedhighlight", "ThreeDHighlight");
        definedSystemColorsCSS2.put("threedlightshadow", "ThreeDLightShadow");
        definedSystemColorsCSS2.put("threedshadow", "ThreeDShadow");
        definedSystemColorsCSS2.put("window", "Window");
        definedSystemColorsCSS2.put("windowframe", "WindowFrame");
        definedSystemColorsCSS2.put("windowtext", "WindowText");
    }

}
