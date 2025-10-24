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

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-text-decor-4-20200506/#propdef-text-decoration-skip-spaces
 */
public class CssTextDecorationSkipSpaces extends org.w3c.css.properties.css.CssTextDecorationSkipSpaces {

    protected static CssIdent[] single_allowed_values, multiple_allowed_values;

    static {
        String _single_values[] = {"none", "all"};
        single_allowed_values = new CssIdent[_single_values.length];
        int i = 0;
        for (String s : _single_values) {
            single_allowed_values[i++] = CssIdent.getIdent(s);
        }
        String _multiple_values[] = {"start", "end"};
        multiple_allowed_values = new CssIdent[_multiple_values.length];
        i = 0;
        for (String s : _multiple_values) {
            multiple_allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    protected static final CssIdent getSingleAllowedValue(CssIdent ident) {
        for (CssIdent id : single_allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    protected static final CssIdent getMultipleAllowedValue(CssIdent ident) {
        for (CssIdent id : multiple_allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        CssIdent id = getSingleAllowedValue(ident);
        if (id != null) {
            return id;
        }
        return getMultipleAllowedValue(ident);
    }


    /**
     * Create a new CssTextDecorationSkipSpaces
     */
    public CssTextDecorationSkipSpaces() {
        value = initial;
    }

    /**
     * Creates a new CssTextDecorationSkipSpaces
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTextDecorationSkipSpaces(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > multiple_allowed_values.length) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val, v;
        CssIdent id;
        char op;

        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            id = val.getIdent();
            if (CssIdent.isCssWide(id)) {
                if (expression.getCount() != 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                values.add(val);
                break;
            }
            v = getSingleAllowedValue(id);
            if (v != null) {
                if (expression.getCount() != 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                values.add(val);
                break;
            }
            v = getMultipleAllowedValue(id);
            if (v == null) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            values.add(val);
            expression.next();

            if (!expression.end() && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        // sanity check
        for (int i=0; i< values.size(); i++) {
            if (values.lastIndexOf(values.get(i)) != i) {
                throw new InvalidParamException("value",
                        values.get(i).toString(),
                        getPropertyName(), ac);
            }
        }
        value = (values.size() > 1) ? new CssValueList(values) : values.get(0);
    }

    public CssTextDecorationSkipSpaces(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

