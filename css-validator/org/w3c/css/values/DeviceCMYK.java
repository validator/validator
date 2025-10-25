//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
//
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

public class DeviceCMYK {
    String output = null;
    CssValue vc, vy, vm, vk, alpha, fallback = null;
    boolean faSet = false;

    /**
     * Create a new DeviceCYMK
     */
    public DeviceCMYK() {
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

    public final void setFallbackColor(ApplContext ac, CssValue val)
            throws InvalidParamException {
        output = null;
        switch (val.getType()) {
            case CssTypes.CSS_HASH_IDENT:
                CssColor c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.toString());
                fallback = c;
                break;
            case CssTypes.CSS_IDENT:
                fallback = new CssColor(ac, (String) val.get());
                break;
            case CssTypes.CSS_COLOR:
                fallback = val;
                break;
            case CssTypes.CSS_FUNCTION:
                CssFunction attr = (CssFunction) val;
                CssExpression params = attr.getParameters();
                String fname = attr.getName();

                if (fname.equals("attr")) {
                    CssValue v1 = params.getValue();
                    params.next();
                    CssValue v2 = params.getValue();
                    if ((params.getCount() != 2)) {
                        throw new InvalidParamException("value",
                                params.getValue(),
                                val.toString(), ac);
                    } else if (v1.getType() != CssTypes.CSS_IDENT) {
                        throw new InvalidParamException("value",
                                params.getValue(),
                                val.toString(), ac);

                    } else if (!(v2.toString().equals("color"))) {
                        throw new InvalidParamException("value",
                                params.getValue(),
                                val.toString(), ac);
                    } else {
                        fallback = val;
                    }
                } else {
                    throw new InvalidParamException("value",
                            params.getValue(),
                            val.toString(), ac);
                }
                break;
            default:
                throw new InvalidParamException("value", "color",
                        val.toString(), ac);
        }
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
            sb.append(vc).append(' ');
            sb.append(vm).append(' ');
            sb.append(vy).append(' ');
            sb.append(vk);
            if (faSet) {
                sb.append(" / ").append(alpha);
            }
            if (fallback != null) {
                sb.append(", ").append(fallback);
            }
            sb.append(')');
            output = sb.toString();
        }
        return output;
    }
}
