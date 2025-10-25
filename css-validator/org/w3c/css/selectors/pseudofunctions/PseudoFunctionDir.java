//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.selectors.pseudofunctions;

import org.w3c.css.selectors.PseudoFunctionSelector;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

/**
 * PseudoFunctionDir
 */
public class PseudoFunctionDir extends PseudoFunctionSelector {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"ltr", "rtl"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedValues(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }


    public PseudoFunctionDir(String name, CssExpression exp, ApplContext ac)
            throws InvalidParamException {
        setName(name);
        setParam(parseDir(ac, exp, functionName()));
    }

    /**
     * verify a language tag per BCP47
     *
     * @param ac     the ApplContext
     * @param exp    the CssExpression
     * @param caller the property/selector/context calling for verification
     * @throws InvalidParamException if invalid
     */
    public static final CssValue parseDir(ApplContext ac, CssExpression exp, String caller)
            throws InvalidParamException {
        if (exp.getCount() != 1) {
            throw new InvalidParamException("unrecognize", caller, ac);
        }
        CssValue val = exp.getValue();
        if ((val.getType() != CssTypes.CSS_IDENT) || (getAllowedValues(val.getIdent()) == null)) {
            throw new InvalidParamException("value", val.toString(), caller, ac);
        }
        exp.next();
        return val;
    }

}
