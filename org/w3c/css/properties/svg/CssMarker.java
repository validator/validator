//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#MarkerStartProperty
 */
public class CssMarker extends org.w3c.css.properties.css.CssMarkerStart {

    /**
     * Create a new CssMarkerStart
     */
    public CssMarker() {
        value = initial;
    }

    /**
     * Creates a new CssMarkerStart
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMarker(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        value = checkMarkerValue(this, ac, expression, check);
    }

    protected static CssValue checkMarkerValue(CssProperty property, ApplContext ac,
                                               CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        property.setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_URL:
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    break;
                }
                if (none.equals(val.getIdent())) {
                    break;
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        property.getPropertyName(), ac);
        }
        expression.next();
        return val;
    }


    public CssMarker(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

