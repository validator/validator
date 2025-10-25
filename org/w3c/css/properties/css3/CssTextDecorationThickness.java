//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#text-decoration-width-property
 */
public class CssTextDecorationThickness extends org.w3c.css.properties.css.CssTextDecorationStyle {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"auto", "from-font"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssTextDecorationStyle
     */
    public CssTextDecorationThickness() {
        value = initial;
    }

    /**
     * Creates a new CssTextDecorationStyle
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTextDecorationThickness(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().getLength();
            case CssTypes.CSS_LENGTH:
            case CssTypes.CSS_PERCENTAGE:
                val.getCheckableValue().checkPositiveness(ac, getPropertyName());
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    value = val;
                    break;
                }
                if (getMatchingIdent(val.getIdent()) != null) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssTextDecorationThickness(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

