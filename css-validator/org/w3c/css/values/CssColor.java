//
// $Id$
// From Philippe Le Hegaret (Philippe.Le_Hegaret@sophia.inria.fr)
//
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.color.ColorMix;
import org.w3c.css.values.color.CssColorCSS1;
import org.w3c.css.values.color.CssColorCSS2;
import org.w3c.css.values.color.CssColorCSS21;
import org.w3c.css.values.color.CssColorCSS3;
import org.w3c.css.values.color.DeviceCMYK;
import org.w3c.css.values.color.HSL;
import org.w3c.css.values.color.HWB;
import org.w3c.css.values.color.LAB;
import org.w3c.css.values.color.LCH;
import org.w3c.css.values.color.LightDark;
import org.w3c.css.values.color.OKLAB;
import org.w3c.css.values.color.OKLCH;
import org.w3c.css.values.color.RGB;
import org.w3c.css.values.color.RGBA;

import java.util.Locale;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @version $Revision$
 */
public class CssColor extends CssValue {

    public static final int type = CssTypes.CSS_COLOR;
    public static final CssIdent relative = CssIdent.getIdent("from");
    public static final CssIdent none = CssIdent.getIdent("none");

    public final int getType() {
        return type;
    }

    Object color = null;
    // FIXME TODO, replace with one color type + one type for converted color for comparison
    RGB rgb = null;
    RGBA rgba = null;
    HSL hsl = null;
    HWB hwb = null;
    LAB lab = null;
    LCH lch = null;
    DeviceCMYK cmyk = null;
    LightDark lightdark = null;
    ColorMix colormix = null;

    boolean contains_variable = false;

    public boolean hasCssVariable() {
        return contains_variable;
    }

    public void markCssVariable() {
        contains_variable = true;
    }

    /**
     * Create a new CssColor.
     */
    public CssColor() {
    }

    /**
     * Create a new CssColor with a color name.
     *
     * @param s The name color.
     * @throws InvalidParamException the color is incorrect
     */
    public CssColor(ApplContext ac, String s) throws InvalidParamException {
        //	setIdentColor(s.toLowerCase(), ac);
        setIdentColor(ac, s);
    }

    /**
     * Set the value from a defined color RBG.
     *
     * @param s the string representation of the color.
     * @throws InvalidParamException the color is incorrect.
     */
    public void set(String s, ApplContext ac) throws InvalidParamException {
        if (s.charAt(0) == '#') {
            setShortRGBColor(ac, s.toLowerCase());
        } else {
            setIdentColor(ac, s);
        }
    }

    /**
     * Return the internal value.
     */
    public Object get() {
        if (color != null) {
            return color;
        } else {
            return rgb;
        }
    }


    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (color != null) {
            return color.toString();
        } else if (rgb != null) {
            return rgb.toString();
        } else if (rgba != null) {
            return rgba.toString();
        } else if (hsl != null) {
            return hsl.toString();
        } else if (hwb != null) {
            return hwb.toString();
        } else if (lab != null) {
            return lab.toString();
        } else if (lch != null) {
            return lch.toString();
        } else if (cmyk != null) {
            return cmyk.toString();
        } else if (lightdark != null) {
            return lightdark.toString();
        } else if (colormix != null) {
            return colormix.toString();
        }
        return "*invalid*";
    }

    public void setLightDark(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        lightdark = LightDark.parseLightDark(ac, exp, this);

    }

    public void setRGBColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        boolean isCss3 = (ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0);
        if (!isCss3) {
            setLegacyRGBColor(ac, exp);
        } else {
            rgba = RGBA.parseModernRGBA(ac, exp, this, "rgb(");
        }
    }

    /**
     * Parse a RGB color.
     * format rgb(<num>%?, <num>%?, <num>%?)
     */
    public void setLegacyRGBColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        CssValue val = exp.getValue();
        char op = exp.getOperator();
        color = null;
        rgb = new RGB();

        if (val == null || op != COMMA) {
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                rgb.setPercent(false);
                rgb.setRed(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                rgb.setPercent(true);
                rgb.setRed(ac, val);
                break;
            default:
                throw new InvalidParamException("rgb", val, ac);
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();

        if (val == null || op != COMMA) {
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (rgb.isPercent()) {
                    throw new InvalidParamException("percent", val, ac);
                }
                rgb.setGreen(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (!rgb.isPercent()) {
                    throw new InvalidParamException("integer", val, ac);
                }
                rgb.setGreen(ac, val);
                break;
            default:
                throw new InvalidParamException("rgb", val, ac);
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();

        if (val == null) {
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (rgb.isPercent()) {
                    throw new InvalidParamException("percent", val, ac);
                }
                rgb.setBlue(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (!rgb.isPercent()) {
                    throw new InvalidParamException("integer", val, ac);
                }
                rgb.setBlue(ac, val);
                break;
            default:
                throw new InvalidParamException("rgb", val, ac);
        }

        exp.next();
        if (exp.getValue() != null) {
            throw new InvalidParamException("rgb", exp.getValue(), ac);
        }
    }

    /**
     * Parse a RGB color.
     * format rgb( <percentage>{3} [ / <alpha-value> ]? ) |
     * rgb( <number>{3} [ / <alpha-value> ]? )
     */
    public void setModernRGBColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {

        rgba = RGBA.parseModernRGBA(ac, exp, this, "rgb");
    }

    /**
     * Parse a HSL color
     */
    public void setHSLColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        hsl = HSL.parseHSL(ac, exp, this);
    }

    /**
     * Parse a HSLA color
     */
    public void setHSLAColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        hsl = HSL.parseHSL(ac, exp, this);
        hsl.setFunctionName("hsla");
    }

    /**
     * Parse a RGB color.
     * format #(3-6)<hexnum>
     */
    public void setShortRGBColor(ApplContext ac, String s)
            throws InvalidParamException {
        rgba = RGBA.parseShortRGBColor(ac, s);
        color = null;
    }

    /**
     * Parse an ident color.
     */
    protected void setIdentColor(ApplContext ac, String s)
            throws InvalidParamException {
        String lower_s = s.toLowerCase(Locale.ENGLISH);
        switch (ac.getCssVersion()) {
            case CSS1:
                rgb = CssColorCSS1.getRGB(lower_s);
                if (rgb == null) {
                    throw new InvalidParamException("value", s, "color", ac);
                }
                color = lower_s;
                break;
            case CSS2:
                rgb = CssColorCSS2.getRGB(lower_s);
                if (rgb == null) {
                    color = CssColorCSS2.getSystem(lower_s);
                    if (color == null) {
                        throw new InvalidParamException("value", s, "color", ac);
                    }
                } else {
                    color = lower_s;
                }
                break;
            case CSS21:
                rgb = CssColorCSS21.getRGB(lower_s);
                if (rgb == null) {
                    color = CssColorCSS21.getSystem(lower_s);
                    if (color == null) {
                        throw new InvalidParamException("value", s, "color", ac);
                    }
                } else {
                    color = lower_s;
                }
                break;
            case CSS3:
            case CSS_2015:
            case CSS:
                // test RGB colors, RGBA colors (transparent), deprecated system colors
                rgba = CssColorCSS3.getRGBA(lower_s);
                if (rgba != null) {
                    color = lower_s;
                    break;
                }
                color = CssColorCSS3.getSystem(lower_s);
                if (color != null) {
                    break;
                }
                color = CssColorCSS3.getDeprecatedSystem(lower_s);
                if (color != null) {
                    ac.getFrame().addWarning("deprecated_replacement", s, color.toString());
                    color = s;
                    break;
                }
                color = CssColorCSS3.getIdentColor(lower_s);
                if (color != null) {
                    break;
                }

                // inherit or current color will be handled in the property def
                throw new InvalidParamException("value", s, "color", ac);
            default:
                throw new InvalidParamException("value", s, "color", ac);
        }
    }

    /**
     * Compares two values for equality.
     *
     * @param cssColor The other value.
     */
    public boolean equals(Object cssColor) {
        if (!(cssColor instanceof CssColor)) {
            return false;
        }
        CssColor otherColor = (CssColor) cssColor;
        // FIXME we can have rgba(a,b,c,1) == rgb(a,b,c)
        if ((color != null) && (otherColor.color != null)) {
            return color.equals(otherColor.color);
        } else if ((rgb != null) && (otherColor.rgb != null)) {
            return rgb.equals(otherColor.rgb);
        } else if ((rgba != null) && (otherColor.rgba != null)) {
            return rgba.equals(otherColor.rgba);
        } else if ((hsl != null) && (otherColor.hsl != null)) {
            return hsl.equals(otherColor.hsl);
        } else if ((hwb != null) && (otherColor.hwb != null)) {
            return hwb.equals(otherColor.hwb);
        } else if ((lab != null) && (otherColor.lab != null)) {
            return lab.equals(otherColor.lab);
        } else if ((lch != null) && (otherColor.lch != null)) {
            return lch.equals(otherColor.lch);
        } else if ((cmyk != null) && (otherColor.cmyk != null)) {
            return cmyk.equals(otherColor.cmyk);
        }
        return false;
    }

    public void setATSCRGBAColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        rgba = new RGBA("atsc-rgba");
        __setRGBAColor(ac, exp, rgba);

    }

    public void setRGBAColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        rgba = RGBA.parseModernRGBA(ac, exp, this, "rgba(");
    }

    // use only for atsc profile, superseded by setModernRGBColor
    private void __setRGBAColor(ApplContext ac, CssExpression exp, RGBA rgba)
            throws InvalidParamException {
        CssValue val;
        char op;
        color = null;

        val = exp.getValue();
        op = exp.getOperator();
        if (val == null || op != COMMA) {
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                rgba.setRed(ac, val);
                rgba.setPercent(false);
                break;
            case CssTypes.CSS_PERCENTAGE:
                rgba.setRed(ac, val);
                rgba.setPercent(true);
                break;
            default:
                throw new InvalidParamException("rgb", val, ac); // FIXME rgba
        }
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val == null || op != COMMA) {
            throw new InvalidParamException("invalid-color", ac);
        }
        // green
        // and validate against the "percentageness"
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (rgba.isPercent()) {
                    exp.starts();
                    throw new InvalidParamException("percent", val, ac);
                }
                rgba.setGreen(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (!rgba.isPercent()) {
                    exp.starts();
                    throw new InvalidParamException("integer", val, ac);
                }
                rgba.setGreen(ac, val);
                break;
            default:
                exp.starts();
                throw new InvalidParamException("rgb", val, ac); // FIXME rgba
        }
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val == null || op != COMMA) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        // blue
        // and validate against the "percentageness"
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (rgba.isPercent()) {
                    exp.starts();
                    throw new InvalidParamException("percent", val, ac);
                }
                rgba.setBlue(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (!rgba.isPercent()) {
                    exp.starts();
                    throw new InvalidParamException("integer", val, ac);
                }
                rgba.setBlue(ac, val);
                break;
            default:
                exp.starts();
                throw new InvalidParamException("rgb", val, ac); // FIXME rgba
        }
        exp.next();
        val = exp.getValue();
        if (val == null) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                // starting with CSS Color 4
            case CssTypes.CSS_PERCENTAGE:
                rgba.setAlpha(ac, val);
                break;
            default:
                exp.starts();
                throw new InvalidParamException("rgb", val, ac); // FIXME rgba
        }
        exp.next();
        if (exp.getValue() != null) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
    }

    public void setHWBColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        hwb = HWB.parseHWBColor(ac, exp, this);
    }

    public void setLABColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        lab = LAB.parseLABColor(ac, exp, this);
    }

    public void setOKLABColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        lab = OKLAB.parseOKLABColor(ac, exp, this);
    }

    public void setLCHColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // LCH defined in CSSColor Level 4 and onward, currently used in the CSS level
        lch = LCH.parseLCHColor(ac, exp, this);
    }

    public void setOKLCHColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // OKLch defined in CSSColor Level 4 and onward, currently used in the CSS level
        lch = OKLCH.parseOKLCHColor(ac, exp, this);
    }

    public void setDeviceCMYKColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // HWB defined in CSSColor Level 5 and onward, currently used in the CSS level
        cmyk = DeviceCMYK.parseDeviceCMYK(ac, exp, this);
    }

    public void setColorMix(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        colormix = ColorMix.parseColorMix(ac, exp, this);
    }

}

