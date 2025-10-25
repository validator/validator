//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
//
package org.w3c.css.values;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;

public class LightDark {
    String output = null;
    CssColor light, dark;
    boolean has_css_variable = false;

    /**
     * Create a new LightDark
     */
    public LightDark() {
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
