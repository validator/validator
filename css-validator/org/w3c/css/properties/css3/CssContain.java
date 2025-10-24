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
 * @spec https://www.w3.org/TR/2020/WD-css-contain-2-20201216/#propdef-contain
 * @spec https://www.w3.org/TR/2022/WD-css-contain-3-20220818/#contain-property
 */
public class CssContain extends org.w3c.css.properties.css.CssContain {

    public static final CssIdent[] allowed_single_values;
    public static final CssIdent[] allowed_multiple_values;
    public static final CssIdent auto = CssIdent.getIdent("auto");
    public static final CssIdent size = CssIdent.getIdent("size");
    public static final CssIdent inline_size = CssIdent.getIdent("inline-size");

    static {
        String[] _allowed_single_values = {"none", "strict", "content"};
        allowed_single_values = new CssIdent[_allowed_single_values.length];
        int i = 0;
        for (String s : _allowed_single_values) {
            allowed_single_values[i++] = CssIdent.getIdent(s);
        }
        // "style" added as of css-contain-2 but at-risk
        String[] _allowed_multiple_values = {"size", "inline-size", "layout", "paint", "style"};
        i = 0;
        allowed_multiple_values = new CssIdent[_allowed_multiple_values.length];
        for (String s : _allowed_multiple_values) {
            allowed_multiple_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getAllowedSingleIdent(CssIdent ident) {
        for (CssIdent id : allowed_single_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getAllowedMultipleIdent(CssIdent ident) {
        for (CssIdent id : allowed_multiple_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static boolean isExclusiveIdent(CssIdent ident) {
        return (size.equals(ident) || inline_size.equals(ident));
    }

    /**
     * Create a new CssContain
     */
    public CssContain() {
        value = initial;
    }

    /**
     * Creates a new CssContain
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssContain(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op;
        CssIdent id, ident;
        boolean got_single = false;
        boolean got_exclusive = false;

        setByUser();

        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssValue> idvalues = new ArrayList<>();

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
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                continue;
            }

            if (getAllowedSingleIdent(id) != null) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                continue;
            }

            ident = getAllowedMultipleIdent(id);
            if (ident == null) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            // check possible duplication
            if (idvalues.contains(ident)) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            if (isExclusiveIdent(ident)) {
                if (got_exclusive) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                } else {
                    got_exclusive = true;
                }
            }
            idvalues.add(ident);
            values.add(val);
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        // no need to check for single values as it was done earlier.
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssContain(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}



