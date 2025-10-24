//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2021/CR-css-scroll-snap-1-20210311/#scroll-snap-align
 */
public class CssScrollSnapAlign extends org.w3c.css.properties.css.CssScrollSnapAlign {

    private static CssIdent[] allowed_values;

    static {
        String id_values[] = {"none", "start", "end", "center"};
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
     * Create a new CssScrollSnapAlign
     */
    public CssScrollSnapAlign() {
        value = initial;
    }

    /**
     * Creates a new CssScrollSnapAlign
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssScrollSnapAlign(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        CssValue val = expression.getValue();
        char op = expression.getOperator();
        ArrayList<CssValue> values = new ArrayList<>();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                CssIdent ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    values.add(val);
                    break;
                }
                if (getMatchingIdent(ident) != null) {
                    values.add(val);
                    break;
                }
                // unrecognized... fail.
            default:
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
        }
        expression.next();
        if (!expression.end()) {
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            val = expression.getValue();
            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    // can't have inherit second, so don't test for it
                    if (getMatchingIdent(ident) != null) {
                        values.add(val);
                        break;
                    }
                    // unrecognized... fail.
                default:
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssScrollSnapAlign(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

