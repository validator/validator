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
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec https://www.w3.org/TR/2018/WD-css-animations-1-20181011/#propdef-animation-iteration-count
 */
public class CssAnimationIterationCount extends org.w3c.css.properties.css.CssAnimationIterationCount {

    public static final CssIdent infinite = CssIdent.getIdent("infinite");

    public static CssIdent getAllowedIdent(CssIdent ident) {
        return (infinite.equals(ident) ? infinite : null);
    }

    /**
     * Create a new CssAnimationIterationCount
     */
    public CssAnimationIterationCount() {
        value = initial;
    }

    /**
     * Creates a new CssAnimationIterationCount
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssAnimationIterationCount(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        boolean singleVal = false;
        CssValue sValue = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkPositiveness(ac, this);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        singleVal = true;
                        sValue = val;
                        values.add(val);
                        break;
                    } else {
                        if (getAllowedIdent(id) != null) {
                            values.add(val);
                            break;
                        }
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (singleVal && values.size() > 1) {
            throw new InvalidParamException("value",
                    sValue.toString(),
                    getPropertyName(), ac);
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssAnimationIterationCount(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

