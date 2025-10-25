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
import org.w3c.css.util.Util;

import java.math.RoundingMode;

public class HSL {
    String output = null;
    CssValue vh, vs, vl, va;

    static final String functionname = "hsl";

    /**
     * Create a new HSL
     */
    public HSL() {
    }

    public static final CssValue filterValue(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if (val.getType() == CssTypes.CSS_PERCENTAGE) {
                CssCheckableValue v = val.getCheckableValue();
                if (!v.warnPositiveness(ac, "RGB")) {
                    CssNumber nb = new CssNumber();
                    nb.setIntValue(0);
                    return nb;
                }
                if (val.getRawType() == CssTypes.CSS_PERCENTAGE) {
                    float p = ((CssPercentage) val).floatValue();
                    if (p > 100.) {
                        ac.getFrame().addWarning("out-of-range", Util.displayFloat(p));
                        return new CssPercentage(100);
                    }
                }
            }
        }
        return val;
    }

    public final static CssValue filterHue(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if (val.getType() == CssTypes.CSS_NUMBER) {
                // numbers are treated as degrees
                CssCheckableValue v = val.getCheckableValue();
                if (!v.isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                    if (val.getRawType() == CssTypes.CSS_NUMBER) {
                        float p = ((CssNumber) val).getValue();
                        CssNumber nb = new CssNumber();
                        nb.setFloatValue((float) ((((double) p % 360.0) + 360.0) % 360.0));
                        return nb;
                    }
                }
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    float p = ((CssNumber) val).getValue();
                    if (p > 360.) {
                        ac.getFrame().addWarning("out-of-range", Util.displayFloat(p));
                        CssNumber nb = new CssNumber();
                        nb.setFloatValue((float) ((((double) p % 360.0) + 360.0) % 360.0));
                        return nb;
                    }
                }
            } else if (val.getType() == CssTypes.CSS_ANGLE) {
                // since css-color-4
                CssCheckableValue v = val.getCheckableValue();
                if (!v.isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                }
                if (val.getRawType() == CssTypes.CSS_ANGLE) {
                    CssAngle a = (CssAngle) val;
                    float p = a.getValue();
                    if (p > a.deg360.divide(a.factor, 2, RoundingMode.HALF_DOWN).floatValue()) {
                        ac.getFrame().addWarning("out-of-range", Util.displayFloat(p));
                    }
                    // if a proper angle we normalize it after checking everything.
                    a.normalizeValue();
                }
            }
        }
        return val;
    }

    public final void setHue(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vh = filterHue(ac, val);

    }

    public final void setSaturation(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vs = filterValue(ac, val);
    }

    public final void setLightness(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vl = filterValue(ac, val);
    }


    public void setAlpha(ApplContext ac, CssValue alpha)
            throws InvalidParamException {
        output = null;
        va = RGBA.filterAlpha(ac, alpha);
    }

    public boolean equals(HSL other) {
        if (other != null) {
            return (vh.equals(other.vh) && vs.equals(other.vs) && vl.equals(other.vl) &&
                    ((va == null && other.va == null) || (va != null && va.equals(other.va))));
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder(functionname);
            sb.append('(').append(vh).append(' ');
            sb.append(vs).append(' ').append(vl);
            if (va != null) {
                sb.append(" / ").append(va);
            }
            sb.append(')');
            output = sb.toString();
        }
        return output;
    }
}
