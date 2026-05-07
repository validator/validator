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
import org.w3c.css.values.CssColor;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import static org.w3c.css.values.CssOperator.COMMA;

public class LightDark {
    String output = null;
    CssColor light, dark;
    boolean has_css_variable = false;

    /**
     * Create a new LightDark
     */
    public LightDark() {
    }

    public static final LightDark parseLightDark(ApplContext ac, CssExpression exp, CssColor caller)
            throws InvalidParamException {
        if (ac.getCssVersion().compareTo(CssVersion.CSS3) < 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("light-dark(").append(exp.toStringFromStart()).append(')');
            throw new InvalidParamException("notversion", sb.toString(),
                    ac.getCssVersionString(), ac);
        }
        if ((exp == null) || (exp.getCount() != 2)) {
            throw new InvalidParamException("invalid-color", ac);
        }
        LightDark ld = new LightDark();
        CssValue l = exp.getValue();
        char op = exp.getOperator();
        if (l == null || op != COMMA) {
            throw new InvalidParamException("invalid-color", ac);
        }
        exp.next();
        CssExpression nex = new CssExpression();
        nex.addValue(l);
        ld.setLight(ac, new org.w3c.css.properties.css3.CssColor(ac, nex).getColor());
        CssValue d = exp.getValue();
        nex = new CssExpression();
        nex.addValue(d);
        ld.setDark(ac, new org.w3c.css.properties.css3.CssColor(ac, nex).getColor());
        exp.next();
        if (ld.has_css_variable) {
            caller.markCssVariable();
        }
        return ld;
    }

    public final void setLight(ApplContext ac, CssValue val) throws InvalidParamException {

        if (val.getType() == CssTypes.CSS_COLOR) {
            light = (CssColor) val;
            if (light.hasCssVariable()) {
                has_css_variable = true;
            }
            output = null;
        }
    }

    public final void setDark(ApplContext ac, CssValue val) throws InvalidParamException {
        if (val.getType() == CssTypes.CSS_COLOR) {
            dark = (CssColor) val;
            if (dark.hasCssVariable()) {
                has_css_variable = true;
            }
            output = null;
        }
    }

    public boolean equals(LightDark other) {
        if (other != null) {
            return (light.equals(other.light) && dark.equals(other.dark));
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (output == null) {
            StringBuilder sb = new StringBuilder("light-dark(");
            sb.append(light).append(", ").append(dark).append(')');
            output = sb.toString();
        }
        return output;
    }
}
