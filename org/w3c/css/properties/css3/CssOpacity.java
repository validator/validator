// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// (c) COPYRIGHT 1995-2000  World Wide Web Consortium (MIT, INRIA, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssNumber;
import org.w3c.css.values.CssPercentage;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.math.BigDecimal;

/**
 * @spec https://www.w3.org/TR/2016/WD-css-color-4-20160705/#propdef-opacity
 */

public class CssOpacity extends org.w3c.css.properties.css.CssOpacity {

    /**
     * Create a new CssOpacity
     */
    public CssOpacity() {
        value = initial;
    }

    /**
     * Create a new CssOpacity
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssOpacity(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser(); // tell this property is set by the user
        CssValue val = expression.getValue();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                if (val.getRawType() == CssTypes.CSS_NUMBER) {
                    if (!val.getCheckableValue().isPositive()) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        CssNumber nb = new CssNumber();
                        nb.setIntValue(0);
                        value = nb;
                        break;
                    }
                    BigDecimal pp = val.getNumber().getBigDecimalValue();
                    if (pp.compareTo(BigDecimal.ONE) > 0) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        CssNumber nb = new CssNumber();
                        nb.setIntValue(1);
                        value = nb;
                        break;
                    }
                } else {
                    // we can only check if >= 0 for now
                    val.getCheckableValue().warnPositiveness(ac, this);
                }
                value = val;
                break;
            case CssTypes.CSS_PERCENTAGE:
                // This starts with CSS Color 4
                if (!val.getCheckableValue().isPositive()) {
                    ac.getFrame().addWarning("out-of-range", val.toString());
                    CssNumber nb = new CssNumber();
                    nb.setIntValue(0);
                    value = nb;
                    break;
                }
                if (val.getRawType() == CssTypes.CSS_PERCENTAGE) {
                    float p = ((CssPercentage) val).floatValue();
                    if (p > 100.) {
                        ac.getFrame().addWarning("out-of-range", val.toString());
                        value = new CssPercentage(100);
                        break;
                    }
                }
                value = val;
                break;
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    value = val;
                    break;
                }
                // let it flow through the exception
            default:
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssOpacity(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value It is used by all macro for
     * the function <code>print</code>
     */
    public boolean isDefault() {
        if (value.getRawType() == CssTypes.CSS_NUMBER) {
            try {
                return (value.getNumber().getValue() == 1.f);
            } catch (Exception ex) {
                return false;
            }
        }
        return (value == initial);
    }

}
