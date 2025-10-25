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
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

public class RGBA extends RGB {
    static final String functionname = "rgba";

    private String output = null;
    String fname;

    CssValue va;

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
                    BigDecimal pp = ((CssNumber) val).value;
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
     * Create a RGBA and with a specific function name
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
        CssNumber n = new CssNumber();
        n.setFloatValue(a);
        va = n;
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
