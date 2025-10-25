//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM and Keio)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-count
 */

public class CssColumnCount extends org.w3c.css.properties.css.CssColumnCount {


    static CssIdent auto = CssIdent.getIdent("auto");

    /**
     * Create a new CssColumnCount
     */
    public CssColumnCount() {
        value = initial;
    }

    /**
     * Create a new CssColumnCount
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Incorrect value
     */
    public CssColumnCount(ApplContext ac, CssExpression expression,
                          boolean check) throws InvalidParamException {

        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                CssCheckableValue num = val.getCheckableValue();
                num.checkInteger(ac, this);
                num.checkStrictPositiveness(ac, this);
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (auto.equals(ident)) {
                    value = val;
                    break;
                }
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                    break;
                }
            default:
                throw new InvalidParamException("value", expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssColumnCount(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (value == auto) || (value == initial);
    }
}
