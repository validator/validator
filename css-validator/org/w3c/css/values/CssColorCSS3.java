// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values;

import java.util.HashMap;
import java.util.Locale;

/**
 * @spec https://www.w3.org/TR/2016/WD-css-color-4-20160705/
 */
public class CssColorCSS3 {
    protected static final HashMap<String, RGB> definedRGBColorsCSS3;
    protected static final HashMap<String, String> definedSystemColorsCSS3;
    protected static final HashMap<String, String> definedDeprecatedSystemColorsCSS3;

    private static final RGBA trans;

    static final CssIdent currentColor = CssIdent.getIdent("currentColor");

    public static RGB getRGB(String ident) {
        return definedRGBColorsCSS3.get(ident);
    }

    public static RGBA getRGBA(String ident) {
        if ("transparent".equalsIgnoreCase(ident)) {
            return trans;
        }
        return null;
    }

    public static String getSystem(String ident) {
        return definedSystemColorsCSS3.get(ident);
    }

    public static String getDeprecatedSystem(String ident) {
        return definedDeprecatedSystemColorsCSS3.get(ident);
    }

    // special case for currentColor and possible ident-only defined colors.
    public static String getIdentColor(String ident) {
        if ("currentColor".equalsIgnoreCase(ident)) {
            return currentColor.toString();
        }
        return null;
    }

    static {
        trans = new RGBA(true, 0, 0, 0, 0.f);
        // https://www.w3.org/TR/2016/WD-css-color-4-20160705/#named-colors
        definedRGBColorsCSS3 = new HashMap<String, RGB>();

        definedRGBColorsCSS3.put("aliceblue", new RGB(true, 240, 248, 255));
        definedRGBColorsCSS3.put("antiquewhite", new RGB(true, 250, 235, 215));
        definedRGBColorsCSS3.put("aqua", new RGB(true, 0, 255, 255));
        definedRGBColorsCSS3.put("aquamarine", new RGB(true, 127, 255, 212));
        definedRGBColorsCSS3.put("azure", new RGB(true, 240, 255, 255));
        definedRGBColorsCSS3.put("beige", new RGB(true, 245, 245, 220));
        definedRGBColorsCSS3.put("bisque", new RGB(true, 255, 228, 196));
        definedRGBColorsCSS3.put("black", new RGB(true, 0, 0, 0));
        definedRGBColorsCSS3.put("blanchedalmond", new RGB(true, 255, 235, 205));
        definedRGBColorsCSS3.put("blue", new RGB(true, 0, 0, 255));
        definedRGBColorsCSS3.put("blueviolet", new RGB(true, 138, 43, 226));
        definedRGBColorsCSS3.put("brown", new RGB(true, 165, 42, 42));
        definedRGBColorsCSS3.put("burlywood", new RGB(true, 222, 184, 135));
        definedRGBColorsCSS3.put("cadetblue", new RGB(true, 95, 158, 160));
        definedRGBColorsCSS3.put("chartreuse", new RGB(true, 127, 255, 0));
        definedRGBColorsCSS3.put("chocolate", new RGB(true, 210, 105, 30));
        definedRGBColorsCSS3.put("coral", new RGB(true, 255, 127, 80));
        definedRGBColorsCSS3.put("cornflowerblue", new RGB(true, 100, 149, 237));
        definedRGBColorsCSS3.put("cornsilk", new RGB(true, 255, 248, 220));
        definedRGBColorsCSS3.put("crimson", new RGB(true, 220, 20, 60));
        definedRGBColorsCSS3.put("cyan", new RGB(true, 0, 255, 255));
        definedRGBColorsCSS3.put("darkblue", new RGB(true, 0, 0, 139));
        definedRGBColorsCSS3.put("darkcyan", new RGB(true, 0, 139, 139));
        definedRGBColorsCSS3.put("darkgoldenrod", new RGB(true, 184, 134, 11));
        definedRGBColorsCSS3.put("darkgray", new RGB(true, 169, 169, 169));
        definedRGBColorsCSS3.put("darkgreen", new RGB(true, 0, 100, 0));
        definedRGBColorsCSS3.put("darkgrey", new RGB(true, 169, 169, 169));
        definedRGBColorsCSS3.put("darkkhaki", new RGB(true, 189, 183, 107));
        definedRGBColorsCSS3.put("darkmagenta", new RGB(true, 139, 0, 139));
        definedRGBColorsCSS3.put("darkolivegreen", new RGB(true, 85, 107, 47));
        definedRGBColorsCSS3.put("darkorange", new RGB(true, 255, 140, 0));
        definedRGBColorsCSS3.put("darkorchid", new RGB(true, 153, 50, 204));
        definedRGBColorsCSS3.put("darkred", new RGB(true, 139, 0, 0));
        definedRGBColorsCSS3.put("darksalmon", new RGB(true, 233, 150, 122));
        definedRGBColorsCSS3.put("darkseagreen", new RGB(true, 143, 188, 143));
        definedRGBColorsCSS3.put("darkslateblue", new RGB(true, 72, 61, 139));
        definedRGBColorsCSS3.put("darkslategray", new RGB(true, 47, 79, 79));
        definedRGBColorsCSS3.put("darkslategrey", new RGB(true, 47, 79, 79));
        definedRGBColorsCSS3.put("darkturquoise", new RGB(true, 0, 206, 209));
        definedRGBColorsCSS3.put("darkviolet", new RGB(true, 148, 0, 211));
        definedRGBColorsCSS3.put("deeppink", new RGB(true, 255, 20, 147));
        definedRGBColorsCSS3.put("deepskyblue", new RGB(true, 0, 191, 255));
        definedRGBColorsCSS3.put("dimgray", new RGB(true, 105, 105, 105));
        definedRGBColorsCSS3.put("dimgrey", new RGB(true, 105, 105, 105));
        definedRGBColorsCSS3.put("dodgerblue", new RGB(true, 30, 144, 255));
        definedRGBColorsCSS3.put("firebrick", new RGB(true, 178, 34, 34));
        definedRGBColorsCSS3.put("floralwhite", new RGB(true, 255, 250, 240));
        definedRGBColorsCSS3.put("forestgreen", new RGB(true, 34, 139, 34));
        definedRGBColorsCSS3.put("fuchsia", new RGB(true, 255, 0, 255));
        definedRGBColorsCSS3.put("gainsboro", new RGB(true, 220, 220, 220));
        definedRGBColorsCSS3.put("ghostwhite", new RGB(true, 248, 248, 255));
        definedRGBColorsCSS3.put("gold", new RGB(true, 255, 215, 0));
        definedRGBColorsCSS3.put("goldenrod", new RGB(true, 218, 165, 32));
        definedRGBColorsCSS3.put("gray", new RGB(true, 128, 128, 128));
        definedRGBColorsCSS3.put("green", new RGB(true, 0, 128, 0));
        definedRGBColorsCSS3.put("greenyellow", new RGB(true, 173, 255, 47));
        definedRGBColorsCSS3.put("grey", new RGB(true, 128, 128, 128));
        definedRGBColorsCSS3.put("honeydew", new RGB(true, 240, 255, 240));
        definedRGBColorsCSS3.put("hotpink", new RGB(true, 255, 105, 180));
        definedRGBColorsCSS3.put("indianred", new RGB(true, 205, 92, 92));
        definedRGBColorsCSS3.put("indigo", new RGB(true, 75, 0, 130));
        definedRGBColorsCSS3.put("ivory", new RGB(true, 255, 255, 240));
        definedRGBColorsCSS3.put("khaki", new RGB(true, 240, 230, 140));
        definedRGBColorsCSS3.put("lavender", new RGB(true, 230, 230, 250));
        definedRGBColorsCSS3.put("lavenderblush", new RGB(true, 255, 240, 245));
        definedRGBColorsCSS3.put("lawngreen", new RGB(true, 124, 252, 0));
        definedRGBColorsCSS3.put("lemonchiffon", new RGB(true, 255, 250, 205));
        definedRGBColorsCSS3.put("lightblue", new RGB(true, 173, 216, 230));
        definedRGBColorsCSS3.put("lightcoral", new RGB(true, 240, 128, 128));
        definedRGBColorsCSS3.put("lightcyan", new RGB(true, 224, 255, 255));
        definedRGBColorsCSS3.put("lightgoldenrodyellow", new RGB(true, 250, 250, 210));
        definedRGBColorsCSS3.put("lightgray", new RGB(true, 211, 211, 211));
        definedRGBColorsCSS3.put("lightgreen", new RGB(true, 144, 238, 144));
        definedRGBColorsCSS3.put("lightgrey", new RGB(true, 211, 211, 211));
        definedRGBColorsCSS3.put("lightpink", new RGB(true, 255, 182, 193));
        definedRGBColorsCSS3.put("lightsalmon", new RGB(true, 255, 160, 122));
        definedRGBColorsCSS3.put("lightseagreen", new RGB(true, 32, 178, 170));
        definedRGBColorsCSS3.put("lightskyblue", new RGB(true, 135, 206, 250));
        definedRGBColorsCSS3.put("lightslategray", new RGB(true, 119, 136, 153));
        definedRGBColorsCSS3.put("lightslategrey", new RGB(true, 119, 136, 153));
        definedRGBColorsCSS3.put("lightsteelblue", new RGB(true, 176, 196, 222));
        definedRGBColorsCSS3.put("lightyellow", new RGB(true, 255, 255, 224));
        definedRGBColorsCSS3.put("lime", new RGB(true, 0, 255, 0));
        definedRGBColorsCSS3.put("limegreen", new RGB(true, 50, 205, 50));
        definedRGBColorsCSS3.put("linen", new RGB(true, 250, 240, 230));
        definedRGBColorsCSS3.put("magenta", new RGB(true, 255, 0, 255));
        definedRGBColorsCSS3.put("maroon", new RGB(true, 128, 0, 0));
        definedRGBColorsCSS3.put("mediumaquamarine", new RGB(true, 102, 205, 170));
        definedRGBColorsCSS3.put("mediumblue", new RGB(true, 0, 0, 205));
        definedRGBColorsCSS3.put("mediumorchid", new RGB(true, 186, 85, 211));
        definedRGBColorsCSS3.put("mediumpurple", new RGB(true, 147, 112, 219));
        definedRGBColorsCSS3.put("mediumseagreen", new RGB(true, 60, 179, 113));
        definedRGBColorsCSS3.put("mediumslateblue", new RGB(true, 123, 104, 238));
        definedRGBColorsCSS3.put("mediumspringgreen", new RGB(true, 0, 250, 154));
        definedRGBColorsCSS3.put("mediumturquoise", new RGB(true, 72, 209, 204));
        definedRGBColorsCSS3.put("mediumvioletred", new RGB(true, 199, 21, 133));
        definedRGBColorsCSS3.put("midnightblue", new RGB(true, 25, 25, 112));
        definedRGBColorsCSS3.put("mintcream", new RGB(true, 245, 255, 250));
        definedRGBColorsCSS3.put("mistyrose", new RGB(true, 255, 228, 225));
        definedRGBColorsCSS3.put("moccasin", new RGB(true, 255, 228, 181));
        definedRGBColorsCSS3.put("navajowhite", new RGB(true, 255, 222, 173));
        definedRGBColorsCSS3.put("navy", new RGB(true, 0, 0, 128));
        definedRGBColorsCSS3.put("oldlace", new RGB(true, 253, 245, 230));
        definedRGBColorsCSS3.put("olive", new RGB(true, 128, 128, 0));
        definedRGBColorsCSS3.put("olivedrab", new RGB(true, 107, 142, 35));
        definedRGBColorsCSS3.put("orange", new RGB(true, 255, 165, 0));
        definedRGBColorsCSS3.put("orangered", new RGB(true, 255, 69, 0));
        definedRGBColorsCSS3.put("orchid", new RGB(true, 218, 112, 214));
        definedRGBColorsCSS3.put("palegoldenrod", new RGB(true, 238, 232, 170));
        definedRGBColorsCSS3.put("palegreen", new RGB(true, 152, 251, 152));
        definedRGBColorsCSS3.put("paleturquoise", new RGB(true, 175, 238, 238));
        definedRGBColorsCSS3.put("palevioletred", new RGB(true, 219, 112, 147));
        definedRGBColorsCSS3.put("papayawhip", new RGB(true, 255, 239, 213));
        definedRGBColorsCSS3.put("peachpuff", new RGB(true, 255, 218, 185));
        definedRGBColorsCSS3.put("peru", new RGB(true, 205, 133, 63));
        definedRGBColorsCSS3.put("pink", new RGB(true, 255, 192, 203));
        definedRGBColorsCSS3.put("plum", new RGB(true, 221, 160, 221));
        definedRGBColorsCSS3.put("powderblue", new RGB(true, 176, 224, 230));
        definedRGBColorsCSS3.put("purple", new RGB(true, 128, 0, 128));
        definedRGBColorsCSS3.put("rebeccapurple", new RGB(true, 102, 51, 153));
        definedRGBColorsCSS3.put("red", new RGB(true, 255, 0, 0));
        definedRGBColorsCSS3.put("rosybrown", new RGB(true, 188, 143, 143));
        definedRGBColorsCSS3.put("royalblue", new RGB(true, 65, 105, 225));
        definedRGBColorsCSS3.put("saddlebrown", new RGB(true, 139, 69, 19));
        definedRGBColorsCSS3.put("salmon", new RGB(true, 250, 128, 114));
        definedRGBColorsCSS3.put("sandybrown", new RGB(true, 244, 164, 96));
        definedRGBColorsCSS3.put("seagreen", new RGB(true, 46, 139, 87));
        definedRGBColorsCSS3.put("seashell", new RGB(true, 255, 245, 238));
        definedRGBColorsCSS3.put("sienna", new RGB(true, 160, 82, 45));
        definedRGBColorsCSS3.put("silver", new RGB(true, 192, 192, 192));
        definedRGBColorsCSS3.put("skyblue", new RGB(true, 135, 206, 235));
        definedRGBColorsCSS3.put("slateblue", new RGB(true, 106, 90, 205));
        definedRGBColorsCSS3.put("slategray", new RGB(true, 112, 128, 144));
        definedRGBColorsCSS3.put("slategrey", new RGB(true, 112, 128, 144));
        definedRGBColorsCSS3.put("snow", new RGB(true, 255, 250, 250));
        definedRGBColorsCSS3.put("springgreen", new RGB(true, 0, 255, 127));
        definedRGBColorsCSS3.put("steelblue", new RGB(true, 70, 130, 180));
        definedRGBColorsCSS3.put("tan", new RGB(true, 210, 180, 140));
        definedRGBColorsCSS3.put("teal", new RGB(true, 0, 128, 128));
        definedRGBColorsCSS3.put("thistle", new RGB(true, 216, 191, 216));
        definedRGBColorsCSS3.put("tomato", new RGB(true, 255, 99, 71));
        definedRGBColorsCSS3.put("turquoise", new RGB(true, 64, 224, 208));
        definedRGBColorsCSS3.put("violet", new RGB(true, 238, 130, 238));
        definedRGBColorsCSS3.put("wheat", new RGB(true, 245, 222, 179));
        definedRGBColorsCSS3.put("white", new RGB(true, 255, 255, 255));
        definedRGBColorsCSS3.put("whitesmoke", new RGB(true, 245, 245, 245));
        definedRGBColorsCSS3.put("yellow", new RGB(true, 255, 255, 0));
        definedRGBColorsCSS3.put("yellowgreen", new RGB(true, 154, 205, 50));

        // https://www.w3.org/TR/2021/WD-css-color-4-20210601/#typedef-system-color
        definedSystemColorsCSS3 = new HashMap<>();
        String[] _system_colors = {"Canvas", "CanvasText", "LinkText", "VisitedText", "ActiveText",
                "ButtonFace", "ButtonText", "ButtonBorder", "Field", "FieldText", "Highlight",
                "HighlightText", "Mark", "MarkText", "GrayText"};
        for (String s : _system_colors) {
            definedSystemColorsCSS3.put(s.toLowerCase(Locale.ENGLISH), s);
        }

        // https://www.w3.org/TR/2021/WD-css-color-4-20210601/#deprecated-system-colors
        definedDeprecatedSystemColorsCSS3 = new HashMap<>();
        String[] _deprecated_system_colors = {"ActiveBorder", "ActiveCaption", "AppWorkspace",
                "Background", "ButtonHighlight", "ButtonShadow", "CaptionText",
                "InactiveBorder", "InactiveCaption", "InactiveCaptionText", "InfoBackground",
                "InfoText", "Menu", "MenuText", "Scrollbar", "ThreeDDarkShadow",
                "ThreeDFace", "ThreeDHighlight", "ThreeDLightShadow", "ThreeDShadow",
                "Window", "WindowFrame", "WindowText"};
        String[] _deprecated_replacement_colors = {"ButtonBorder", "CanvasText", "Canvas",
                "Canvas", "ButtonFace", "ButtonFace", "CanvasText",
                "ButtonBorder", "Canvas", "GrayText", "Canvas",
                "CanvasText", "Canvas", "CanvasText", "Canvas", "ButtonBorder",
                "ButtonFace", "ButtonBorder", "ButtonBorder", "ButtonBorder",
                "Canvas", "ButtonBorder", "CanvasText"};
        for (int i = 0; i < _deprecated_system_colors.length; i++) {
            definedDeprecatedSystemColorsCSS3.put(_deprecated_system_colors[i].toLowerCase(Locale.ENGLISH),
                    _deprecated_replacement_colors[i]);
        }

    }
}
