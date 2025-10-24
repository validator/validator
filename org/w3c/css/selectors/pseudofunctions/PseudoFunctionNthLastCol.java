//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.pseudofunctions;

import org.w3c.css.selectors.PseudoFunctionSelector;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssANPlusB;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * PseudoFunctionNthLastCol
 */
public class PseudoFunctionNthLastCol extends PseudoFunctionSelector {

    public PseudoFunctionNthLastCol(String name, CssExpression expression,
                                    ApplContext ac)
            throws InvalidParamException {
        CssANPlusB anpb = null;

        setName(name);
        if (expression == null || expression.getCount() == 0) {
            throw new InvalidParamException("unrecognize", functionName(), ac);
        }
        CssValue val = expression.getValue();
        if (val.getType() != CssTypes.CSS_ANPLUSB) {
            throw new InvalidParamException("value", val.toString(), functionName(), ac);
        }
        anpb = (CssANPlusB) val;
        expression.next();
        setParam(anpb.toString());

    }

}
