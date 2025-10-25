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

import java.util.Locale;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @version $Revision$
 */
public class CssColor extends CssValue {

    public static final int type = CssTypes.CSS_COLOR;

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
        }
        return "*invalid*";
    }

    public void setLightDark(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        if ((exp == null) || (exp.getCount() != 2)) {
              throw new InvalidParamException("invalid-color", ac);
        }
        LightDark ld = new LightDark();
        CssValue l = exp.getValue();
        char op = exp.getOperator();
        if (l == null || op != COMMA) {
            throw new InvalidParamException("invalid-color", ac);
        }
        exp.next();
        CssValue d = exp.getValue();
        ld.setLight(ac, l);
        ld.setDark(ac, d);
        this.lightdark = ld;
        exp.next();
    }

    public void setRGBColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        boolean isCss3 = (ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0);
        if (!isCss3) {
            setLegacyRGBColor(ac, exp);
        } else {
            setModernRGBColor(ac, exp);
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
        CssValue val;
        char op;
        color = null;
        rgba = new RGBA("rgb");
        boolean separator_space = true;

        // check for variables
        if (exp.hasCssVariable()) {
            // don't check value then
            markCssVariable();

            val = exp.getValue();
            op = exp.getOperator();
            separator_space = (op == SPACE);
            rgba.setModernStyle(separator_space);

            if (val == null || (!separator_space && (op != COMMA))) {
                // don't throw, perhaps a warning instead? FIXME
                // throw new InvalidParamException("invalid-color", ac);
            }
            rgba.vr = val;

            exp.next();
            val = exp.getValue();
            op = exp.getOperator();

            if (val == null || (separator_space && (op != SPACE)) ||
                    (!separator_space && (op != COMMA))) {
                // don't throw, perhaps a warning instead? FIXME
                // throw new InvalidParamException("invalid-color", ac);
            }
            rgba.vg = val;
            exp.next();
            val = exp.getValue();
            op = exp.getOperator();

            rgba.vb = val;
            exp.next();

            if (!exp.end()) {
                // care for old syntax
                if (op == COMMA && !separator_space) {
                    val = exp.getValue();
                    rgba.va = val;
                } else {
                    // otherwise modern syntax
                    if (op != SPACE) {
                        // don't throw, perhaps a warning instead? FIXME
                        //         throw new InvalidParamException("invalid-color", ac);
                    }
                    // now we need an alpha.
                    val = exp.getValue();
                    op = exp.getOperator();

                    if (val.getType() != CssTypes.CSS_SWITCH) {
                        // don't throw, perhaps a warning instead? FIXME
//                        throw new InvalidParamException("rgb", val, ac);
                    }
                    if (op != SPACE) {
                        // don't throw, perhaps a warning instead? FIXME
//                        throw new InvalidParamException("invalid-color", ac);
                    }
                    exp.next();
                    // now we get the alpha value
                    if (exp.end()) {
                        // don't throw, perhaps a warning instead? FIXME
//                        throw new InvalidParamException("rgb", exp.getValue(), ac);
                    }
                    val = exp.getValue();
                    rgba.va = val;
                }
                exp.next();
            }
            return;
        }

        val = exp.getValue();
        op = exp.getOperator();
        separator_space = (op == SPACE);
        rgba.setModernStyle(separator_space);

        if (val == null || (!separator_space && (op != COMMA))) {
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                rgba.setPercent(false);
                rgba.setRed(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                rgba.setPercent(true);
                rgba.setRed(ac, val);
                break;
            default:
                throw new InvalidParamException("rgb", val, ac);
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();

        if (val == null || (separator_space && (op != SPACE)) ||
                (!separator_space && (op != COMMA))) {
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (rgba.isPercent()) {
                    throw new InvalidParamException("percent", val, ac);
                }
                rgba.setGreen(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (!rgba.isPercent()) {
                    throw new InvalidParamException("integer", val, ac);
                }
                rgba.setGreen(ac, val);
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
                if (rgba.isPercent()) {
                    throw new InvalidParamException("percent", val, ac);
                }
                rgba.setBlue(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (!rgba.isPercent()) {
                    throw new InvalidParamException("integer", val, ac);
                }
                rgba.setBlue(ac, val);
                break;
            default:
                throw new InvalidParamException("rgb", val, ac);
        }
        exp.next();

        // check for optional alpha channel
        if (!exp.end()) {
            // care for old syntax
            if (op == COMMA && !separator_space) {
                val = exp.getValue();
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                        rgba.setAlpha(ac, val);
                        break;
                    default:
                        throw new InvalidParamException("rgb", val, ac);
                }
            } else {
                // otherwise modern syntax
                if (op != SPACE) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                // now we need an alpha.
                val = exp.getValue();
                op = exp.getOperator();

                if (val.getType() != CssTypes.CSS_SWITCH) {
                    throw new InvalidParamException("rgb", val, ac);
                }
                if (op != SPACE) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                exp.next();
                // now we get the alpha value
                if (exp.end()) {
                    throw new InvalidParamException("rgb", exp.getValue(), ac);
                }
                val = exp.getValue();
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                        rgba.setAlpha(ac, val);
                        break;
                    default:
                        throw new InvalidParamException("rgb", val, ac);
                }
            }
            exp.next();

            if (!exp.end()) {
                throw new InvalidParamException("rgb", exp.getValue(), ac);
            }
        }
    }

    /**
     * Parse a HSL color.
     * format hsl( <percentage>{3} [ / <alpha-value> ]? ) |
     * hsl( <number>{3} [ / <alpha-value> ]? )
     */
    public void setHSLColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // HSL defined in CSS3 and onward
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("hsl(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }

        CssValue val = exp.getValue();
        char op = exp.getOperator();
        color = null;
        hsl = new HSL();
        boolean separator_space = (op == SPACE);

        if (exp.hasCssVariable()) {
            markCssVariable();
            if (exp.getCount() < 3) {
                // check if we can expand
                while (!exp.end()) {
                    val = exp.getValue();
                    if (val.getRawType() == CssTypes.CSS_VARIABLE) {
                        CssExpression varexp = ((CssVariable) val).getVariableExpression();
                        if ((varexp != null) && (varexp.getCount() > 1)) {
                            // TODO something fancy, merging expression
                            return;
                        }
                    }
                    exp.next();
                }
                return;
            }
        }

        if (val == null || (!separator_space && (op != COMMA))) {
            if (!hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
        }

        // H
        switch (val.getType()) {
            case CssTypes.CSS_ANGLE:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                hsl.setHue(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();

        if (val == null || (separator_space && (op != SPACE)) ||
                (!separator_space && (op != COMMA))) {
            if (!hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
        }

        // S
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                hsl.setSaturation(ac, val);
                break;
            default:
                exp.starts();
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();

        if (val == null && !hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }

        // L
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                hsl.setLightness(ac, val);
                break;
            default:
                exp.starts();
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
        }

        exp.next();

        // check for optional alpha channel
        if (!exp.end()) {
            // care for old syntax
            if (op == COMMA && !separator_space) {
                val = exp.getValue();
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                    case CssTypes.CSS_VARIABLE:
                        hsl.setAlpha(ac, val);
                        break;
                    default:
                        exp.starts();
                        if (!hasCssVariable()) {
                            throw new InvalidParamException("colorfunc", val, "HSL", ac);
                        }
                }
            } else {
                // otherwise modern syntax
                if (op != SPACE && !hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                // now we need an alpha.
                val = exp.getValue();
                op = exp.getOperator();

                if (val.getType() != CssTypes.CSS_SWITCH && !hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
                if (op != SPACE && !hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                exp.next();
                // now we get the alpha value
                if (exp.end() && !hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", exp.getValue(), "HSL", ac);
                }
                val = exp.getValue();
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                    case CssTypes.CSS_VARIABLE:
                        hsl.setAlpha(ac, val);
                        break;
                    default:
                        if (!hasCssVariable()) {
                            throw new InvalidParamException("colorfunc", val, "HSL", ac);
                        }
                }
            }
            exp.next();

            if (!exp.end() && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", exp.getValue(), "HSL", ac);
            }
        }
    }

    /**
     * Parse a RGB color.
     * format #(3-6)<hexnum>
     */
    public void setShortRGBColor(ApplContext ac, String s)
            throws InvalidParamException {
        int r;
        int g;
        int b;
        int a;
        int v;
        int idx;
        boolean islong;
        boolean hasAlpha = false;
        boolean isCss3;

        isCss3 = (ac.getCssVersion().compareTo(CssVersion.CSS3) >= 0);
        idx = 1;
        a = 0;
        switch (s.length()) {
            case 5:
                a = Character.digit(s.charAt(4), 16);
                if (!isCss3 || a < 0) {
                    throw new InvalidParamException("rgb", s, ac);
                }
                hasAlpha = true;
            case 4:
                r = Character.digit(s.charAt(idx++), 16);
                g = Character.digit(s.charAt(idx++), 16);
                b = Character.digit(s.charAt(idx++), 16);
                if (r < 0 || g < 0 || b < 0) {
                    throw new InvalidParamException("rgb", s, ac);
                }
                break;
            case 9:
                a = Character.digit(s.charAt(7), 16);
                v = Character.digit(s.charAt(8), 16);
                if (!isCss3 || a < 0 || v < 0) {
                    throw new InvalidParamException("rgb", s, ac);
                }
                a = (a << 4) + v;
                hasAlpha = true;
            case 7:
                r = Character.digit(s.charAt(idx++), 16);
                v = Character.digit(s.charAt(idx++), 16);
                if (r < 0 || v < 0) {
                    throw new InvalidParamException("rgb", s, ac);
                }
                r = (r << 4) + v;
                g = Character.digit(s.charAt(idx++), 16);
                v = Character.digit(s.charAt(idx++), 16);
                if (g < 0 || v < 0) {
                    throw new InvalidParamException("rgb", s, ac);
                }
                g = (g << 4) + v;
                b = Character.digit(s.charAt(idx++), 16);
                v = Character.digit(s.charAt(idx++), 16);
                if (b < 0 || v < 0) {
                    throw new InvalidParamException("rgb", s, ac);
                }
                b = (b << 4) + v;
                break;
            default:
                throw new InvalidParamException("rgb", s, ac);
        }

        color = null;
        if (hasAlpha) {
            rgba = new RGBA(isCss3, r, g, b, a);
            rgba.setRepresentationString(s);
        } else {
            rgb = new RGB(isCss3, r, g, b);
            // we force the specific display of it
            rgb.setRepresentationString(s);
        }
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
                rgb = CssColorCSS3.getRGB(lower_s);
                if (rgb != null) {
                    color = lower_s;
                    break;
                }
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
        // RGBA defined in CSS3 and onward
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("rgba(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        setModernRGBColor(ac, exp);
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
        // HWB defined in CSSColor Level 4 and onward, currently used in the CSS level
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("hwb(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        if (exp.hasCssVariable()) {
            markCssVariable();
        }

        color = null;
        hwb = new HWB();

        CssValue val = exp.getValue();
        char op = exp.getOperator();
        // H
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_ANGLE:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                hwb.setHue(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HWB", ac);
                }
        }

        // W
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                hwb.setWhiteness(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "HWB", ac);
                }
        }

        // B
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val == null || (op != SPACE && exp.getRemainingCount() > 1)) {
            if (!hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("invalid-color", ac);
            }
        }
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                hwb.setBlackness(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "HWB", ac);
                }
        }

        hwb.normalize();

        // A
        exp.next();
        if (!exp.end()) {
            if (op != SPACE && !hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            // now we need an alpha.
            val = exp.getValue();
            op = exp.getOperator();

            if ((val.getType() != CssTypes.CSS_SWITCH) && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "HWB", ac);
            }
            if (op != SPACE && !hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            exp.next();
            // now we get the alpha value
            val = exp.getValue();
            if (val == null && !hasCssVariable()) {
                throw new InvalidParamException("invalid-color", exp.toStringFromStart(), ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_VARIABLE:
                    hwb.setAlpha(ac, val);
                    break;
                default:
                    if (!hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "HWB", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end()) {
            if (!hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "HWB", ac);
            }
        }
    }


    public void setLABColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // HWB defined in CSSColor Level 4 and onward, currently used in the CSS level
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("lab(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }

        if (exp.hasCssVariable()) {
            markCssVariable();
            // we still parse variables as they will be ignored
            // we check the delimiter syntax, and other failures
        }

        color = null;
        lab = new LAB();
        CssValue val = exp.getValue();
        char op = exp.getOperator();
        // L
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp, "Lab", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                lab.setL(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "Lab", ac);
                }
        }

        // A
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                lab.setA(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "Lab", ac);
                }
        }

        // B
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val == null) {
            if (!hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp, "Lab", ac);
            }
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                lab.setB(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "Lab", ac);
                }
        }

        exp.next();
        if (!exp.end()) {
            if (op != SPACE && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", op, "Lab", ac);
            }
            // now we need an alpha.
            val = exp.getValue();
            op = exp.getOperator();

            if ((val.getType() != CssTypes.CSS_SWITCH) && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "Lab", ac);
            }
            if (op != SPACE && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "Lab", ac);
            }
            exp.next();
            // now we get the alpha value
            val = exp.getValue();
            if ((val == null) && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "Lab", ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_VARIABLE:
                    lab.setAlpha(ac, val);
                    break;
                default:
                    if (!hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "Lab", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end()) {
            exp.starts();
            if (!hasCssVariable()) {
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "Lab", ac);
            }
        }
    }


    public void setLCHColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // HWB defined in CSSColor Level 4 and onward, currently used in the CSS level
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("lch(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        if (exp.hasCssVariable()) {
            markCssVariable();
        }

        color = null;
        lch = new LCH();
        CssValue val = exp.getValue();
        char op = exp.getOperator();
        // L
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                lch.setL(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
        }

        // A
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                lch.setC(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
        }

        // B
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null) && !hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "LCH", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_ANGLE:
            case CssTypes.CSS_VARIABLE:
                lch.setH(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
        }

        exp.next();
        if (!exp.end()) {
            if ((op != SPACE) && !hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            // now we need an alpha.
            val = exp.getValue();
            op = exp.getOperator();

            if ((val.getType() != CssTypes.CSS_SWITCH) && !hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "LCH", ac);
            }
            if ((op != SPACE) && !hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            exp.next();
            // now we get the alpha value
            val = exp.getValue();
            if (val == null) {
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "LCH", ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_VARIABLE:
                    lch.setAlpha(ac, val);
                    break;
                default:
                    if (!hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "LCH", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end()) {
            if (!hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "LCH", ac);
            }
        }
    }


    public void setDeviceCMYKColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // HWB defined in CSSColor Level 4 and onward, currently used in the CSS level
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("device-cmyk(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }

        color = null;
        cmyk = new DeviceCMYK();
        CssValue val = exp.getValue();
        char op = exp.getOperator();
        boolean gotFallback = false;

        if (exp.hasCssVariable()) {
            markCssVariable();
        }
        // C
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setC(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }

        // M
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setM(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }

        // Y
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null) && !hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setY(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }
        // K
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null) && !hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setK(ac, val);
                break;
            default:
                if (!hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }

        exp.next();
        if (!exp.end()) {
            if ((op == SPACE) && !hasCssVariable()) {
                // now we need an alpha.
                val = exp.getValue();
                op = exp.getOperator();

                if ((val.getType() != CssTypes.CSS_SWITCH) && !hasCssVariable()) {
                    throw new InvalidParamException("rgb", val, ac);
                }
                if ((op != SPACE) && !hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                exp.next();
                // now we get the alpha value
                val = exp.getValue();
                if ((val == null) && !hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", exp.toStringFromStart(), ac);
                }
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                    case CssTypes.CSS_VARIABLE:
                        cmyk.setAlpha(ac, val);
                        break;
                    default:
                        if (!hasCssVariable()) {
                            exp.starts();
                            throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                        }
                }
                // need to check if we get a comma after this.
                op = exp.getOperator();
                exp.next();
            }
            if (op == COMMA) {
                //the optional fallback
                if (exp.end() && !hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
                }
                val = exp.getValue();
                cmyk.setFallbackColor(ac, val);
                exp.next();
            }
        }
        // extra values?
        if (!exp.end() && !hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
        }
    }

    /**
     * Parse a LightDark color.
     * format: light-dark( <color>, <color>) [ / <alpha-value> ]? ) |
     */
    public void setLightDarkColor(ApplContext ac, CssExpression exp)
            throws InvalidParamException {
        // light-dark defined in CSS3 and onward
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("light-dark(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        lightdark = new LightDark();
    }
}

