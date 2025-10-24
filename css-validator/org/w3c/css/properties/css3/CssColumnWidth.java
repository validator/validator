//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewriten 2010 Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 1995-2010  World Wide Web Consortium (MIT, ERCIM, Keio, Beihang)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-width
 * @spec https://www.w3.org/TR/2021/WD-css-sizing-3-20210317/
 */

public class CssColumnWidth extends org.w3c.css.properties.css.CssColumnWidth {

    public static final CssIdent[] allowed_values;

    static {
        // max|min-content from css-sizing
        String[] _allowed_values = {"auto", "max-content", "min-content"};

        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
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
     * Create a new CssColumnWidth
     */
    public CssColumnWidth() {
        value = initial;
    }

    /**
     * Create a new CssColumnWidth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Incorrect value
     */
    public CssColumnWidth(ApplContext ac, CssExpression expression,
                          boolean check) throws InvalidParamException {

        setByUser();
        CssValue val = expression.getValue();

        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, this);
            case CssTypes.CSS_LENGTH:
                CssCheckableValue l = val.getCheckableValue();
                l.checkPositiveness(ac, this);
                // warn for 1px clamping
                if (l.isZero()) {
                    ac.getFrame().addWarning("greaterequal", new String[]{l.toString(), "1px"});
                }
                value = val;
                break;
            case CssTypes.CSS_FUNCTION:
                value = parseFitContentFunction(ac, val, this);
                break;
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    value = val;
                } else {
                    if (getAllowedIdent(ident) != null) {
                        value = val;
                    } else {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                }
                break;

            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssColumnWidth(ApplContext ac, CssExpression expression)
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

    /**
     * @spec https://www.w3.org/TR/2018/WD-css-sizing-3-20180304/#valdef-column-width-fit-content-length-percentage
     */
    protected static CssFunction parseFitContentFunction(ApplContext ac, CssValue value,
                                                         CssProperty caller)
            throws InvalidParamException {

        CssFunction function = value.getFunction();
        CssExpression exp = function.getParameters();
        CssValue val;

        if (!"fit-content".equals(function.getName())) {
            throw new InvalidParamException("function", ac);
        }
        if (exp.getCount() > 1) {
            throw new InvalidParamException("function", ac);
        }
        val = exp.getValue();
        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                val.getCheckableValue().checkEqualsZero(ac, caller);
            case CssTypes.CSS_PERCENTAGE:
            case CssTypes.CSS_LENGTH:
                break;
            default:
                throw new InvalidParamException("value", val,
                        caller.getPropertyName(), ac);
        }
        return function;
    }

}
