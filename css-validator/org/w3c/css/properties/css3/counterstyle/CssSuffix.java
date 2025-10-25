//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.counterstyle;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2015/CR-css-counter-styles-3-20150611/#descdef-counter-style-suffix
 */
public class CssSuffix extends org.w3c.css.properties.css.counterstyle.CssSuffix {

    public static final CssString default_suffix = new CssString(". ");

    /**
     * Create a new CssSuffix
     */
    public CssSuffix() {
        value = default_suffix;
    }

    /**
     * Creates a new CssSuffix
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssSuffix(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;

        setByUser();

        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_URL:
            case CssTypes.CSS_IMAGE:
            case CssTypes.CSS_STRING:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (!CssIdent.isCssWide((CssIdent) val)) {
                    value = val;
                    break;
                }
                // reserved keyword, fail
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();

    }

    public CssSuffix(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

