//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontfeaturevalues;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * @spec https://www.w3.org/TR/2026/WD-css-fonts-4-20260303/
 */
public class CssCatchallProperty extends org.w3c.css.properties.css.fontfeaturevalues.CatchallProperty {

    /**
     * Create a new CssBasePalette
     */
    public CssCatchallProperty() {
        super();
        value = initial;
    }

    /**
     * Creates a new CssBasePalette
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssCatchallProperty(ApplContext ac, String propertyName,
                               CssExpression expression, boolean check)
            throws InvalidParamException {
        this.name = propertyName;

        setByUser();

        char op;
        CssValue val;
        if (expression.getRemainingCount() > 1) {
            throw new InvalidParamException("unrecognize", ac);
        }

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_NUMBER:
                value = val;
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssCatchallProperty(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, "**catchall**", expression, false);
    }

}

