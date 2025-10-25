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
 * @spec https://www.w3.org/TR/2021/CR-css-scroll-snap-1-20210311/#propdef-scroll-snap-type
 */
public class CssScrollSnapType extends org.w3c.css.properties.css.CssScrollSnapType {

    private static CssIdent[] allowed_axis_values;
    private static CssIdent[] allowed_strictness_values;

    static {
        String axis_values[] = {"x", "y", "block", "inline", "both"};
        allowed_axis_values = new CssIdent[axis_values.length];
        int i = 0;
        for (String s : axis_values) {
            allowed_axis_values[i++] = CssIdent.getIdent(s);
        }
        String strictness_values[] = {"mandatory", "proximity"};
        allowed_strictness_values = new CssIdent[strictness_values.length];
        i = 0;
        for (String s : strictness_values) {
            allowed_strictness_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingAxisIdent(CssIdent ident) {
        for (CssIdent id : allowed_axis_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getMatchingStrictnesssIdent(CssIdent ident) {
        for (CssIdent id : allowed_strictness_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssScrollSnapType
     */
    public CssScrollSnapType() {
        value = initial;
    }

    /**
     * Creates a new CssScrollSnapType
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssScrollSnapType(ApplContext ac, CssExpression expression, boolean check)
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
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                    break;
                }
                // 'none' is a strictness value, but can only appear alone
                if (none.equals(ident)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                    break;
                }
                if (getMatchingAxisIdent(ident) != null) {
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
                    if (getMatchingStrictnesssIdent(ident) != null) {
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

    public CssScrollSnapType(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

