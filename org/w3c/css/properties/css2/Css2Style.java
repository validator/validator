//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.properties.css.CssAzimuth;
import org.w3c.css.properties.css.CssBorderCollapse;
import org.w3c.css.properties.css.CssBorderSpacing;
import org.w3c.css.properties.css.CssBottom;
import org.w3c.css.properties.css.CssCaptionSide;
import org.w3c.css.properties.css.CssClip;
import org.w3c.css.properties.css.CssContent;
import org.w3c.css.properties.css.CssCounterIncrement;
import org.w3c.css.properties.css.CssCounterReset;
import org.w3c.css.properties.css.CssCue;
import org.w3c.css.properties.css.CssCueAfter;
import org.w3c.css.properties.css.CssCueBefore;
import org.w3c.css.properties.css.CssCursor;
import org.w3c.css.properties.css.CssDirection;
import org.w3c.css.properties.css.CssElevation;
import org.w3c.css.properties.css.CssEmptyCells;
import org.w3c.css.properties.css.CssLeft;
import org.w3c.css.properties.css.CssMarkerOffset;
import org.w3c.css.properties.css.CssMarks;
import org.w3c.css.properties.css.CssMaxHeight;
import org.w3c.css.properties.css.CssMaxWidth;
import org.w3c.css.properties.css.CssMinHeight;
import org.w3c.css.properties.css.CssMinWidth;
import org.w3c.css.properties.css.CssOrphans;
import org.w3c.css.properties.css.CssOutline;
import org.w3c.css.properties.css.CssOutlineColor;
import org.w3c.css.properties.css.CssOutlineStyle;
import org.w3c.css.properties.css.CssOutlineWidth;
import org.w3c.css.properties.css.CssOverflow;
import org.w3c.css.properties.css.CssPageBreakAfter;
import org.w3c.css.properties.css.CssPageBreakBefore;
import org.w3c.css.properties.css.CssPageBreakInside;
import org.w3c.css.properties.css.CssPause;
import org.w3c.css.properties.css.CssPauseAfter;
import org.w3c.css.properties.css.CssPauseBefore;
import org.w3c.css.properties.css.CssPitch;
import org.w3c.css.properties.css.CssPitchRange;
import org.w3c.css.properties.css.CssPlayDuring;
import org.w3c.css.properties.css.CssPosition;
import org.w3c.css.properties.css.CssQuotes;
import org.w3c.css.properties.css.CssRichness;
import org.w3c.css.properties.css.CssRight;
import org.w3c.css.properties.css.CssSize;
import org.w3c.css.properties.css.CssSpeak;
import org.w3c.css.properties.css.CssSpeakHeader;
import org.w3c.css.properties.css.CssSpeakNumeral;
import org.w3c.css.properties.css.CssSpeakPunctuation;
import org.w3c.css.properties.css.CssSpeechRate;
import org.w3c.css.properties.css.CssStress;
import org.w3c.css.properties.css.CssTableLayout;
import org.w3c.css.properties.css.CssTextShadow;
import org.w3c.css.properties.css.CssTop;
import org.w3c.css.properties.css.CssUnicodeBidi;
import org.w3c.css.properties.css.CssVisibility;
import org.w3c.css.properties.css.CssVoiceFamily;
import org.w3c.css.properties.css.CssVolume;
import org.w3c.css.properties.css.CssWidows;
import org.w3c.css.properties.css.fontface.CssSrc;
import org.w3c.css.properties.css1.Css1Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;
import org.w3c.css.values.CssIdent;

/**
 * @version $Revision$
 */
public class Css2Style extends Css1Style {

    static final CssIdent marker = CssIdent.getIdent("marker");
    /**
     * font-face
     */
    public CssSrc fontFaceCssSrc;

    /**
     * aural properties
     */
    public CssAzimuth cssAzimuth;
    public CssElevation cssElevation;
    public CssVolume cssVolume;
    public CssSpeak cssSpeak;
    public CssSpeechRate cssSpeechRate;
    public CssSpeakPunctuation cssSpeakPunctuation;
    public CssSpeakNumeral cssSpeakNumeral;
    public CssRichness cssRichness;
    public CssStress cssStress;
    public CssPitchRange cssPitchRange;
    public CssCueAfter cssCueAfter;
    public CssCueBefore cssCueBefore;
    public CssCue cssCue;
    public CssPitch cssPitch;
    public CssPauseAfter cssPauseAfter;
    public CssPauseBefore cssPauseBefore;
    public CssPause cssPause;
    public CssPlayDuring cssPlayDuring;
    public CssVoiceFamily cssVoiceFamily;

    public CssPageBreakAfter cssPageBreakAfter;
    public CssPageBreakBefore cssPageBreakBefore;
    public CssPageBreakInside cssPageBreakInside;
    public CssMarks cssMarks;
    public CssSize cssSize;

    public CssContent cssContent;

    /**
     * font properties
     */
    public org.w3c.css.properties.css.CssFontStretch cssFontStretch;
    public org.w3c.css.properties.css.CssFontSizeAdjust cssFontSizeAdjust;

    /**
     * text properties
     */
    public CssTextShadow cssTextShadow;

    public CssTop cssTop;
    public CssBottom cssBottom;
    public CssLeft cssLeft;
    public CssRight cssRight;

    public CssPosition cssPosition;

    public CssMinWidth cssMinWidth;
    public CssMaxWidth cssMaxWidth;
    public CssMinHeight cssMinHeight;
    public CssMaxHeight cssMaxHeight;

    public CssOutlineWidth cssOutlineWidth;
    public CssOutlineStyle cssOutlineStyle;
    public CssOutlineColor cssOutlineColor;
    public CssOutline cssOutline;
    public CssCursor cssCursor;

    public CssClip cssClip;
    public CssMarkerOffset cssMarkerOffset;
    public CssDirection cssDirection;
    public CssUnicodeBidi cssUnicodeBidi;
    public CssVisibility cssVisibility;
    public CssOverflow cssOverflow;
    public CssQuotes cssQuotes;
    public CssCounterIncrement cssCounterIncrement;
    public CssCounterReset cssCounterReset;

    public CssCaptionSide cssCaptionSide;
    public CssBorderCollapse cssBorderCollapse;
    public CssBorderSpacing cssBorderSpacing;
    public CssEmptyCells cssEmptyCells;
    public CssTableLayout cssTableLayout;
    public CssSpeakHeader cssSpeakHeader;

    public CssOrphans cssOrphans;
    public CssWidows cssWidows;


    public CssSrc getFontFaceCssSrc() {
        if (fontFaceCssSrc == null) {
            fontFaceCssSrc = (CssSrc) style.CascadingOrder(new CssSrc(),
                    style, selector);
        }
        return fontFaceCssSrc;
    }

    /**
     * Get the azimuth
     */
    public org.w3c.css.properties.css.CssAzimuth getAzimuth() {
        if (cssAzimuth == null) {
            cssAzimuth = (org.w3c.css.properties.css.CssAzimuth) style.CascadingOrder(new org.w3c.css.properties.css.CssAzimuth(),
                    style, selector);
        }
        return cssAzimuth;
    }

    /**
     * Get the elevation
     */
    public org.w3c.css.properties.css.CssElevation getElevation() {
        if (cssElevation == null) {
            cssElevation = (org.w3c.css.properties.css.CssElevation) style.CascadingOrder(new org.w3c.css.properties.css.CssElevation(),
                    style, selector);
        }
        return cssElevation;
    }

    /**
     * Get the border-top-style property
     */
    public final org.w3c.css.properties.css.CssBorderTopStyle getBorderTopStyle() {
        if (cssBorder.borderStyle.top == null) {
            cssBorder.borderStyle.top =
                    (org.w3c.css.properties.css.CssBorderTopStyle) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderTopStyle(),
                            style, selector);
        }
        return cssBorder.borderStyle.top;
    }

    /**
     * Get the border-right-style property
     */
    public final org.w3c.css.properties.css.CssBorderRightStyle getBorderRightStyle() {
        if (cssBorder.borderStyle.right == null) {
            cssBorder.borderStyle.right =
                    (org.w3c.css.properties.css.CssBorderRightStyle) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderRightStyle(),
                            style, selector);
        }
        return cssBorder.borderStyle.right;
    }

    /**
     * Get the border-bottom-style property
     */
    public final org.w3c.css.properties.css.CssBorderBottomStyle getBorderBottomStyle() {
        if (cssBorder.borderStyle.bottom == null) {
            cssBorder.borderStyle.bottom =
                    (org.w3c.css.properties.css.CssBorderBottomStyle) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderBottomStyle(),
                            style, selector);
        }
        return cssBorder.borderStyle.bottom;
    }

    /**
     * Get the border-left-style property
     */
    public final org.w3c.css.properties.css.CssBorderLeftStyle getBorderLeftStyle() {
        if (cssBorder.borderStyle.left == null) {
            cssBorder.borderStyle.left =
                    (org.w3c.css.properties.css.CssBorderLeftStyle) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderLeftStyle(),
                            style, selector);
        }
        return cssBorder.borderStyle.left;
    }


    /**
     * Get the page-break-after property
     */
    public final CssPageBreakAfter getPageBreakAfter() {
        if (cssPageBreakAfter == null) {
            cssPageBreakAfter =
                    (CssPageBreakAfter) style.CascadingOrder(new CssPageBreakAfter(),
                            style, selector);
        }
        return cssPageBreakAfter;
    }

    /**
     * Get the page-break-before property
     */
    public final CssPageBreakBefore getPageBreakBefore() {
        if (cssPageBreakBefore == null) {
            cssPageBreakBefore =
                    (CssPageBreakBefore) style.CascadingOrder(new CssPageBreakBefore(),
                            style, selector);
        }
        return cssPageBreakBefore;
    }

    /**
     * Get the page-break-inside property
     */
    public final CssPageBreakInside getPageBreakInside() {
        if (cssPageBreakInside == null) {
            cssPageBreakInside =
                    (CssPageBreakInside) style.CascadingOrder(new CssPageBreakInside(),
                            style, selector);
        }
        return cssPageBreakInside;
    }

    /**
     * Get the marks property
     */
    public final CssMarks getMarks() {
        if (cssMarks == null) {
            cssMarks =
                    (CssMarks) style.CascadingOrder(new CssMarks(),
                            style, selector);
        }
        return cssMarks;
    }

    /**
     * Get the size property
     */
    public final CssSize getSize() {
        if (cssSize == null) {
            cssSize =
                    (CssSize) style.CascadingOrder(new CssSize(),
                            style, selector);
        }
        return cssSize;
    }

    /**
     * get the font-stretch property
     *
     * @return a CssFontStretch instance
     */
    public org.w3c.css.properties.css.CssFontStretch getFontStretch() {
        if (cssFontStretch == null) {
            cssFontStretch = (org.w3c.css.properties.css.CssFontStretch) style.CascadingOrder(new org.w3c.css.properties.css.CssFontStretch(),
                    style, selector);
        }
        return cssFontStretch;
    }

    /**
     * get the font-size-adjust property
     *
     * @return a CssFontSizeAdjust instance
     */
    public org.w3c.css.properties.css.CssFontSizeAdjust getFontSizeAdjust() {
        if (cssFontSizeAdjust == null) {
            cssFontSizeAdjust = (org.w3c.css.properties.css.CssFontSizeAdjust) style.CascadingOrder(new org.w3c.css.properties.css.CssFontSizeAdjust(),
                    style, selector);
        }
        return cssFontSizeAdjust;
    }

    /**
     * Get the text-shadow property
     *
     * @return a CssTextShadow instance
     */
    public final org.w3c.css.properties.css.CssTextShadow getTextShadow() {
        if (cssTextShadow == null) {
            cssTextShadow =
                    (org.w3c.css.properties.css.CssTextShadow) style.CascadingOrder(new org.w3c.css.properties.css.CssTextShadow(),
                            style, selector);
        }
        return cssTextShadow;
    }

    /**
     * Get the top property
     */
    public final CssTop getTop() {
        if (cssTop == null) {
            cssTop =
                    (CssTop) style.CascadingOrder(new CssTop(), style, selector);
        }
        return cssTop;
    }

    /**
     * Get the bottom property
     */
    public final CssBottom getBottom() {
        if (cssBottom == null) {
            cssBottom =
                    (CssBottom) style.CascadingOrder(new CssBottom(), style, selector);
        }
        return cssBottom;
    }

    /**
     * Get the left property
     */
    public final CssLeft getLeft() {
        if (cssLeft == null) {
            cssLeft =
                    (CssLeft) style.CascadingOrder(new CssLeft(), style, selector);
        }
        return cssLeft;
    }

    /**
     * Get the right property
     */
    public final CssRight getRight() {
        if (cssRight == null) {
            cssRight =
                    (CssRight) style.CascadingOrder(new CssRight(), style, selector);
        }
        return cssRight;
    }

    /**
     * Get the position property
     */
    public final CssPosition getPosition() {
        if (cssPosition == null) {
            cssPosition =
                    (CssPosition) style.CascadingOrder(new CssPosition(), style, selector);
        }
        return cssPosition;
    }

    public final CssMinWidth getMinWidth() {
        if (cssMinWidth == null) {
            cssMinWidth =
                    (CssMinWidth) style.CascadingOrder(new CssMinWidth(), style, selector);
        }
        return cssMinWidth;
    }

    public final CssMaxWidth getMaxWidth() {
        if (cssMaxWidth == null) {
            cssMaxWidth =
                    (CssMaxWidth) style.CascadingOrder(new CssMaxWidth(), style, selector);
        }
        return cssMaxWidth;
    }

    public final CssMinHeight getMinHeight() {
        if (cssMinHeight == null) {
            cssMinHeight =
                    (CssMinHeight) style.CascadingOrder(new CssMinHeight(), style, selector);
        }
        return cssMinHeight;
    }

    public final CssMaxHeight getMaxHeight() {
        if (cssMaxHeight == null) {
            cssMaxHeight =
                    (CssMaxHeight) style.CascadingOrder(new CssMaxHeight(), style, selector);
        }
        return cssMaxHeight;
    }

    public final CssOutlineWidth getOutlineWidth() {
        if (cssOutlineWidth == null) {
            cssOutlineWidth =
                    (CssOutlineWidth) style.CascadingOrder(new CssOutlineWidth(), style, selector);
        }
        return cssOutlineWidth;
    }

    public final CssOutlineStyle getOutlineStyle() {
        if (cssOutlineStyle == null) {
            cssOutlineStyle =
                    (CssOutlineStyle) style.CascadingOrder(new CssOutlineStyle(), style, selector);
        }
        return cssOutlineStyle;
    }

    public final CssOutlineColor getOutlineColor() {
        if (cssOutlineColor == null) {
            cssOutlineColor =
                    (CssOutlineColor) style.CascadingOrder(new CssOutlineColor(), style, selector);
        }
        return cssOutlineColor;
    }

    public final CssOutline getOutline() {
        if (cssOutline == null) {
            cssOutline =
                    (CssOutline) style.CascadingOrder(new CssOutline(), style, selector);
        }
        return cssOutline;
    }

    public final CssCursor getCursor() {
        if (cssCursor == null) {
            cssCursor =
                    (CssCursor) style.CascadingOrder(new CssCursor(), style, selector);
        }
        return cssCursor;
    }

    public final CssMarkerOffset getMarkerOffset() {
        if (cssMarkerOffset == null) {
            cssMarkerOffset =
                    (CssMarkerOffset) style.CascadingOrder(new CssMarkerOffset(),
                            style, selector);
        }
        return cssMarkerOffset;
    }

    /**
     * Get the content property
     */
    public final CssContent getContent() {
        if (cssContent == null) {
            cssContent = (CssContent) style.CascadingOrder(new CssContent(), style, selector);
        }
        return cssContent;
    }

    /**
     * Get the clip property
     */
    public final CssClip getClip() {
        if (cssClip == null) {
            cssClip =
                    (CssClip) style.CascadingOrder(new CssClip(),
                            style, selector);
        }
        return cssClip;
    }

    /**
     * Get the direction property
     */
    public final CssDirection getDirection() {
        if (cssDirection == null) {
            cssDirection =
                    (CssDirection) style.CascadingOrder(new CssDirection(),
                            style, selector);
        }
        return cssDirection;
    }

    /**
     * Get the unicode-bidi property
     */
    public final CssUnicodeBidi getUnicodeBidi() {
        if (cssUnicodeBidi == null) {
            cssUnicodeBidi =
                    (CssUnicodeBidi) style.CascadingOrder(new CssUnicodeBidi(),
                            style, selector);
        }
        return cssUnicodeBidi;
    }

    /**
     * Get the visibility property
     */
    public final CssVisibility getVisibility() {
        if (cssVisibility == null) {
            cssVisibility =
                    (CssVisibility) style.CascadingOrder(new CssVisibility(),
                            style, selector);
        }
        return cssVisibility;
    }

    /**
     * Get the overflow property
     */
    public final CssOverflow getOverflow() {
        if (cssOverflow == null) {
            cssOverflow =
                    (CssOverflow) style.CascadingOrder(new CssOverflow(),
                            style, selector);
        }
        return cssOverflow;
    }

    /**
     * Get the quotes property
     */
    public final CssQuotes getQuotes() {
        if (cssQuotes == null) {
            cssQuotes =
                    (CssQuotes) style.CascadingOrder(new CssQuotes(),
                            style, selector);
        }
        return cssQuotes;
    }

    /**
     * Get the counter-increment property
     */
    public final CssCounterIncrement getCounterIncrement() {
        if (cssCounterIncrement == null) {
            cssCounterIncrement =
                    (CssCounterIncrement) style.CascadingOrder(new CssCounterIncrement(),
                            style, selector);
        }
        return cssCounterIncrement;
    }

    /**
     * Get the counter-reset property
     */
    public final CssCounterReset getCounterReset() {
        if (cssCounterReset == null) {
            cssCounterReset =
                    (CssCounterReset) style.CascadingOrder(new CssCounterReset(),
                            style, selector);
        }
        return cssCounterReset;
    }

    public final CssCaptionSide getCaptionSide() {
        if (cssCaptionSide == null) {
            cssCaptionSide =
                    (CssCaptionSide) style.CascadingOrder(new CssCaptionSide(),
                            style, selector);
        }
        return cssCaptionSide;
    }

    public final CssBorderCollapse getBorderCollapse() {
        if (cssBorderCollapse == null) {
            cssBorderCollapse =
                    (CssBorderCollapse) style.CascadingOrder(new CssBorderCollapse(),
                            style, selector);
        }
        return cssBorderCollapse;
    }

    public final CssEmptyCells getEmptyCells() {
        if (cssEmptyCells == null) {
            cssEmptyCells =
                    (CssEmptyCells) style.CascadingOrder(new CssEmptyCells(),
                            style, selector);
        }
        return cssEmptyCells;
    }

    public final CssTableLayout getTableLayout() {
        if (cssTableLayout == null) {
            cssTableLayout =
                    (CssTableLayout) style.CascadingOrder(new CssTableLayout(),
                            style, selector);
        }
        return cssTableLayout;
    }

    public final CssBorderSpacing getBorderSpacing() {
        if (cssBorderSpacing == null) {
            cssBorderSpacing =
                    (CssBorderSpacing) style.CascadingOrder(new CssBorderSpacing(),
                            style, selector);
        }
        return cssBorderSpacing;
    }

    public final CssSpeakHeader getSpeakHeader() {
        if (cssSpeakHeader == null) {
            cssSpeakHeader =
                    (CssSpeakHeader) style.CascadingOrder(new CssSpeakHeader(),
                            style, selector);
        }
        return cssSpeakHeader;
    }

    public final CssVolume getVolume() {
        if (cssVolume == null) {
            cssVolume =
                    (CssVolume) style.CascadingOrder(new CssVolume(),
                            style, selector);
        }
        return cssVolume;
    }

    public final CssSpeak getSpeak() {
        if (cssSpeak == null) {
            cssSpeak =
                    (CssSpeak) style.CascadingOrder(new CssSpeak(),
                            style, selector);
        }
        return cssSpeak;
    }

    public final CssSpeechRate getSpeechRate() {
        if (cssSpeechRate == null) {
            cssSpeechRate =
                    (CssSpeechRate) style.CascadingOrder(new CssSpeechRate(),
                            style, selector);
        }
        return cssSpeechRate;
    }

    public final CssSpeakPunctuation getSpeakPunctuation() {
        if (cssSpeakPunctuation == null) {
            cssSpeakPunctuation =
                    (CssSpeakPunctuation) style.CascadingOrder(new CssSpeakPunctuation(),
                            style, selector);
        }
        return cssSpeakPunctuation;
    }

    public final CssSpeakNumeral getSpeakNumeral() {
        if (cssSpeakNumeral == null) {
            cssSpeakNumeral =
                    (CssSpeakNumeral) style.CascadingOrder(new CssSpeakNumeral(),
                            style, selector);
        }
        return cssSpeakNumeral;
    }

    public final CssRichness getRichness() {
        if (cssRichness == null) {
            cssRichness =
                    (CssRichness) style.CascadingOrder(new CssRichness(),
                            style, selector);
        }
        return cssRichness;
    }

    public final CssStress getStress() {
        if (cssStress == null) {
            cssStress =
                    (CssStress) style.CascadingOrder(new CssStress(),
                            style, selector);
        }
        return cssStress;
    }

    public final CssPitchRange getPitchRange() {
        if (cssPitchRange == null) {
            cssPitchRange =
                    (CssPitchRange) style.CascadingOrder(new CssPitchRange(),
                            style, selector);
        }
        return cssPitchRange;
    }

    public final CssCueAfter getCueAfter() {
        if (cssCueAfter == null) {
            cssCueAfter =
                    (CssCueAfter) style.CascadingOrder(new CssCueAfter(),
                            style, selector);
        }
        return cssCueAfter;
    }

    public final CssCueBefore getCueBefore() {
        if (cssCueBefore == null) {
            cssCueBefore =
                    (CssCueBefore) style.CascadingOrder(new CssCueBefore(),
                            style, selector);
        }
        return cssCueBefore;
    }

    public final CssCue getCue() {
        if (cssCue == null) {
            cssCue =
                    (CssCue) style.CascadingOrder(new CssCue(),
                            style, selector);
        }
        return cssCue;
    }

    public final CssPitch getPitch() {
        if (cssPitch == null) {
            cssPitch =
                    (CssPitch) style.CascadingOrder(new CssPitch(),
                            style, selector);
        }
        return cssPitch;
    }

    public final CssPauseAfter getPauseAfter() {
        if (cssPauseAfter == null) {
            cssPauseAfter =
                    (CssPauseAfter) style.CascadingOrder(new CssPauseAfter(),
                            style, selector);
        }
        return cssPauseAfter;
    }

    public final CssPauseBefore getPauseBefore() {
        if (cssPauseBefore == null) {
            cssPauseBefore =
                    (CssPauseBefore) style.CascadingOrder(new CssPauseBefore(),
                            style, selector);
        }
        return cssPauseBefore;
    }

    public final CssPause getPause() {
        if (cssPause == null) {
            cssPause =
                    (CssPause) style.CascadingOrder(new CssPause(),
                            style, selector);
        }
        return cssPause;
    }

    public final CssPlayDuring getPlayDuring() {
        if (cssPlayDuring == null) {
            cssPlayDuring =
                    (CssPlayDuring) style.CascadingOrder(new CssPlayDuring(),
                            style, selector);
        }
        return cssPlayDuring;
    }

    public final CssVoiceFamily getVoiceFamily() {
        if (cssVoiceFamily == null) {
            cssVoiceFamily =
                    (CssVoiceFamily) style.CascadingOrder(new CssVoiceFamily(),
                            style, selector);
        }
        return cssVoiceFamily;
    }

    public final CssOrphans getOrphans() {
        if (cssOrphans == null) {
            cssOrphans =
                    (CssOrphans) style.CascadingOrder(new CssOrphans(),
                            style, selector);
        }
        return cssOrphans;
    }

    public final CssWidows getWidows() {
        if (cssWidows == null) {
            cssWidows =
                    (CssWidows) style.CascadingOrder(new CssWidows(),
                            style, selector);
        }
        return cssWidows;
    }

    /**
     * Find conflicts in this Style
     *
     * @param warnings     For warnings reports.
     * @param allSelectors All contexts is the entire style sheet.
     */
    public void findConflicts(ApplContext ac, Warnings warnings,
                              CssSelectors selector, CssSelectors[] allSelectors) {
        super.findConflicts(ac, warnings, selector, allSelectors);

        if (cssMarkerOffset != null) {
            if ((cssDisplay == null) || (!marker.equals(cssDisplay.get()))) {
                warnings.addWarning(new Warning(cssMarkerOffset,
                        "marker", 1, ac));
            }
        }
    }

}
