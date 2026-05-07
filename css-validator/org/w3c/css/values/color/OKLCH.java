//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2025.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.values.color;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;

public class OKLCH extends LCH {

    public OKLCH(LCH lch) {
        this.vl = lch.vl;
        this.vc = lch.vc;
        this.vh = lch.vh;
        this.faSet = lch.faSet;
        this.alpha = lch.alpha;
        this.isRelative = lch.isRelative;
        this.fromValue = lch.fromValue;
    }

    public static final OKLCH parseOKLCHColor(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {
        return new OKLCH(parseLCHColor(ac, exp, caller));
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb;

            sb = new StringBuilder("oklch(");
            if (isRelative) {
                sb.append("from ").append(fromValue.toString()).append(' ');
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
