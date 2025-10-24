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
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-user-select
 */

public class CssUserSelect extends org.w3c.css.properties.css.CssUserSelect {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    public static final CssIdent[] allowed_values;

    static {
        int i = 0;
        String[] _allowed_values = {"auto", "text", "none", "contain", "all"};
        allowed_values = new CssIdent[_allowed_values.length];
        i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
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

    /**
     * Create a new CssUserSelect
     */
    public CssUserSelect() {
        value = initial;
    }

    /**
     * Create a new CssUserSelect
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect value
     */
    public CssUserSelect(ApplContext ac, CssExpression expression,
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
            }
        } else {
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssUserSelect(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return ((value == auto) || (value == initial));
    }
}
