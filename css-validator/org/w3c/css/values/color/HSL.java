///
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2025.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values.color;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.CssVersion;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Util;
import org.w3c.css.values.CssAngle;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssVariable;

import java.math.RoundingMode;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

public class HSL {
    public static final CssIdent[] colorRelativeValues;

    static {
        String[] _allowed_values = {"h", "s", "l", "alpha"};
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
    CssValue vh, vs, vl, va;
    boolean isRelative = false;
    CssValue fromValue;

    String functionname = "hsl";

    /**
     * Create a new HSL
     */
    public HSL() {
    }

    /**
     * Parse HSL per spec below
     *
     * @param ac
     * @param exp
     * @param caller
     * @return
     * @throws InvalidParamException
     * @spec https://www.w3.org/TR/2025/CRD-css-color-4-20250424/#the-hsl-notation
     * @spec https://www.w3.org/TR/2025/WD-css-color-5-20250318/#relative-HSL
     */

    public static final HSL parseHSL(ApplContext ac, CssExpression exp, CssColor caller)
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
        HSL hsl = new HSL();
        boolean separator_space = (op == SPACE);

        if (exp.hasCssVariable()) {
            caller.markCssVariable();
            if (exp.getCount() < 3) {
                // check if we can expand
                while (!exp.end()) {
                    val = exp.getValue();
                    if (val.getRawType() == CssTypes.CSS_VARIABLE) {
                        CssExpression varexp = ((CssVariable) val).getVariableExpression();
                        if ((varexp != null) && (varexp.getCount() > 1)) {
                            // TODO something fancy, merging expression
                            // FIXME don't return empty
                            return hsl;
                        }
                    }
                    exp.next();
                }
                // FIXME don't return empty
                return hsl;
            }
        }

        if (val == null || (!separator_space && (op != COMMA))) {
            if (!exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
        }

        if (val.getType() == CssTypes.CSS_IDENT) {
            if (CssColor.relative.equals(val.getIdent())) {
                // we need to parse a color now
                if ((val == null || op != SPACE || hsl.isRelative) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
                hsl.isRelative = true;  // so that we get only one from
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
                hsl.fromValue = val;
                exp.next();
                val = exp.getValue();
                op = exp.getOperator();
                if ((val == null || op != SPACE) && !exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("invalid-color", ac);
                }
            }
        }

        // H
        switch (val.getType()) {
            case CssTypes.CSS_ANGLE:
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_VARIABLE:
                hsl.setHue(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (separator_space && CssColor.none.equals(val.getIdent()) ||
                        (hsl.isRelative && isColorRelativeValue(val.getIdent()))) {
                    hsl.setHue(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        // relative is always modern, so bail out if the separator is the wrong one
        if (val == null || ((hsl.isRelative || separator_space) && (op != SPACE)) ||
                (!separator_space && (op != COMMA))) {
            if (!exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
        }

        // S
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (!separator_space) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                hsl.setSaturation(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (separator_space && CssColor.none.equals(val.getIdent()) ||
                        (hsl.isRelative && isColorRelativeValue(val.getIdent()))) {
                    hsl.setSaturation(ac, val);
                    break;
                }
            default:
                exp.starts();
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
        }

        exp.next();
        val = exp.getValue();
        op = exp.getOperator();

        if (val == null && !exp.hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }

        // L
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (!separator_space) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                hsl.setLightness(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (separator_space && CssColor.none.equals(val.getIdent()) ||
                        (hsl.isRelative && isColorRelativeValue(val.getIdent()))) {
                    hsl.setLightness(ac, val);
                    break;
                }
            default:
                exp.starts();
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
        }

        exp.next();

        // check for optional alpha channel
        if (!exp.end()) {
            // care for old syntax
            if (op == COMMA && !separator_space && !hsl.isRelative) {
                val = exp.getValue();
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                    case CssTypes.CSS_VARIABLE:
                        hsl.setAlpha(ac, val);
                        break;
                    default:
                        exp.starts();
                        if (!exp.hasCssVariable()) {
                            throw new InvalidParamException("colorfunc", val, "HSL", ac);
                        }
                }
            } else {
                // otherwise modern syntax
                if (op != SPACE && !exp.hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                // now we need an alpha.
                val = exp.getValue();
                op = exp.getOperator();

                if (val.getType() != CssTypes.CSS_SWITCH && !exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "HSL", ac);
                }
                if (op != SPACE && !exp.hasCssVariable()) {
                    throw new InvalidParamException("invalid-color", ac);
                }
                exp.next();
                // now we get the alpha value
                if (exp.end() && !exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", exp.getValue(), "HSL", ac);
                }
                val = exp.getValue();
                switch (val.getType()) {
                    case CssTypes.CSS_NUMBER:
                    case CssTypes.CSS_PERCENTAGE:
                    case CssTypes.CSS_VARIABLE:
                        hsl.setAlpha(ac, val);
                        break;
                    case CssTypes.CSS_IDENT:
                        if ((op != COMMA) && (CssColor.none.equals(val.getIdent()) ||
                                (hsl.isRelative && isColorRelativeValue(val.getIdent())))) {
                            hsl.setAlpha(ac, val);
                            break;
                        }
                    default:
                        if (!exp.hasCssVariable()) {
                            throw new InvalidParamException("colorfunc", val, "HSL", ac);
                        }
                }
            }
            exp.next();

            if (!exp.end() && !exp.hasCssVariable()) {
                throw new InvalidParamException("colorfunc", exp.getValue(), "HSL", ac);
            }
        }
        return hsl;
    }

    public static final CssValue filterValue(ApplContext ac, CssValue val)
            throws InvalidParamException {
        if (val.getRawType() == CssTypes.CSS_CALC) {
            // TODO add warning about uncheckability
            // might need to extend...
        } else {
            if ((val.getType() == CssTypes.CSS_PERCENTAGE) ||
                    (val.getType() == CssTypes.CSS_NUMBER)) {
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
                } else if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    val.getNumber().warnLowerEqualThan(ac, 100., null);
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
            // if relative, check it starts from the same color
            if (isRelative) {
                if (!other.isRelative || !fromValue.equals(other.fromValue)) {
                    return false;
                }
            }
            return (vh.equals(other.vh) && vs.equals(other.vs) && vl.equals(other.vl) &&
                    ((va == null && other.va == null) || (va != null && va.equals(other.va))));
        }
        return false;
    }

    public void setFunctionName(String name) {
        functionname = name;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder(functionname).append('(');
            if (isRelative) {
                sb.append("from ").append(fromValue.toString()).append(' ');
            }
            sb.append(vh).append(' ');
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
