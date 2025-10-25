//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2025.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-transforms-2-20211109/#propdef-translate
 */
public class CssTranslate extends org.w3c.css.properties.css.CssTranslate {

    /**
     * Create a new CssTranslate
     */
    public CssTranslate() {
        value = initial;
    }

    /**
     * Creates a new CssTranslate
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTranslate(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        CssValue val;
        ArrayList<CssValue> v = new ArrayList<>();
        char op;
        setByUser();


        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    // only 0 can be a length...
                    CssCheckableValue p = val.getCheckableValue();
                    p.checkEqualsZero(ac, this);
                    v.add(val);
                    break;
                case CssTypes.CSS_PERCENTAGE:
                    if (v.size() == 2) {
                        // percentage can appear only in the first two values
                        throw new InvalidParamException("value", val.toString(),
                                getPropertyName(), ac);
                    }
                case CssTypes.CSS_LENGTH:
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (none.equals(id) || CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val.toString(),
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    // unrecognize ident, let it fail
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }


    public CssTranslate(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

