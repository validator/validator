// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
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
 * @spec https://github.com/w3c/csswg-drafts/blob/d644687e36b3290d62fe0117a94345addf3468af/css-size-adjust-1/Overview.bs
 */

public class CssTextSizeAdjust extends org.w3c.css.properties.css.CssTextSizeAdjust {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    public static final CssIdent[] allowed_values;

    static {
        int i = 0;
        String[] _allowed_values = {"auto", "none"};
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
     * Create a new CssTextSizeAdjust
     */
    public CssTextSizeAdjust() {
        value = initial;
    }

    /**
     * Create a new CssTextSizeAdjust
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect value
     */
    public CssTextSizeAdjust(ApplContext ac, CssExpression expression,
                             boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;

        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    value = val;
                    break;
                } else {
                    if (getAllowedIdent(id) == null) {
                        throw new InvalidParamException("value", val.toString(),
                                getPropertyName(), ac);
                    }
                    value = val;
                    break;
                }
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, this);
            case CssTypes.CSS_PERCENTAGE:
                value = val;
                break;
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
    }

    public CssTextSizeAdjust(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    public boolean isDefault() {
        return ((auto.equals(value)) || (initial.equals(value)));
    }

}
