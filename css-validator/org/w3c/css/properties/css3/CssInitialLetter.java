//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssCheckableValue;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec https://www.w3.org/TR/2020/WD-css-inline-3-20200827/#propdef-initial-letter
 */
public class CssInitialLetter extends org.w3c.css.properties.css.CssInitialLetter {

    CssIdent normal = CssIdent.getIdent("normal");

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"drop", "raise"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssInitialLetter
     */
    public CssInitialLetter() {
        value = initial;
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException The expression is incorrect
     */
    public CssInitialLetter(ApplContext ac, CssExpression expression,
                            boolean check) throws InvalidParamException {

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> v = new ArrayList<>(2);
        setByUser();
        char op;
        CssValue val;
        boolean got_number = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    if (!got_number) {
                        got_number = true;
                    } else {
                        // the second integer must be an integer
                        CssCheckableValue n = val.getCheckableValue();
                        n.checkInteger(ac, this);
                    }
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id) || normal.equals(id)) {
                        // we can have only one ident
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", expression.getValue(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    if (getAllowedIdent(id) != null) {
                        v.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (!v.isEmpty()) {
            // sanity check, we cannot have two idents
            if (v.size() == 2 && !got_number) {
                throw new InvalidParamException("value", v.get(1),
                        getPropertyName(), ac);
            }
            value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
        }
    }

    public CssInitialLetter(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

