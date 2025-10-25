//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2018/WD-css-logical-1-20180827/#propdef-border-block
 */
public class CssBorderBlock extends org.w3c.css.properties.css.CssBorderBlock {

    /**
     * Create a new CssBorderBlock
     */
    public CssBorderBlock() {
        value = initial;
    }

    /**
     * Creates a new CssBorderBlock
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssBorderBlock(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssBorder.SideValues values = CssBorder.parseBorderSide(ac, expression, check, this);
        ArrayList<CssValue> vl = new ArrayList<>();

        if (values.width != null) {
            vl.add(values.width);
        }
        if (values.style != null) {
            vl.add(values.style);
        }
        if (values.color != null) {
            vl.add(values.color);
        }
        setByUser();

        if (vl.size() == 1) {
            value = vl.get(0);
        } else {
            if (vl.contains(inherit)) {
                // no need to bail out, multiple inherit value is already checked
                value = inherit;
            } else {
                value = new CssValueList(vl);
            }
        }
    }


    public CssBorderBlock(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

