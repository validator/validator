//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang University, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-lists-3-20201117/#propdef-list-style-image
 */
public class CssListStyleImage extends org.w3c.css.properties.css.CssListStyleImage {

    /**
     * Create a new CssListStyleImage
     */
    public CssListStyleImage() {
        value = initial;
    }


    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssListStyleImage(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssListStyleImage(ApplContext ac, CssExpression expression,
                             boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;

        val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_IMAGE:
            case CssTypes.CSS_URL:
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id) || none.equals(id)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }
}
