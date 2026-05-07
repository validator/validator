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

public class OKLAB extends LAB {

    public OKLAB(LAB lab) {
        this.vl = lab.vl;
        this.va = lab.va;
        this.vb = lab.vb;
        this.faSet = lab.faSet;
        this.alpha = lab.alpha;
        this.isRelative = lab.isRelative;
        this.fromValue = lab.fromValue;
    }

    public static final OKLAB parseOKLABColor(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {
        return new OKLAB(parseLABColor(ac, exp, caller));
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb;

            sb = new StringBuilder("oklab(");
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
