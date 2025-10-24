//
// Author: Yves Lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 2018 World Wide Web Consortium, (MIT, ERCIM, Keio, Beihang)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-align-3-20200421/#propdef-row-gap
 */

public class CssRowGap extends org.w3c.css.properties.css.CssRowGap {

    static CssIdent normal;

    static {
        normal = CssIdent.getIdent("normal");
    }

    /**
     * Create a new CssColumnGap
     */
    public CssRowGap() {
        value = initial;
    }

    /**
     * Create a new CssColumnGap
     */
    public CssRowGap(ApplContext ac, CssExpression expression,
                     boolean check) throws InvalidParamException {
        setByUser();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        value = parseRowGap(ac, expression, this);
    }

    public static CssValue parseRowGap(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, caller);
                break;
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_LENGTH:
                CssCheckableValue l = val.getCheckableValue();
                l.checkPositiveness(ac, caller);
                break;
            case CssTypes.CSS_IDENT:
                CssIdent id = val.getIdent();
                if (normal.equals(id)) {
                    break;
                }
                if (CssIdent.isCssWide(id)) {
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        caller.getPropertyName(), ac);
        }
        expression.next();
        return val;

    }

    public CssRowGap(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (value == initial);
    }

}
