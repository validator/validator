/*
 * Copyright (c) 2001 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 *
 * $Id$
 */
package org.w3c.css.values.color;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.math.BigDecimal;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

public class RGBA extends RGB {
    static final String functionname = "rgba";

    public static final CssIdent[] colorRelativeValues;

    static {
        String[] _allowed_values = {"r", "g", "b", "alpha"};
        colorRelativeValues = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            colorRelativeValues[i++] = CssIdent.getIdent(s);
        }
    }

    public static boolean isColorRelativeValue(CssIdent ident) {
        for (CssIdent id : colorRelativeValues) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }
    
    private String output = null;
    String fname;

    CssValue va;
    boolean isRelative = false;
    CssValue fromValue;

    public static final CssValue filterAlpha(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if (val.getType() == CssTypes.CSS_NUMBER) {
                CssCheckableValue v = val.getCheckableValue();
                if (!v.isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                    CssNumber nb = new CssNumber();
                    nb.setIntValue(0);
                    return nb;
                }
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    BigDecimal pp = val.getNumber().getBigDecimalValue();
                    if (pp.compareTo(BigDecimal.ONE) > 0) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        CssNumber nb = new CssNumber();
                        nb.setIntValue(1);
                        return nb;
                    }
                }
            } else if (val.getType() == CssTypes.CSS_PERCENTAGE) {
                // This starts with CSS Color 4
                CssCheckableValue v = val.getCheckableValue();
                if (!v.isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                    CssNumber nb = new CssNumber();
                    nb.setIntValue(0);
                    return nb;
                }
                if (val.getRawType() == CssTypes.CSS_PERCENTAGE) {
                    float p = ((CssPercentage) val).floatValue();
                    if (p > 100.) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        return new CssPercentage(100);
                    }
                }
            }
        }
        return val;
    }

    public static RGBA parseShortRGBColor(ApplContext ac, String s)
            throws InvalidParamException {
        int r;
        int g;
        int b;
        int a;
        int v;
        int idx;
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
        RGBA rgba;

        if (hasAlpha) {
            rgba = new RGBA(isCss3, r, g, b, a);
            rgba.setRepresentationString(s);
        } else {
            rgba = new RGBA(isCss3, r, g, b);
            // we force the specific display of it
        }
        rgba.setRepresentationString(s);
        return rgba;
    }

    /**
     * Parse a rgb/rgba color
     *
     * @param ac
     * @param exp
     * @param caller
     * @param funcname
     * @return RGBA
     * @throws InvalidParamException
     * @spec https://www.w3.org/TR/2026/CRD-css-color-4-20260227/#rgb-functions
     * @spec https://www.w3.org/TR/2026/WD-css-color-5-20260227/#relative-RGB
     */
    public static RGBA parseModernRGBA(ApplContext ac, CssExpression exp, CssColor caller, String funcname)
            throws InvalidParamException {
        RGBA rgba = new RGBA();
        boolean gotNumber = false;
        boolean gotPercentage = false;

        if (funcname == null) {
            int paren = funcname.lastIndexOf('(');
            if (paren > 0) {
                rgba.setFunctionName(funcname.substring(0, paren));
            } else {
                rgba.setFunctionName(funcname);
            }
        }

        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(funcname).append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }

        if (exp.hasCssVariable()) {
            caller.markCssVariable();
        }

        CssValue val = exp.getValue();
        char op = exp.getOperator();

        if (val == null && !exp.hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }

        if (val.getType() == CssTypes.CSS_IDENT) {
            if (CssColor.relative.equals(val.getIdent())) {
                // we need to parse a color now
                if ((val == null || op != SPACE || rgba.isRelative) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
                rgba.isRelative = true;  // so that we get only one from
                rgba.isModernCss = true; // if we are there we must use the modern syntax
                exp.next();
                val = exp.getValue();
                op = exp.getOperator();
                CssExpression nex = new CssExpression();
                nex.addValue(val);
                CssColor c = new org.w3c.css.properties.css3.CssColor(ac, nex).getColor();
                if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
                rgba.fromValue = val;
                exp.next();
                val = exp.getValue();
                op = exp.getOperator();
                if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
            }
        }
        rgba.setModernStyle(op == SPACE);

        // R
        switch (val.getType()) {
            case CssTypes.CSS_VARIABLE:
                exp.markCssVariable();
                caller.markCssVariable();
            case CssTypes.CSS_NUMBER:
                if (gotPercentage) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
                rgba.setRed(ac, val);
                gotNumber = true;
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (gotNumber) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
                rgba.setRed(ac, val);
                gotPercentage = true;
                break;
            case CssTypes.CSS_IDENT:
                if ((CssColor.none.equals(val.getIdent()) && rgba.isModernCss) ||
                        (rgba.isRelative && isColorRelativeValue(val.getIdent()))) {
                    rgba.setRed(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
        }
        exp.next();

        // G
        val = exp.getValue();
        op = exp.getOperator();
        if (((val == null) ||
                (rgba.isModernCss && (op != SPACE)) ||
                (!rgba.isModernCss && (op != COMMA))) && !exp.hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_VARIABLE:
                exp.markCssVariable();
                caller.markCssVariable();
            case CssTypes.CSS_NUMBER:
                if (gotPercentage) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
                rgba.setGreen(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (gotNumber) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
                rgba.setGreen(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if ((CssColor.none.equals(val.getIdent()) && rgba.isModernCss) ||
                        (rgba.isRelative && isColorRelativeValue(val.getIdent()))) {
                    rgba.setGreen(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
        }
        exp.next();

        // B
        val = exp.getValue();
        op = exp.getOperator();
        // check only the value as operator will be checked if there are more values
        if ((val == null) && !exp.hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_VARIABLE:
                exp.markCssVariable();
                caller.markCssVariable();
            case CssTypes.CSS_NUMBER:
                if (gotPercentage) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
                rgba.setBlue(ac, val);
                break;
            case CssTypes.CSS_PERCENTAGE:
                if (gotNumber) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
                rgba.setBlue(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if ((CssColor.none.equals(val.getIdent()) && rgba.isModernCss) ||
                        (rgba.isRelative && isColorRelativeValue(val.getIdent()))) {
                    rgba.setBlue(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                }
        }
        exp.next();
        // check for optional alpha
        if (!exp.end()) {
            if (((rgba.isModernCss && (op != SPACE)) || (!rgba.isModernCss && (op != COMMA)))
                    && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            // modern syntax? check for a /
            if (rgba.isModernCss) {
                // now look for a switch
                // now we need an alpha.
                val = exp.getValue();
                op = exp.getOperator();

                if ((val.getType() != CssTypes.CSS_SWITCH) && !exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
                if ((op != SPACE) && !exp.hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                exp.next();
            }
            val = exp.getValue();
            op = exp.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_VARIABLE:
                    exp.markCssVariable();
                    caller.markCssVariable();
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                    rgba.setAlpha(ac, val);
                    break;
                case CssTypes.CSS_IDENT:
                    if ((CssColor.none.equals(val.getIdent()) && rgba.isModernCss) ||
                            (rgba.isRelative && isColorRelativeValue(val.getIdent()))) {
                        rgba.setAlpha(ac, val);
                        break;
                    }
                default:
                    if (!exp.hasCssVariable()) {
                        throw new InvalidParamException("colorfunc", val, rgba.fname, ac);
                    }
            }
        }
        return rgba;
    }

    public final void setAlpha(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        va = filterAlpha(ac, val);
    }

    public boolean equals(RGBA other) {
        if (other != null) {
            return super.equals(other) && ((va == null && other.va == null) || (va != null && va.equals(other.va)));
        }
        return false;
    }

    /**
     * Create a new RGBA
     */
    public RGBA() {
        fname = functionname;
    }

    /**
     * Create new RGBA and with a specific function name
     * (like astc-rgba http://www.atsc.org/cms/standards/a100/a_100_2.pdf #5.2.1.8.4.1
     */
    public RGBA(String fname) {
        this.fname = fname;
    }

    /**
     * Create a new RGBA with default values
     *
     * @param r the red channel
     * @param g the green channel
     * @param b the blue channel
     * @param a the alpha channel
     */
    public RGBA(int r, int g, int b, float a) {
        super(r, g, b);
        fname = functionname;
        CssNumber n = new CssNumber();
        n.setFloatValue(a);
        va = n;
        setPercent(false);
    }

    /**
     * Create a new RGBA with default values
     *
     * @param r the red channel
     * @param g the green channel
     * @param b the blue channel
     */
    public RGBA(int r, int g, int b) {
        super(r, g, b);
        fname = functionname;
        va = null;
        setPercent(false);
    }

    /**
     * Create a new RGBA with default values
     *
     * @param isModernCss a boolean toggling the output of RGB
     * @param r           the red channel, an <EM>int</EM>
     * @param g           the green channel, an <EM>int</EM>
     * @param b           the blue channel, an <EM>int</EM>
     * @param a           the alpha channel, an <EM>float</EM>
     */
    public RGBA(boolean isModernCss, int r, int g, int b, float a) {
        this(r, g, b, a);
        this.isModernCss = isModernCss;
    }

    /**
     * Create a new RGBA with default values
     *
     * @param isModernCss a boolean toggling the output of RGB
     * @param r           the red channel, an <EM>int</EM>
     * @param g           the green channel, an <EM>int</EM>
     * @param b           the blue channel, an <EM>int</EM>
     */
    public RGBA(boolean isModernCss, int r, int g, int b) {
        this(r, g, b);
        this.isModernCss = isModernCss;
    }

    /**
     * Set function name (basically rgb or rgba for now)
     *
     * @param fname
     */
    public void setFunctionName(String fname) {
        this.fname = fname;
    }

    protected void setRepresentationString(String s) {
        output = s;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder();
            if (isModernCss) {
                if (isRelative) {
                    sb.append("from ").append(fromValue).append(' ');
                }
                sb.append(RGB.functionname).append('(');
                sb.append(vr).append(' ');
                sb.append(vg).append(' ').append(vb);
                if (va != null) {
                    sb.append(" / ").append(va);
                }
                sb.append(')');
            } else {
                sb.append(fname).append('(');
                sb.append(vr).append(", ");
                sb.append(vg).append(", ").append(vb);
                if (va != null) {
                    sb.append(", ").append(va);
                }
                sb.append(')');
            }
            output = sb.toString();
        }
        return output;
    }
}
