//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
//
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

import java.math.BigDecimal;

public class LCH {
    String output = null;
    CssValue vl, vc, vh, alpha;
    boolean faSet = false;

    /**
     * Create a new LCH
     */
    public LCH() {
    }

    public static final CssValue filterC(ApplContext ac, CssValue val)
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
                    nb.setValue(BigDecimal.ZERO);
                    return nb;
                }
            }
        }
        return val;
    }

    public static final CssValue filterH(ApplContext ac, CssValue val)
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
                    if (pp.compareTo(CssAngle.deg360) > 0) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        CssNumber nb = new CssNumber();
                        nb.setValue(CssAngle.deg360);
                        return nb;
                    }
                }
            }
        }
        return val;
    }

    public final void setL(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vl = LAB.filterL(ac, val);

    }

    public final void setC(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vc = filterC(ac, val);
    }

    public final void setH(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vh = filterH(ac, val);
    }

    public final void setAlpha(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        faSet = true;
        alpha = RGBA.filterAlpha(ac, val);
    }

    public boolean equals(LCH other) {
        if (other != null) {
            return (vl.equals(other.vl) && vc.equals(other.vc) && vh.equals(other.vh) &&
                    ((alpha == null && other.alpha == null) || (alpha != null && alpha.equals(other.alpha))));
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder("lch(");
            sb.append(vl).append(' ');
            sb.append(vc).append(' ');
            sb.append(vh);
            if (faSet) {
                sb.append(" / ").append(alpha);
            }
            sb.append(')');
            output = sb.toString();
        }
        return output;
    }
}
