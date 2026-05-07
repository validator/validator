//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
//
package org.w3c.css.values.color;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssAngle;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.math.BigDecimal;

import static org.w3c.css.values.CssOperator.SPACE;

public class LCH {
    public static final CssIdent[] colorRelativeValues;

    static {
        String[] _allowed_values = {"l", "c", "h", "alpha"};
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
    CssValue vl, vc, vh, alpha;
    boolean faSet = false;
    boolean isRelative = false;
    CssValue fromValue;

    /**
     * Create a new LCH
     */
    public LCH() {
    }

    /**
     * Parse a LCH color
     *
     * @spec https://www.w3.org/TR/2025/WD-css-color-5-20250318/#funcdef-lch
     */
    public static final LCH parseLCHColor(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {

        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("lch(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        if (exp.hasCssVariable()) {
            caller.markCssVariable();
        }

        LCH lch = new LCH();
        CssValue val = exp.getValue();
        char op = exp.getOperator();

        if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }

        if (val.getType() == CssTypes.CSS_IDENT) {
            if (CssColor.relative.equals(val.getIdent())) {
                // we need to parse a color now
                if ((val == null || op != SPACE || lch.isRelative) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
                lch.isRelative = true;  // so that we get only one from
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
                lch.fromValue = val;
                exp.next();
                val = exp.getValue();
                op = exp.getOperator();
                if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
            }
        }

        // L
        switch (val.getType()) {
            case CssTypes.CSS_VARIABLE:
                exp.markCssVariable();
                caller.markCssVariable();
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
                lch.setL(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (lch.isRelative && isColorRelativeValue(val.getIdent()))) {
                    lch.setL(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
        }

        // C
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_VARIABLE:
                exp.markCssVariable();
                caller.markCssVariable();
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
                lch.setC(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (lch.isRelative && isColorRelativeValue(val.getIdent()))) {
                    lch.setC(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
        }

        // H
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null) && !exp.hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "LCH", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_VARIABLE:
                exp.markCssVariable();
                caller.markCssVariable();
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_ANGLE:
                lch.setH(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (lch.isRelative && isColorRelativeValue(val.getIdent()))) {
                    lch.setH(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "LCH", ac);
                }
        }

        exp.next();
        if (!exp.end()) {
            if ((op != SPACE) && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
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
                case CssTypes.CSS_IDENT:
                    if (CssColor.none.equals(val.getIdent()) ||
                            (lch.isRelative && isColorRelativeValue(val.getIdent()))) {
                        lch.setAlpha(ac, val);
                        break;
                    }
                default:
                    if (!exp.hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "LCH", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end()) {
            if (!exp.hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "LCH", ac);
            }
        }
        return lch;
    }

    public static final CssValue filterC(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if ((val.getType() == CssTypes.CSS_NUMBER) ||
                    (val.getType() == CssTypes.CSS_PERCENTAGE)) {
                CssCheckableValue v = val.getCheckableValue();
                if (!v.isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                    val.getCheckableValue().setValue(BigDecimal.ZERO);
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
                    BigDecimal pp = val.getNumber().getBigDecimalValue();
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
            if (isRelative) {
                if (!other.isRelative || !fromValue.equals(other.fromValue)) {
                    return false;
                }
            }
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
            if (isRelative) {
                sb.append("from ").append(fromValue).append(' ');
            }
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
