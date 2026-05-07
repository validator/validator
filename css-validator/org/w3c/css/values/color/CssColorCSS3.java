// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.values.color;

import org.w3c.css.values.CssIdent;

import java.util.HashMap;
import java.util.Locale;

/**
 * @spec https://www.w3.org/TR/2016/WD-css-color-4-20160705/
 */
public class CssColorCSS3 {
    protected static final HashMap<String, RGBA> definedRGBColorsCSS3;
    protected static final HashMap<String, String> definedSystemColorsCSS3;
    protected static final HashMap<String, String> definedDeprecatedSystemColorsCSS3;

    private static final RGBA trans;

    public static final CssIdent currentColor = CssIdent.getIdent("currentColor");

    public static RGBA getRGBA(String ident) {
        if ("transparent".equalsIgnoreCase(ident)) {
            return trans;
        }
        return definedRGBColorsCSS3.get(ident);
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
        definedRGBColorsCSS3 = new HashMap<String, RGBA>();

        definedRGBColorsCSS3.put("aliceblue", new RGBA(true, 240, 248, 255));
        definedRGBColorsCSS3.put("antiquewhite", new RGBA(true, 250, 235, 215));
        definedRGBColorsCSS3.put("aqua", new RGBA(true, 0, 255, 255));
        definedRGBColorsCSS3.put("aquamarine", new RGBA(true, 127, 255, 212));
        definedRGBColorsCSS3.put("azure", new RGBA(true, 240, 255, 255));
        definedRGBColorsCSS3.put("beige", new RGBA(true, 245, 245, 220));
        definedRGBColorsCSS3.put("bisque", new RGBA(true, 255, 228, 196));
        definedRGBColorsCSS3.put("black", new RGBA(true, 0, 0, 0));
        definedRGBColorsCSS3.put("blanchedalmond", new RGBA(true, 255, 235, 205));
        definedRGBColorsCSS3.put("blue", new RGBA(true, 0, 0, 255));
        definedRGBColorsCSS3.put("blueviolet", new RGBA(true, 138, 43, 226));
        definedRGBColorsCSS3.put("brown", new RGBA(true, 165, 42, 42));
        definedRGBColorsCSS3.put("burlywood", new RGBA(true, 222, 184, 135));
        definedRGBColorsCSS3.put("cadetblue", new RGBA(true, 95, 158, 160));
        definedRGBColorsCSS3.put("chartreuse", new RGBA(true, 127, 255, 0));
        definedRGBColorsCSS3.put("chocolate", new RGBA(true, 210, 105, 30));
        definedRGBColorsCSS3.put("coral", new RGBA(true, 255, 127, 80));
        definedRGBColorsCSS3.put("cornflowerblue", new RGBA(true, 100, 149, 237));
        definedRGBColorsCSS3.put("cornsilk", new RGBA(true, 255, 248, 220));
        definedRGBColorsCSS3.put("crimson", new RGBA(true, 220, 20, 60));
        definedRGBColorsCSS3.put("cyan", new RGBA(true, 0, 255, 255));
        definedRGBColorsCSS3.put("darkblue", new RGBA(true, 0, 0, 139));
        definedRGBColorsCSS3.put("darkcyan", new RGBA(true, 0, 139, 139));
        definedRGBColorsCSS3.put("darkgoldenrod", new RGBA(true, 184, 134, 11));
        definedRGBColorsCSS3.put("darkgray", new RGBA(true, 169, 169, 169));
        definedRGBColorsCSS3.put("darkgreen", new RGBA(true, 0, 100, 0));
        definedRGBColorsCSS3.put("darkgrey", new RGBA(true, 169, 169, 169));
        definedRGBColorsCSS3.put("darkkhaki", new RGBA(true, 189, 183, 107));
        definedRGBColorsCSS3.put("darkmagenta", new RGBA(true, 139, 0, 139));
        definedRGBColorsCSS3.put("darkolivegreen", new RGBA(true, 85, 107, 47));
        definedRGBColorsCSS3.put("darkorange", new RGBA(true, 255, 140, 0));
        definedRGBColorsCSS3.put("darkorchid", new RGBA(true, 153, 50, 204));
        definedRGBColorsCSS3.put("darkred", new RGBA(true, 139, 0, 0));
        definedRGBColorsCSS3.put("darksalmon", new RGBA(true, 233, 150, 122));
        definedRGBColorsCSS3.put("darkseagreen", new RGBA(true, 143, 188, 143));
        definedRGBColorsCSS3.put("darkslateblue", new RGBA(true, 72, 61, 139));
        definedRGBColorsCSS3.put("darkslategray", new RGBA(true, 47, 79, 79));
        definedRGBColorsCSS3.put("darkslategrey", new RGBA(true, 47, 79, 79));
        definedRGBColorsCSS3.put("darkturquoise", new RGBA(true, 0, 206, 209));
        definedRGBColorsCSS3.put("darkviolet", new RGBA(true, 148, 0, 211));
        definedRGBColorsCSS3.put("deeppink", new RGBA(true, 255, 20, 147));
        definedRGBColorsCSS3.put("deepskyblue", new RGBA(true, 0, 191, 255));
        definedRGBColorsCSS3.put("dimgray", new RGBA(true, 105, 105, 105));
        definedRGBColorsCSS3.put("dimgrey", new RGBA(true, 105, 105, 105));
        definedRGBColorsCSS3.put("dodgerblue", new RGBA(true, 30, 144, 255));
        definedRGBColorsCSS3.put("firebrick", new RGBA(true, 178, 34, 34));
        definedRGBColorsCSS3.put("floralwhite", new RGBA(true, 255, 250, 240));
        definedRGBColorsCSS3.put("forestgreen", new RGBA(true, 34, 139, 34));
        definedRGBColorsCSS3.put("fuchsia", new RGBA(true, 255, 0, 255));
        definedRGBColorsCSS3.put("gainsboro", new RGBA(true, 220, 220, 220));
        definedRGBColorsCSS3.put("ghostwhite", new RGBA(true, 248, 248, 255));
        definedRGBColorsCSS3.put("gold", new RGBA(true, 255, 215, 0));
        definedRGBColorsCSS3.put("goldenrod", new RGBA(true, 218, 165, 32));
        definedRGBColorsCSS3.put("gray", new RGBA(true, 128, 128, 128));
        definedRGBColorsCSS3.put("green", new RGBA(true, 0, 128, 0));
        definedRGBColorsCSS3.put("greenyellow", new RGBA(true, 173, 255, 47));
        definedRGBColorsCSS3.put("grey", new RGBA(true, 128, 128, 128));
        definedRGBColorsCSS3.put("honeydew", new RGBA(true, 240, 255, 240));
        definedRGBColorsCSS3.put("hotpink", new RGBA(true, 255, 105, 180));
        definedRGBColorsCSS3.put("indianred", new RGBA(true, 205, 92, 92));
        definedRGBColorsCSS3.put("indigo", new RGBA(true, 75, 0, 130));
        definedRGBColorsCSS3.put("ivory", new RGBA(true, 255, 255, 240));
        definedRGBColorsCSS3.put("khaki", new RGBA(true, 240, 230, 140));
        definedRGBColorsCSS3.put("lavender", new RGBA(true, 230, 230, 250));
        definedRGBColorsCSS3.put("lavenderblush", new RGBA(true, 255, 240, 245));
        definedRGBColorsCSS3.put("lawngreen", new RGBA(true, 124, 252, 0));
        definedRGBColorsCSS3.put("lemonchiffon", new RGBA(true, 255, 250, 205));
        definedRGBColorsCSS3.put("lightblue", new RGBA(true, 173, 216, 230));
        definedRGBColorsCSS3.put("lightcoral", new RGBA(true, 240, 128, 128));
        definedRGBColorsCSS3.put("lightcyan", new RGBA(true, 224, 255, 255));
        definedRGBColorsCSS3.put("lightgoldenrodyellow", new RGBA(true, 250, 250, 210));
        definedRGBColorsCSS3.put("lightgray", new RGBA(true, 211, 211, 211));
        definedRGBColorsCSS3.put("lightgreen", new RGBA(true, 144, 238, 144));
        definedRGBColorsCSS3.put("lightgrey", new RGBA(true, 211, 211, 211));
        definedRGBColorsCSS3.put("lightpink", new RGBA(true, 255, 182, 193));
        definedRGBColorsCSS3.put("lightsalmon", new RGBA(true, 255, 160, 122));
        definedRGBColorsCSS3.put("lightseagreen", new RGBA(true, 32, 178, 170));
        definedRGBColorsCSS3.put("lightskyblue", new RGBA(true, 135, 206, 250));
        definedRGBColorsCSS3.put("lightslategray", new RGBA(true, 119, 136, 153));
        definedRGBColorsCSS3.put("lightslategrey", new RGBA(true, 119, 136, 153));
        definedRGBColorsCSS3.put("lightsteelblue", new RGBA(true, 176, 196, 222));
        definedRGBColorsCSS3.put("lightyellow", new RGBA(true, 255, 255, 224));
        definedRGBColorsCSS3.put("lime", new RGBA(true, 0, 255, 0));
        definedRGBColorsCSS3.put("limegreen", new RGBA(true, 50, 205, 50));
        definedRGBColorsCSS3.put("linen", new RGBA(true, 250, 240, 230));
        definedRGBColorsCSS3.put("magenta", new RGBA(true, 255, 0, 255));
        definedRGBColorsCSS3.put("maroon", new RGBA(true, 128, 0, 0));
        definedRGBColorsCSS3.put("mediumaquamarine", new RGBA(true, 102, 205, 170));
        definedRGBColorsCSS3.put("mediumblue", new RGBA(true, 0, 0, 205));
        definedRGBColorsCSS3.put("mediumorchid", new RGBA(true, 186, 85, 211));
        definedRGBColorsCSS3.put("mediumpurple", new RGBA(true, 147, 112, 219));
        definedRGBColorsCSS3.put("mediumseagreen", new RGBA(true, 60, 179, 113));
        definedRGBColorsCSS3.put("mediumslateblue", new RGBA(true, 123, 104, 238));
        definedRGBColorsCSS3.put("mediumspringgreen", new RGBA(true, 0, 250, 154));
        definedRGBColorsCSS3.put("mediumturquoise", new RGBA(true, 72, 209, 204));
        definedRGBColorsCSS3.put("mediumvioletred", new RGBA(true, 199, 21, 133));
        definedRGBColorsCSS3.put("midnightblue", new RGBA(true, 25, 25, 112));
        definedRGBColorsCSS3.put("mintcream", new RGBA(true, 245, 255, 250));
        definedRGBColorsCSS3.put("mistyrose", new RGBA(true, 255, 228, 225));
        definedRGBColorsCSS3.put("moccasin", new RGBA(true, 255, 228, 181));
        definedRGBColorsCSS3.put("navajowhite", new RGBA(true, 255, 222, 173));
        definedRGBColorsCSS3.put("navy", new RGBA(true, 0, 0, 128));
        definedRGBColorsCSS3.put("oldlace", new RGBA(true, 253, 245, 230));
        definedRGBColorsCSS3.put("olive", new RGBA(true, 128, 128, 0));
        definedRGBColorsCSS3.put("olivedrab", new RGBA(true, 107, 142, 35));
        definedRGBColorsCSS3.put("orange", new RGBA(true, 255, 165, 0));
        definedRGBColorsCSS3.put("orangered", new RGBA(true, 255, 69, 0));
        definedRGBColorsCSS3.put("orchid", new RGBA(true, 218, 112, 214));
        definedRGBColorsCSS3.put("palegoldenrod", new RGBA(true, 238, 232, 170));
        definedRGBColorsCSS3.put("palegreen", new RGBA(true, 152, 251, 152));
        definedRGBColorsCSS3.put("paleturquoise", new RGBA(true, 175, 238, 238));
        definedRGBColorsCSS3.put("palevioletred", new RGBA(true, 219, 112, 147));
        definedRGBColorsCSS3.put("papayawhip", new RGBA(true, 255, 239, 213));
        definedRGBColorsCSS3.put("peachpuff", new RGBA(true, 255, 218, 185));
        definedRGBColorsCSS3.put("peru", new RGBA(true, 205, 133, 63));
        definedRGBColorsCSS3.put("pink", new RGBA(true, 255, 192, 203));
        definedRGBColorsCSS3.put("plum", new RGBA(true, 221, 160, 221));
        definedRGBColorsCSS3.put("powderblue", new RGBA(true, 176, 224, 230));
        definedRGBColorsCSS3.put("purple", new RGBA(true, 128, 0, 128));
        definedRGBColorsCSS3.put("rebeccapurple", new RGBA(true, 102, 51, 153));
        definedRGBColorsCSS3.put("red", new RGBA(true, 255, 0, 0));
        definedRGBColorsCSS3.put("rosybrown", new RGBA(true, 188, 143, 143));
        definedRGBColorsCSS3.put("royalblue", new RGBA(true, 65, 105, 225));
        definedRGBColorsCSS3.put("saddlebrown", new RGBA(true, 139, 69, 19));
        definedRGBColorsCSS3.put("salmon", new RGBA(true, 250, 128, 114));
        definedRGBColorsCSS3.put("sandybrown", new RGBA(true, 244, 164, 96));
        definedRGBColorsCSS3.put("seagreen", new RGBA(true, 46, 139, 87));
        definedRGBColorsCSS3.put("seashell", new RGBA(true, 255, 245, 238));
        definedRGBColorsCSS3.put("sienna", new RGBA(true, 160, 82, 45));
        definedRGBColorsCSS3.put("silver", new RGBA(true, 192, 192, 192));
        definedRGBColorsCSS3.put("skyblue", new RGBA(true, 135, 206, 235));
        definedRGBColorsCSS3.put("slateblue", new RGBA(true, 106, 90, 205));
        definedRGBColorsCSS3.put("slategray", new RGBA(true, 112, 128, 144));
        definedRGBColorsCSS3.put("slategrey", new RGBA(true, 112, 128, 144));
        definedRGBColorsCSS3.put("snow", new RGBA(true, 255, 250, 250));
        definedRGBColorsCSS3.put("springgreen", new RGBA(true, 0, 255, 127));
        definedRGBColorsCSS3.put("steelblue", new RGBA(true, 70, 130, 180));
        definedRGBColorsCSS3.put("tan", new RGBA(true, 210, 180, 140));
        definedRGBColorsCSS3.put("teal", new RGBA(true, 0, 128, 128));
        definedRGBColorsCSS3.put("thistle", new RGBA(true, 216, 191, 216));
        definedRGBColorsCSS3.put("tomato", new RGBA(true, 255, 99, 71));
        definedRGBColorsCSS3.put("turquoise", new RGBA(true, 64, 224, 208));
        definedRGBColorsCSS3.put("violet", new RGBA(true, 238, 130, 238));
        definedRGBColorsCSS3.put("wheat", new RGBA(true, 245, 222, 179));
        definedRGBColorsCSS3.put("white", new RGBA(true, 255, 255, 255));
        definedRGBColorsCSS3.put("whitesmoke", new RGBA(true, 245, 245, 245));
        definedRGBColorsCSS3.put("yellow", new RGBA(true, 255, 255, 0));
        definedRGBColorsCSS3.put("yellowgreen", new RGBA(true, 154, 205, 50));

        // https://www.w3.org/TR/2026/CRD-css-color-4-20260427/#typedef-system-color
        definedSystemColorsCSS3 = new HashMap<>();
        String[] _system_colors = {"AccentColor", "AccentColorText", "ActiveText",
                "ButtonBorder", "ButtonFace", "ButtonText", "Canvas", "CanvasText",
                "Field", "FieldText", "GrayText", "Highlight", "HighlightText",
                "LinkText", "Mark", "MarkText", "SelectedItem", "SelectedItemText",
                "VisitedText"};
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
