// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement at:
// https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-appearance
 */

public class CssAppearance extends org.w3c.css.properties.css.CssAppearance {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    public static final CssIdent[] allowed_values, compat_auto;

    static {
        int i;
        String[] _allowed_values = {"none", "auto", "textfield", "menulist-button"};
        String[] _compat_values = {"searchfield", "textarea", "push-button", "slider-horizontal", "checkbox", "radio",
                "square-button", "menulist", "listbox", "meter", "progress-bar", "button"};
        allowed_values = new CssIdent[_allowed_values.length + _compat_values.length];
        compat_auto = new CssIdent[_compat_values.length];
        i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        for (String s : _compat_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        i = 0;
        for (String s : _compat_values) {
            compat_auto[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static boolean isCompatAuto(CssIdent ident) {
        for (CssIdent id : compat_auto) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new CssAppearance
     */
    public CssAppearance() {
        value = initial;
    }

    /**
     * Create a new CssAppearance
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect value
     */
    public CssAppearance(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;

        val = expression.getValue();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id)) {
                value = val;
            } else {
                if (getAllowedIdent(id) == null) {
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
                }
                value = val;
                // output will use auto
                if (isCompatAuto(id)) {
                    // need a specific warning to tell that it is seen as "auto"?
                    ac.getFrame().addWarning("value-unofficial",
                            val.toString(), getPropertyName());
                    // let's replace it if it was a real ident
                    if (val.getRawType() == CssTypes.CSS_IDENT) {
                        value = auto;
                    }
                }
            }
        } else {
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssAppearance(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return ((value == none) || (value == initial));
    }

}
