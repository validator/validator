//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css1;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssClear;
import org.w3c.css.properties.css.CssDisplay;
import org.w3c.css.properties.css.CssFloat;
import org.w3c.css.properties.css.CssListStyle;
import org.w3c.css.properties.css.CssListStyleImage;
import org.w3c.css.properties.css.CssListStylePosition;
import org.w3c.css.properties.css.CssListStyleType;
import org.w3c.css.properties.css.CssMargin;
import org.w3c.css.properties.css.CssMarginBottom;
import org.w3c.css.properties.css.CssMarginLeft;
import org.w3c.css.properties.css.CssMarginRight;
import org.w3c.css.properties.css.CssMarginTop;
import org.w3c.css.properties.css.CssPadding;
import org.w3c.css.properties.css.CssPaddingBottom;
import org.w3c.css.properties.css.CssPaddingLeft;
import org.w3c.css.properties.css.CssPaddingRight;
import org.w3c.css.properties.css.CssPaddingTop;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.properties.css.CssTextAlign;
import org.w3c.css.properties.css.CssTextDecoration;
import org.w3c.css.properties.css.CssTextIndent;
import org.w3c.css.properties.css.CssTextTransform;
import org.w3c.css.properties.css.CssVerticalAlign;
import org.w3c.css.properties.css.CssWordSpacing;
import org.w3c.css.properties.css.CssZIndex;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Util;
import org.w3c.css.util.Warning;
import org.w3c.css.util.Warnings;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * The Css1Style main class.
 */
public class Css1Style extends CssStyle {

    /**
     * Font properties
     */
    public org.w3c.css.properties.css.CssFont cssFont = new org.w3c.css.properties.css.CssFont();


	/* Color and Background properties */
    /**
     * Color property
     */
    public org.w3c.css.properties.css.CssColor cssColor;

    /**
     * background properties
     */
    public org.w3c.css.properties.css.CssBackground cssBackground = new org.w3c.css.properties.css.CssBackground();

	/* Text properties */
    /**
     * word-spacing property
     */
    public CssWordSpacing cssWordSpacing;
    /**
     * letter-spacing property
     */
    public org.w3c.css.properties.css.CssLetterSpacing cssLetterSpacing;
    /**
     * text-decoration property
     */
    public CssTextDecoration cssTextDecoration;
    /**
     * vertical-align property
     */
    public CssVerticalAlign cssVerticalAlign;

    /**
     * text-transform property
     */
    public CssTextTransform cssTextTransform;
    /**
     * text-align property
     */
    public CssTextAlign cssTextAlign;
    /**
     * text-ident property
     */
    public CssTextIndent cssTextIndent;
    public CssTextIndentMob cssTextIndentMob;

    // line-heigth : see cssFont

	/* Box properties */
    /**
     * margin properties
     */
    public CssMargin cssMargin = new CssMargin();
    /**
     * padding properties
     */
    public org.w3c.css.properties.css.CssPadding cssPadding = new org.w3c.css.properties.css.CssPadding();
    /**
     * border properties
     */
    public org.w3c.css.properties.css.CssBorder cssBorder = new org.w3c.css.properties.css.CssBorder(true);

    /**
     * width property
     */
    public org.w3c.css.properties.css.CssWidth cssWidth;
    public CssWidthMob cssWidthMob;

    /**
     * height property
     */
    public org.w3c.css.properties.css.CssHeight cssHeight;
    public CssHeightMob cssHeightMob;
    /**
     * float property
     */
    public CssFloat cssFloat;
    /**
     * clear property
     */
    public CssClear cssClear;


	/* Classification properties */
    /**
     * display property
     */
    public CssDisplay cssDisplay;

    /**
     * z-index property
     */
    public CssZIndex cssZIndex;

    /**
     * white-space property
     */
    public org.w3c.css.properties.css.CssWhiteSpace cssWhiteSpace;
    /**
     * list-style properties
     */
    public CssListStylePosition cssListStylePosition;
    public CssListStyleImage cssListStyleImage;
    public CssListStyleType cssListStyleType;
    public CssListStyle cssListStyle;


    public String[] emptyArray = {};

	/*
         * Font Properties
		 */

    /**
     * Get the font-style property
     */
    public final org.w3c.css.properties.css.CssFontStyle getFontStyle() {
        if (cssFont.fontStyle == null) {
            cssFont.fontStyle =
                    (org.w3c.css.properties.css.CssFontStyle) style.CascadingOrder(new org.w3c.css.properties.css.CssFontStyle(),
                            style, selector);
        }
        return cssFont.fontStyle;
    }

    /**
     * Get the font-variant property
     */
    public final org.w3c.css.properties.css.CssFontVariant getFontVariant() {
        if (cssFont.fontVariant == null) {
            cssFont.fontVariant =
                    (org.w3c.css.properties.css.CssFontVariant) style.CascadingOrder(new org.w3c.css.properties.css.CssFontVariant(),
                            style, selector);
        }
        return cssFont.fontVariant;
    }


    /**
     * Get the font-weight property
     */
    public final org.w3c.css.properties.css.CssFontWeight getFontWeight() {
        if (cssFont.fontWeight == null) {
            cssFont.fontWeight =
                    (org.w3c.css.properties.css.CssFontWeight) style.CascadingOrder(new org.w3c.css.properties.css.CssFontWeight(),
                            style, selector);
        }
        return cssFont.fontWeight;
    }


    /**
     * Get the font-size property
     */

    public final org.w3c.css.properties.css.CssFontSize getFontSize() {
        if (cssFont.fontSize == null) {
            cssFont.fontSize =
                    (org.w3c.css.properties.css.CssFontSize) style.CascadingOrder(new org.w3c.css.properties.css.CssFontSize(),
                            style, selector);
        }
        return cssFont.fontSize;
    }

    /**
     * Get the font-family property
     */
    public final org.w3c.css.properties.css.CssFontFamily getFontFamily() {
        if (cssFont.fontFamily == null) {
            cssFont.fontFamily =
                    (org.w3c.css.properties.css.CssFontFamily) style.CascadingOrder(new org.w3c.css.properties.css.CssFontFamily(),
                            style, selector);
        }
        return cssFont.fontFamily;
    }

    /**
     * Get the font property
     */
    public final org.w3c.css.properties.css.CssFont getFont() {
        if (cssFont.value != null) {
            // nothing
        } else {
            if (cssFont.fontStyle == null) {
                cssFont.fontStyle = getFontStyle();
            }
            if (cssFont.fontVariant == null) {
                cssFont.fontVariant = getFontVariant();
            }
            if (cssFont.fontWeight == null) {
                cssFont.fontWeight = getFontWeight();
            }
            if (cssFont.fontSize == null) {
                cssFont.fontSize = getFontSize();
            }
            if (cssFont.lineHeight == null) {
                cssFont.lineHeight = getLineHeight();
            }
            if (cssFont.fontFamily == null) {
                cssFont.fontFamily = getFontFamily();
            }
        }
        return cssFont;
    }


	/*
		 * Color and Background properties
		 */

    /**
     * Get the color property
     */
    public final org.w3c.css.properties.css.CssColor getColor() {
        if (cssColor == null) {
            cssColor = (org.w3c.css.properties.css.CssColor)
                    style.CascadingOrder(new CssColor(), style, selector);
        }
        return cssColor;
    }

    /**
     * Get the background-color property
     */
    public final org.w3c.css.properties.css.CssBackgroundColor getBackgroundColor() {
        if (cssBackground.color == null) {
            cssBackground.color =
                    (org.w3c.css.properties.css.CssBackgroundColor) style.CascadingOrder(new org.w3c.css.properties.css.CssBackgroundColor(),
                            style, selector);
        }
        return cssBackground.color;
    }

    /**
     * Get the background-image property
     */
    public final org.w3c.css.properties.css.CssBackgroundImage getBackgroundImage() {
        if (cssBackground.image == null) {
            cssBackground.image =
                    (org.w3c.css.properties.css.CssBackgroundImage) style.CascadingOrder(new org.w3c.css.properties.css.CssBackgroundImage(),
                            style, selector);
        }
        return cssBackground.image;
    }

    /**
     * Get the background-repeat property
     */
    public final org.w3c.css.properties.css.CssBackgroundRepeat getBackgroundRepeat() {
        if (cssBackground.repeat == null) {
            cssBackground.repeat =
                    (org.w3c.css.properties.css.CssBackgroundRepeat) style.CascadingOrder(new org.w3c.css.properties.css.CssBackgroundRepeat(),
                            style, selector);
        }
        return cssBackground.repeat;
    }

    /**
     * Get the background-attachment property
     */
    public final org.w3c.css.properties.css.CssBackgroundAttachment getBackgroundAttachment() {
        if (cssBackground.attachment == null) {
            cssBackground.attachment =
                    (org.w3c.css.properties.css.CssBackgroundAttachment) style.CascadingOrder(new org.w3c.css.properties.css.CssBackgroundAttachment(),
                            style, selector);
        }
        return cssBackground.attachment;
    }

    /**
     * Get the background-position property
     */
    public final org.w3c.css.properties.css.CssBackgroundPosition getBackgroundPosition() {
        if (cssBackground.position == null) {
            cssBackground.position =
                    (org.w3c.css.properties.css.CssBackgroundPosition) style.CascadingOrder(new org.w3c.css.properties.css.CssBackgroundPosition(),
                            style, selector);
        }
        return cssBackground.position;
    }

    /**
     * Get the background property
     */
    public final org.w3c.css.properties.css.CssBackground getBackground() {
        if (cssBackground.getColor() == null) {
            cssBackground.color = getBackgroundColor();
        }
        if (cssBackground.image == null) {
            cssBackground.image = getBackgroundImage();
        }
        if (cssBackground.repeat == null) {
            cssBackground.repeat = getBackgroundRepeat();
        }
        if (cssBackground.attachment == null) {
            cssBackground.attachment = getBackgroundAttachment();
        }
        if (cssBackground.position == null) {
            cssBackground.position = getBackgroundPosition();
        }
        return cssBackground;
    }

    /**
     * Get the word-spacing property
     */
    public final org.w3c.css.properties.css.CssWordSpacing getWordSpacing() {
        if (cssWordSpacing == null) {
            cssWordSpacing =
                    (org.w3c.css.properties.css.CssWordSpacing) style.CascadingOrder(new org.w3c.css.properties.css.CssWordSpacing(),
                            style, selector);
        }
        return cssWordSpacing;
    }

    /**
     * Get the letter-spacing property
     */
    public final org.w3c.css.properties.css.CssLetterSpacing getLetterSpacing() {
        if (cssLetterSpacing == null) {
            cssLetterSpacing =
                    (org.w3c.css.properties.css.CssLetterSpacing) style.CascadingOrder(new org.w3c.css.properties.css.CssLetterSpacing(),
                            style, selector);
        }
        return cssLetterSpacing;
    }

    /**
     * Get the text-decoration property
     */
    public final CssTextDecoration getTextDecoration() {
        if (cssTextDecoration == null) {
            cssTextDecoration =
                    (CssTextDecoration) style.CascadingOrder(new CssTextDecoration(),
                            style, selector);
        }
        return cssTextDecoration;
    }

    /**
     * Get the vertical-align property
     */
    public final CssVerticalAlign getVerticalAlign() {
        if (cssVerticalAlign == null) {
            cssVerticalAlign =
                    (CssVerticalAlign) style.CascadingOrder(new CssVerticalAlign(),
                            style, selector);
        }
        return cssVerticalAlign;
    }

    /**
     * Get the text-transform property
     */
    public final CssTextTransform getTextTransform() {
        if (cssTextTransform == null) {
            cssTextTransform =
                    (CssTextTransform) style.CascadingOrder(new CssTextTransform(),
                            style, selector);
        }
        return cssTextTransform;
    }

    /**
     * Get the text-align property
     */
    public final CssTextAlign getTextAlign() {
        if (cssTextAlign == null) {
            cssTextAlign =
                    (CssTextAlign) style.CascadingOrder(new CssTextAlign(),
                            style, selector);
        }
        return cssTextAlign;
    }

    /**
     * Get the text-indent property
     */
    public final CssTextIndent getTextIndent() {
        if (cssTextIndent == null) {
            cssTextIndent =
                    (CssTextIndent) style.CascadingOrder(new CssTextIndent(),
                            style, selector);
        }
        return cssTextIndent;
    }

    public final CssTextIndentMob getTextIndentMob() {
        if (cssTextIndentMob == null) {
            cssTextIndentMob =
                    (CssTextIndentMob) style.CascadingOrder(new CssTextIndentMob(),
                            style, selector);
        }
        return cssTextIndentMob;
    }

    /**
     * Get the line-height property
     */
    public final org.w3c.css.properties.css.CssLineHeight getLineHeight() {
        if (cssFont.lineHeight == null) {
            cssFont.lineHeight =
                    (org.w3c.css.properties.css.CssLineHeight) style.CascadingOrder(new CssLineHeight(),
                            style, selector);
        }
        return cssFont.lineHeight;
    }

	/*
		 * Box properties
		 */

    /**
     * Get the margin-top property
     */
    public final CssMarginTop getMarginTop() {
        if (cssMargin.marginTop == null) {
            cssMargin.marginTop =
                    (CssMarginTop) style.CascadingOrder(new CssMarginTop(),
                            style, selector);
        }
        return cssMargin.marginTop;
    }

    /**
     * Get the margin-right property
     */
    public final CssMarginRight getMarginRight() {
        if (cssMargin.marginRight == null) {
            cssMargin.marginRight =
                    (CssMarginRight) style.CascadingOrder(new CssMarginRight(),
                            style, selector);
        }
        return cssMargin.marginRight;
    }

    /**
     * Get the margin-bottom property
     */
    public final CssMarginBottom getMarginBottom() {
        if (cssMargin.marginBottom == null) {
            cssMargin.marginBottom =
                    (CssMarginBottom) style.CascadingOrder(new CssMarginBottom(),
                            style, selector);
        }
        return cssMargin.marginBottom;
    }

    /**
     * Get the margin-left property
     */
    public final CssMarginLeft getMarginLeft() {
        if (cssMargin.marginLeft == null) {
            cssMargin.marginLeft =
                    (CssMarginLeft) style.CascadingOrder(new CssMarginLeft(),
                            style, selector);
        }
        return cssMargin.marginLeft;
    }

    /**
     * Get the margin property
     */
    public final CssMargin getMargin() {
        if (cssMargin.marginTop == null)
            cssMargin.marginTop = getMarginTop();
        if (cssMargin.marginRight == null)
            cssMargin.marginRight = getMarginRight();
        if (cssMargin.marginBottom == null)
            cssMargin.marginBottom = getMarginBottom();
        if (cssMargin.marginLeft == null)
            cssMargin.marginLeft = getMarginLeft();
        return cssMargin;
    }

    /**
     * Get the padding-top property
     */
    public final CssPaddingTop getPaddingTop() {
        if (cssPadding.paddingTop == null) {
            cssPadding.paddingTop =
                    (CssPaddingTop) style.CascadingOrder(new CssPaddingTop(),
                            style, selector);
        }
        return cssPadding.paddingTop;
    }

    /**
     * Get the padding-right property
     */
    public final CssPaddingRight getPaddingRight() {
        if (cssPadding.paddingRight == null) {
            cssPadding.paddingRight =
                    (CssPaddingRight) style.CascadingOrder(new CssPaddingRight(),
                            style, selector);
        }
        return cssPadding.paddingRight;
    }

    /**
     * Get the padding-bottom property
     */
    public final CssPaddingBottom getPaddingBottom() {
        if (cssPadding.paddingBottom == null) {
            cssPadding.paddingBottom =
                    (CssPaddingBottom) style.CascadingOrder(new CssPaddingBottom(),
                            style, selector);
        }
        return cssPadding.paddingBottom;
    }

    /**
     * Get the padding-left property
     */
    public final CssPaddingLeft getPaddingLeft() {
        if (cssPadding.paddingLeft == null) {
            cssPadding.paddingLeft =
                    (CssPaddingLeft) style.CascadingOrder(new CssPaddingLeft(),
                            style, selector);
        }
        return cssPadding.paddingLeft;
    }

    /**
     * Get the padding property
     */
    public final CssPadding getPadding() {
        if (cssPadding.paddingTop == null)
            cssPadding.paddingTop = getPaddingTop();
        if (cssPadding.paddingRight == null)
            cssPadding.paddingRight = getPaddingRight();
        if (cssPadding.paddingBottom == null)
            cssPadding.paddingBottom = getPaddingBottom();
        if (cssPadding.paddingLeft == null)
            cssPadding.paddingLeft = getPaddingLeft();
        return cssPadding;
    }

    /**
     * Get the border-top-width property
     */
    public final org.w3c.css.properties.css.CssBorderTopWidth getBorderTopWidth() {
        if (cssBorder.borderWidth.top == null) {
            cssBorder.borderWidth.top =
                    (org.w3c.css.properties.css.CssBorderTopWidth) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderTopWidth(),
                            style, selector);
        }
        return cssBorder.borderWidth.top;
    }

    /**
     * Get the border-right-width property
     */
    public final org.w3c.css.properties.css.CssBorderRightWidth getBorderRightWidth() {
        if (cssBorder.borderWidth.right == null) {
            cssBorder.borderWidth.right =
                    (org.w3c.css.properties.css.CssBorderRightWidth) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderRightWidth(),
                            style, selector);
        }
        return cssBorder.borderWidth.right;
    }

    /**
     * Get the border-bottom-width property
     */
    public final org.w3c.css.properties.css.CssBorderBottomWidth getBorderBottomWidth() {
        if (cssBorder.borderWidth.bottom == null) {
            cssBorder.borderWidth.bottom =
                    (org.w3c.css.properties.css.CssBorderBottomWidth) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderBottomWidth(),
                            style, selector);
        }
        return cssBorder.borderWidth.bottom;
    }

    /**
     * Get the border-left-width property
     */
    public final org.w3c.css.properties.css.CssBorderLeftWidth getBorderLeftWidth() {
        if (cssBorder.borderWidth.left == null) {
            cssBorder.borderWidth.left =
                    (org.w3c.css.properties.css.CssBorderLeftWidth) style.CascadingOrder(new org.w3c.css.properties.css.CssBorderLeftWidth(),
                            style, selector);
        }
        return cssBorder.borderWidth.left;
    }


    /**
     * Get the border property
     */
    public final org.w3c.css.properties.css.CssBorder getBorder() {
        return cssBorder;
    }


    /**
     * Get the width property
     */
    public final org.w3c.css.properties.css.CssWidth getWidth() {
        if (cssWidth == null) {
            cssWidth =
                    (org.w3c.css.properties.css.CssWidth) style.CascadingOrder(new CssWidth(), style, selector);
        }
        return cssWidth;
    }

    public final CssWidthMob getWidthMob() {
        if (cssWidthMob == null) {
            cssWidthMob =
                    (CssWidthMob) style.CascadingOrder(new CssWidthMob(), style, selector);
        }
        return cssWidthMob;
    }

    /**
     * Get the height property
     */
    public final org.w3c.css.properties.css.CssHeight getHeight() {
        if (cssHeight == null) {
            cssHeight =
                    (org.w3c.css.properties.css.CssHeight) style.CascadingOrder(new org.w3c.css.properties.css.CssHeight(), style, selector);
        }
        return cssHeight;
    }

    public final CssHeightMob getHeightMob() {
        if (cssHeightMob == null) {
            cssHeightMob =
                    (CssHeightMob) style.CascadingOrder(new CssHeightMob(), style, selector);
        }
        return cssHeightMob;
    }

    /**
     * Get the float property
     */
    public final CssFloat getFloat() {
        if (cssFloat == null) {
            cssFloat =
                    (CssFloat) style.CascadingOrder(new CssFloat(), style, selector);
        }
        return cssFloat;
    }

    /**
     * Get the clear property
     */
    public final CssClear getClear() {
        if (cssClear == null) {
            cssClear =
                    (CssClear) style.CascadingOrder(new CssClear(), style, selector);
        }
        return cssClear;
    }

	/*
		 * Classification properties
		 */

    /**
     * Get the display property
     */
    public final CssDisplay getDisplay() {
        if (cssDisplay == null) {
            cssDisplay =
                    (CssDisplay) style.CascadingOrder(new CssDisplay(), style, selector);
        }
        return cssDisplay;
    }

    /**
     * Get the z-index property
     */
    public final CssZIndex getZIndex() {
        if (cssZIndex == null) {
            cssZIndex =
                    (CssZIndex) style.CascadingOrder(new CssZIndex(),
                            style, selector);
        }
        return cssZIndex;
    }

    /**
     * Get the white-space property
     */
    public final org.w3c.css.properties.css.CssWhiteSpace getWhiteSpace() {
        if (cssWhiteSpace == null) {
            cssWhiteSpace =
                    (org.w3c.css.properties.css.CssWhiteSpace) style.CascadingOrder(new org.w3c.css.properties.css.CssWhiteSpace(),
                            style, selector);
        }
        return cssWhiteSpace;
    }

    /**
     * Get the list-style-type property
     */
    public final CssListStyleType getListStyleType() {
        if (cssListStyleType == null) {
            cssListStyleType =
                    (CssListStyleType) style.CascadingOrder(new CssListStyleType(),
                            style, selector);
        }
        return cssListStyleType;
    }

    /**
     * Get the list-style-image property
     */
    public final CssListStyleImage getListStyleImage() {
        if (cssListStyleImage == null) {
            cssListStyleImage =
                    (CssListStyleImage) style.CascadingOrder(new CssListStyleImage(),
                            style, selector);
        }
        return cssListStyleImage;
    }

    /**
     * Get the list-style-position property
     */
    public final CssListStylePosition getListStylePosition() {
        if (cssListStylePosition == null) {
            cssListStylePosition =
                    (CssListStylePosition)
                            style.CascadingOrder(new CssListStylePosition(),
                                    style, selector);
        }
        return cssListStylePosition;
    }

    /**
     * Get the list-style property
     */
    public final CssListStyle getListStyle() {
        if (cssListStyle == null) {
            cssListStyle = (CssListStyle) style.CascadingOrder(new CssListStyle(),
                    style, selector);
        }
        return cssListStyle;
    }

    /**
     * Find conflicts in this Style
     * For the 'font-family' property
     *
     * @param warnings     For warnings reports.
     * @param allSelectors All contexts is the entire style sheet.
     */
    private void findConflictsFontFamily(ApplContext ac, Warnings warnings,
                                         CssSelectors selector,
                                         CssSelectors[] allSelectors) {
        if (cssFont.fontFamily != null) {
            if (!cssFont.fontFamily.hasGenericFamily()) {
                warnings.addWarning(new Warning(cssFont.fontFamily,
                        "no-generic-family", 2, ac));
            }
        }
    }

    /**
     * Find conflicts in this Style
     * For the float elements
     *
     * @param warnings     For warnings reports.
     * @param allSelectors All contexts is the entire style sheet.
     */
    private void findConflictsFloatElements(ApplContext ac, Warnings warnings,
                                            CssSelectors selector,
                                            CssSelectors[] allSelectors) {
        if (cssWidth == null) {
            String selectorElement = selector.getElement();
            // for null element, or element without intrinsic width
            if ((selectorElement == null) ||
                    (!selectorElement.equals("html") &&
                            !selectorElement.equals("img") &&
                            !selectorElement.equals("input") &&
                            !selectorElement.equals("object") &&
                            !selectorElement.equals("textarea") &&
                            !selectorElement.equals("select")
                    )) {
                // float needs a declared width
                warnings.addWarning(new Warning(cssFloat, "float-no-width",
                        1, ac));
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

        // check conflicts for 'font-family'
        findConflictsFontFamily(ac, warnings, selector, allSelectors);

        // warning for floats
        if (cssFloat != null) {
            //findConflictsFloatElements(ac, warnings, selector, allSelectors);
            // disabled per thread on www-validator-css
            // and changes in CSS2.1 removing this constraint
        }

        if (cssBackground.getColor() != null) {
            org.w3c.css.properties.css.CssColor fgColor = cssColor;
            if (fgColor != null) {
                if (cssBackground.getColor().equals(fgColor.getColor())) {
                    // background and color can't have the same color
                    warnings.addWarning(new Warning(cssBackground.color,
                            "same-colors", 1, fgColor, ac));
                } else if (cssBackground.getColor().equals(
                        CssProperty.transparent)) {
//		  It's better to have a background color with a color
                    warnings.addWarning(new Warning(
                            fgColor, "no-background-color", 2, emptyArray, ac));
                }
            } else {
                CssValue color = cssBackground.getColor();

                if (!color.equals(org.w3c.css.properties.css.CssBackgroundColor.transparent)) {
                    // It's better to have a color when a background is defined.
                    warnings.addWarning(new Warning(cssBackground.color,
                            "no-color", 2, emptyArray, ac));
                }
            }

            // Note : For borders, I don't look for inherited value.
            //        So I can't find same colors in two differents contexts.
            if (cssBorder.borderColor.shorthand) {
                CssValue color = (CssValue) cssBorder.borderColor.top.get();
                if (color != CssProperty.inherit
                        && cssBackground.getColor().equals(color)) {
                    // background and border-color can't have the same color
                    warnings.addWarning(new Warning(cssBackground.color,
                            "same-colors", 1,
                            cssBorder.borderColor, ac));
                }
            } else {
                if (cssBorder.borderColor.top != null) {
                    CssValue color = (CssValue) cssBorder.borderColor.top.get();
                    if (color != CssProperty.inherit
                            && cssBackground.getColor().equals(color)) {
                        // background and border-color can't have the same color
                        warnings.addWarning(new Warning(cssBackground.color,
                                "same-colors", 1,
                                cssBorder.borderColor.top, ac));
                    }
                }
                if (cssBorder.borderColor.right != null) {
                    CssValue color = (CssValue) cssBorder.borderColor.right.get();
                    if (color != CssProperty.inherit
                            && cssBackground.getColor().equals(color)) {
                        // background and border-color can't have the same color
                        warnings.addWarning(new Warning(cssBackground.color,
                                "same-colors", 1,
                                cssBorder.borderColor.right, ac));
                    }
                }
                if (cssBorder.borderColor.bottom != null) {
                    CssValue color = (CssValue) cssBorder.borderColor.bottom.get();
                    if (color != CssProperty.inherit
                            && cssBackground.getColor().equals(color)) {
                        // background and border-color can't have the same color
                        warnings.addWarning(new Warning(cssBackground.color,
                                "same-colors", 1,
                                cssBorder.borderColor.bottom, ac));
                    }
                }
                if (cssBorder.borderColor.left != null) {
                    CssValue color = (CssValue) cssBorder.borderColor.left.get();
                    if (color != CssProperty.inherit
                            && cssBackground.getColor().equals(color)) {
                        // background and border-color can't have the same color
                        warnings.addWarning(new Warning(cssBackground.color,
                                "same-colors", 1,
                                cssBorder.borderColor.left, ac));
                    }
                }
            }
        } else if (cssColor != null) {
            // It's better to have a background color with a color
            warnings.addWarning(new Warning(cssColor,
                    "no-background-color", 2, emptyArray, ac));
        }

        // now testing for % and length in padding and marging

        RelativeAndAbsolute checker = new RelativeAndAbsolute();
        CssProperty info = null;

        if (cssMargin.marginTop != null) {
            info = cssMargin.marginTop;
            checker.compute(cssMargin.marginTop.value);
        }
        if (cssMargin.marginBottom != null) {
            info = cssMargin.marginBottom;
            checker.compute(cssMargin.marginBottom.value);
        }
        if (checker.isNotRobust()) {
            warnings.addWarning(new Warning(info.getSourceFile(),
                    info.getLine(),
                    "relative-absolute", 2,
                    new String[]{"margin"}, ac));
        }
        checker.reset();

        if (cssMargin.marginRight != null) {
            info = cssMargin.marginRight;
            checker.compute(cssMargin.marginRight.value);
        }
        if (cssMargin.marginLeft != null) {
            info = cssMargin.marginLeft;
            checker.compute(cssMargin.marginLeft.value);
        }
        if (checker.isNotRobust()) {
            warnings.addWarning(new Warning(info.getSourceFile(),
                    info.getLine(),
                    "relative-absolute", 2,
                    new String[]{"margin"}, ac));
        }
        checker.reset();

        if (cssPadding.paddingTop != null && !cssPadding.paddingTop.isSoftlyInherited()) {
            info = cssPadding.paddingTop;
            checker.compute(cssPadding.paddingTop.value);
        }
        if (cssPadding.paddingBottom != null && !cssPadding.paddingBottom.isSoftlyInherited()) {
            info = cssPadding.paddingBottom;
            checker.compute(cssPadding.paddingBottom.value);
        }
        if (checker.isNotRobust()) {
            warnings.addWarning(new Warning(info.getSourceFile(),
                    info.getLine(),
                    "relative-absolute", 2,
                    new String[]{"padding"}, ac));
        }
        checker.reset();

        if (cssPadding.paddingRight != null && !cssPadding.paddingRight.isSoftlyInherited()) {
            info = cssPadding.paddingRight;
            checker.compute(cssPadding.paddingRight.value);
        }
        if (cssPadding.paddingLeft != null && !cssPadding.paddingLeft.isSoftlyInherited()) {
            info = cssPadding.paddingLeft;
            checker.compute(cssPadding.paddingLeft.value);
        }

        if (checker.isNotRobust()) {
            warnings.addWarning(new Warning(info.getSourceFile(),
                    info.getLine(),
                    "relative-absolute", 2,
                    new String[]{"padding"}, ac));
        }

        if (Util.fromHTMLFile) {
            if ((cssTextIndent != null)
                    && (selector != null)
                    && (!selector.isBlockLevelElement())) {
                warnings.addWarning(new Warning(cssTextIndent,
                        "block-level", 1, ac));
            }
            if ((cssTextAlign != null)
                    && (selector != null)
                    && (!selector.isBlockLevelElement())) {
                warnings.addWarning(new Warning(cssTextAlign,
                        "block-level", 1, ac));
            }
            if ((cssWhiteSpace != null)
                    && (selector != null)
                    && (!selector.isBlockLevelElement())) {
                warnings.addWarning(new Warning(cssWhiteSpace,
                        "block-level", 1, ac));
            }
        }
    }
}

class RelativeAndAbsolute {
    boolean relative = false;
    boolean absolute = false;

    final void reset() {
        relative = false;
        absolute = false;
    }

    final boolean isNotRobust() {
        return relative && absolute;
    }

    final void compute(CssValue value) {
        switch (value.getType()) {
            case CssTypes.CSS_PERCENTAGE:
                // FIXME, it depends on the unit of the parent in the cascade.
                try {
                    CssCheckableValue percent = value.getCheckableValue();
                    if (!percent.isZero()) {
                        relative = true;
                    }
                } catch (InvalidParamException ex) {
                    // let's consider it false then...
                }
                break;
            case CssTypes.CSS_LENGTH:
                try {
                    CssCheckableValue length = value.getCheckableValue();
                    // 0 is always 0, no need to check
                    if (!length.isZero()) {
                        if (length.getRawType() == CssTypes.CSS_LENGTH) {
                            if (length.getLength().isAbsolute()) {
                                absolute = true;
                            } else {
                                relative = true;
                            }
                        }
                    }
                } catch (InvalidParamException ipe) {
                }
                break;
            default:
				/* should never happen */
        }
    }
}
