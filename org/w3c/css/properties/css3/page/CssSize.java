//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang University 2014.
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
 * @spec https://www.w3.org/TR/2018/WD-css-page-3-20181018/#page-size-prop
 */
public class CssSize extends org.w3c.css.properties.css.CssSize {

    public static final CssIdent[] allowed_modifier;
    public static final CssIdent[] allowed_page_size;
    public static final CssIdent auto;

    static {
        auto = CssIdent.getIdent("auto");

        String[] _allowed_values = {"portrait", "landscape"};
        int i = 0;
        allowed_modifier = new CssIdent[_allowed_values.length];
        for (String s : _allowed_values) {
            allowed_modifier[i++] = CssIdent.getIdent(s);
        }

        String[] _page_size_values = {"a5", "a4", "a3", "b5", "b4", "letter", "legal", "ledger"};
        i = 0;
        allowed_page_size = new CssIdent[_page_size_values.length];
        for (String s : _page_size_values) {
            allowed_page_size[i++] = CssIdent.getIdent(s);
        }
    }


    public static final CssIdent getAllowedIdent(CssIdent ident) {
        for (CssIdent id : allowed_modifier) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedSizeIdent(CssIdent ident) {
        for (CssIdent id : allowed_page_size) {
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
        value = initial;
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

        boolean gotSize = false;
        boolean gotModifier = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkEqualsZero(ac, this);
                case CssTypes.CSS_LENGTH:
                    vals.add(val);
                    // not in the spec, but size ought to be non-negative
                    val.getCheckableValue().checkPositiveness(ac, this);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent ident = (CssIdent) val;
                    if (auto.equals(ident)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        vals.add(auto);
                        break;
                    }
                    CssIdent v = null;
                    if (!gotSize) {
                        v = getAllowedIdent(ident);
                        gotSize = (v != null);
                    }
                    if (v == null && !gotModifier) {
                        v = getAllowedSizeIdent(ident);
                        gotModifier = (v != null);
                    }
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
        // some sanity checking...
        if (vals.size() > 1) {
            CssValue v1, v2;
            v1 = vals.get(0);
            v2 = vals.get(1);
            // we only get 2 lengths or two idents.
            if (v1.getType() != v2.getType()) {
                throw new InvalidParamException("value", v2,
                        getPropertyName(), ac);
            }
            if (v1.getType() == CssTypes.CSS_IDENT) {
                // here, auto must be alone
                if (v1 == auto || v2 == auto) {
                    throw new InvalidParamException("value", auto,
                            getPropertyName(), ac);
                }
                // the modifier/size has been dealt with during parsing
            }

        }
        // set the value
        value = (vals.size() == 1) ? vals.get(0) : new CssValueList(vals);
    }

    public CssSize(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

