//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
/*
 */
package org.w3c.css.properties.css2.font;

/**
 * @version $Revision$
 */
public class Css2Style extends org.w3c.css.properties.paged.Css2Style {

    FontFamily fontFamily;
    FontStyle fontStyle;
    FontVariant fontVariant;
    FontWeight fontWeight;
    FontStretch fontStretch;
    FontSize fontSize;
    UnicodeRange unicodeRange;
    UnitsPerEm unitsPerEm;
    Src src;
    Panose1 panose1;
    Bbox bbox;
    Widths widths;
    Stemv stemv;
    Stemh stemh;
    Slope slope;
    CapHeight capHeight;
    XHeight xHeight;
    Ascent ascent;
    Descent descent;
    Baseline baseline;
    Centerline centerline;
    Mathline mathline;
    Topline topline;
    DefinitionSrc definitionSrc;

    /**
     * Get the font-family property
     */
    public final FontFamily getFaceFontFamily() {
        return fontFamily;
    }

    /**
     * Get the font-style property
     */
    public final FontStyle getFaceFontStyle() {
        return fontStyle;
    }

    /**
     * Get the font-variant property
     */
    public final FontVariant getFaceFontVariant() {
        return fontVariant;
    }

    /**
     * Get the font-weight property
     */
    public final FontWeight getFaceFontWeight() {
        return fontWeight;
    }

    /**
     * Get the font-stretch property
     */
    public final FontStretch getFaceFontStretch() {
        return fontStretch;
    }

    /**
     * Get the font-size property
     */
    public final FontSize getFaceFontSize() {
        return fontSize;
    }

    /**
     * Get the unicode-range property
     */
    public final UnicodeRange getFaceUnicodeRange() {
        return unicodeRange;
    }


    /**
     * Get the units-per-em property
     */
    public final UnitsPerEm getUnitsPerEm() {
        return unitsPerEm;
    }

    /**
     * Get the stemv property
     */
    public final Stemv getStemv() {
        return stemv;
    }

    /**
     * Get the src property
     */
    public final Src getSrc() {
        return src;
    }

    /**
     * Get the panose1 property
     */
    public final Panose1 getPanose1() {
        return panose1;
    }

    /**
     * Get the widths property
     */
    public final Widths getWidths() {
        return widths;
    }

    /**
     * Get the bbox property
     */
    public final Bbox getBbox() {
        return bbox;
    }

    /**
     * Get the stemh property
     */
    public final Stemh getStemh() {
        return stemh;
    }

    /**
     * Get the slope property
     */
    public final Slope getSlope() {
        return slope;
    }

    /**
     * Get the ascent property
     */
    public final Ascent getAscent() {
        return ascent;
    }

    /**
     * Get the descent property
     */
    public final Descent getDescent() {
        return descent;
    }

    /**
     * Get the cap-height property
     */
    public final CapHeight getCapHeight() {
        return capHeight;
    }

    /**
     * Get the x-height property
     */
    public final XHeight getXHeight() {
        return xHeight;
    }

    /**
     * Get the baseline property
     */
    public final Baseline getBaseline() {
        return baseline;
    }

    /**
     * Get the centerline property
     */
    public final Centerline getCenterline() {
        return centerline;
    }

    /**
     * Get the mathline property
     */
    public final Mathline getMathline() {
        return mathline;
    }

    /**
     * Get the topline property
     */
    public final Topline getTopline() {
        return topline;
    }

    /**
     * Get the definition-src property
     */
    public final DefinitionSrc getDefinitionSrc() {
        return definitionSrc;
    }


}
