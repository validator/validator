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
import java.math.RoundingMode;

public class HWB {
    String output = null;
    boolean faSet = false;

    CssValue vh, vw, vb, va;

    static final BigDecimal s100 = new BigDecimal(100);

    /**
     * Create a new HWB
     */
    public HWB() {
    }


    public static final CssValue filterValue(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if (val.getType() == CssTypes.CSS_PERCENTAGE) {
                CssCheckableValue v = val.getCheckableValue();
                v.checkPositiveness(ac, "RGB");
                if (val.getRawType() == CssTypes.CSS_PERCENTAGE) {
                    float p = ((CssPercentage) val).floatValue();
                    if (p > 100.) {
                        throw new InvalidParamException("range", val, ac);
                    }
                }
            }
        }
        return val;
    }

    public final void setHue(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vh = HSL.filterHue(ac, val);
    }

    public final void setWhiteness(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vw = filterValue(ac, val);
    }

    public final void setBlackness(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vb = filterValue(ac, val);
    }

    public final void setAlpha(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        faSet = true;
        va = RGBA.filterAlpha(ac, val);
    }

    public void normalize() {
        if (vw == null || vb == null) {
            return;
        }
        if (vw.getRawType() == CssTypes.CSS_PERCENTAGE &&
                vb.getRawType() == CssTypes.CSS_PERCENTAGE) {
            CssPercentage pw, pb;
            BigDecimal w, b, s;
            pw = (CssPercentage) vw;
            pb = (CssPercentage) vb;
            w = pw.getValue();
            b = pb.getValue();
            s = w.add(b);
            if (s.compareTo(s100) > 0) {
                w = w.multiply(s100).divide(s, 3, RoundingMode.HALF_UP).stripTrailingZeros();
                b = b.multiply(s100).divide(s, 3, RoundingMode.HALF_UP).stripTrailingZeros();
                pw.setValue(w);
                pb.setValue(b);
            }
        }
    }

    public boolean equals(HWB other) {
        if (other != null) {
            return (vh.equals(other.vh) && vw.equals(other.vw) && vb.equals(other.vb) &&
                    ((va == null && other.va == null) || (va != null && va.equals(other.va))));
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            normalize();
            StringBuilder sb = new StringBuilder("hwb(");
            sb.append(vh).append(' ');
            sb.append(vw).append(' ');
            sb.append(vb);
            if (!faSet) {
                sb.append(')');
            } else {
                sb.append(" / ").append(va).append(')');
            }
            output = sb.toString();
        }
        return output;
    }
}
