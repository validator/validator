//

// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// COPYRIGHT (c) 1995-2000 World Wide Web Consortium, (MIT, INRIA, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.properties.atsc.ATSCStyle;
import org.w3c.css.properties.css.CssAccentColor;
import org.w3c.css.properties.css.CssAlignContent;
import org.w3c.css.properties.css.CssAlignItems;
import org.w3c.css.properties.css.CssAlignSelf;
import org.w3c.css.properties.css.CssAlignmentBaseline;
import org.w3c.css.properties.css.CssAnimation;
import org.w3c.css.properties.css.CssAnimationDelay;
import org.w3c.css.properties.css.CssAnimationDirection;
import org.w3c.css.properties.css.CssAnimationDuration;
import org.w3c.css.properties.css.CssAnimationFillMode;
import org.w3c.css.properties.css.CssAnimationIterationCount;
import org.w3c.css.properties.css.CssAnimationName;
import org.w3c.css.properties.css.CssAnimationPlayState;
import org.w3c.css.properties.css.CssAnimationTimingFunction;
import org.w3c.css.properties.css.CssAppearance;
import org.w3c.css.properties.css.CssAspectRatio;
import org.w3c.css.properties.css.CssBackdropFilter;
import org.w3c.css.properties.css.CssBackfaceVisibility;
import org.w3c.css.properties.css.CssBackgroundBlendMode;
import org.w3c.css.properties.css.CssBackgroundClip;
import org.w3c.css.properties.css.CssBackgroundOrigin;
import org.w3c.css.properties.css.CssBackgroundPositionX;
import org.w3c.css.properties.css.CssBackgroundPositionY;
import org.w3c.css.properties.css.CssBackgroundSize;
import org.w3c.css.properties.css.CssBaselineShift;
import org.w3c.css.properties.css.CssBaselineSource;
import org.w3c.css.properties.css.CssBlockSize;
import org.w3c.css.properties.css.CssBorderBlock;
import org.w3c.css.properties.css.CssBorderBlockColor;
import org.w3c.css.properties.css.CssBorderBlockEnd;
import org.w3c.css.properties.css.CssBorderBlockEndColor;
import org.w3c.css.properties.css.CssBorderBlockEndStyle;
import org.w3c.css.properties.css.CssBorderBlockEndWidth;
import org.w3c.css.properties.css.CssBorderBlockStart;
import org.w3c.css.properties.css.CssBorderBlockStartColor;
import org.w3c.css.properties.css.CssBorderBlockStartStyle;
import org.w3c.css.properties.css.CssBorderBlockStartWidth;
import org.w3c.css.properties.css.CssBorderBlockStyle;
import org.w3c.css.properties.css.CssBorderBlockWidth;
import org.w3c.css.properties.css.CssBorderEndEndRadius;
import org.w3c.css.properties.css.CssBorderEndStartRadius;
import org.w3c.css.properties.css.CssHyphenateCharacter;
import org.w3c.css.properties.css.CssHyphenateLimitChars;
import org.w3c.css.properties.css.CssHyphenateLimitLast;
import org.w3c.css.properties.css.CssHyphenateLimitLines;
import org.w3c.css.properties.css.CssHyphenateLimitZone;
import org.w3c.css.properties.css.CssImageRendering;
import org.w3c.css.properties.css.CssBorderImageSource;
import org.w3c.css.properties.css.CssBorderInline;
import org.w3c.css.properties.css.CssBorderInlineColor;
import org.w3c.css.properties.css.CssBorderInlineEnd;
import org.w3c.css.properties.css.CssBorderInlineEndColor;
import org.w3c.css.properties.css.CssBorderInlineEndStyle;
import org.w3c.css.properties.css.CssBorderInlineEndWidth;
import org.w3c.css.properties.css.CssBorderInlineStart;
import org.w3c.css.properties.css.CssBorderInlineStartColor;
import org.w3c.css.properties.css.CssBorderInlineStartStyle;
import org.w3c.css.properties.css.CssBorderInlineStartWidth;
import org.w3c.css.properties.css.CssBorderInlineStyle;
import org.w3c.css.properties.css.CssBorderInlineWidth;
import org.w3c.css.properties.css.CssBorderStartEndRadius;
import org.w3c.css.properties.css.CssBorderStartStartRadius;
import org.w3c.css.properties.css.CssBoxDecorationBreak;
import org.w3c.css.properties.css.CssBoxShadow;
import org.w3c.css.properties.css.CssBoxSizing;
import org.w3c.css.properties.css.CssBoxSuppress;
import org.w3c.css.properties.css.CssBreakAfter;
import org.w3c.css.properties.css.CssBreakBefore;
import org.w3c.css.properties.css.CssBreakInside;
import org.w3c.css.properties.css.CssCaret;
import org.w3c.css.properties.css.CssCaretColor;
import org.w3c.css.properties.css.CssCaretShape;
import org.w3c.css.properties.css.CssColorAdjust;
import org.w3c.css.properties.css.CssColorInterpolationFilters;
import org.w3c.css.properties.css.CssColorScheme;
import org.w3c.css.properties.css.CssColumnCount;
import org.w3c.css.properties.css.CssColumnFill;
import org.w3c.css.properties.css.CssColumnGap;
import org.w3c.css.properties.css.CssColumnRule;
import org.w3c.css.properties.css.CssColumnRuleColor;
import org.w3c.css.properties.css.CssColumnRuleStyle;
import org.w3c.css.properties.css.CssColumnRuleWidth;
import org.w3c.css.properties.css.CssColumnSpan;
import org.w3c.css.properties.css.CssColumnWidth;
import org.w3c.css.properties.css.CssColumns;
import org.w3c.css.properties.css.CssContain;
import org.w3c.css.properties.css.CssContentVisibility;
import org.w3c.css.properties.css.CssCounterSet;
import org.w3c.css.properties.css.CssDominantBaseline;
import org.w3c.css.properties.css.CssFilter;
import org.w3c.css.properties.css.CssFlex;
import org.w3c.css.properties.css.CssFlexBasis;
import org.w3c.css.properties.css.CssFlexDirection;
import org.w3c.css.properties.css.CssFlexFlow;
import org.w3c.css.properties.css.CssFlexGrow;
import org.w3c.css.properties.css.CssFlexShrink;
import org.w3c.css.properties.css.CssFlexWrap;
import org.w3c.css.properties.css.CssFloatDefer;
import org.w3c.css.properties.css.CssFloatOffset;
import org.w3c.css.properties.css.CssFloatReference;
import org.w3c.css.properties.css.CssFloodColor;
import org.w3c.css.properties.css.CssFloodOpacity;
import org.w3c.css.properties.css.CssFontFeatureSettings;
import org.w3c.css.properties.css.CssFontKerning;
import org.w3c.css.properties.css.CssFontLanguageOverride;
import org.w3c.css.properties.css.CssFontOpticalSizing;
import org.w3c.css.properties.css.CssFontPalette;
import org.w3c.css.properties.css.CssFontSynthesis;
import org.w3c.css.properties.css.CssFontSynthesisSmallCaps;
import org.w3c.css.properties.css.CssFontSynthesisStyle;
import org.w3c.css.properties.css.CssFontSynthesisWeight;
import org.w3c.css.properties.css.CssFontVariantAlternates;
import org.w3c.css.properties.css.CssFontVariantCaps;
import org.w3c.css.properties.css.CssFontVariantEastAsian;
import org.w3c.css.properties.css.CssFontVariantEmoji;
import org.w3c.css.properties.css.CssFontVariantLigatures;
import org.w3c.css.properties.css.CssFontVariantNumeric;
import org.w3c.css.properties.css.CssFontVariantPosition;
import org.w3c.css.properties.css.CssFontVariationSettings;
import org.w3c.css.properties.css.CssForcedColorAdjust;
import org.w3c.css.properties.css.CssGap;
import org.w3c.css.properties.css.CssGrid;
import org.w3c.css.properties.css.CssGridArea;
import org.w3c.css.properties.css.CssGridAutoColumns;
import org.w3c.css.properties.css.CssGridAutoFlow;
import org.w3c.css.properties.css.CssGridAutoRows;
import org.w3c.css.properties.css.CssGridColumn;
import org.w3c.css.properties.css.CssGridColumnEnd;
import org.w3c.css.properties.css.CssGridColumnGap;
import org.w3c.css.properties.css.CssGridColumnStart;
import org.w3c.css.properties.css.CssGridGap;
import org.w3c.css.properties.css.CssGridRow;
import org.w3c.css.properties.css.CssGridRowEnd;
import org.w3c.css.properties.css.CssGridRowGap;
import org.w3c.css.properties.css.CssGridRowStart;
import org.w3c.css.properties.css.CssGridTemplate;
import org.w3c.css.properties.css.CssGridTemplateAreas;
import org.w3c.css.properties.css.CssGridTemplateColumns;
import org.w3c.css.properties.css.CssGridTemplateRows;
import org.w3c.css.properties.css.CssHangingPunctuation;
import org.w3c.css.properties.css.CssHyphens;
import org.w3c.css.properties.css.CssIcon;
import org.w3c.css.properties.css.CssImageOrientation;
import org.w3c.css.properties.css.CssImageResolution;
import org.w3c.css.properties.css.CssImeMode;
import org.w3c.css.properties.css.CssInitialLetter;
import org.w3c.css.properties.css.CssInitialLetterAlign;
import org.w3c.css.properties.css.CssInitialLetterWrap;
import org.w3c.css.properties.css.CssInlineSize;
import org.w3c.css.properties.css.CssInlineSizing;
import org.w3c.css.properties.css.CssInset;
import org.w3c.css.properties.css.CssInsetBlock;
import org.w3c.css.properties.css.CssInsetBlockEnd;
import org.w3c.css.properties.css.CssInsetBlockStart;
import org.w3c.css.properties.css.CssInsetInline;
import org.w3c.css.properties.css.CssInsetInlineEnd;
import org.w3c.css.properties.css.CssInsetInlineStart;
import org.w3c.css.properties.css.CssIsolation;
import org.w3c.css.properties.css.CssJustifyContent;
import org.w3c.css.properties.css.CssJustifyItems;
import org.w3c.css.properties.css.CssJustifySelf;
import org.w3c.css.properties.css.CssLightingColor;
import org.w3c.css.properties.css.CssLineBreak;
import org.w3c.css.properties.css.CssLinePadding;
import org.w3c.css.properties.css.CssMarginBlock;
import org.w3c.css.properties.css.CssMarginBlockEnd;
import org.w3c.css.properties.css.CssMarginBlockStart;
import org.w3c.css.properties.css.CssMarginInline;
import org.w3c.css.properties.css.CssMarginInlineEnd;
import org.w3c.css.properties.css.CssMarginInlineStart;
import org.w3c.css.properties.css.CssMarkerSide;
import org.w3c.css.properties.css.CssMarqueeDirection;
import org.w3c.css.properties.css.CssMarqueePlayCount;
import org.w3c.css.properties.css.CssMarqueeSpeed;
import org.w3c.css.properties.css.CssMarqueeStyle;
import org.w3c.css.properties.css.CssMaxBlockSize;
import org.w3c.css.properties.css.CssMaxInlineSize;
import org.w3c.css.properties.css.CssMinBlockSize;
import org.w3c.css.properties.css.CssMinInlineSize;
import org.w3c.css.properties.css.CssMixBlendMode;
import org.w3c.css.properties.css.CssNavDown;
import org.w3c.css.properties.css.CssNavLeft;
import org.w3c.css.properties.css.CssNavRight;
import org.w3c.css.properties.css.CssNavUp;
import org.w3c.css.properties.css.CssObjectFit;
import org.w3c.css.properties.css.CssObjectPosition;
import org.w3c.css.properties.css.CssOpacity;
import org.w3c.css.properties.css.CssOrder;
import org.w3c.css.properties.css.CssOutlineOffset;
import org.w3c.css.properties.css.CssOverflowAnchor;
import org.w3c.css.properties.css.CssOverflowStyle;
import org.w3c.css.properties.css.CssOverflowWrap;
import org.w3c.css.properties.css.CssOverflowX;
import org.w3c.css.properties.css.CssOverflowY;
import org.w3c.css.properties.css.CssOverscrollBehavior;
import org.w3c.css.properties.css.CssOverscrollBehaviorBlock;
import org.w3c.css.properties.css.CssOverscrollBehaviorInline;
import org.w3c.css.properties.css.CssOverscrollBehaviorX;
import org.w3c.css.properties.css.CssOverscrollBehaviorY;
import org.w3c.css.properties.css.CssPaddingBlock;
import org.w3c.css.properties.css.CssPaddingBlockEnd;
import org.w3c.css.properties.css.CssPaddingBlockStart;
import org.w3c.css.properties.css.CssPaddingInline;
import org.w3c.css.properties.css.CssPaddingInlineEnd;
import org.w3c.css.properties.css.CssPaddingInlineStart;
import org.w3c.css.properties.css.CssPerspective;
import org.w3c.css.properties.css.CssPerspectiveOrigin;
import org.w3c.css.properties.css.CssPlaceContent;
import org.w3c.css.properties.css.CssPlaceItems;
import org.w3c.css.properties.css.CssPlaceSelf;
import org.w3c.css.properties.css.CssPrintColorAdjust;
import org.w3c.css.properties.css.CssResize;
import org.w3c.css.properties.css.CssRest;
import org.w3c.css.properties.css.CssRestAfter;
import org.w3c.css.properties.css.CssRestBefore;
import org.w3c.css.properties.css.CssRotate;
import org.w3c.css.properties.css.CssRowGap;
import org.w3c.css.properties.css.CssRubyAlign;
import org.w3c.css.properties.css.CssRubyMerge;
import org.w3c.css.properties.css.CssRubyPosition;
import org.w3c.css.properties.css.CssScale;
import org.w3c.css.properties.css.CssScrollBehavior;
import org.w3c.css.properties.css.CssScrollMargin;
import org.w3c.css.properties.css.CssScrollMarginBlock;
import org.w3c.css.properties.css.CssScrollMarginBlockEnd;
import org.w3c.css.properties.css.CssScrollMarginBlockStart;
import org.w3c.css.properties.css.CssScrollMarginBottom;
import org.w3c.css.properties.css.CssScrollMarginInline;
import org.w3c.css.properties.css.CssScrollMarginInlineEnd;
import org.w3c.css.properties.css.CssScrollMarginInlineStart;
import org.w3c.css.properties.css.CssScrollMarginLeft;
import org.w3c.css.properties.css.CssScrollMarginRight;
import org.w3c.css.properties.css.CssScrollMarginTop;
import org.w3c.css.properties.css.CssScrollPadding;
import org.w3c.css.properties.css.CssScrollPaddingBlock;
import org.w3c.css.properties.css.CssScrollPaddingBlockEnd;
import org.w3c.css.properties.css.CssScrollPaddingBlockStart;
import org.w3c.css.properties.css.CssScrollPaddingBottom;
import org.w3c.css.properties.css.CssScrollPaddingInline;
import org.w3c.css.properties.css.CssScrollPaddingInlineEnd;
import org.w3c.css.properties.css.CssScrollPaddingInlineStart;
import org.w3c.css.properties.css.CssScrollPaddingLeft;
import org.w3c.css.properties.css.CssScrollPaddingRight;
import org.w3c.css.properties.css.CssScrollPaddingTop;
import org.w3c.css.properties.css.CssScrollSnapAlign;
import org.w3c.css.properties.css.CssScrollSnapStop;
import org.w3c.css.properties.css.CssScrollSnapType;
import org.w3c.css.properties.css.CssScrollbarColor;
import org.w3c.css.properties.css.CssScrollbarWidth;
import org.w3c.css.properties.css.CssSpeakAs;
import org.w3c.css.properties.css.CssTabSize;
import org.w3c.css.properties.css.CssTextAlignAll;
import org.w3c.css.properties.css.CssTextAlignLast;
import org.w3c.css.properties.css.CssTextAutospace;
import org.w3c.css.properties.css.CssTextCombineUpright;
import org.w3c.css.properties.css.CssTextDecorationColor;
import org.w3c.css.properties.css.CssTextDecorationLine;
import org.w3c.css.properties.css.CssTextDecorationSkip;
import org.w3c.css.properties.css.CssTextDecorationSkipBox;
import org.w3c.css.properties.css.CssTextDecorationSkipInk;
import org.w3c.css.properties.css.CssTextDecorationSkipInset;
import org.w3c.css.properties.css.CssTextDecorationSkipSelf;
import org.w3c.css.properties.css.CssTextDecorationSkipSpaces;
import org.w3c.css.properties.css.CssTextDecorationStyle;
import org.w3c.css.properties.css.CssTextDecorationThickness;
import org.w3c.css.properties.css.CssTextEmphasis;
import org.w3c.css.properties.css.CssTextEmphasisColor;
import org.w3c.css.properties.css.CssTextEmphasisPosition;
import org.w3c.css.properties.css.CssTextEmphasisStyle;
import org.w3c.css.properties.css.CssTextGroupAlign;
import org.w3c.css.properties.css.CssTextJustify;
import org.w3c.css.properties.css.CssTextOrientation;
import org.w3c.css.properties.css.CssTextOverflow;
import org.w3c.css.properties.css.CssTextSizeAdjust;
import org.w3c.css.properties.css.CssTextSpacing;
import org.w3c.css.properties.css.CssTextSpacingTrim;
import org.w3c.css.properties.css.CssTextUnderlineOffset;
import org.w3c.css.properties.css.CssTextUnderlinePosition;
import org.w3c.css.properties.css.CssTextWrap;
import org.w3c.css.properties.css.CssTextWrapMode;
import org.w3c.css.properties.css.CssTextWrapStyle;
import org.w3c.css.properties.css.CssTouchAction;
import org.w3c.css.properties.css.CssTransform;
import org.w3c.css.properties.css.CssTransformBox;
import org.w3c.css.properties.css.CssTransformOrigin;
import org.w3c.css.properties.css.CssTransformStyle;
import org.w3c.css.properties.css.CssTransition;
import org.w3c.css.properties.css.CssTransitionDelay;
import org.w3c.css.properties.css.CssTransitionDuration;
import org.w3c.css.properties.css.CssTransitionProperty;
import org.w3c.css.properties.css.CssTransitionTimingFunction;
import org.w3c.css.properties.css.CssTranslate;
import org.w3c.css.properties.css.CssUserSelect;
import org.w3c.css.properties.css.CssVoiceBalance;
import org.w3c.css.properties.css.CssVoiceDuration;
import org.w3c.css.properties.css.CssVoicePitch;
import org.w3c.css.properties.css.CssVoiceRange;
import org.w3c.css.properties.css.CssVoiceRate;
import org.w3c.css.properties.css.CssVoiceStress;
import org.w3c.css.properties.css.CssVoiceVolume;
import org.w3c.css.properties.css.CssWhiteSpaceCollapse;
import org.w3c.css.properties.css.CssWhiteSpaceTrim;
import org.w3c.css.properties.css.CssWillChange;
import org.w3c.css.properties.css.CssWordBreak;
import org.w3c.css.properties.css.CssWordSpaceTransform;
import org.w3c.css.properties.css.CssWrapAfter;
import org.w3c.css.properties.css.CssWrapBefore;
import org.w3c.css.properties.css.CssWrapInside;
import org.w3c.css.properties.css.CssWritingMode;
import org.w3c.css.properties.css.counterstyle.CssAdditiveSymbols;
import org.w3c.css.properties.css.counterstyle.CssFallback;
import org.w3c.css.properties.css.counterstyle.CssNegative;
import org.w3c.css.properties.css.counterstyle.CssPad;
import org.w3c.css.properties.css.counterstyle.CssPrefix;
import org.w3c.css.properties.css.counterstyle.CssRange;
import org.w3c.css.properties.css.counterstyle.CssSuffix;
import org.w3c.css.properties.css.counterstyle.CssSymbols;
import org.w3c.css.properties.css.counterstyle.CssSystem;
import org.w3c.css.properties.css.fontface.CssAscentOverride;
import org.w3c.css.properties.css.fontface.CssDescentOverride;
import org.w3c.css.properties.css.fontface.CssFontDisplay;
import org.w3c.css.properties.css.fontface.CssFontNamedInstance;
import org.w3c.css.properties.css.fontface.CssFontStretch;
import org.w3c.css.properties.css.fontface.CssFontStyle;
import org.w3c.css.properties.css.fontface.CssFontWeight;
import org.w3c.css.properties.css.fontface.CssLineGapOverride;
import org.w3c.css.properties.css.fontface.CssUnicodeRange;
import org.w3c.css.properties.css.viewport.CssMaxZoom;
import org.w3c.css.properties.css.viewport.CssMinZoom;
import org.w3c.css.properties.css.viewport.CssOrientation;
import org.w3c.css.properties.css.viewport.CssUserZoom;
import org.w3c.css.properties.css.viewport.CssZoom;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.Util;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;

public class Css3Style extends ATSCStyle {

    public org.w3c.css.properties.css.counterstyle.CssSpeakAs counterStyleCssSpeakAs;
    public CssSystem counterStyleCssSystem;
    public CssFallback counterStyleCssFallback;
    public CssSymbols counterStyleCssSymbols;
    public CssPrefix counterStyleCssPrefix;
    public CssSuffix counterStyleCssSuffix;
    public CssRange counterStyleCssRange;
    public CssPad counterStyleCssPad;
    public CssAdditiveSymbols counterStyleCssAdditiveSymbols;
    public CssNegative counterStyleCssNegative;

    public org.w3c.css.properties.css.page.CssMarks pageCssMarks;

    public org.w3c.css.properties.css.viewport.CssMinWidth viewportCssMinWidth;
    public org.w3c.css.properties.css.viewport.CssMaxWidth viewportCssMaxWidth;
    public org.w3c.css.properties.css.viewport.CssMinHeight viewportCssMinHeight;
    public org.w3c.css.properties.css.viewport.CssMaxHeight viewportCssMaxHeight;
    public CssZoom viewportCssZoom;
    public CssMinZoom viewportCssMinZoom;
    public CssMaxZoom viewportCssMaxZoom;
    public CssUserZoom viewportCssUserZoom;
    public CssOrientation viewportCssOrientation;
    public org.w3c.css.properties.css.viewport.CssHeight viewportCssHeight;
    public org.w3c.css.properties.css.viewport.CssWidth viewportCssWidth;

    public CssBackgroundPositionX cssBackgroundPositionX;
    public CssBackgroundPositionY cssBackgroundPositionY;

    public CssWritingMode cssWritingMode;
    public CssTouchAction cssTouchAction;
    public CssAppearance cssAppearance;
    public CssUserSelect cssUserSelect;
    public CssScrollBehavior cssScrollBehavior;

    public CssScrollMarginBlockStart cssScrollMarginBlockStart;
    public CssScrollMarginBlockEnd cssScrollMarginBlockEnd;
    public CssScrollMarginInlineStart cssScrollMarginInlineStart;
    public CssScrollMarginInlineEnd cssScrollMarginInlineEnd;
    public CssScrollMarginBlock cssScrollMarginBlock;
    public CssScrollMarginInline cssScrollMarginInline;
    public CssScrollMarginBottom cssScrollMarginBottom;
    public CssScrollMarginLeft cssScrollMarginLeft;
    public CssScrollMarginRight cssScrollMarginRight;
    public CssScrollMarginTop cssScrollMarginTop;
    public CssScrollMargin cssScrollMargin;
    public CssScrollPaddingInlineStart cssScrollPaddingInlineStart;
    public CssScrollPaddingInlineEnd cssScrollPaddingInlineEnd;
    public CssScrollPaddingInline cssScrollPaddingInline;
    public CssScrollPaddingBlockStart cssScrollPaddingBlockStart;
    public CssScrollPaddingBlockEnd cssScrollPaddingBlockEnd;
    public CssScrollPaddingBlock cssScrollPaddingBlock;
    public CssScrollPadding cssScrollPadding;
    public CssScrollPaddingBottom cssScrollPaddingBottom;
    public CssScrollPaddingLeft cssScrollPaddingLeft;
    public CssScrollPaddingRight cssScrollPaddingRight;
    public CssScrollPaddingTop cssScrollPaddingTop;
    public CssScrollSnapStop cssScrollSnapStop;
    public CssScrollSnapAlign cssScrollSnapAlign;
    public CssScrollSnapType cssScrollSnapType;

    public CssMarkerSide cssMarkerSide;
    public CssCounterSet cssCounterSet;

    public CssGridRowGap cssGridRowGap;
    public CssGridColumnGap cssGridColumnGap;
    public CssGridGap cssGridGap;
    public CssGridRowStart cssGridRowStart;
    public CssGridRowEnd cssGridRowEnd;
    public CssGridColumnStart cssGridColumnStart;
    public CssGridColumnEnd cssGridColumnEnd;
    public CssGridColumn cssGridColumn;
    public CssGridRow cssGridRow;
    public CssGridArea cssGridArea;
    public CssGridAutoFlow cssGridAutoFlow;
    public CssGridTemplateAreas cssGridTemplateAreas;
    public CssGridAutoRows cssGridAutoRows;
    public CssGridAutoColumns cssGridAutoColumns;
    public CssGridTemplateRows cssGridTemplateRows;
    public CssGridTemplateColumns cssGridTemplateColumns;
    public CssGridTemplate cssGridTemplate;
    public CssGrid cssGrid;

    public CssWillChange cssWillChange;

    public CssContain cssContain;

    public CssMixBlendMode cssMixBlendMode;
    public CssIsolation cssIsolation;
    public CssBackgroundBlendMode cssBackgroundBlendMode;

    public CssVoiceBalance cssVoiceBalance;
    public CssVoiceDuration cssVoiceDuration;
    public CssVoiceRate cssVoiceRate;
    public CssVoiceStress cssVoiceStress;
    public CssVoiceVolume cssVoiceVolume;
    public CssRestAfter cssRestAfter;
    public CssRestBefore cssRestBefore;
    public CssRest cssRest;
    public CssSpeakAs cssSpeakAs;
    public CssVoicePitch cssVoicePitch;
    public CssVoiceRange cssVoiceRange;

    public CssBoxSuppress cssBoxSuppress;

    public CssRubyPosition cssRubyPosition;
    public CssRubyAlign cssRubyAlign;
    public CssRubyMerge cssRubyMerge;
    public org.w3c.css.properties.css.CssRubyOverhang cssRubyOverhang;

    public CssAlignmentBaseline cssAlignmentBaseline;
    public CssBaselineShift cssBaselineShift;
    public CssDominantBaseline cssDominantBaseline;
    public CssInitialLetter cssInitialLetter;
    public CssInitialLetterAlign cssInitialLetterAlign;
    public CssInitialLetterWrap cssInitialLetterWrap;
    public CssInlineSizing cssInlineSizing;
    public CssBaselineSource cssBaselineSource;

    public CssOpacity cssOpacity;
    public CssBackgroundClip cssBackgroundClip;
    public CssBackgroundSize cssBackgroundSize;
    public CssBackgroundOrigin cssBackgroundOrigin;
    public CssColumns cssColumns;
    public CssColumnCount cssColumnCount;
    public CssColumnFill cssColumnFill;
    public CssColumnGap cssColumnGap;
    public CssColumnRule cssColumnRule;
    public CssColumnRuleColor cssColumnRuleColor;
    public CssColumnRuleStyle cssColumnRuleStyle;
    public CssColumnRuleWidth cssColumnRuleWidth;
    public CssColumnSpan cssColumnSpan;
    public CssColumnWidth cssColumnWidth;
    public CssBreakAfter cssBreakAfter;
    public CssBreakBefore cssBreakBefore;
    public CssBreakInside cssBreakInside;
    public CssBoxShadow cssBoxShadow;
    public CssBoxDecorationBreak cssBoxDecorationBreak;
    public CssFontKerning cssFontKerning;
    public CssFontLanguageOverride cssFontLanguageOverride;
    public CssFontVariantCaps cssFontVariantCaps;
    public CssFontVariantPosition cssFontVariantPosition;
    public CssFontSynthesis cssFontSynthesis;
    public CssFontVariantEastAsian cssFontVariantEastAsian;
    public CssFontVariantLigatures cssFontVariantLigatures;
    public CssFontVariantNumeric cssFontVariantNumeric;
    public CssFontFeatureSettings cssFontFeatureSettings;
    public CssFontVariantAlternates cssFontVariantAlternates;
    public CssFontSynthesisSmallCaps cssFontSynthesisSmallCaps;
    public CssFontSynthesisStyle cssFontSynthesisStyle;
    public CssFontSynthesisWeight cssFontSynthesisWeight;
    public CssFontVariantEmoji cssFontVariantEmoji;
    public CssFontOpticalSizing cssFontOpticalSizing;
    public CssFontPalette cssFontPalette;
    public CssFontVariationSettings cssFontVariationSettings;

    public CssOverflowWrap cssOverflowWrap;
    public CssWordBreak cssWordBreak;
    public CssHyphens cssHyphens;
    public CssLineBreak cssLineBreak;
    public CssTextAlignAll cssTextAlignAll;
    public CssTextAlignLast cssTextAlignLast;
    public CssTextJustify cssTextJustify;
    public CssTextDecorationColor cssTextDecorationColor;
    public CssTextDecorationLine cssTextDecorationLine;
    public CssTextDecorationSkip cssTextDecorationSkip;
    public CssTextDecorationStyle cssTextDecorationStyle;
    public CssTextEmphasis cssTextEmphasis;
    public CssTextEmphasisColor cssTextEmphasisColor;
    public CssTextEmphasisPosition cssTextEmphasisPosition;
    public CssTextEmphasisStyle cssTextEmphasisStyle;
    public CssTextSizeAdjust cssTextSizeAdjust;
    public CssTextUnderlinePosition cssTextUnderlinePosition;
    public CssHangingPunctuation cssHangingPunctuation;
    public CssTabSize cssTabSize;

    public CssMarqueeDirection cssMarqueeDirection;
    public CssMarqueeSpeed cssMarqueeSpeed;
    public CssMarqueeStyle cssMarqueeStyle;
    public CssMarqueePlayCount cssMarqueePlayCount;
    public CssOverflowStyle cssOverflowStyle;

    public CssTransition cssTransition;
    public CssTransitionDelay cssTransitionDelay;
    public CssTransitionDuration cssTransitionDuration;
    public CssTransitionProperty cssTransitionProperty;
    public CssTransitionTimingFunction cssTransitionTimingFunction;

    public CssAnimation cssAnimation;
    public CssAnimationDelay cssAnimationDelay;
    public CssAnimationDirection cssAnimationDirection;
    public CssAnimationDuration cssAnimationDuration;
    public CssAnimationFillMode cssAnimationFillMode;
    public CssAnimationIterationCount cssAnimationIterationCount;
    public CssAnimationName cssAnimationName;
    public CssAnimationPlayState cssAnimationPlayState;
    public CssAnimationTimingFunction cssAnimationTimingFunction;

    public CssAlignContent cssAlignContent;
    public CssAlignItems cssAlignItems;
    public CssAlignSelf cssAlignSelf;
    public CssFlex cssFlex;
    public CssFlexBasis cssFlexBasis;
    public CssFlexDirection cssFlexDirection;
    public CssFlexWrap cssFlexWrap;
    public CssFlexFlow cssFlexFlow;
    public CssFlexGrow cssFlexGrow;
    public CssFlexShrink cssFlexShrink;
    public CssJustifyContent cssJustifyContent;
    public CssOrder cssOrder;

    public CssTransformStyle cssTransformStyle;
    public CssBackfaceVisibility cssBackfaceVisibility;
    public CssPerspective cssPerspective;
    public CssPerspectiveOrigin cssPerspectiveOrigin;
    public CssTransformOrigin cssTransformOrigin;
    public CssTransform cssTransform;
    public CssTransformBox cssTransformBox;
    public CssScale cssScale;

    public CssBoxSizing cssBoxSizing;
    public CssResize cssResize;
    public CssOutlineOffset cssOutlineOffset;
    public CssImeMode cssImeMode;
    public CssNavUp cssNavUp;
    public CssNavRight cssNavRight;
    public CssNavDown cssNavDown;
    public CssNavLeft cssNavLeft;
    public CssTextOverflow cssTextOverflow;
    public CssIcon cssIcon;
    public CssCaretColor cssCaretColor;
    public CssCaretShape cssCaretShape;
    public CssCaret cssCaret;

    public CssOverflowX cssOverflowX;
    public CssOverflowY cssOverflowY;

    public CssObjectFit cssObjectFit;
    public CssObjectPosition cssObjectPosition;
    public CssImageOrientation cssImageOrientation;
    public CssImageResolution cssImageResolution;
    public CssImageRendering cssImageRendering;

    CssRubySpan cssRubySpan;

    public CssFilter cssFilter;
    public CssColorInterpolationFilters cssColorInterpolationFilters;
    public CssFloodColor cssFloodColor;
    public CssFloodOpacity cssFloodOpacity;
    public CssLightingColor cssLightingColor;
    public CssBackdropFilter cssBackdropFilter;

    public CssFloatReference cssFloatReference;
    public CssFloatOffset cssFloatOffset;
    public CssFloatDefer cssFloatDefer;

    public CssTextCombineUpright cssTextCombineUpright;
    public CssTextOrientation cssTextOrientation;

    public CssRowGap cssRowGap;
    public CssGap cssGap;
    public CssJustifySelf cssJustifySelf;
    public CssPlaceSelf cssPlaceSelf;
    public CssPlaceContent cssPlaceContent;
    public CssJustifyItems cssJustifyItems;
    public CssPlaceItems cssPlaceItems;

    public CssFontDisplay fontFaceCssFontDisplay;
    public CssFontWeight fontFaceCssFontWeight;
    public CssFontStretch fontFaceCssFontStretch;
    public CssFontStyle fontFaceCssFontStyle;
    public org.w3c.css.properties.css.fontface.CssFontLanguageOverride fontFaceCssFontLanguageOverride;
    public CssFontNamedInstance fontFaceCssFontNamedInstance;
    public CssAscentOverride fontFaceCssAscentOverride;
    public CssDescentOverride fontFaceCssDescentOverride;
    public CssLineGapOverride fontFaceCssLineGapOverride;
    public CssUnicodeRange fontFaceCssUnicodeRange;
    public org.w3c.css.properties.css.fontface.CssFontFamily fontFaceCssFontFamily;
    public org.w3c.css.properties.css.fontface.CssFontFeatureSettings fontFaceCssFontFeatureSettings;
    public org.w3c.css.properties.css.fontface.CssFontVariationSettings fontFaceCssFontVariationSettings;

    public CssColorAdjust cssColorAdjust;
    public CssForcedColorAdjust cssForcedColorAdjust;
    public CssColorScheme cssColorScheme;
    public CssPrintColorAdjust cssPrintColorAdjust;

    public CssBlockSize cssBlockSize;
    public CssInlineSize cssInlineSize;
    public CssMinBlockSize cssMinBlockSize;
    public CssMinInlineSize cssMinInlineSize;
    public CssMaxBlockSize cssMaxBlockSize;
    public CssMaxInlineSize cssMaxInlineSize;
    public CssMarginBlockStart cssMarginBlockStart;
    public CssMarginBlockEnd cssMarginBlockEnd;
    public CssMarginBlock cssMarginBlock;
    public CssMarginInlineStart cssMarginInlineStart;
    public CssMarginInlineEnd cssMarginInlineEnd;
    public CssMarginInline cssMarginInline;
    public CssInsetBlockStart cssInsetBlockStart;
    public CssInsetBlockEnd cssInsetBlockEnd;
    public CssInsetBlock cssInsetBlock;
    public CssInsetInlineStart cssInsetInlineStart;
    public CssInsetInlineEnd cssInsetInlineEnd;
    public CssInsetInline cssInsetInline;
    public CssInset cssInset;
    public CssPaddingBlockStart cssPaddingBlockStart;
    public CssPaddingBlockEnd cssPaddingBlockEnd;
    public CssPaddingBlock cssPaddingBlock;
    public CssPaddingInlineStart cssPaddingInlineStart;
    public CssPaddingInlineEnd cssPaddingInlineEnd;
    public CssPaddingInline cssPaddingInline;
    public CssBorderBlockStartWidth cssBorderBlockStartWidth;
    public CssBorderBlockEndWidth cssBorderBlockEndWidth;
    public CssBorderInlineStartWidth cssBorderInlineStartWidth;
    public CssBorderInlineEndWidth cssBorderInlineEndWidth;
    public CssBorderBlockWidth cssBorderBlockWidth;
    public CssBorderInlineWidth cssBorderInlineWidth;
    public CssBorderBlockStartStyle cssBorderBlockStartStyle;
    public CssBorderBlockEndStyle cssBorderBlockEndStyle;
    public CssBorderBlockStyle cssBorderBlockStyle;
    public CssBorderInlineStartStyle cssBorderInlineStartStyle;
    public CssBorderInlineEndStyle cssBorderInlineEndStyle;
    public CssBorderInlineStyle cssBorderInlineStyle;
    public CssBorderBlockStartColor cssBorderBlockStartColor;
    public CssBorderBlockEndColor cssBorderBlockEndColor;
    public CssBorderBlockColor cssBorderBlockColor;
    public CssBorderInlineStartColor cssBorderInlineStartColor;
    public CssBorderInlineEndColor cssBorderInlineEndColor;
    public CssBorderInlineColor cssBorderInlineColor;
    public CssBorderBlockStart cssBorderBlockStart;
    public CssBorderBlockEnd cssBorderBlockEnd;
    public CssBorderInlineStart cssBorderInlineStart;
    public CssBorderInlineEnd cssBorderInlineEnd;
    public CssBorderBlock cssBorderBlock;
    public CssBorderInline cssBorderInline;
    public CssBorderStartStartRadius cssBorderStartStartRadius;
    public CssBorderStartEndRadius cssBorderStartEndRadius;
    public CssBorderEndStartRadius cssBorderEndStartRadius;
    public CssBorderEndEndRadius cssBorderEndEndRadius;
    public CssAspectRatio cssAspectRatio;
    public CssAccentColor cssAccentColor;
    public CssOverflowAnchor cssOverflowAnchor;
    public CssTextDecorationThickness cssTextDecorationThickness;
    public CssTextUnderlineOffset cssTextUnderlineOffset;
    public CssTextDecorationSkipSelf cssTextDecorationSkipSelf;
    public CssTextDecorationSkipBox cssTextDecorationSkipBox;
    public CssTextDecorationSkipInset cssTextDecorationSkipInset;
    public CssTextDecorationSkipInk cssTextDecorationSkipInk;
    public CssTextDecorationSkipSpaces cssTextDecorationSkipSpaces;
    public CssScrollbarWidth cssScrollbarWidth;
    public CssScrollbarColor cssScrollbarColor;

    public CssOverscrollBehaviorX cssOverscrollBehaviorX;
    public CssOverscrollBehaviorY cssOverscrollBehaviorY;
    public CssOverscrollBehavior cssOverscrollBehavior;
    public CssOverscrollBehaviorBlock cssOverscrollBehaviorBlock;
    public CssOverscrollBehaviorInline cssOverscrollBehaviorInline;
    public CssContentVisibility cssContentVisibility;

    public CssWordSpaceTransform cssWordSpaceTransform;
    public CssTextWrapMode cssTextWrapMode;
    public CssTextWrapStyle cssTextWrapStyle;
    public CssWrapInside cssWrapInside;
    public CssWrapAfter cssWrapAfter;
    public CssWrapBefore cssWrapBefore;
    public CssTextWrap cssTextWrap;
    public CssWhiteSpaceCollapse cssWhiteSpaceCollapse;
    public CssWhiteSpaceTrim cssWhiteSpaceTrim;
    public CssHyphenateCharacter cssHyphenateCharacter;
    public CssHyphenateLimitZone cssHyphenateLimitZone;
    public CssHyphenateLimitChars cssHyphenateLimitChars;
    public CssHyphenateLimitLines cssHyphenateLimitLines;
    public CssHyphenateLimitLast cssHyphenateLimitLast;
    public CssTextGroupAlign cssTextGroupAlign;
    public CssLinePadding cssLinePadding;
    public CssTextAutospace cssTextAutospace;
    public CssTextSpacingTrim cssTextSpacingTrim;
    public CssTextSpacing cssTextSpacing;
    public CssTranslate cssTranslate;
    public CssRotate cssRotate;

    public CssRotate getRotate() {
        if (cssRotate == null) {
            cssRotate =
                    (CssRotate) style.CascadingOrder(new CssRotate(),
                            style, selector);
        }
        return cssRotate;
    }

    public CssTranslate getTranslate() {
        if (cssTranslate == null) {
            cssTranslate =
                    (CssTranslate) style.CascadingOrder(new CssTranslate(),
                            style, selector);
        }
        return cssTranslate;
    }
    
    public CssScale getScale() {
        if (cssScale == null) {
            cssScale =
                    (CssScale) style.CascadingOrder(new CssScale(),
                            style, selector);
        }
        return cssScale;
    }
    
    public CssTextSpacing getTextSpacing() {
        if (cssTextSpacing == null) {
            cssTextSpacing =
                    (CssTextSpacing) style.CascadingOrder(new CssTextSpacing(),
                            style, selector);
        }
        return cssTextSpacing;
    }

    public CssTextSpacingTrim getTextSpacingTrim() {
        if (cssTextSpacingTrim == null) {
            cssTextSpacingTrim =
                    (CssTextSpacingTrim) style.CascadingOrder(new CssTextSpacingTrim(),
                            style, selector);
        }
        return cssTextSpacingTrim;
    }
    
    public CssTextAutospace getTextAutospace() {
        if (cssTextAutospace == null) {
            cssTextAutospace =
                    (CssTextAutospace) style.CascadingOrder(new CssTextAutospace(),
                            style, selector);
        }
        return cssTextAutospace;
    }
    
    public CssLinePadding getLinePadding() {
        if (cssLinePadding == null) {
            cssLinePadding =
                    (CssLinePadding) style.CascadingOrder(new CssLinePadding(),
                            style, selector);
        }
        return cssLinePadding;
    }
    
    public CssTextGroupAlign getTextGroupAlign() {
        if (cssTextGroupAlign == null) {
            cssTextGroupAlign =
                    (CssTextGroupAlign) style.CascadingOrder(new CssTextGroupAlign(),
                            style, selector);
        }
        return cssTextGroupAlign;
    }

    public CssHyphenateLimitLast getHyphenateLimitLast() {
        if (cssHyphenateLimitLast == null) {
            cssHyphenateLimitLast =
                    (CssHyphenateLimitLast) style.CascadingOrder(new CssHyphenateLimitLast(),
                            style, selector);
        }
        return cssHyphenateLimitLast;
    }

    public CssHyphenateLimitLines getHyphenateLimitLines() {
        if (cssHyphenateLimitLines == null) {
            cssHyphenateLimitLines =
                    (CssHyphenateLimitLines) style.CascadingOrder(new CssHyphenateLimitLines(),
                            style, selector);
        }
        return cssHyphenateLimitLines;
    }

    public CssHyphenateLimitChars getHyphenateLimitChars() {
        if (cssHyphenateLimitChars == null) {
            cssHyphenateLimitChars =
                    (CssHyphenateLimitChars) style.CascadingOrder(new CssHyphenateLimitChars(),
                            style, selector);
        }
        return cssHyphenateLimitChars;
    }

    public CssHyphenateLimitZone getHyphenateLimitZone() {
        if (cssHyphenateLimitZone == null) {
            cssHyphenateLimitZone =
                    (CssHyphenateLimitZone) style.CascadingOrder(new CssHyphenateLimitZone(),
                            style, selector);
        }
        return cssHyphenateLimitZone;
    }

    public CssHyphenateCharacter getHyphenateCharacter() {
        if (cssHyphenateCharacter == null) {
            cssHyphenateCharacter =
                    (CssHyphenateCharacter) style.CascadingOrder(new CssHyphenateCharacter(),
                            style, selector);
        }
        return cssHyphenateCharacter;
    }

    public CssWhiteSpaceTrim getWhiteSpaceTrim() {
        if (cssWhiteSpaceTrim == null) {
            cssWhiteSpaceTrim =
                    (CssWhiteSpaceTrim) style.CascadingOrder(new CssWhiteSpaceTrim(),
                            style, selector);
        }
        return cssWhiteSpaceTrim;
    }

    public CssWhiteSpaceCollapse getWhiteSpaceCollapse() {
        if (cssWhiteSpaceCollapse == null) {
            cssWhiteSpaceCollapse =
                    (CssWhiteSpaceCollapse) style.CascadingOrder(new CssWhiteSpaceCollapse(),
                            style, selector);
        }
        return cssWhiteSpaceCollapse;
    }

    public CssWrapBefore getWrapBefore() {
        if (cssWrapBefore == null) {
            cssWrapBefore =
                    (CssWrapBefore) style.CascadingOrder(new CssWrapBefore(),
                            style, selector);
        }
        return cssWrapBefore;
    }

    public CssWrapAfter getWrapAfter() {
        if (cssWrapAfter == null) {
            cssWrapAfter =
                    (CssWrapAfter) style.CascadingOrder(new CssWrapAfter(),
                            style, selector);
        }
        return cssWrapAfter;
    }

    public CssWrapInside getWrapInside() {
        if (cssWrapInside == null) {
            cssWrapInside =
                    (CssWrapInside) style.CascadingOrder(new CssWrapInside(),
                            style, selector);
        }
        return cssWrapInside;
    }

    public CssTextWrap getTextWrap() {
        if (cssTextWrap == null) {
            cssTextWrap =
                    (CssTextWrap) style.CascadingOrder(new CssTextWrap(),
                            style, selector);
        }
        return cssTextWrap;
    }

    public CssTextWrapStyle getTextWrapStyle() {
        if (cssTextWrapStyle == null) {
            cssTextWrapStyle =
                    (CssTextWrapStyle) style.CascadingOrder(new CssTextWrapStyle(),
                            style, selector);
        }
        return cssTextWrapStyle;
    }

    public CssTextWrapMode getTextWrapMode() {
        if (cssTextWrapMode == null) {
            cssTextWrapMode =
                    (CssTextWrapMode) style.CascadingOrder(new CssTextWrapMode(),
                            style, selector);
        }
        return cssTextWrapMode;
    }

    public CssWordSpaceTransform getWordSpaceTransform() {
        if (cssWordSpaceTransform == null) {
            cssWordSpaceTransform =
                    (CssWordSpaceTransform) style.CascadingOrder(new CssWordSpaceTransform(),
                            style, selector);
        }
        return cssWordSpaceTransform;
    }

    public CssContentVisibility getContentVisibility() {
        if (cssContentVisibility == null) {
            cssContentVisibility =
                    (CssContentVisibility) style.CascadingOrder(new CssContentVisibility(),
                            style, selector);
        }
        return cssContentVisibility;
    }

    public CssOverscrollBehavior getOverscrollBehavior() {
        if (cssOverscrollBehavior == null) {
            cssOverscrollBehavior =
                    (CssOverscrollBehavior) style.CascadingOrder(new CssOverscrollBehavior(),
                            style, selector);
        }
        return cssOverscrollBehavior;
    }

    public CssOverscrollBehaviorX getOverscrollBehaviorX() {
        if (cssOverscrollBehaviorX == null) {
            cssOverscrollBehaviorX =
                    (CssOverscrollBehaviorX) style.CascadingOrder(new CssOverscrollBehaviorX(),
                            style, selector);
        }
        return cssOverscrollBehaviorX;
    }

    public CssOverscrollBehaviorY getOverscrollBehaviorY() {
        if (cssOverscrollBehaviorY == null) {
            cssOverscrollBehaviorY =
                    (CssOverscrollBehaviorY) style.CascadingOrder(new CssOverscrollBehaviorY(),
                            style, selector);
        }
        return cssOverscrollBehaviorY;
    }

    public CssOverscrollBehaviorBlock getOverscrollBehaviorBlock() {
        if (cssOverscrollBehaviorBlock == null) {
            cssOverscrollBehaviorBlock =
                    (CssOverscrollBehaviorBlock) style.CascadingOrder(new CssOverscrollBehaviorBlock(),
                            style, selector);
        }
        return cssOverscrollBehaviorBlock;
    }

    public CssOverscrollBehaviorInline getOverscrollBehaviorInline() {
        if (cssOverscrollBehaviorInline == null) {
            cssOverscrollBehaviorInline =
                    (CssOverscrollBehaviorInline) style.CascadingOrder(new CssOverscrollBehaviorInline(),
                            style, selector);
        }
        return cssOverscrollBehaviorInline;
    }

    public CssScrollbarColor getScrollbarColor() {
        if (cssScrollbarColor == null) {
            cssScrollbarColor =
                    (CssScrollbarColor) style.CascadingOrder(new CssScrollbarColor(),
                            style, selector);
        }
        return cssScrollbarColor;
    }

    public CssScrollbarWidth getScrollbarWidth() {
        if (cssScrollbarWidth == null) {
            cssScrollbarWidth =
                    (CssScrollbarWidth) style.CascadingOrder(new CssScrollbarWidth(),
                            style, selector);
        }
        return cssScrollbarWidth;
    }

    public CssTextDecorationSkipSpaces getTextDecorationSkipSpaces() {
        if (cssTextDecorationSkipSpaces == null) {
            cssTextDecorationSkipSpaces =
                    (CssTextDecorationSkipSpaces) style.CascadingOrder(new CssTextDecorationSkipSpaces(),
                            style, selector);
        }
        return cssTextDecorationSkipSpaces;
    }

    public CssTextDecorationSkipInk getTextDecorationSkipInk() {
        if (cssTextDecorationSkipInk == null) {
            cssTextDecorationSkipInk =
                    (CssTextDecorationSkipInk) style.CascadingOrder(new CssTextDecorationSkipInk(),
                            style, selector);
        }
        return cssTextDecorationSkipInk;
    }

    public CssTextDecorationSkipInset getTextDecorationSkipInset() {
        if (cssTextDecorationSkipInset == null) {
            cssTextDecorationSkipInset =
                    (CssTextDecorationSkipInset) style.CascadingOrder(new CssTextDecorationSkipInset(),
                            style, selector);
        }
        return cssTextDecorationSkipInset;
    }

    public CssTextDecorationSkipBox getTextDecorationSkipBox() {
        if (cssTextDecorationSkipBox == null) {
            cssTextDecorationSkipBox =
                    (CssTextDecorationSkipBox) style.CascadingOrder(new CssTextDecorationSkipBox(),
                            style, selector);
        }
        return cssTextDecorationSkipBox;
    }

    public CssTextDecorationSkipSelf getTextDecorationSkipSelf() {
        if (cssTextDecorationSkipSelf == null) {
            cssTextDecorationSkipSelf =
                    (CssTextDecorationSkipSelf) style.CascadingOrder(new CssTextDecorationSkipSelf(),
                            style, selector);
        }
        return cssTextDecorationSkipSelf;
    }

    public CssTextUnderlineOffset getTextUnderlineOffset() {
        if (cssTextUnderlineOffset == null) {
            cssTextUnderlineOffset =
                    (CssTextUnderlineOffset) style.CascadingOrder(new CssTextUnderlineOffset(),
                            style, selector);
        }
        return cssTextUnderlineOffset;
    }

    public CssTextDecorationThickness getTextDecorationThickness() {
        if (cssTextDecorationThickness == null) {
            cssTextDecorationThickness =
                    (CssTextDecorationThickness) style.CascadingOrder(new CssTextDecorationThickness(),
                            style, selector);
        }
        return cssTextDecorationThickness;
    }

    public CssOverflowAnchor getOverflowAnchor() {
        if (cssOverflowAnchor == null) {
            cssOverflowAnchor =
                    (CssOverflowAnchor) style.CascadingOrder(new CssOverflowAnchor(),
                            style, selector);
        }
        return cssOverflowAnchor;
    }

    public CssAccentColor getAccentColor() {
        if (cssAccentColor == null) {
            cssAccentColor =
                    (CssAccentColor) style.CascadingOrder(new CssAccentColor(),
                            style, selector);
        }
        return cssAccentColor;
    }

    public CssAspectRatio getAspectRatio() {
        if (cssAspectRatio == null) {
            cssAspectRatio =
                    (CssAspectRatio) style.CascadingOrder(new CssAspectRatio(),
                            style, selector);
        }
        return cssAspectRatio;
    }

    public CssBorderEndEndRadius getBorderEndEndRadius() {
        if (cssBorderEndEndRadius == null) {
            cssBorderEndEndRadius =
                    (CssBorderEndEndRadius) style.CascadingOrder(new CssBorderEndEndRadius(),
                            style, selector);
        }
        return cssBorderEndEndRadius;
    }

    public CssBorderEndStartRadius getBorderEndStartRadius() {
        if (cssBorderEndStartRadius == null) {
            cssBorderEndStartRadius =
                    (CssBorderEndStartRadius) style.CascadingOrder(new CssBorderEndStartRadius(),
                            style, selector);
        }
        return cssBorderEndStartRadius;
    }

    public CssBorderStartEndRadius getBorderStartEndRadius() {
        if (cssBorderStartEndRadius == null) {
            cssBorderStartEndRadius =
                    (CssBorderStartEndRadius) style.CascadingOrder(new CssBorderStartEndRadius(),
                            style, selector);
        }
        return cssBorderStartEndRadius;
    }

    public CssBorderStartStartRadius getBorderStartStartRadius() {
        if (cssBorderStartStartRadius == null) {
            cssBorderStartStartRadius =
                    (CssBorderStartStartRadius) style.CascadingOrder(new CssBorderStartStartRadius(),
                            style, selector);
        }
        return cssBorderStartStartRadius;
    }

    public CssBorderInline getBorderInline() {
        if (cssBorderInline == null) {
            cssBorderInline =
                    (CssBorderInline) style.CascadingOrder(new CssBorderInline(),
                            style, selector);
        }
        return cssBorderInline;
    }

    public CssBorderBlock getBorderBlock() {
        if (cssBorderBlock == null) {
            cssBorderBlock =
                    (CssBorderBlock) style.CascadingOrder(new CssBorderBlock(),
                            style, selector);
        }
        return cssBorderBlock;
    }

    public CssBorderInlineEnd getBorderInlineEnd() {
        if (cssBorderInlineEnd == null) {
            cssBorderInlineEnd =
                    (CssBorderInlineEnd) style.CascadingOrder(new CssBorderInlineEnd(),
                            style, selector);
        }
        return cssBorderInlineEnd;
    }

    public CssBorderInlineStart getBorderInlineStart() {
        if (cssBorderInlineStart == null) {
            cssBorderInlineStart =
                    (CssBorderInlineStart) style.CascadingOrder(new CssBorderInlineStart(),
                            style, selector);
        }
        return cssBorderInlineStart;
    }

    public CssBorderBlockEnd getBorderBlockEnd() {
        if (cssBorderBlockEnd == null) {
            cssBorderBlockEnd =
                    (CssBorderBlockEnd) style.CascadingOrder(new CssBorderBlockEnd(),
                            style, selector);
        }
        return cssBorderBlockEnd;
    }

    public CssBorderBlockStart getBorderBlockStart() {
        if (cssBorderBlockStart == null) {
            cssBorderBlockStart =
                    (CssBorderBlockStart) style.CascadingOrder(new CssBorderBlockStart(),
                            style, selector);
        }
        return cssBorderBlockStart;
    }

    public CssBorderInlineColor getBorderInlineColor() {
        if (cssBorderInlineColor == null) {
            cssBorderInlineColor =
                    (CssBorderInlineColor) style.CascadingOrder(new CssBorderInlineColor(),
                            style, selector);
        }
        return cssBorderInlineColor;
    }

    public CssBorderInlineEndColor getBorderInlineEndColor() {
        if (cssBorderInlineEndColor == null) {
            cssBorderInlineEndColor =
                    (CssBorderInlineEndColor) style.CascadingOrder(new CssBorderInlineEndColor(),
                            style, selector);
        }
        return cssBorderInlineEndColor;
    }

    public CssBorderInlineStartColor getBorderInlineStartColor() {
        if (cssBorderInlineStartColor == null) {
            cssBorderInlineStartColor =
                    (CssBorderInlineStartColor) style.CascadingOrder(new CssBorderInlineStartColor(),
                            style, selector);
        }
        return cssBorderInlineStartColor;
    }

    public CssBorderBlockColor getBorderBlockColor() {
        if (cssBorderBlockColor == null) {
            cssBorderBlockColor =
                    (CssBorderBlockColor) style.CascadingOrder(new CssBorderBlockColor(),
                            style, selector);
        }
        return cssBorderBlockColor;
    }

    public CssBorderBlockEndColor getBorderBlockEndColor() {
        if (cssBorderBlockEndColor == null) {
            cssBorderBlockEndColor =
                    (CssBorderBlockEndColor) style.CascadingOrder(new CssBorderBlockEndColor(),
                            style, selector);
        }
        return cssBorderBlockEndColor;
    }

    public CssBorderBlockStartColor getBorderBlockStartColor() {
        if (cssBorderBlockStartColor == null) {
            cssBorderBlockStartColor =
                    (CssBorderBlockStartColor) style.CascadingOrder(new CssBorderBlockStartColor(),
                            style, selector);
        }
        return cssBorderBlockStartColor;
    }

    public CssBorderInlineStyle getBorderInlineStyle() {
        if (cssBorderInlineStyle == null) {
            cssBorderInlineStyle =
                    (CssBorderInlineStyle) style.CascadingOrder(new CssBorderInlineStyle(),
                            style, selector);
        }
        return cssBorderInlineStyle;
    }

    public CssBorderInlineEndStyle getBorderInlineEndStyle() {
        if (cssBorderInlineEndStyle == null) {
            cssBorderInlineEndStyle =
                    (CssBorderInlineEndStyle) style.CascadingOrder(new CssBorderInlineEndStyle(),
                            style, selector);
        }
        return cssBorderInlineEndStyle;
    }

    public CssBorderInlineStartStyle getBorderInlineStartStyle() {
        if (cssBorderInlineStartStyle == null) {
            cssBorderInlineStartStyle =
                    (CssBorderInlineStartStyle) style.CascadingOrder(new CssBorderInlineStartStyle(),
                            style, selector);
        }
        return cssBorderInlineStartStyle;
    }

    public CssBorderBlockStyle getBorderBlockStyle() {
        if (cssBorderBlockStyle == null) {
            cssBorderBlockStyle =
                    (CssBorderBlockStyle) style.CascadingOrder(new CssBorderBlockStyle(),
                            style, selector);
        }
        return cssBorderBlockStyle;
    }

    public CssBorderBlockEndStyle getBorderBlockEndStyle() {
        if (cssBorderBlockEndStyle == null) {
            cssBorderBlockEndStyle =
                    (CssBorderBlockEndStyle) style.CascadingOrder(new CssBorderBlockEndStyle(),
                            style, selector);
        }
        return cssBorderBlockEndStyle;
    }

    public CssBorderBlockStartStyle getBorderBlockStartStyle() {
        if (cssBorderBlockStartStyle == null) {
            cssBorderBlockStartStyle =
                    (CssBorderBlockStartStyle) style.CascadingOrder(new CssBorderBlockStartStyle(),
                            style, selector);
        }
        return cssBorderBlockStartStyle;
    }

    public CssBorderInlineWidth getBorderInlineWidth() {
        if (cssBorderInlineWidth == null) {
            cssBorderInlineWidth =
                    (CssBorderInlineWidth) style.CascadingOrder(new CssBorderInlineWidth(),
                            style, selector);
        }
        return cssBorderInlineWidth;
    }

    public CssBorderBlockWidth getBorderBlockWidth() {
        if (cssBorderBlockWidth == null) {
            cssBorderBlockWidth =
                    (CssBorderBlockWidth) style.CascadingOrder(new CssBorderBlockWidth(),
                            style, selector);
        }
        return cssBorderBlockWidth;
    }

    public CssBorderInlineEndWidth getBorderInlineEndWidth() {
        if (cssBorderInlineEndWidth == null) {
            cssBorderInlineEndWidth =
                    (CssBorderInlineEndWidth) style.CascadingOrder(new CssBorderInlineEndWidth(),
                            style, selector);
        }
        return cssBorderInlineEndWidth;
    }

    public CssBorderInlineStartWidth getBorderInlineStartWidth() {
        if (cssBorderInlineStartWidth == null) {
            cssBorderInlineStartWidth =
                    (CssBorderInlineStartWidth) style.CascadingOrder(new CssBorderInlineStartWidth(),
                            style, selector);
        }
        return cssBorderInlineStartWidth;
    }

    public CssBorderBlockEndWidth getBorderBlockEndWidth() {
        if (cssBorderBlockEndWidth == null) {
            cssBorderBlockEndWidth =
                    (CssBorderBlockEndWidth) style.CascadingOrder(new CssBorderBlockEndWidth(),
                            style, selector);
        }
        return cssBorderBlockEndWidth;
    }

    public CssBorderBlockStartWidth getBorderBlockStartWidth() {
        if (cssBorderBlockStartWidth == null) {
            cssBorderBlockStartWidth =
                    (CssBorderBlockStartWidth) style.CascadingOrder(new CssBorderBlockStartWidth(),
                            style, selector);
        }
        return cssBorderBlockStartWidth;
    }

    public CssPaddingInline getPaddingInline() {
        if (cssPaddingInline == null) {
            cssPaddingInline =
                    (CssPaddingInline) style.CascadingOrder(new CssPaddingInline(),
                            style, selector);
        }
        return cssPaddingInline;
    }

    public CssPaddingInlineEnd getPaddingInlineEnd() {
        if (cssPaddingInlineEnd == null) {
            cssPaddingInlineEnd =
                    (CssPaddingInlineEnd) style.CascadingOrder(new CssPaddingInlineEnd(),
                            style, selector);
        }
        return cssPaddingInlineEnd;
    }

    public CssPaddingInlineStart getPaddingInlineStart() {
        if (cssPaddingInlineStart == null) {
            cssPaddingInlineStart =
                    (CssPaddingInlineStart) style.CascadingOrder(new CssPaddingInlineStart(),
                            style, selector);
        }
        return cssPaddingInlineStart;
    }

    public CssPaddingBlock getPaddingBlock() {
        if (cssPaddingBlock == null) {
            cssPaddingBlock =
                    (CssPaddingBlock) style.CascadingOrder(new CssPaddingBlock(),
                            style, selector);
        }
        return cssPaddingBlock;
    }

    public CssPaddingBlockEnd getPaddingBlockEnd() {
        if (cssPaddingBlockEnd == null) {
            cssPaddingBlockEnd =
                    (CssPaddingBlockEnd) style.CascadingOrder(new CssPaddingBlockEnd(),
                            style, selector);
        }
        return cssPaddingBlockEnd;
    }

    public CssPaddingBlockStart getPaddingBlockStart() {
        if (cssPaddingBlockStart == null) {
            cssPaddingBlockStart =
                    (CssPaddingBlockStart) style.CascadingOrder(new CssPaddingBlockStart(),
                            style, selector);
        }
        return cssPaddingBlockStart;
    }

    public CssInset getInset() {
        if (cssInset == null) {
            cssInset =
                    (CssInset) style.CascadingOrder(new CssInset(),
                            style, selector);
        }
        return cssInset;
    }

    public CssInsetInline getInsetInline() {
        if (cssInsetInline == null) {
            cssInsetInline =
                    (CssInsetInline) style.CascadingOrder(new CssInsetInline(),
                            style, selector);
        }
        return cssInsetInline;
    }

    public CssInsetInlineEnd getInsetInlineEnd() {
        if (cssInsetInlineEnd == null) {
            cssInsetInlineEnd =
                    (CssInsetInlineEnd) style.CascadingOrder(new CssInsetInlineEnd(),
                            style, selector);
        }
        return cssInsetInlineEnd;
    }

    public CssInsetInlineStart getInsetInlineStart() {
        if (cssInsetInlineStart == null) {
            cssInsetInlineStart =
                    (CssInsetInlineStart) style.CascadingOrder(new CssInsetInlineStart(),
                            style, selector);
        }
        return cssInsetInlineStart;
    }

    public CssInsetBlock getInsetBlock() {
        if (cssInsetBlock == null) {
            cssInsetBlock =
                    (CssInsetBlock) style.CascadingOrder(new CssInsetBlock(),
                            style, selector);
        }
        return cssInsetBlock;
    }

    public CssInsetBlockEnd getInsetBlockEnd() {
        if (cssInsetBlockEnd == null) {
            cssInsetBlockEnd =
                    (CssInsetBlockEnd) style.CascadingOrder(new CssInsetBlockEnd(),
                            style, selector);
        }
        return cssInsetBlockEnd;
    }

    public CssInsetBlockStart getInsetBlockStart() {
        if (cssInsetBlockStart == null) {
            cssInsetBlockStart =
                    (CssInsetBlockStart) style.CascadingOrder(new CssInsetBlockStart(),
                            style, selector);
        }
        return cssInsetBlockStart;
    }

    public CssMarginInline getMarginInline() {
        if (cssMarginInline == null) {
            cssMarginInline =
                    (CssMarginInline) style.CascadingOrder(new CssMarginInline(),
                            style, selector);
        }
        return cssMarginInline;
    }

    public CssMarginInlineEnd getMarginInlineEnd() {
        if (cssMarginInlineEnd == null) {
            cssMarginInlineEnd =
                    (CssMarginInlineEnd) style.CascadingOrder(new CssMarginInlineEnd(),
                            style, selector);
        }
        return cssMarginInlineEnd;
    }

    public CssMarginInlineStart getMarginInlineStart() {
        if (cssMarginInlineStart == null) {
            cssMarginInlineStart =
                    (CssMarginInlineStart) style.CascadingOrder(new CssMarginInlineStart(),
                            style, selector);
        }
        return cssMarginInlineStart;
    }

    public CssMarginBlock getMarginBlock() {
        if (cssMarginBlock == null) {
            cssMarginBlock =
                    (CssMarginBlock) style.CascadingOrder(new CssMarginBlock(),
                            style, selector);
        }
        return cssMarginBlock;
    }

    public CssMarginBlockEnd getMarginBlockEnd() {
        if (cssMarginBlockEnd == null) {
            cssMarginBlockEnd =
                    (CssMarginBlockEnd) style.CascadingOrder(new CssMarginBlockEnd(),
                            style, selector);
        }
        return cssMarginBlockEnd;
    }

    public CssMarginBlockStart getMarginBlockStart() {
        if (cssMarginBlockStart == null) {
            cssMarginBlockStart =
                    (CssMarginBlockStart) style.CascadingOrder(new CssMarginBlockStart(),
                            style, selector);
        }
        return cssMarginBlockStart;
    }

    public CssMaxInlineSize getMaxInlineSize() {
        if (cssMaxInlineSize == null) {
            cssMaxInlineSize =
                    (CssMaxInlineSize) style.CascadingOrder(new CssMaxInlineSize(),
                            style, selector);
        }
        return cssMaxInlineSize;
    }

    public CssMaxBlockSize getMaxBlockSize() {
        if (cssMaxBlockSize == null) {
            cssMaxBlockSize =
                    (CssMaxBlockSize) style.CascadingOrder(new CssMaxBlockSize(),
                            style, selector);
        }
        return cssMaxBlockSize;
    }

    public CssMinInlineSize getMinInlineSize() {
        if (cssMinInlineSize == null) {
            cssMinInlineSize =
                    (CssMinInlineSize) style.CascadingOrder(new CssMinInlineSize(),
                            style, selector);
        }
        return cssMinInlineSize;
    }

    public CssMinBlockSize getMinBlockSize() {
        if (cssMinBlockSize == null) {
            cssMinBlockSize =
                    (CssMinBlockSize) style.CascadingOrder(new CssMinBlockSize(),
                            style, selector);
        }
        return cssMinBlockSize;
    }

    public CssInlineSize getInlineSize() {
        if (cssInlineSize == null) {
            cssInlineSize =
                    (CssInlineSize) style.CascadingOrder(new CssInlineSize(),
                            style, selector);
        }
        return cssInlineSize;
    }

    public CssBlockSize getBlockSize() {
        if (cssBlockSize == null) {
            cssBlockSize =
                    (CssBlockSize) style.CascadingOrder(new CssBlockSize(),
                            style, selector);
        }
        return cssBlockSize;
    }

    public CssColorScheme getColorScheme() {
        if (cssColorScheme == null) {
            cssColorScheme =
                    (CssColorScheme) style.CascadingOrder(new CssColorScheme(),
                            style, selector);
        }
        return cssColorScheme;
    }

    public CssPrintColorAdjust getPrintColorAdjust() {
        if (cssPrintColorAdjust == null) {
            cssPrintColorAdjust =
                    (CssPrintColorAdjust) style.CascadingOrder(new CssPrintColorAdjust(),
                            style, selector);
        }
        return cssPrintColorAdjust;
    }

    public CssForcedColorAdjust getForcedColorAdjust() {
        if (cssForcedColorAdjust == null) {
            cssForcedColorAdjust =
                    (CssForcedColorAdjust) style.CascadingOrder(new CssForcedColorAdjust(),
                            style, selector);
        }
        return cssForcedColorAdjust;
    }

    public CssColorAdjust getColorAdjust() {
        if (cssColorAdjust == null) {
            cssColorAdjust =
                    (CssColorAdjust) style.CascadingOrder(new CssColorAdjust(),
                            style, selector);
        }
        return cssColorAdjust;
    }

    public org.w3c.css.properties.css.page.CssMarks getPageCssMarks() {
        if (pageCssMarks == null) {
            pageCssMarks =
                    (org.w3c.css.properties.css.page.CssMarks) style.CascadingOrder(new org.w3c.css.properties.css.page.CssMarks(),
                            style, selector);
        }
        return pageCssMarks;
    }

    public org.w3c.css.properties.css.fontface.CssFontFamily getFontFaceCssFontFamily() {
        if (fontFaceCssFontFamily == null) {
            fontFaceCssFontFamily =
                    (org.w3c.css.properties.css.fontface.CssFontFamily) style.CascadingOrder(new org.w3c.css.properties.css.fontface.CssFontFamily(),
                            style, selector);
        }
        return fontFaceCssFontFamily;
    }

    public org.w3c.css.properties.css.fontface.CssFontFeatureSettings getFontFaceCssFontFeatureSettings() {
        if (fontFaceCssFontFeatureSettings == null) {
            fontFaceCssFontFeatureSettings =
                    (org.w3c.css.properties.css.fontface.CssFontFeatureSettings) style.CascadingOrder(new org.w3c.css.properties.css.fontface.CssFontFeatureSettings(),
                            style, selector);
        }
        return fontFaceCssFontFeatureSettings;
    }

    public org.w3c.css.properties.css.fontface.CssFontVariationSettings getFontFaceCssFontVariationSettings() {
        if (fontFaceCssFontVariationSettings == null) {
            fontFaceCssFontVariationSettings =
                    (org.w3c.css.properties.css.fontface.CssFontVariationSettings) style.CascadingOrder(new org.w3c.css.properties.css.fontface.CssFontVariationSettings(),
                            style, selector);
        }
        return fontFaceCssFontVariationSettings;
    }

    public CssUnicodeRange getFontFaceCssUnicodeRange() {
        if (fontFaceCssUnicodeRange == null) {
            fontFaceCssUnicodeRange =
                    (CssUnicodeRange) style.CascadingOrder(new CssUnicodeRange(),
                            style, selector);
        }
        return fontFaceCssUnicodeRange;
    }

    public org.w3c.css.properties.css.fontface.CssFontLanguageOverride getFontFaceCssFontLanguageOverride() {
        if (fontFaceCssFontLanguageOverride == null) {
            fontFaceCssFontLanguageOverride =
                    (org.w3c.css.properties.css.fontface.CssFontLanguageOverride) style.CascadingOrder(new org.w3c.css.properties.css.fontface.CssFontLanguageOverride(),
                            style, selector);
        }
        return fontFaceCssFontLanguageOverride;
    }

    public CssAscentOverride getFontFaceCssAscentOverride() {
        if (fontFaceCssAscentOverride == null) {
            fontFaceCssAscentOverride =
                    (CssAscentOverride) style.CascadingOrder(new CssAscentOverride(),
                            style, selector);
        }
        return fontFaceCssAscentOverride;
    }

    public CssDescentOverride getFontFaceCssDescentOverride() {
        if (fontFaceCssDescentOverride == null) {
            fontFaceCssDescentOverride =
                    (CssDescentOverride) style.CascadingOrder(new CssDescentOverride(),
                            style, selector);
        }
        return fontFaceCssDescentOverride;
    }

    public CssLineGapOverride getFontFaceCssLineGapOverride() {
        if (fontFaceCssLineGapOverride == null) {
            fontFaceCssLineGapOverride =
                    (CssLineGapOverride) style.CascadingOrder(new CssLineGapOverride(),
                            style, selector);
        }
        return fontFaceCssLineGapOverride;
    }

    public CssFontNamedInstance getFontFaceCssFontNamedInstance() {
        if (fontFaceCssFontNamedInstance == null) {
            fontFaceCssFontNamedInstance =
                    (CssFontNamedInstance) style.CascadingOrder(new CssFontNamedInstance(),
                            style, selector);
        }
        return fontFaceCssFontNamedInstance;
    }

    public CssFontDisplay getFontFaceCssFontDisplay() {
        if (fontFaceCssFontDisplay == null) {
            fontFaceCssFontDisplay =
                    (CssFontDisplay) style.CascadingOrder(new CssFontDisplay(),
                            style, selector);
        }
        return fontFaceCssFontDisplay;
    }

    public CssFontStretch getFontFaceCssFontStretch() {
        if (fontFaceCssFontStretch == null) {
            fontFaceCssFontStretch =
                    (CssFontStretch) style.CascadingOrder(new CssFontStretch(),
                            style, selector);
        }
        return fontFaceCssFontStretch;
    }

    public CssFontStyle getFontFaceCssFontStyle() {
        if (fontFaceCssFontStyle == null) {
            fontFaceCssFontStyle =
                    (CssFontStyle) style.CascadingOrder(new CssFontStyle(),
                            style, selector);
        }
        return fontFaceCssFontStyle;
    }

    public CssFontWeight getFontFaceCssFontWeight() {
        if (fontFaceCssFontWeight == null) {
            fontFaceCssFontWeight =
                    (CssFontWeight) style.CascadingOrder(new CssFontWeight(),
                            style, selector);
        }
        return fontFaceCssFontWeight;
    }

    public CssPlaceItems getPlaceItems() {
        if (cssPlaceItems == null) {
            cssPlaceItems =
                    (CssPlaceItems) style.CascadingOrder(new CssPlaceItems(),
                            style, selector);
        }
        return cssPlaceItems;
    }

    public CssJustifyItems getJustifyItems() {
        if (cssJustifyItems == null) {
            cssJustifyItems =
                    (CssJustifyItems) style.CascadingOrder(new CssJustifyItems(),
                            style, selector);
        }
        return cssJustifyItems;
    }

    public CssPlaceContent getPlaceContent() {
        if (cssPlaceContent == null) {
            cssPlaceContent =
                    (CssPlaceContent) style.CascadingOrder(new CssPlaceContent(),
                            style, selector);
        }
        return cssPlaceContent;
    }

    public CssPlaceSelf getPlaceSelf() {
        if (cssPlaceSelf == null) {
            cssPlaceSelf =
                    (CssPlaceSelf) style.CascadingOrder(new CssPlaceSelf(),
                            style, selector);
        }
        return cssPlaceSelf;
    }

    public CssJustifySelf getJustifySelf() {
        if (cssJustifySelf == null) {
            cssJustifySelf =
                    (CssJustifySelf) style.CascadingOrder(new CssJustifySelf(),
                            style, selector);
        }
        return cssJustifySelf;
    }

    public CssGap getGap() {
        if (cssGap == null) {
            cssGap =
                    (CssGap) style.CascadingOrder(new CssGap(),
                            style, selector);
        }
        return cssGap;
    }

    public CssRowGap getRowGap() {
        if (cssRowGap == null) {
            cssRowGap =
                    (CssRowGap) style.CascadingOrder(new CssRowGap(),
                            style, selector);
        }
        return cssRowGap;
    }

    public CssTextCombineUpright getTextCombineUpright() {
        if (cssTextCombineUpright == null) {
            cssTextCombineUpright =
                    (CssTextCombineUpright) style.CascadingOrder(new CssTextCombineUpright(),
                            style, selector);
        }
        return cssTextCombineUpright;
    }

    public CssTextOrientation getTextOrientation() {
        if (cssTextOrientation == null) {
            cssTextOrientation =
                    (CssTextOrientation) style.CascadingOrder(new CssTextOrientation(),
                            style, selector);
        }
        return cssTextOrientation;
    }

    public org.w3c.css.properties.css.viewport.CssWidth getViewportWidth() {
        if (viewportCssWidth == null) {
            viewportCssWidth =
                    (org.w3c.css.properties.css.viewport.CssWidth) style.CascadingOrder(new org.w3c.css.properties.css.viewport.CssWidth(),
                            style, selector);
        }
        return viewportCssWidth;
    }

    public org.w3c.css.properties.css.viewport.CssHeight getViewportHeight() {
        if (viewportCssHeight == null) {
            viewportCssHeight =
                    (org.w3c.css.properties.css.viewport.CssHeight) style.CascadingOrder(new org.w3c.css.properties.css.viewport.CssHeight(),
                            style, selector);
        }
        return viewportCssHeight;
    }

    public CssOrientation getViewportOrientation() {
        if (viewportCssOrientation == null) {
            viewportCssOrientation =
                    (org.w3c.css.properties.css.viewport.CssOrientation) style.CascadingOrder(new CssOrientation(),
                            style, selector);
        }
        return viewportCssOrientation;
    }

    public CssUserZoom getViewportUserZoom() {
        if (viewportCssUserZoom == null) {
            viewportCssUserZoom =
                    (org.w3c.css.properties.css.viewport.CssUserZoom) style.CascadingOrder(new CssUserZoom(),
                            style, selector);
        }
        return viewportCssUserZoom;
    }

    public CssMaxZoom getViewportMaxZoom() {
        if (viewportCssMaxZoom == null) {
            viewportCssMaxZoom =
                    (org.w3c.css.properties.css.viewport.CssMaxZoom) style.CascadingOrder(new CssMaxZoom(),
                            style, selector);
        }
        return viewportCssMaxZoom;
    }

    public CssMinZoom getViewportMinZoom() {
        if (viewportCssMinZoom == null) {
            viewportCssMinZoom =
                    (org.w3c.css.properties.css.viewport.CssMinZoom) style.CascadingOrder(new CssMinZoom(),
                            style, selector);
        }
        return viewportCssMinZoom;
    }

    public CssZoom getViewportZoom() {
        if (viewportCssZoom == null) {
            viewportCssZoom =
                    (org.w3c.css.properties.css.viewport.CssZoom) style.CascadingOrder(new CssZoom(),
                            style, selector);
        }
        return viewportCssZoom;
    }

    public org.w3c.css.properties.css.viewport.CssMaxHeight getViewportMaxHeight() {
        if (viewportCssMaxHeight == null) {
            viewportCssMaxHeight =
                    (org.w3c.css.properties.css.viewport.CssMaxHeight) style.CascadingOrder(new CssMaxHeight(),
                            style, selector);
        }
        return viewportCssMaxHeight;
    }

    public org.w3c.css.properties.css.viewport.CssMinHeight getViewportMinHeight() {
        if (viewportCssMinHeight == null) {
            viewportCssMinHeight =
                    (org.w3c.css.properties.css.viewport.CssMinHeight) style.CascadingOrder(new CssMinHeight(),
                            style, selector);
        }
        return viewportCssMinHeight;
    }

    public org.w3c.css.properties.css.viewport.CssMaxWidth getViewportMaxWidth() {
        if (viewportCssMaxWidth == null) {
            viewportCssMaxWidth =
                    (org.w3c.css.properties.css.viewport.CssMaxWidth) style.CascadingOrder(new CssMaxWidth(),
                            style, selector);
        }
        return viewportCssMaxWidth;
    }

    public org.w3c.css.properties.css.viewport.CssMinWidth getViewportMinWidth() {
        if (viewportCssMinWidth == null) {
            viewportCssMinWidth =
                    (org.w3c.css.properties.css.viewport.CssMinWidth) style.CascadingOrder(new CssMinWidth(),
                            style, selector);
        }
        return viewportCssMinWidth;
    }

    public CssBackgroundPositionY getBackgroundPositionY() {
        if (cssBackgroundPositionY == null) {
            cssBackgroundPositionY =
                    (CssBackgroundPositionY) style.CascadingOrder(new CssBackgroundPositionY(),
                            style, selector);
        }
        return cssBackgroundPositionY;
    }

    public CssBackgroundPositionX getBackgroundPositionX() {
        if (cssBackgroundPositionX == null) {
            cssBackgroundPositionX =
                    (CssBackgroundPositionX) style.CascadingOrder(new CssBackgroundPositionX(),
                            style, selector);
        }
        return cssBackgroundPositionX;
    }

    public CssWritingMode getWritingMode() {
        if (cssWritingMode == null) {
            cssWritingMode =
                    (CssWritingMode) style.CascadingOrder(new CssWritingMode(),
                            style, selector);
        }
        return cssWritingMode;
    }

    public CssTouchAction getTouchAction() {
        if (cssTouchAction == null) {
            cssTouchAction =
                    (CssTouchAction) style.CascadingOrder(new CssTouchAction(),
                            style, selector);
        }
        return cssTouchAction;
    }

    public CssAppearance getAppearance() {
        if (cssAppearance == null) {
            cssAppearance =
                    (CssAppearance) style.CascadingOrder(new CssAppearance(),
                            style, selector);
        }
        return cssAppearance;
    }

    public CssUserSelect getUserSelect() {
        if (cssUserSelect == null) {
            cssUserSelect =
                    (CssUserSelect) style.CascadingOrder(new CssUserSelect(),
                            style, selector);
        }
        return cssUserSelect;
    }

    public CssScrollBehavior getScrollBehavior() {
        if (cssScrollBehavior == null) {
            cssScrollBehavior =
                    (CssScrollBehavior) style.CascadingOrder(new CssScrollBehavior(),
                            style, selector);
        }
        return cssScrollBehavior;
    }

    public org.w3c.css.properties.css.counterstyle.CssSpeakAs getCounterStyleCssSpeakAs() {
        if (counterStyleCssSpeakAs == null) {
            counterStyleCssSpeakAs = (org.w3c.css.properties.css.counterstyle.CssSpeakAs) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssSpeakAs(), style, selector);
        }
        return counterStyleCssSpeakAs;
    }

    public org.w3c.css.properties.css.counterstyle.CssSystem getCounterStyleCssSystem() {
        if (counterStyleCssSystem == null) {
            counterStyleCssSystem = (org.w3c.css.properties.css.counterstyle.CssSystem) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssSystem(), style, selector);
        }
        return counterStyleCssSystem;
    }

    public org.w3c.css.properties.css.counterstyle.CssFallback getCounterStyleCssFallback() {
        if (counterStyleCssFallback == null) {
            counterStyleCssFallback = (org.w3c.css.properties.css.counterstyle.CssFallback) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssFallback(), style, selector);
        }
        return counterStyleCssFallback;
    }

    public org.w3c.css.properties.css.counterstyle.CssSymbols getCounterStyleCssSymbols() {
        if (counterStyleCssSymbols == null) {
            counterStyleCssSymbols = (org.w3c.css.properties.css.counterstyle.CssSymbols) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssSymbols(), style, selector);
        }
        return counterStyleCssSymbols;
    }

    public org.w3c.css.properties.css.counterstyle.CssPrefix getCounterStyleCssPrefix() {
        if (counterStyleCssPrefix == null) {
            counterStyleCssPrefix = (org.w3c.css.properties.css.counterstyle.CssPrefix) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssPrefix(), style, selector);
        }
        return counterStyleCssPrefix;
    }

    public org.w3c.css.properties.css.counterstyle.CssSuffix getCounterStyleCssSuffix() {
        if (counterStyleCssSuffix == null) {
            counterStyleCssSuffix = (org.w3c.css.properties.css.counterstyle.CssSuffix) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssSuffix(), style, selector);
        }
        return counterStyleCssSuffix;
    }

    public org.w3c.css.properties.css.counterstyle.CssRange getCounterStyleCssRange() {
        if (counterStyleCssRange == null) {
            counterStyleCssRange = (org.w3c.css.properties.css.counterstyle.CssRange) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssRange(), style, selector);
        }
        return counterStyleCssRange;
    }

    public org.w3c.css.properties.css.counterstyle.CssPad getCounterStyleCssPad() {
        if (counterStyleCssPad == null) {
            counterStyleCssPad = (org.w3c.css.properties.css.counterstyle.CssPad) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssPad(), style, selector);
        }
        return counterStyleCssPad;
    }

    public org.w3c.css.properties.css.counterstyle.CssAdditiveSymbols getCounterStyleCssAdditiveSymbols() {
        if (counterStyleCssAdditiveSymbols == null) {
            counterStyleCssAdditiveSymbols = (org.w3c.css.properties.css.counterstyle.CssAdditiveSymbols) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssAdditiveSymbols(), style, selector);
        }
        return counterStyleCssAdditiveSymbols;
    }

    public org.w3c.css.properties.css.counterstyle.CssNegative getCounterStyleCssNegative() {
        if (counterStyleCssNegative == null) {
            counterStyleCssNegative = (org.w3c.css.properties.css.counterstyle.CssNegative) style.CascadingOrder(new org.w3c.css.properties.css.counterstyle.CssNegative(), style, selector);
        }
        return counterStyleCssNegative;
    }

    public CssImageRendering getImageRendering() {
        if (cssImageRendering == null) {
            cssImageRendering = (CssImageRendering) style.CascadingOrder(new CssImageRendering(), style, selector);
        }
        return cssImageRendering;
    }

    public CssBorderImageSource getBorderImageSource() {
        if (cssBorder.borderImage.source == null) {
            cssBorder.borderImage.source = (CssBorderImageSource) style.CascadingOrder(new CssBorderImageSource(), style, selector);
        }
        return cssBorder.borderImage.source;

    }

    public CssMarkerSide getMarkerSide() {
        if (cssMarkerSide == null) {
            cssMarkerSide =
                    (CssMarkerSide) style.CascadingOrder(new CssMarkerSide(),
                            style, selector);
        }
        return cssMarkerSide;
    }

    public CssCounterSet getCounterSet() {
        if (cssCounterSet == null) {
            cssCounterSet =
                    (CssCounterSet) style.CascadingOrder(new CssCounterSet(),
                            style, selector);
        }
        return cssCounterSet;
    }

    public CssGridColumnGap getGridColumnGap() {
        if (cssGridColumnGap == null) {
            cssGridColumnGap =
                    (CssGridColumnGap) style.CascadingOrder(new CssGridColumnGap(),
                            style, selector);
        }
        return cssGridColumnGap;
    }

    public CssGridGap getGridGap() {
        if (cssGridGap == null) {
            cssGridGap =
                    (CssGridGap) style.CascadingOrder(new CssGridGap(),
                            style, selector);
        }
        return cssGridGap;
    }

    public CssGridRowGap getGridRowGap() {
        if (cssGridRowGap == null) {
            cssGridRowGap =
                    (CssGridRowGap) style.CascadingOrder(new CssGridRowGap(),
                            style, selector);
        }
        return cssGridRowGap;
    }

    public CssGridRowStart getGridRowStart() {
        if (cssGridRowStart == null) {
            cssGridRowStart =
                    (CssGridRowStart) style.CascadingOrder(new CssGridRowStart(),
                            style, selector);
        }
        return cssGridRowStart;
    }

    public CssGridRowEnd getGridRowEnd() {
        if (cssGridRowEnd == null) {
            cssGridRowEnd =
                    (CssGridRowEnd) style.CascadingOrder(new CssGridRowEnd(),
                            style, selector);
        }
        return cssGridRowEnd;
    }

    public CssGridColumnStart getGridColumnStart() {
        if (cssGridColumnStart == null) {
            cssGridColumnStart =
                    (CssGridColumnStart) style.CascadingOrder(new CssGridColumnStart(),
                            style, selector);
        }
        return cssGridColumnStart;
    }

    public CssGridColumnEnd getGridColumnEnd() {
        if (cssGridColumnEnd == null) {
            cssGridColumnEnd =
                    (CssGridColumnEnd) style.CascadingOrder(new CssGridColumnEnd(),
                            style, selector);
        }
        return cssGridColumnEnd;
    }

    public CssGridColumn getGridColumn() {
        if (cssGridColumn == null) {
            cssGridColumn =
                    (CssGridColumn) style.CascadingOrder(new CssGridColumn(),
                            style, selector);
        }
        return cssGridColumn;
    }

    public CssGridRow getGridRow() {
        if (cssGridRow == null) {
            cssGridRow =
                    (CssGridRow) style.CascadingOrder(new CssGridRow(),
                            style, selector);
        }
        return cssGridRow;
    }

    public CssGridArea getGridArea() {
        if (cssGridArea == null) {
            cssGridArea =
                    (CssGridArea) style.CascadingOrder(new CssGridArea(),
                            style, selector);
        }
        return cssGridArea;
    }

    public CssGridTemplateAreas getGridTemplateAreas() {
        if (cssGridTemplateAreas == null) {
            cssGridTemplateAreas =
                    (CssGridTemplateAreas) style.CascadingOrder(new CssGridTemplateAreas(),
                            style, selector);
        }
        return cssGridTemplateAreas;
    }

    public CssGridAutoFlow getGridAutoFlow() {
        if (cssGridAutoFlow == null) {
            cssGridAutoFlow =
                    (CssGridAutoFlow) style.CascadingOrder(new CssGridAutoFlow(),
                            style, selector);
        }
        return cssGridAutoFlow;
    }

    public CssGridAutoRows getGridAutoRows() {
        if (cssGridAutoRows == null) {
            cssGridAutoRows =
                    (CssGridAutoRows) style.CascadingOrder(new CssGridAutoRows(),
                            style, selector);
        }
        return cssGridAutoRows;
    }

    public CssGridAutoColumns getGridAutoColumns() {
        if (cssGridAutoColumns == null) {
            cssGridAutoColumns =
                    (CssGridAutoColumns) style.CascadingOrder(new CssGridAutoColumns(),
                            style, selector);
        }
        return cssGridAutoColumns;
    }

    public CssGridTemplateRows getGridTemplateRows() {
        if (cssGridTemplateRows == null) {
            cssGridTemplateRows =
                    (CssGridTemplateRows) style.CascadingOrder(new CssGridTemplateRows(),
                            style, selector);
        }
        return cssGridTemplateRows;
    }

    public CssGridTemplateColumns getGridTemplateColumns() {
        if (cssGridTemplateColumns == null) {
            cssGridTemplateColumns =
                    (CssGridTemplateColumns) style.CascadingOrder(new CssGridTemplateColumns(),
                            style, selector);
        }
        return cssGridTemplateColumns;
    }

    public CssGridTemplate getGridTemplate() {
        if (cssGridTemplate == null) {
            cssGridTemplate =
                    (CssGridTemplate) style.CascadingOrder(new CssGridTemplate(),
                            style, selector);
        }
        return cssGridTemplate;
    }

    public CssGrid getGrid() {
        if (cssGrid == null) {
            cssGrid =
                    (CssGrid) style.CascadingOrder(new CssGrid(),
                            style, selector);
        }
        return cssGrid;
    }

    public CssScrollMarginBlockStart getScrollMarginBlockStart() {
        if (cssScrollMarginBlockStart == null) {
            cssScrollMarginBlockStart =
                    (CssScrollMarginBlockStart) style.CascadingOrder(new CssScrollMarginBlockStart(),
                            style, selector);
        }
        return cssScrollMarginBlockStart;
    }

    public CssScrollMarginBlockEnd getScrollMarginBlockEnd() {
        if (cssScrollMarginBlockEnd == null) {
            cssScrollMarginBlockEnd =
                    (CssScrollMarginBlockEnd) style.CascadingOrder(new CssScrollMarginBlockEnd(),
                            style, selector);
        }
        return cssScrollMarginBlockEnd;
    }

    public CssScrollMarginInlineStart getScrollMarginInlineStart() {
        if (cssScrollMarginInlineStart == null) {
            cssScrollMarginInlineStart =
                    (CssScrollMarginInlineStart) style.CascadingOrder(new CssScrollMarginInlineStart(),
                            style, selector);
        }
        return cssScrollMarginInlineStart;
    }

    public CssScrollMarginInlineEnd getScrollMarginInlineEnd() {
        if (cssScrollMarginInlineEnd == null) {
            cssScrollMarginInlineEnd =
                    (CssScrollMarginInlineEnd) style.CascadingOrder(new CssScrollMarginInlineEnd(),
                            style, selector);
        }
        return cssScrollMarginInlineEnd;
    }

    public CssScrollMarginBlock getScrollMarginBlock() {
        if (cssScrollMarginBlock == null) {
            cssScrollMarginBlock =
                    (CssScrollMarginBlock) style.CascadingOrder(new org.w3c.css.properties.css.CssScrollMarginBlock(),
                            style, selector);
        }
        return cssScrollMarginBlock;
    }

    public CssScrollMarginInline getScrollMarginInline() {
        if (cssScrollMarginInline == null) {
            cssScrollMarginInline =
                    (CssScrollMarginInline) style.CascadingOrder(new CssScrollMarginInline(),
                            style, selector);
        }
        return cssScrollMarginInline;
    }

    public CssScrollMarginBottom getScrollMarginBottom() {
        if (cssScrollMarginBottom == null) {
            cssScrollMarginBottom =
                    (CssScrollMarginBottom) style.CascadingOrder(new CssScrollMarginBottom(),
                            style, selector);
        }
        return cssScrollMarginBottom;
    }

    public CssScrollMarginLeft getScrollMarginLeft() {
        if (cssScrollMarginLeft == null) {
            cssScrollMarginLeft =
                    (CssScrollMarginLeft) style.CascadingOrder(new CssScrollMarginLeft(),
                            style, selector);
        }
        return cssScrollMarginLeft;
    }

    public CssScrollMarginRight getScrollMarginRight() {
        if (cssScrollMarginRight == null) {
            cssScrollMarginRight =
                    (CssScrollMarginRight) style.CascadingOrder(new CssScrollMarginRight(),
                            style, selector);
        }
        return cssScrollMarginRight;
    }

    public CssScrollMarginTop getScrollMarginTop() {
        if (cssScrollMarginTop == null) {
            cssScrollMarginTop =
                    (CssScrollMarginTop) style.CascadingOrder(new CssScrollMarginTop(),
                            style, selector);
        }
        return cssScrollMarginTop;
    }

    public CssScrollMargin getScrollMargin() {
        if (cssScrollMargin == null) {
            cssScrollMargin =
                    (CssScrollMargin) style.CascadingOrder(new CssScrollMargin(),
                            style, selector);
        }
        return cssScrollMargin;
    }

    public CssScrollPaddingInlineStart getScrollPaddingInlineStart() {
        if (cssScrollPaddingInlineStart == null) {
            cssScrollPaddingInlineStart =
                    (CssScrollPaddingInlineStart) style.CascadingOrder(new CssScrollPaddingInlineStart(),
                            style, selector);
        }
        return cssScrollPaddingInlineStart;
    }

    public CssScrollPaddingInlineEnd getScrollPaddingInlineEnd() {
        if (cssScrollPaddingInlineEnd == null) {
            cssScrollPaddingInlineEnd =
                    (CssScrollPaddingInlineEnd) style.CascadingOrder(new CssScrollPaddingInlineEnd(),
                            style, selector);
        }
        return cssScrollPaddingInlineEnd;
    }

    public CssScrollPaddingInline getScrollPaddingInline() {
        if (cssScrollPaddingInline == null) {
            cssScrollPaddingInline =
                    (CssScrollPaddingInline) style.CascadingOrder(new CssScrollPaddingInline(),
                            style, selector);
        }
        return cssScrollPaddingInline;
    }

    public CssScrollPaddingBlockStart getScrollPaddingBlockStart() {
        if (cssScrollPaddingBlockStart == null) {
            cssScrollPaddingBlockStart =
                    (CssScrollPaddingBlockStart) style.CascadingOrder(new CssScrollPaddingBlockStart(),
                            style, selector);
        }
        return cssScrollPaddingBlockStart;
    }

    public CssScrollPaddingBlockEnd getScrollPaddingBlockEnd() {
        if (cssScrollPaddingBlockEnd == null) {
            cssScrollPaddingBlockEnd =
                    (CssScrollPaddingBlockEnd) style.CascadingOrder(new CssScrollPaddingBlockEnd(),
                            style, selector);
        }
        return cssScrollPaddingBlockEnd;
    }

    public CssScrollPaddingBlock getScrollPaddingBlock() {
        if (cssScrollPaddingBlock == null) {
            cssScrollPaddingBlock =
                    (CssScrollPaddingBlock) style.CascadingOrder(new CssScrollPaddingBlock(),
                            style, selector);
        }
        return cssScrollPaddingBlock;
    }

    public CssScrollPadding getScrollPadding() {
        if (cssScrollPadding == null) {
            cssScrollPadding =
                    (CssScrollPadding) style.CascadingOrder(new CssScrollPadding(),
                            style, selector);
        }
        return cssScrollPadding;
    }

    public CssScrollPaddingBottom getScrollPaddingBottom() {
        if (cssScrollPaddingBottom == null) {
            cssScrollPaddingBottom =
                    (CssScrollPaddingBottom) style.CascadingOrder(new CssScrollPaddingBottom(),
                            style, selector);
        }
        return cssScrollPaddingBottom;
    }

    public CssScrollPaddingLeft getScrollPaddingLeft() {
        if (cssScrollPaddingLeft == null) {
            cssScrollPaddingLeft =
                    (CssScrollPaddingLeft) style.CascadingOrder(new CssScrollPaddingLeft(),
                            style, selector);
        }
        return cssScrollPaddingLeft;
    }

    public CssScrollPaddingRight getScrollPaddingRight() {
        if (cssScrollPaddingRight == null) {
            cssScrollPaddingRight =
                    (CssScrollPaddingRight) style.CascadingOrder(new CssScrollPaddingRight(),
                            style, selector);
        }
        return cssScrollPaddingRight;
    }

    public CssScrollPaddingTop getScrollPaddingTop() {
        if (cssScrollPaddingTop == null) {
            cssScrollPaddingTop =
                    (CssScrollPaddingTop) style.CascadingOrder(new CssScrollPaddingTop(),
                            style, selector);
        }
        return cssScrollPaddingTop;
    }

    public CssScrollSnapStop getScrollSnapStop() {
        if (cssScrollSnapStop == null) {
            cssScrollSnapStop =
                    (CssScrollSnapStop) style.CascadingOrder(new CssScrollSnapStop(),
                            style, selector);
        }
        return cssScrollSnapStop;
    }

    public CssScrollSnapAlign getScrollSnapAlign() {
        if (cssScrollSnapAlign == null) {
            cssScrollSnapAlign =
                    (CssScrollSnapAlign) style.CascadingOrder(new CssScrollSnapAlign(),
                            style, selector);
        }
        return cssScrollSnapAlign;
    }

    public CssScrollSnapType getScrollSnapType() {
        if (cssScrollSnapType == null) {
            cssScrollSnapType =
                    (CssScrollSnapType) style.CascadingOrder(new CssScrollSnapType(),
                            style, selector);
        }
        return cssScrollSnapType;
    }

    public CssWillChange getWillChange() {
        if (cssWillChange == null) {
            cssWillChange =
                    (CssWillChange) style.CascadingOrder(new CssWillChange(),
                            style, selector);
        }
        return cssWillChange;
    }

    public CssContain getContain() {
        if (cssContain == null) {
            cssContain =
                    (CssContain) style.CascadingOrder(new CssContain(),
                            style, selector);
        }
        return cssContain;
    }

    public CssMixBlendMode getMixBlendMode() {
        if (cssMixBlendMode == null) {
            cssMixBlendMode =
                    (CssMixBlendMode) style.CascadingOrder(new CssMixBlendMode(),
                            style, selector);
        }
        return cssMixBlendMode;
    }

    public CssIsolation getIsolation() {
        if (cssIsolation == null) {
            cssIsolation =
                    (CssIsolation) style.CascadingOrder(new CssIsolation(),
                            style, selector);
        }
        return cssIsolation;
    }

    public CssBackgroundBlendMode getBackgroundBlendMode() {
        if (cssBackgroundBlendMode == null) {
            cssBackgroundBlendMode =
                    (CssBackgroundBlendMode) style.CascadingOrder(new CssBackgroundBlendMode(),
                            style, selector);
        }
        return cssBackgroundBlendMode;
    }

    public CssOpacity getOpacity() {
        if (cssOpacity == null) {
            cssOpacity =
                    (CssOpacity) style.CascadingOrder(new CssOpacity(),
                            style, selector);
        }
        return cssOpacity;
    }

    public CssRubyPosition getRubyPosition() {
        if (cssRubyPosition == null) {
            cssRubyPosition =
                    (CssRubyPosition) style.CascadingOrder(
                            new CssRubyPosition(), style, selector);
        }
        return cssRubyPosition;
    }

    public CssRubyAlign getRubyAlign() {
        if (cssRubyAlign == null) {
            cssRubyAlign =
                    (CssRubyAlign) style.CascadingOrder(
                            new CssRubyAlign(), style, selector);
        }
        return cssRubyAlign;
    }

    public CssRubyMerge getRubyMerge() {
        if (cssRubyMerge == null) {
            cssRubyMerge =
                    (CssRubyMerge) style.CascadingOrder(
                            new CssRubyMerge(), style, selector);
        }
        return cssRubyMerge;
    }

    public org.w3c.css.properties.css.CssRubyOverhang getRubyOverhang() {
        if (cssRubyOverhang == null) {
            cssRubyOverhang =
                    (org.w3c.css.properties.css.CssRubyOverhang) style.CascadingOrder(
                            new org.w3c.css.properties.css.CssRubyOverhang(), style, selector);
        }
        return cssRubyOverhang;
    }

    public CssBoxSuppress getBoxSuppress() {
        if (cssBoxSuppress == null) {
            cssBoxSuppress =
                    (CssBoxSuppress) style.CascadingOrder(
                            new CssBoxSuppress(), style, selector);
        }
        return cssBoxSuppress;
    }

    public CssBoxSizing getBoxSizing() {
        if (cssBoxSizing == null) {
            cssBoxSizing =
                    (CssBoxSizing) style.CascadingOrder(
                            new CssBoxSizing(), style, selector);
        }
        return cssBoxSizing;
    }

    public CssResize getResize() {
        if (cssResize == null) {
            cssResize =
                    (CssResize) style.CascadingOrder(
                            new CssResize(), style, selector);
        }
        return cssResize;
    }

    public CssTextJustify getTextJustify() {
        if (cssTextJustify == null) {
            cssTextJustify =
                    (CssTextJustify) style.CascadingOrder(
                            new CssTextJustify(), style, selector);
        }
        return cssTextJustify;
    }

    public CssTextAlignAll getTextAlignAll() {
        if (cssTextAlignAll == null) {
            cssTextAlignAll =
                    (CssTextAlignAll) style.CascadingOrder(
                            new CssTextAlignAll(), style, selector);
        }
        return cssTextAlignAll;
    }

    public CssTextAlignLast getTextAlignLast() {
        if (cssTextAlignLast == null) {
            cssTextAlignLast =
                    (CssTextAlignLast) style.CascadingOrder(
                            new CssTextAlignLast(), style, selector);
        }
        return cssTextAlignLast;
    }

    public CssInlineSizing getInlineSizing() {
        if (cssInlineSizing == null) {
            cssInlineSizing =
                    (CssInlineSizing) style.CascadingOrder(
                            new CssInlineSizing(), style, selector);
        }
        return cssInlineSizing;
    }

    public CssDominantBaseline getDominantBaseline() {
        if (cssDominantBaseline == null) {
            cssDominantBaseline =
                    (CssDominantBaseline) style.CascadingOrder(
                            new CssDominantBaseline(), style, selector);
        }
        return cssDominantBaseline;
    }

    public CssAlignmentBaseline getAlignmentBaseline() {
        if (cssAlignmentBaseline == null) {
            cssAlignmentBaseline =
                    (CssAlignmentBaseline) style.CascadingOrder(
                            new CssAlignmentBaseline(), style, selector);
        }
        return cssAlignmentBaseline;
    }

    public CssBaselineShift getBaselineShift() {
        if (cssBaselineShift == null) {
            cssBaselineShift =
                    (CssBaselineShift) style.CascadingOrder(
                            new CssBaselineShift(), style, selector);
        }
        return cssBaselineShift;
    }

    public CssBaselineSource getBaselineSource() {
        if (cssBaselineSource == null) {
            cssBaselineSource =
                    (CssBaselineSource) style.CascadingOrder(
                            new CssBaselineSource(), style, selector);
        }
        return cssBaselineSource;
    }

    public CssInitialLetter getInitialLetter() {
        if (cssInitialLetter == null) {
            cssInitialLetter =
                    (CssInitialLetter) style.CascadingOrder(
                            new CssInitialLetter(), style, selector);
        }
        return cssInitialLetter;
    }

    public CssInitialLetterAlign getInitialLetterAlign() {
        if (cssInitialLetterAlign == null) {
            cssInitialLetterAlign =
                    (CssInitialLetterAlign) style.CascadingOrder(
                            new CssInitialLetterAlign(), style, selector);
        }
        return cssInitialLetterAlign;
    }

    public CssInitialLetterWrap getInitialLetterWrap() {
        if (cssInitialLetterWrap == null) {
            cssInitialLetterWrap =
                    (CssInitialLetterWrap) style.CascadingOrder(
                            new CssInitialLetterWrap(), style, selector);
        }
        return cssInitialLetterWrap;
    }

    public CssLineBreak getLineBreak() {
        if (cssLineBreak == null) {
            cssLineBreak =
                    (CssLineBreak) style.CascadingOrder(
                            new CssLineBreak(), style, selector);
        }
        return cssLineBreak;
    }

    public CssWordBreak getWordBreak() {
        if (cssWordBreak == null) {
            cssWordBreak =
                    (CssWordBreak) style.CascadingOrder(
                            new CssWordBreak(), style, selector);
        }
        return cssWordBreak;
    }

    public CssColumns getColumns() {
        if (cssColumns == null) {
            cssColumns =
                    (CssColumns) style.CascadingOrder(
                            new CssColumns(), style, selector);
        }
        return cssColumns;
    }

    public CssColumnCount getColumnCount() {
        if (cssColumnCount == null) {
            cssColumnCount =
                    (CssColumnCount) style.CascadingOrder(
                            new CssColumnCount(), style, selector);
        }
        return cssColumnCount;
    }

    public CssColumnSpan getColumnSpan() {
        if (cssColumnSpan == null) {
            cssColumnSpan =
                    (CssColumnSpan) style.CascadingOrder(
                            new CssColumnSpan(), style, selector);
        }
        return cssColumnSpan;
    }

    public CssColumnWidth getColumnWidth() {
        if (cssColumnWidth == null) {
            cssColumnWidth =
                    (CssColumnWidth) style.CascadingOrder(
                            new CssColumnWidth(), style, selector);
        }
        return cssColumnWidth;
    }

    public CssBackgroundClip getCssBackgroundClip() {
        if (cssBackgroundClip == null) {
            cssBackgroundClip =
                    (CssBackgroundClip) style.CascadingOrder(
                            new CssBackgroundClip(), style, selector);
        }
        return cssBackgroundClip;
    }

    public CssBackgroundSize getCssBackgroundSize() {
        if (cssBackgroundSize == null) {
            cssBackgroundSize =
                    (CssBackgroundSize) style.CascadingOrder(
                            new CssBackgroundSize(), style, selector);
        }
        return cssBackgroundSize;
    }

    public CssBackgroundOrigin getCssBackgroundOrigin() {
        if (cssBackgroundOrigin == null) {
            cssBackgroundOrigin =
                    (CssBackgroundOrigin) style.CascadingOrder(
                            new CssBackgroundOrigin(), style, selector);
        }
        return cssBackgroundOrigin;
    }

    public CssHangingPunctuation getHangingPunctuation() {
        if (cssHangingPunctuation == null) {
            cssHangingPunctuation =
                    (CssHangingPunctuation) style.CascadingOrder(
                            new CssHangingPunctuation(), style, selector);
        }
        return cssHangingPunctuation;
    }

    public CssColumnGap getColumnGap() {
        if (cssColumnGap == null) {
            cssColumnGap =
                    (CssColumnGap) style.CascadingOrder(
                            new CssColumnGap(), style, selector);
        }
        return cssColumnGap;
    }

    public CssBreakBefore getBreakBefore() {
        if (cssBreakBefore == null) {
            cssBreakBefore =
                    (CssBreakBefore) style.CascadingOrder(
                            new CssBreakBefore(), style, selector);
        }
        return cssBreakBefore;
    }

    public CssBreakAfter getBreakAfter() {
        if (cssBreakAfter == null) {
            cssBreakAfter =
                    (CssBreakAfter) style.CascadingOrder(
                            new CssBreakAfter(), style, selector);
        }
        return cssBreakAfter;
    }

    public CssBreakInside getBreakInside() {
        if (cssBreakInside == null) {
            cssBreakInside =
                    (CssBreakInside) style.CascadingOrder(
                            new CssBreakInside(), style, selector);
        }
        return cssBreakInside;
    }

    public CssColumnFill getColumnFill() {
        if (cssColumnFill == null) {
            cssColumnFill =
                    (CssColumnFill) style.CascadingOrder(
                            new CssColumnFill(), style, selector);
        }
        return cssColumnFill;
    }

    public CssColumnRuleColor getColumnRuleColor() {
        if (cssColumnRuleColor == null) {
            cssColumnRuleColor =
                    (CssColumnRuleColor) style.CascadingOrder(
                            new CssColumnRuleColor(), style, selector);
        }
        return cssColumnRuleColor;
    }

    public CssColumnRuleStyle getColumnRuleStyle() {
        if (cssColumnRuleStyle == null) {
            cssColumnRuleStyle =
                    (CssColumnRuleStyle) style.CascadingOrder(
                            new CssColumnRuleStyle(), style, selector);
        }
        return cssColumnRuleStyle;
    }

    public CssColumnRuleWidth getColumnRuleWidth() {
        if (cssColumnRuleWidth == null) {
            cssColumnRuleWidth =
                    (CssColumnRuleWidth) style.CascadingOrder(
                            new CssColumnRuleWidth(), style, selector);
        }
        return cssColumnRuleWidth;
    }

    public CssColumnRule getColumnRule() {
        if (cssColumnRule == null) {
            cssColumnRule =
                    (CssColumnRule) style.CascadingOrder(
                            new CssColumnRule(), style, selector);
        }
        return cssColumnRule;
    }

    public CssIcon getIcon() {
        if (cssIcon == null) {
            cssIcon =
                    (CssIcon) style.CascadingOrder(
                            new CssIcon(), style, selector);
        }
        return cssIcon;
    }

    // TODO FIXME should be getNavUp... fix ATSC for that.
    public CssNavUp getNavUpCSS3() {
        if (cssNavUp == null) {
            cssNavUp =
                    (CssNavUp) style.CascadingOrder(
                            new CssNavUp(), style, selector);
        }
        return cssNavUp;
    }

    public CssNavRight getNavRightCSS3() {
        if (cssNavRight == null) {
            cssNavRight =
                    (CssNavRight) style.CascadingOrder(
                            new CssNavRight(), style, selector);
        }
        return cssNavRight;
    }

    public CssNavDown getNavDownCSS3() {
        if (cssNavDown == null) {
            cssNavDown =
                    (CssNavDown) style.CascadingOrder(
                            new CssNavDown(), style, selector);
        }
        return cssNavDown;
    }

    public CssNavLeft getNavLeftCSS3() {
        if (cssNavLeft == null) {
            cssNavLeft =
                    (CssNavLeft) style.CascadingOrder(
                            new CssNavLeft(), style, selector);
        }
        return cssNavLeft;
    }

    public CssCaretColor getCaretColor() {
        if (cssCaretColor == null) {
            cssCaretColor =
                    (CssCaretColor) style.CascadingOrder(
                            new CssCaretColor(), style, selector);
        }
        return cssCaretColor;
    }

    public CssCaretShape getCaretShape() {
        if (cssCaretShape == null) {
            cssCaretShape =
                    (CssCaretShape) style.CascadingOrder(
                            new CssCaretShape(), style, selector);
        }
        return cssCaretShape;
    }

    public CssCaret getCaret() {
        if (cssCaret == null) {
            cssCaret =
                    (CssCaret) style.CascadingOrder(
                            new CssCaret(), style, selector);
        }
        return cssCaret;
    }

    public CssOutlineOffset getOutlineOffset() {
        if (cssOutlineOffset == null) {
            cssOutlineOffset =
                    (CssOutlineOffset) style.CascadingOrder(
                            new CssOutlineOffset(), style, selector);
        }
        return cssOutlineOffset;
    }

    public CssOverflowX getOverflowX() {
        if (cssOverflowX == null) {
            cssOverflowX =
                    (CssOverflowX) style.CascadingOrder(
                            new CssOverflowX(), style, selector);
        }
        return cssOverflowX;
    }

    public CssOverflowY getOverflowY() {
        if (cssOverflowY == null) {
            cssOverflowY =
                    (CssOverflowY) style.CascadingOrder(
                            new CssOverflowY(), style, selector);
        }
        return cssOverflowY;
    }

    public CssRubySpan getRubySpan() {
        if (cssRubySpan == null) {
            cssRubySpan =
                    (CssRubySpan) style.CascadingOrder(
                            new CssRubySpan(), style, selector);
        }
        return cssRubySpan;
    }

    public CssMarqueeDirection getMarqueeDirection() {
        if (cssMarqueeDirection == null) {
            cssMarqueeDirection =
                    (CssMarqueeDirection) style.CascadingOrder(
                            new CssMarqueeDirection(), style, selector);
        }
        return cssMarqueeDirection;
    }

    public CssMarqueePlayCount getMarqueePlayCount() {
        if (cssMarqueePlayCount == null) {
            cssMarqueePlayCount =
                    (CssMarqueePlayCount) style.CascadingOrder(
                            new CssMarqueePlayCount(), style, selector);
        }
        return cssMarqueePlayCount;
    }

    public CssMarqueeSpeed getMarqueeSpeed() {
        if (cssMarqueeSpeed == null) {
            cssMarqueeSpeed =
                    (CssMarqueeSpeed) style.CascadingOrder(
                            new CssMarqueeSpeed(), style, selector);
        }
        return cssMarqueeSpeed;
    }

    public CssMarqueeStyle getMarqueeStyle() {
        if (cssMarqueeStyle == null) {
            cssMarqueeStyle =
                    (CssMarqueeStyle) style.CascadingOrder(
                            new CssMarqueeStyle(), style, selector);
        }
        return cssMarqueeStyle;
    }

    public CssOverflowStyle getOverflowStyle() {
        if (cssOverflowStyle == null) {
            cssOverflowStyle =
                    (CssOverflowStyle) style.CascadingOrder(
                            new CssOverflowStyle(), style, selector);
        }
        return cssOverflowStyle;
    }

    public org.w3c.css.properties.css.CssBorderTopRightRadius getBorderTopRightRadius() {
        if (cssBorder.borderRadius.topRight == null) {
            cssBorder.borderRadius.topRight =
                    (org.w3c.css.properties.css.CssBorderTopRightRadius) style.CascadingOrder(
                            new CssBorderTopRightRadius(), style, selector);
        }
        return cssBorder.borderRadius.topRight;
    }

    public org.w3c.css.properties.css.CssBorderBottomRightRadius getBorderBottomRightRadius() {
        if (cssBorder.borderRadius.bottomRight == null) {
            cssBorder.borderRadius.bottomRight =
                    (org.w3c.css.properties.css.CssBorderBottomRightRadius) style.CascadingOrder(
                            new CssBorderBottomRightRadius(), style, selector);
        }
        return cssBorder.borderRadius.bottomRight;
    }

    public org.w3c.css.properties.css.CssBorderBottomLeftRadius getBorderBottomLeftRadius() {
        if (cssBorder.borderRadius.bottomLeft == null) {
            cssBorder.borderRadius.bottomLeft =
                    (org.w3c.css.properties.css.CssBorderBottomLeftRadius) style.CascadingOrder(
                            new org.w3c.css.properties.css.CssBorderBottomLeftRadius(), style, selector);
        }
        return cssBorder.borderRadius.bottomLeft;
    }

    public org.w3c.css.properties.css.CssBorderTopLeftRadius getBorderTopLeftRadius() {
        if (cssBorder.borderRadius.topLeft == null) {
            cssBorder.borderRadius.topLeft =
                    (org.w3c.css.properties.css.CssBorderTopLeftRadius) style.CascadingOrder(
                            new org.w3c.css.properties.css.CssBorderTopLeftRadius(), style, selector);
        }
        return cssBorder.borderRadius.topLeft;
    }

    public org.w3c.css.properties.css.CssBorderRadius getBorderRadius() {
        if (cssBorder.borderRadius == null) {
            cssBorder.borderRadius =
                    (org.w3c.css.properties.css.CssBorderRadius) style.CascadingOrder(
                            new org.w3c.css.properties.css.CssBorderRadius(), style, selector);
        }
        return cssBorder.borderRadius;
    }

    public CssBoxShadow getBoxShadow() {
        if (cssBoxShadow == null) {
            cssBoxShadow =
                    (CssBoxShadow) style.CascadingOrder(
                            new CssBoxShadow(), style, selector);
        }
        return cssBoxShadow;
    }

    public CssBoxDecorationBreak getBoxDecorationBreak() {
        if (cssBoxDecorationBreak == null) {
            cssBoxDecorationBreak =
                    (CssBoxDecorationBreak) style.CascadingOrder(
                            new CssBoxDecorationBreak(), style, selector);
        }
        return cssBoxDecorationBreak;
    }

    public CssFontKerning getFontKerning() {
        if (cssFontKerning == null) {
            cssFontKerning =
                    (CssFontKerning) style.CascadingOrder(
                            new CssFontKerning(), style, selector);
        }
        return cssFontKerning;
    }

    public CssFontLanguageOverride getFontLanguageOverride() {
        if (cssFontLanguageOverride == null) {
            cssFontLanguageOverride =
                    (CssFontLanguageOverride) style.CascadingOrder(
                            new CssFontLanguageOverride(), style, selector);
        }
        return cssFontLanguageOverride;
    }

    public CssFontVariantCaps getFontVariantCaps() {
        if (cssFontVariantCaps == null) {
            cssFontVariantCaps =
                    (CssFontVariantCaps) style.CascadingOrder(
                            new CssFontVariantCaps(), style, selector);
        }
        return cssFontVariantCaps;
    }

    public CssFontVariantPosition getFontVariantPosition() {
        if (cssFontVariantPosition == null) {
            cssFontVariantPosition =
                    (CssFontVariantPosition) style.CascadingOrder(
                            new CssFontVariantPosition(), style, selector);
        }
        return cssFontVariantPosition;
    }

    public CssFontSynthesisSmallCaps getFontSynthesisSmallCaps() {
        if (cssFontSynthesisSmallCaps == null) {
            cssFontSynthesisSmallCaps =
                    (CssFontSynthesisSmallCaps) style.CascadingOrder(
                            new CssFontSynthesisSmallCaps(), style, selector);
        }
        return cssFontSynthesisSmallCaps;
    }

    public CssFontSynthesisStyle getFontSynthesisStyle() {
        if (cssFontSynthesisStyle == null) {
            cssFontSynthesisStyle =
                    (CssFontSynthesisStyle) style.CascadingOrder(
                            new CssFontSynthesisStyle(), style, selector);
        }
        return cssFontSynthesisStyle;
    }

    public CssFontSynthesisWeight getFontSynthesisWeight() {
        if (cssFontSynthesisWeight == null) {
            cssFontSynthesisWeight =
                    (CssFontSynthesisWeight) style.CascadingOrder(
                            new CssFontSynthesisWeight(), style, selector);
        }
        return cssFontSynthesisWeight;
    }

    public CssFontSynthesis getFontSynthesis() {
        if (cssFontSynthesis == null) {
            cssFontSynthesis =
                    (CssFontSynthesis) style.CascadingOrder(
                            new CssFontSynthesis(), style, selector);
        }
        return cssFontSynthesis;
    }

    public CssFontVariantEastAsian getFontVariantEastAsian() {
        if (cssFontVariantEastAsian == null) {
            cssFontVariantEastAsian =
                    (CssFontVariantEastAsian) style.CascadingOrder(
                            new CssFontVariantEastAsian(), style, selector);
        }
        return cssFontVariantEastAsian;
    }

    public CssFontVariantLigatures getFontVariantLigatures() {
        if (cssFontVariantLigatures == null) {
            cssFontVariantLigatures =
                    (CssFontVariantLigatures) style.CascadingOrder(
                            new CssFontVariantLigatures(), style, selector);
        }
        return cssFontVariantLigatures;
    }

    public CssFontVariantEmoji getFontVariantEmoji() {
        if (cssFontVariantEmoji == null) {
            cssFontVariantEmoji =
                    (CssFontVariantEmoji) style.CascadingOrder(
                            new CssFontVariantEmoji(), style, selector);
        }
        return cssFontVariantEmoji;
    }

    public CssFontVariantNumeric getFontVariantNumeric() {
        if (cssFontVariantNumeric == null) {
            cssFontVariantNumeric =
                    (CssFontVariantNumeric) style.CascadingOrder(
                            new CssFontVariantNumeric(), style, selector);
        }
        return cssFontVariantNumeric;
    }

    public CssFontOpticalSizing getFontOpticalSizing() {
        if (cssFontOpticalSizing == null) {
            cssFontOpticalSizing =
                    (CssFontOpticalSizing) style.CascadingOrder(
                            new CssFontOpticalSizing(), style, selector);
        }
        return cssFontOpticalSizing;
    }

    public CssFontPalette getFontPalette() {
        if (cssFontPalette == null) {
            cssFontPalette =
                    (CssFontPalette) style.CascadingOrder(
                            new CssFontPalette(), style, selector);
        }
        return cssFontPalette;
    }

    public CssFontFeatureSettings getFontFeatureSettings() {
        if (cssFontFeatureSettings == null) {
            cssFontFeatureSettings =
                    (CssFontFeatureSettings) style.CascadingOrder(
                            new CssFontFeatureSettings(), style, selector);
        }
        return cssFontFeatureSettings;
    }

    public CssFontVariationSettings getFontVariationSettings() {
        if (cssFontVariationSettings == null) {
            cssFontVariationSettings =
                    (CssFontVariationSettings) style.CascadingOrder(
                            new CssFontVariationSettings(), style, selector);
        }
        return cssFontVariationSettings;
    }

    public CssFontVariantAlternates getFontVariantAlternates() {
        if (cssFontVariantAlternates == null) {
            cssFontVariantAlternates =
                    (CssFontVariantAlternates) style.CascadingOrder(
                            new CssFontVariantAlternates(), style, selector);
        }
        return cssFontVariantAlternates;
    }

    public CssOverflowWrap getOverflowWrap() {
        if (cssOverflowWrap == null) {
            cssOverflowWrap =
                    (CssOverflowWrap) style.CascadingOrder(
                            new CssOverflowWrap(), style, selector);
        }
        return cssOverflowWrap;
    }

    public CssHyphens getHyphens() {
        if (cssHyphens == null) {
            cssHyphens =
                    (CssHyphens) style.CascadingOrder(
                            new CssHyphens(), style, selector);
        }
        return cssHyphens;
    }

    public CssTextDecorationStyle getTextDecorationStyle() {
        if (cssTextDecorationStyle == null) {
            cssTextDecorationStyle =
                    (CssTextDecorationStyle) style.CascadingOrder(
                            new CssTextDecorationStyle(), style, selector);
        }
        return cssTextDecorationStyle;
    }

    public CssTextDecorationColor getTextDecorationColor() {
        if (cssTextDecorationColor == null) {
            cssTextDecorationColor =
                    (CssTextDecorationColor) style.CascadingOrder(
                            new CssTextDecorationColor(), style, selector);
        }
        return cssTextDecorationColor;
    }

    public CssTextDecorationLine getTextDecorationLine() {
        if (cssTextDecorationLine == null) {
            cssTextDecorationLine =
                    (CssTextDecorationLine) style.CascadingOrder(
                            new CssTextDecorationLine(), style, selector);
        }
        return cssTextDecorationLine;
    }

    public CssTextDecorationSkip getTextDecorationSkip() {
        if (cssTextDecorationSkip == null) {
            cssTextDecorationSkip =
                    (CssTextDecorationSkip) style.CascadingOrder(
                            new CssTextDecorationSkip(), style, selector);
        }
        return cssTextDecorationSkip;
    }

    public CssTextEmphasis getTextEmphasis() {
        if (cssTextEmphasis == null) {
            cssTextEmphasis =
                    (CssTextEmphasis) style.CascadingOrder(
                            new CssTextEmphasis(), style, selector);
        }
        return cssTextEmphasis;
    }

    public CssTextEmphasisColor getTextEmphasisColor() {
        if (cssTextEmphasisColor == null) {
            cssTextEmphasisColor =
                    (CssTextEmphasisColor) style.CascadingOrder(
                            new CssTextEmphasisColor(), style, selector);
        }
        return cssTextEmphasisColor;
    }

    public CssTextEmphasisPosition getTextEmphasisPosition() {
        if (cssTextEmphasisPosition == null) {
            cssTextEmphasisPosition =
                    (CssTextEmphasisPosition) style.CascadingOrder(
                            new CssTextEmphasisPosition(), style, selector);
        }
        return cssTextEmphasisPosition;
    }

    public CssTextEmphasisStyle getTextEmphasisStyle() {
        if (cssTextEmphasisStyle == null) {
            cssTextEmphasisStyle =
                    (CssTextEmphasisStyle) style.CascadingOrder(
                            new CssTextEmphasisStyle(), style, selector);
        }
        return cssTextEmphasisStyle;
    }

    public CssTextSizeAdjust getTextSizeAdjust() {
        if (cssTextSizeAdjust == null) {
            cssTextSizeAdjust =
                    (CssTextSizeAdjust) style.CascadingOrder(
                            new CssTextSizeAdjust(), style, selector);
        }
        return cssTextSizeAdjust;
    }

    public CssTextUnderlinePosition getTextUnderlinePosition() {
        if (cssTextUnderlinePosition == null) {
            cssTextUnderlinePosition =
                    (CssTextUnderlinePosition) style.CascadingOrder(
                            new CssTextUnderlinePosition(), style, selector);
        }
        return cssTextUnderlinePosition;
    }

    public CssTabSize getTabSize() {
        if (cssTabSize == null) {
            cssTabSize =
                    (CssTabSize) style.CascadingOrder(
                            new CssTabSize(), style, selector);
        }
        return cssTabSize;
    }

    public CssAnimation getAnimation() {
        if (cssAnimation == null) {
            cssAnimation =
                    (CssAnimation) style.CascadingOrder(
                            new CssAnimation(), style, selector);
        }
        return cssAnimation;
    }

    public CssAnimationDelay getAnimationDelay() {
        if (cssAnimationDelay == null) {
            cssAnimationDelay =
                    (CssAnimationDelay) style.CascadingOrder(
                            new CssAnimationDelay(), style, selector);
        }
        return cssAnimationDelay;
    }

    public CssAnimationDirection getAnimationDirection() {
        if (cssAnimationDirection == null) {
            cssAnimationDirection =
                    (CssAnimationDirection) style.CascadingOrder(
                            new CssAnimationDirection(), style, selector);
        }
        return cssAnimationDirection;
    }

    public CssAnimationDuration getAnimationDuration() {
        if (cssAnimationDuration == null) {
            cssAnimationDuration =
                    (CssAnimationDuration) style.CascadingOrder(
                            new CssAnimationDuration(), style, selector);
        }
        return cssAnimationDuration;
    }

    public CssAnimationIterationCount getAnimationIterationCount() {
        if (cssAnimationIterationCount == null) {
            cssAnimationIterationCount =
                    (CssAnimationIterationCount) style.CascadingOrder(
                            new CssAnimationIterationCount(), style, selector);
        }
        return cssAnimationIterationCount;
    }

    public CssAnimationName getAnimationName() {
        if (cssAnimationName == null) {
            cssAnimationName =
                    (CssAnimationName) style.CascadingOrder(
                            new CssAnimationName(), style, selector);
        }
        return cssAnimationName;
    }

    public CssAnimationPlayState getAnimationPlayState() {
        if (cssAnimationPlayState == null) {
            cssAnimationPlayState =
                    (CssAnimationPlayState) style.CascadingOrder(
                            new CssAnimationPlayState(), style, selector);
        }
        return cssAnimationPlayState;
    }

    public CssAnimationFillMode getAnimationFillMode() {
        if (cssAnimationFillMode == null) {
            cssAnimationFillMode =
                    (CssAnimationFillMode) style.CascadingOrder(
                            new CssAnimationFillMode(), style, selector);
        }
        return cssAnimationFillMode;
    }

    public CssAnimationTimingFunction getAnimationTimingFunction() {
        if (cssAnimationTimingFunction == null) {
            cssAnimationTimingFunction =
                    (CssAnimationTimingFunction) style.CascadingOrder(
                            new CssAnimationTimingFunction(), style, selector);
        }
        return cssAnimationTimingFunction;
    }

    public CssTransitionDelay getTransitionDelay() {
        if (cssTransitionDelay == null) {
            cssTransitionDelay =
                    (CssTransitionDelay) style.CascadingOrder(
                            new CssTransitionDelay(), style, selector);
        }
        return cssTransitionDelay;
    }

    public CssTransitionDuration getTransitionDuration() {
        if (cssTransitionDuration == null) {
            cssTransitionDuration =
                    (CssTransitionDuration) style.CascadingOrder(
                            new CssTransitionDuration(), style, selector);
        }
        return cssTransitionDuration;
    }

    public CssTransitionProperty getTransitionProperty() {
        if (cssTransitionProperty == null) {
            cssTransitionProperty =
                    (CssTransitionProperty) style.CascadingOrder(
                            new CssTransitionProperty(), style, selector);
        }
        return cssTransitionProperty;
    }

    public CssTransitionTimingFunction getTransitionTimingFunction() {
        if (cssTransitionTimingFunction == null) {
            cssTransitionTimingFunction =
                    (CssTransitionTimingFunction) style.CascadingOrder(
                            new CssTransitionTimingFunction(), style, selector);
        }
        return cssTransitionTimingFunction;
    }


    public CssTransition getTransition() {
        if (cssTransition == null) {
            cssTransition =
                    (CssTransition) style.CascadingOrder(
                            new CssTransition(), style, selector);
        }
        return cssTransition;
    }

    public CssAlignContent getAlignContent() {
        if (cssAlignContent == null) {
            cssAlignContent =
                    (CssAlignContent) style.CascadingOrder(
                            new CssAlignContent(), style, selector);
        }
        return cssAlignContent;
    }

    public CssAlignItems getAlignItems() {
        if (cssAlignItems == null) {
            cssAlignItems =
                    (CssAlignItems) style.CascadingOrder(
                            new CssAlignItems(), style, selector);
        }
        return cssAlignItems;
    }

    public CssAlignSelf getAlignSelf() {
        if (cssAlignSelf == null) {
            cssAlignSelf =
                    (CssAlignSelf) style.CascadingOrder(
                            new CssAlignSelf(), style, selector);
        }
        return cssAlignSelf;
    }

    public CssFlex getFlex() {
        if (cssFlex == null) {
            cssFlex =
                    (CssFlex) style.CascadingOrder(
                            new CssFlex(), style, selector);
        }
        return cssFlex;
    }

    public CssFlexBasis getFlexBasis() {
        if (cssFlexBasis == null) {
            cssFlexBasis =
                    (CssFlexBasis) style.CascadingOrder(
                            new CssFlexBasis(), style, selector);
        }
        return cssFlexBasis;
    }

    public CssFlexDirection getFlexDirection() {
        if (cssFlexDirection == null) {
            cssFlexDirection =
                    (CssFlexDirection) style.CascadingOrder(
                            new CssFlexDirection(), style, selector);
        }
        return cssFlexDirection;
    }

    public CssFlexWrap getFlexWrap() {
        if (cssFlexWrap == null) {
            cssFlexWrap =
                    (CssFlexWrap) style.CascadingOrder(
                            new CssFlexWrap(), style, selector);
        }
        return cssFlexWrap;
    }

    public CssFlexFlow getFlexFlow() {
        if (cssFlexFlow == null) {
            cssFlexFlow =
                    (CssFlexFlow) style.CascadingOrder(
                            new CssFlexFlow(), style, selector);
        }
        return cssFlexFlow;
    }

    public CssFlexGrow getFlexGrow() {
        if (cssFlexGrow == null) {
            cssFlexGrow =
                    (CssFlexGrow) style.CascadingOrder(
                            new CssFlexGrow(), style, selector);
        }
        return cssFlexGrow;
    }

    public CssFlexShrink getFlexShrink() {
        if (cssFlexShrink == null) {
            cssFlexShrink =
                    (CssFlexShrink) style.CascadingOrder(
                            new CssFlexShrink(), style, selector);
        }
        return cssFlexShrink;
    }

    public CssJustifyContent getJustifyContent() {
        if (cssJustifyContent == null) {
            cssJustifyContent =
                    (CssJustifyContent) style.CascadingOrder(
                            new CssJustifyContent(), style, selector);
        }
        return cssJustifyContent;
    }

    public CssOrder getOrder() {
        if (cssOrder == null) {
            cssOrder =
                    (CssOrder) style.CascadingOrder(
                            new CssOrder(), style, selector);
        }
        return cssOrder;
    }

    public CssTransformStyle getTransformStyle() {
        if (cssTransformStyle == null) {
            cssTransformStyle =
                    (CssTransformStyle) style.CascadingOrder(
                            new CssTransformStyle(), style, selector);
        }
        return cssTransformStyle;
    }

    public CssBackfaceVisibility getBackfaceVisibility() {
        if (cssBackfaceVisibility == null) {
            cssBackfaceVisibility =
                    (CssBackfaceVisibility) style.CascadingOrder(
                            new CssBackfaceVisibility(), style, selector);
        }
        return cssBackfaceVisibility;
    }

    public CssPerspective getPerspective() {
        if (cssPerspective == null) {
            cssPerspective =
                    (CssPerspective) style.CascadingOrder(
                            new CssPerspective(), style, selector);
        }
        return cssPerspective;
    }

    public CssPerspectiveOrigin getPerspectiveOrigin() {
        if (cssPerspectiveOrigin == null) {
            cssPerspectiveOrigin =
                    (CssPerspectiveOrigin) style.CascadingOrder(
                            new CssPerspectiveOrigin(), style, selector);
        }
        return cssPerspectiveOrigin;
    }

    public CssTransformOrigin getTransformOrigin() {
        if (cssTransformOrigin == null) {
            cssTransformOrigin =
                    (CssTransformOrigin) style.CascadingOrder(
                            new CssTransformOrigin(), style, selector);
        }
        return cssTransformOrigin;
    }

    public CssTransform getTransform() {
        if (cssTransform == null) {
            cssTransform =
                    (CssTransform) style.CascadingOrder(
                            new CssTransform(), style, selector);
        }
        return cssTransform;
    }

    public CssTransformBox getTransformBox() {
        if (cssTransformBox == null) {
            cssTransformBox =
                    (CssTransformBox) style.CascadingOrder(
                            new CssTransformBox(), style, selector);
        }
        return cssTransformBox;
    }

    public CssImeMode getImeMode() {
        if (cssImeMode == null) {
            cssImeMode =
                    (CssImeMode) style.CascadingOrder(
                            new CssImeMode(), style, selector);
        }
        return cssImeMode;
    }

    public CssTextOverflow getTextOverflow() {
        if (cssTextOverflow == null) {
            cssTextOverflow =
                    (CssTextOverflow) style.CascadingOrder(
                            new CssTextOverflow(), style, selector);
        }
        return cssTextOverflow;
    }

    public CssObjectFit getObjectFit() {
        if (cssObjectFit == null) {
            cssObjectFit =
                    (CssObjectFit) style.CascadingOrder(
                            new CssObjectFit(), style, selector);
        }
        return cssObjectFit;
    }

    public CssObjectPosition getObjectPosition() {
        if (cssObjectPosition == null) {
            cssObjectPosition =
                    (CssObjectPosition) style.CascadingOrder(
                            new CssObjectPosition(), style, selector);
        }
        return cssObjectPosition;
    }

    public CssImageOrientation getImageOrientation() {
        if (cssImageOrientation == null) {
            cssImageOrientation =
                    (CssImageOrientation) style.CascadingOrder(
                            new CssImageOrientation(), style, selector);
        }
        return cssImageOrientation;
    }

    public CssImageResolution getImageResolution() {
        if (cssImageResolution == null) {
            cssImageResolution =
                    (CssImageResolution) style.CascadingOrder(
                            new CssImageResolution(), style, selector);
        }
        return cssImageResolution;
    }

    public final CssVoiceBalance getVoiceBalance() {
        if (cssVoiceBalance == null) {
            cssVoiceBalance =
                    (CssVoiceBalance) style.CascadingOrder(new CssVoiceBalance(),
                            style, selector);
        }
        return cssVoiceBalance;
    }

    public final CssVoiceStress getVoiceStress() {
        if (cssVoiceStress == null) {
            cssVoiceStress =
                    (CssVoiceStress) style.CascadingOrder(new CssVoiceStress(),
                            style, selector);
        }
        return cssVoiceStress;
    }

    public final CssVoiceDuration getVoiceDuration() {
        if (cssVoiceDuration == null) {
            cssVoiceDuration =
                    (CssVoiceDuration) style.CascadingOrder(new CssVoiceDuration(),
                            style, selector);
        }
        return cssVoiceDuration;
    }

    public final CssVoiceRate getVoiceRate() {
        if (cssVoiceRate == null) {
            cssVoiceRate =
                    (CssVoiceRate) style.CascadingOrder(new CssVoiceRate(),
                            style, selector);
        }
        return cssVoiceRate;
    }

    public final CssVoiceVolume getVoiceVolume() {
        if (cssVoiceVolume == null) {
            cssVoiceVolume =
                    (CssVoiceVolume) style.CascadingOrder(new CssVoiceVolume(),
                            style, selector);
        }
        return cssVoiceVolume;
    }

    public final CssRestAfter getRestAfter() {
        if (cssRestAfter == null) {
            cssRestAfter =
                    (CssRestAfter) style.CascadingOrder(new CssRestAfter(),
                            style, selector);
        }
        return cssRestAfter;
    }

    public final CssRestBefore getRestBefore() {
        if (cssRestBefore == null) {
            cssRestBefore =
                    (CssRestBefore) style.CascadingOrder(new CssRestBefore(),
                            style, selector);
        }
        return cssRestBefore;
    }

    public final CssRest getRest() {
        if (cssRest == null) {
            cssRest =
                    (CssRest) style.CascadingOrder(new CssRest(),
                            style, selector);
        }
        return cssRest;
    }

    public final CssSpeakAs getSpeakAs() {
        if (cssSpeakAs == null) {
            cssSpeakAs = (CssSpeakAs) style.CascadingOrder(new CssSpeakAs(), style, selector);
        }
        return cssSpeakAs;
    }

    public final CssVoicePitch getVoicePitch() {
        if (cssVoicePitch == null) {
            cssVoicePitch = (CssVoicePitch) style.CascadingOrder(new CssVoicePitch(), style, selector);
        }
        return cssVoicePitch;
    }

    public final CssVoiceRange getVoiceRange() {
        if (cssVoiceRange == null) {
            cssVoiceRange = (CssVoiceRange) style.CascadingOrder(new CssVoiceRange(), style, selector);
        }
        return cssVoiceRange;
    }

    public final CssFilter getFilter() {
        if (cssFilter == null) {
            cssFilter = (CssFilter) style.CascadingOrder(new CssFilter(), style, selector);
        }
        return cssFilter;
    }

    public final CssBackdropFilter getBackdropFilter() {
        if (cssBackdropFilter == null) {
            cssBackdropFilter = (CssBackdropFilter) style.CascadingOrder(
                    new CssBackdropFilter(), style, selector);
        }
        return cssBackdropFilter;
    }

    public final CssColorInterpolationFilters getColorInterpolationFilters() {
        if (cssColorInterpolationFilters == null) {
            cssColorInterpolationFilters = (CssColorInterpolationFilters) style.CascadingOrder(
                    new CssColorInterpolationFilters(), style, selector);
        }
        return cssColorInterpolationFilters;
    }

    public final CssFloodColor getFloodColor() {
        if (cssFloodColor == null) {
            cssFloodColor = (CssFloodColor) style.CascadingOrder(new CssFloodColor(), style, selector);
        }
        return cssFloodColor;
    }

    public final CssFloodOpacity getFloodOpacity() {
        if (cssFloodOpacity == null) {
            cssFloodOpacity = (CssFloodOpacity) style.CascadingOrder(new CssFloodOpacity(), style, selector);
        }
        return cssFloodOpacity;
    }

    public final CssLightingColor getLightingColor() {
        if (cssLightingColor == null) {
            cssLightingColor = (CssLightingColor) style.CascadingOrder(new CssLightingColor(), style, selector);
        }
        return cssLightingColor;
    }

    public final CssFloatReference getFloatReference() {
        if (cssFloatReference == null) {
            cssFloatReference = (CssFloatReference) style.CascadingOrder(new CssFloatReference(), style, selector);
        }
        return cssFloatReference;
    }

    public final CssFloatDefer getFloatDefer() {
        if (cssFloatDefer == null) {
            cssFloatDefer = (CssFloatDefer) style.CascadingOrder(new CssFloatDefer(), style, selector);
        }
        return cssFloatDefer;
    }

    public final CssFloatOffset getFloatOffset() {
        if (cssFloatOffset == null) {
            cssFloatOffset = (CssFloatOffset) style.CascadingOrder(new CssFloatOffset(), style, selector);
        }
        return cssFloatOffset;
    }
    ///

    /**
     * Returns the name of the actual selector
     */
    public String getSelector() {
        return (selector.getElement().toLowerCase());
    }

	/*    public boolean isRubyText() {
          return(((selector.getElement()).toLowerCase() == "ruby") ||
		  ((selector.getElement()).toLowerCase() == "rb") ||
		  ((selector.getElement()).toLowerCase() == "rt") ||
		  ((selector.getElement()).toLowerCase() == "rbc") ||
		  ((selector.getElement()).toLowerCase() == "rtc"));
		  }

		  public void findConflicts(ApplContext ac) {
		  if ((cssRubyPosition != null)
		  && (selector != null)
		  && (!isRubyText())) {
		  warnings.addWarning(new Warning(cssRubyPosition,
		  "ruby-text", 1, ac));
		  }

		  if ((cssRubyOverhang != null)
		  && (selector != null)
		  && (!isRubyText())) {
		  warnings.addWarning(new Warning(cssRubyOverhang,
		  "ruby-text", 1, ac));
		  }
		  }
		*/

    /**
     * Find conflicts in this Style
     * For the float elements
     *
     * @param warnings     For warnings reports.
     * @param allSelectors All contexts is the entire style sheet.
     */
    private void findConflictsBlockElements(ApplContext ac, Warnings warnings,
                                            CssSelectors selector,
                                            CssSelectors[] allSelectors) {
        if (Util.fromHTMLFile) {
            if ((selector != null) &&
                    (!selector.isBlockLevelElement())) {
                if (cssColumnCount != null) {
                    warnings.addWarning(new Warning(cssColumnCount,
                            "block-level", 1, ac));
                }
                if (cssColumnGap != null) {
                    warnings.addWarning(new Warning(cssColumnGap,
                            "block-level", 1, ac));
                }
                if (cssColumnSpan != null) {
                    warnings.addWarning(new Warning(cssColumnSpan,
                            "block-level", 1, ac));
                }
                if (cssColumnWidth != null) {
                    warnings.addWarning(new Warning(cssColumnWidth,
                            "block-level", 1, ac));
                }
            }
        }
    }

    /**
     * Find conflicts in this Style
     *
     * @param warnings     For warnings reports.
     * @param allSelectors All contexts is the entire style sheet.
     */
    public void findConflicts(ApplContext ac, Warnings warnings,
                              CssSelectors selector, CssSelectors[] allSelectors) {
        findConflictsBlockElements(ac, warnings, selector, allSelectors);
        super.findConflicts(ac, warnings, selector, allSelectors);
    }
}
