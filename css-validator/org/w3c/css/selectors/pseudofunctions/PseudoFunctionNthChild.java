// $Id$
// Author: Jean-Guilhem Rouel
// (c) COPYRIGHT MIT, ERCIM and Keio, 2005.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.pseudofunctions;

import org.w3c.css.parser.CssSelectors;
import org.w3c.css.selectors.PseudoFunctionSelector;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssANPlusB;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

/**
 * PseudoFunctionNthChild<br />
 * Created: Sep 2, 2005 4:22:54 PM<br />
 */
public class PseudoFunctionNthChild extends PseudoFunctionSelector {

    public static final CssIdent of = CssIdent.getIdent("of");

    public PseudoFunctionNthChild(String name, CssExpression expression,
                                  ArrayList<CssSelectors> selector_list,
                                  ApplContext ac)
            throws InvalidParamException
    {
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

        // no ident and selectors_list non-empty? -> fail
        if (expression.end() && selector_list != null) {
            throw new InvalidParamException("value",  CssSelectors.toArrayString(selector_list),
                    functionName(), ac);
        }
        if (!expression.end()) {
            val = expression.getValue();
            if (val.getRawType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", val.toString(), functionName(), ac);
            }
            // waiting for "of"
            if (!of.equals(val.getIdent())) {
                throw new InvalidParamException("value", val.toString(), functionName(), ac);
            }
            expression.next();
            // nothing more expected
            if (!expression.end()) {
                throw new InvalidParamException("value", expression.getValue().toString(), functionName(), ac);
            }
        }
        // now build the string representation
        if (selector_list == null) {
            setParam(anpb.toString());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(anpb.toString()).append(" of ");
            sb.append(CssSelectors.toArrayString(selector_list));
            setParam(sb.toString());
        }

    }

}
