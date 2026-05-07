//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2025.
// Please first read the full copyright statement in file COPYRIGHT.html
//
package org.w3c.css.values.color;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.SPACE;

public class LAB {
    public static final CssIdent[] colorRelativeValues;

    static {
        String[] _allowed_values = {"l", "a", "b", "alpha"};
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
    CssValue vl, va, vb, alpha;
    boolean faSet = false;
    boolean isRelative = false;
    CssValue fromValue;

    /**
     * Create a new LAB
     */
    public LAB() {
    }

    public static final CssValue filterL(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if (val.getType() == CssTypes.CSS_PERCENTAGE) {
                CssCheckableValue v = val.getCheckableValue();
                if (!v.isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                    CssNumber nb = new CssNumber();
                    nb.setIntValue(0);
                    return nb;
                }
                /*
                // L value is not clamped for now, but maybe in the future.
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    BigDecimal pp = ((CssNumber) val).value;
                    if (pp.compareTo(HWB.s100) > 0) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        CssNumber nb = new CssNumber();
                        nb.setIntValue(100);
                        return nb;
                    }
                }
                */
            }
        }
        return val;
    }

    /**
     * Parse a LAB color
     *
     * @spec https://www.w3.org/TR/2025/WD-css-color-5-20250318/#funcdef-lab
     */
    public static final LAB parseLABColor(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {
        // HWB defined in CSSColor Level 4 and onward, currently used in the CSS level
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("lab(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }

        if (exp.hasCssVariable()) {
            caller.markCssVariable();
            // we still parse variables as they will be ignored
            // we check the delimiter syntax, and other failures
        }
        LAB lab = new LAB();
        CssValue val;
        char op;

        val = exp.getValue();
        op = exp.getOperator();
        // from ?
        if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp, "Lab", ac);
        }
        if (val.getType() == CssTypes.CSS_IDENT) {
            if (CssColor.relative.equals(val.getIdent())) {
                // we need to parse a color now
                if ((val == null || op != SPACE || lab.isRelative) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
                lab.isRelative = true;  // so that we get only one from
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
                lab.fromValue = val;
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
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                lab.setL(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (lab.isRelative && isColorRelativeValue((val.getIdent())))) {
                    lab.setL(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "Lab", ac);
                }
        }

        // A
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                lab.setA(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (lab.isRelative && isColorRelativeValue(val.getIdent()))) {
                    lab.setA(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "Lab", ac);
                }
        }

        // B
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if (val == null) {
            if (!exp.hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp, "Lab", ac);
            }
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                lab.setB(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (CssColor.none.equals(val.getIdent()) ||
                        (lab.isRelative && isColorRelativeValue(val.getIdent()))) {
                    lab.setB(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "Lab", ac);
                }
        }

        exp.next();
        if (!exp.end()) {
            if (op != SPACE && !exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", op, "Lab", ac);
            }
            // now we need an alpha.
            val = exp.getValue();
            op = exp.getOperator();

            if ((val.getType() != CssTypes.CSS_SWITCH) && !exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "Lab", ac);
            }
            if (op != SPACE && !exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", val, "Lab", ac);
            }
            exp.next();
            // now we get the alpha value
            val = exp.getValue();
            if ((val == null) && !exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "Lab", ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_VARIABLE:
                    lab.setAlpha(ac, val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssColor.none.equals(val.getIdent()) ||
                            (lab.isRelative && isColorRelativeValue(val.getIdent()))) {
                        lab.setAlpha(ac, val);
                        break;
                    }
                default:
                    if (!exp.hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "Lab", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end()) {
            exp.starts();
            if (!exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "Lab", ac);
            }
        }
        return lab;
    }


    public final void setL(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vl = filterL(ac, val);

    }

    public final void setA(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        va = val;
    }

    public final void setB(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vb = val;
    }

    public final void setAlpha(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        faSet = true;
        alpha = RGBA.filterAlpha(ac, val);
    }

    public boolean equals(LAB other) {
        if (other != null) {
            if (isRelative) {
                if (!other.isRelative || !fromValue.equals(other.fromValue)) {
                    return false;
                }
            }
            return (vl.equals(other.vl) && va.equals(other.va) && vb.equals(other.vb) &&
                    ((alpha == null && other.alpha == null) || (alpha != null && alpha.equals(other.alpha))));
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb;

            sb = new StringBuilder("lab(");
            if (isRelative) {
                sb.append("from ").append(fromValue.toString()).append(' ');
            }
            sb.append(vl).append(' ');
            sb.append(va).append(' ');
            sb.append(vb);
            if (faSet) {
                sb.append(" / ").append(alpha);
            }
            sb.append(')');
            output = sb.toString();
        }
        return output;
    }
}
