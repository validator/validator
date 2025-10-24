//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.util.Util;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#StrokeOpacityProperty
 */
public class CssStrokeOpacity extends org.w3c.css.properties.css.CssStrokeOpacity {

    /**
     * Brings all values back between 0 and 1
     *
     * @param opacity The value to be modified if necessary
     */
    private float clampedValue(ApplContext ac, float opacity) {
        if (opacity < 0.f || opacity > 1.f) {
            ac.getFrame().addWarning("out-of-range", Util.displayFloat(opacity));
            return ((opacity < 0.f) ? 0.f : 1.f);
        }
        return opacity;
    }

    /**
     * Create a new CssStrokeOpacity
     */
    public CssStrokeOpacity() {
        value = initial;
    }

    /**
     * Creates a new CssStrokeOpacity
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssStrokeOpacity(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                // number 0..1
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    // this will generate a warning if necessary
                    CssNumber number = val.getNumber();
                    number.setFloatValue(clampedValue(ac, number.getValue()));
                } else {
                    // we can only check if >= 0 for now
                    val.getCheckableValue().warnPositiveness(ac, this);
                }
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
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

    public CssStrokeOpacity(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

