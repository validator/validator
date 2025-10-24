//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.page;

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
 * @spec https://www.w3.org/TR/2018/WD-css-page-3-20181018/#descdef-page-marks
 */
public class CssMarks extends org.w3c.css.properties.css.page.CssMarks {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"crop", "cross"};
        int i = 0;
        allowed_values = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssMarks
     */
    public CssMarks() {
        value = initial;
    }

    /**
     * Creates a new CssMarks
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMarks(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        ArrayList<CssValue> vals = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = inherit;
                        break;
                    }
                    if (none.equals(val)) {
                        value = none;
                        break;
                    }
                    // now we go to other values...
                    CssValue v = getAllowedIdent((CssIdent) val);
                    if (v != null) {
                        vals.add(v);
                        break;
                    }
                    // unrecognized ident.. fail!
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        // extra checks
        if (vals.size() != 0) {
            // inherit or none are single value
            if (value == inherit || value == none) {
                throw new InvalidParamException("value", vals.get(0),
                        getPropertyName(), ac);
            }
            // if we have two values and those are the same...
            if (vals.size() == 2 && (vals.get(0) == vals.get(1))) {
                throw new InvalidParamException("value", vals.get(0),
                        getPropertyName(), ac);
            }
            // and set the value (the empty arraylist is taken care of during parsing)
            value = (vals.size() == 1) ? vals.get(0) : new CssValueList(vals);
        }
    }

    public CssMarks(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

