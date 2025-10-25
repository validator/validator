// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2014.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css2;

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
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/page.html#propdef-size
 */
public class CssSize extends org.w3c.css.properties.css.CssSize {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"auto", "portrait", "landscape"};
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
     * Create a new CssSize
     */
    public CssSize() {
    }

    /**
     * Creates a new CssSize
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssSize(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        ArrayList<CssValue> vals = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val = val.getLength();
                case CssTypes.CSS_LENGTH:
                    vals.add(val);
                    // not in the spec, but size ought to be non-negative
                    val.getLength().checkPositiveness(ac, this);
                    break;
                case CssTypes.CSS_IDENT:
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    value = getAllowedIdent((CssIdent) val);
                    if (value != null) {
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
        // set the value if needed (only lengths are added in the ArrayList)
        if (vals.size() != 0) {
            // and set the value (the empty arraylist is taken care of during parsing)
            value = (vals.size() == 1) ? vals.get(0) : new CssValueList(vals);
        }
    }

    public CssSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

