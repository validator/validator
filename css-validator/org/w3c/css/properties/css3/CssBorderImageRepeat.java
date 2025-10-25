// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-border-image-repeat
 */
public class CssBorderImageRepeat extends org.w3c.css.properties.css.CssBorderImageRepeat {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"stretch", "repeat", "round", "space"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssBorderImageWidth
     */
    public CssBorderImageRepeat() {
        value = initial;
    }

    /**
     * Creates a new CssBorderImageWidth
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssBorderImageRepeat(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        CssValueList valueList = new CssValueList();
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        valueList.add(val);
                        break;
                    }
                    if (getMatchingIdent(id) != null) {
                        valueList.add(val);
                        break;
                    }
                    // unrecognized ident, let it fail
                default:
                    throw new InvalidParamException("value", val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        value = (valueList.size() == 1) ? valueList.get(0) : valueList;
    }

    public CssBorderImageRepeat(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

