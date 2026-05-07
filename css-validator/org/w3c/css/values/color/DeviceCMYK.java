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
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

public class DeviceCMYK {
    String output = null;
    CssValue vc, vy, vm, vk, alpha = null;
    boolean faSet = false;
    boolean isModern = true;

    /**
     * Create a new DeviceCYMK
     */
    public DeviceCMYK() {
    }

    /**
     *  Parse a CMYK per https://www.w3.org/TR/2025/WD-css-color-5-20250318/#funcdef-device-cmyk
     *  TODO check for legacy mode that it's all numbers or percentages
     * @param ac
     * @param exp
     * @param caller
     * @return
     * @throws InvalidParamException
     */
    public static final DeviceCMYK parseDeviceCMYK(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("device-cmyk(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        DeviceCMYK cmyk = new DeviceCMYK();
        CssValue val = exp.getValue();
        char op = exp.getOperator();

        if (op == SPACE) {
            cmyk.isModern = true;
        } else if (op == COMMA) {
            cmyk.isModern = false;
        } else {
            if (!exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
        }

        if (exp.hasCssVariable()) {
            caller.markCssVariable();
        }
        // C
        if ((val == null) && !exp.hasCssVariable()) {
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setC(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (cmyk.isModern && CssColor.none.equals(val.getIdent())) {
                    cmyk.setC(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }

        // M
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null ||
                ((op != SPACE && cmyk.isModern) || (op != COMMA && !cmyk.isModern)) && !exp.hasCssVariable())) {
            exp.starts();
            throw new InvalidParamException("invalid-color", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setM(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (cmyk.isModern && CssColor.none.equals(val.getIdent())) {
                    cmyk.setM(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }

        // Y
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null ||
                ((op != SPACE && cmyk.isModern) || (op != COMMA && !cmyk.isModern)) && !exp.hasCssVariable())) {
            exp.starts();
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setY(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (cmyk.isModern && CssColor.none.equals(val.getIdent())) {
                    cmyk.setY(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }
        // K
        exp.next();
        val = exp.getValue();
        op = exp.getOperator();
        if ((val == null) && !exp.hasCssVariable()) {
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
        }
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_VARIABLE:
                cmyk.setK(ac, val);
                break;
            case CssTypes.CSS_IDENT:
                if (cmyk.isModern && CssColor.none.equals(val.getIdent())) {
                    cmyk.setK(ac, val);
                    break;
                }
            default:
                if (!exp.hasCssVariable()) {
                    exp.starts();
                    throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                }
        }

        exp.next();
        if (!exp.end()) {
            if ((op != SPACE || !cmyk.isModern) && !exp.hasCssVariable()) {
                exp.starts();
                throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
            }
            // now we need an alpha.
            val = exp.getValue();
            op = exp.getOperator();

            if ((val.getType() != CssTypes.CSS_SWITCH) && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", val, ac);
            }
            if ((op != SPACE) && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", ac);
            }
            exp.next();
            // now we get the alpha value
            val = exp.getValue();
            if ((val == null) && !exp.hasCssVariable()) {
                throw new InvalidParamException("invalid-color", exp.toStringFromStart(), ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_PERCENTAGE:
                case CssTypes.CSS_VARIABLE:
                    cmyk.setAlpha(ac, val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (CssColor.none.equals(val.getIdent())) {
                        cmyk.setAlpha(ac, val);
                        break;
                    }
                default:
                    if (!exp.hasCssVariable()) {
                        exp.starts();
                        throw new InvalidParamException("colorfunc", val, "device-cmyk", ac);
                    }
            }
            exp.next();
        }
        // extra values?
        if (!exp.end() && !exp.hasCssVariable()) {
            exp.starts();
            throw new InvalidParamException("colorfunc", exp.toStringFromStart(), "device-cmyk", ac);
        }
        return cmyk;
    }

    public final void setC(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vc = RGBA.filterAlpha(ac, val);
    }

    public final void setY(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vy = RGBA.filterAlpha(ac, val);
    }

    public final void setM(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vm = RGBA.filterAlpha(ac, val);
    }

    public final void setK(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        vk = RGBA.filterAlpha(ac, val);
    }

    public final void setAlpha(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        faSet = true;
        alpha = RGBA.filterAlpha(ac, val);
    }


    public boolean equals(DeviceCMYK other) {
        if (other != null) {
            return (vc.equals(other.vc) && vm.equals(other.vm) && vy.equals(other.vy) && vk.equals(other.vk) &&
                    ((alpha == null && other.alpha == null) || (alpha != null && alpha.equals(other.alpha))));
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder("device-cmyk(");
            if (isModern) {
                sb.append(vc).append(' ');
                sb.append(vm).append(' ');
                sb.append(vy).append(' ');
                sb.append(vk);
                if (faSet) {
                    sb.append(" / ").append(alpha);
                }
            } else {
                sb.append(vc).append(", ");
                sb.append(vm).append(", ");
                sb.append(vy).append(", ");
                sb.append(vk);
            }
            sb.append(')');
            output = sb.toString();
        }
        return output;
    }
}
