//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2018/CR-css-flexbox-1-20181119/#propdef-flex-basis
 * @see CssWidth
 */
public class CssFlexBasis extends org.w3c.css.properties.css.CssFlexBasis {

    public static final CssIdent content = CssIdent.getIdent("content");

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        if (content.equals(ident)) {
            return content;
        }
        return CssWidth.getAllowedIdent(ident);
    }

    /**
     * Create a new CssFlexBasis
     */
    public CssFlexBasis() {
        value = initial;
    }

    /**
     * Creates a new CssFlexBasis
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssFlexBasis(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();

        setByUser();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    break;
                } else if (content.equals(ident)) {
                    value = val;
                    break;
                }
            default:
                // content or else CssWidth
                CssExpression e = new CssExpression();
                e.addValue(val);
                try {
                    CssWidth cssWidth = new CssWidth(ac, e, check);
                    value = cssWidth.value;
                } catch (InvalidParamException ex) {
                    throw new InvalidParamException("value", val, getPropertyName(), ac);
                }
        }
        expression.next();
    }

    public CssFlexBasis(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return ((value == content) || (value == initial));
    }
}

