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
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.w3c.css.values.CssOperator.SPACE;

public class HWB {

    public static final CssIdent[] colorRelativeValues;

    static {
        String[] _allowed_values = {"h", "w", "b", "alpha"};
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

    String output = null;
    boolean faSet = false;

    CssValue vh, vw, vb, va;
    boolean isRelative = false;
    CssValue fromValue;

    static final BigDecimal s100 = new BigDecimal(100);

    /**
     * Create a new HWB
     */
    public HWB() {
    }

    public static final HWB parseHWBColor(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {
        // HWB defined in CSSColor Level 4 and onward, currently used in the CSS level
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("hwb(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }

        if (exp.hasCssVariable()) {
            caller.markCssVariable();
        }

        HWB hwb = new HWB();

        CssValue val = exp.getValue();
        char op = exp.getOperator();
        // H
        if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }

        if (val.getType() == CssTypes.CSS_IDENT) {
            if (CssColor.relative.equals(val.getIdent())) {
                // we need to parse a color now
                if ((val == null || op != SPACE || hwb.isRelative) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
                hwb.isRelative = true;  // so that we get only one from
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
                hwb.fromValue = val;
                exp.next();
                val = exp.getValue();
                op = exp.getOperator();
                if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
            }
        }

        switch (val.getType()) {
            case CssTypes.CSS_ANGLE:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                hwb.setHue(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (hwb.isRelative && isColorRelativeValue(val.getIdent()))) {
                    hwb.setHue(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HWB", ac);
                }
        }

        // W
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                hwb.setWhiteness(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (hwb.isRelative && isColorRelativeValue(val.getIdent()))) {
                    hwb.setWhiteness(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "HWB", ac);
                }
        }

        // B
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val == null || (op != SPACE && exp.getRemainingCount() > 1)) {
            if (!exp.hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("invalid-color", ac);
            }
        }
        switch (val.getType()) {
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                hwb.setBlackness(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (hwb.isRelative && isColorRelativeValue(val.getIdent()))) {
                    hwb.setBlackness(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "HWB", ac);
                }
        }

        hwb.normalize();

        // A
        exp.next();
        if (!exp.end()) {
            if (op != SPACE && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            // now we need an alpha.
            val = exp.getValue();
            op = exp.getOperator();

            if ((val.getType() != CssTypes.CSS_SWITCH) && !exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "HWB", ac);
            }
            if (op != SPACE && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            exp.next();
            // now we get the alpha value
            val = exp.getValue();
            if (val == null && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", exp.toStringFromStart(), ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_VARIABLE:
                    hwb.setAlpha(ac, val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssColor.none.equals(val.getIdent()) ||
                            (hwb.isRelative && isColorRelativeValue(val.getIdent()))) {
                        hwb.setAlpha(ac, val);
                        break;
                    }
                default:
                    if (!exp.hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "HWB", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end()) {
            if (!exp.hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "HWB", ac);
            }
        }
        return hwb;
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
            // if relative, check it starts from the same color
            if (isRelative) {
                if (!other.isRelative || !fromValue.equals(other.fromValue)) {
                    return false;
                }
            }
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
            if (isRelative) {
                sb.append("from ").append(fromValue.toString()).append(' ');
            }
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
