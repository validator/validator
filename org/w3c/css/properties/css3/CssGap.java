//
// Author: Yves Lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM, Keio, Beihang)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-align-3-20200421/#propdef-gap
 */

public class CssGap extends org.w3c.css.properties.css.CssGap {

    private CssColumnGap columnGap;
    private CssRowGap rowGap;

    /**
     * Create a new CssGap
     */
    public CssGap() {
        value = initial;
        columnGap = new CssColumnGap();
        rowGap = new CssRowGap();
    }

    /**
     * Create a new CssGap
     */
    public CssGap(ApplContext ac, CssExpression expression,
                  boolean check) throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val;
        // get the separator now in case we need it.
        char op = expression.getOperator();
        // create the values we will fill
        columnGap = new CssColumnGap();
        rowGap = new CssRowGap();

        val = CssRowGap.parseRowGap(ac, expression, this);
        rowGap.value = val;
        if (!expression.end()) {
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            // inherit can only be alone
            if ((val.getType()== CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
            }
            val = CssRowGap.parseRowGap(ac, expression, this);
            // same for value #2
            if ((val.getType()== CssTypes.CSS_IDENT) && CssIdent.isCssWide(val.getIdent())) {
                throw new InvalidParamException("value", val.toString(),
                        getPropertyName(), ac);
            }
            columnGap.value = val;
            ArrayList<CssValue> v = new ArrayList<>(2);
            v.add(rowGap.value);
            v.add(columnGap.value);
            value = new CssValueList(v);
        } else {
            value = val;
            columnGap.value = val;
        }

    }

    public CssGap(ApplContext ac, CssExpression expression)
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
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        columnGap.addToStyle(ac, style);
        rowGap.addToStyle(ac, style);
    }
}
