//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio University, Beihang, 2012.
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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-text-spacing
 * @see CssTextAutospace
 * @see CssTextSpacingTrim
 */
public class CssTextSpacing extends org.w3c.css.properties.css.CssTextSpacing {

    private static CssIdent[] allowed_values;

    static {
        String[] id_values = {"none", "auto"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
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
     * Create a new CssTextSpacing
     */
    public CssTextSpacing() {
        value = initial;
    }

    /**
     * Creates a new CssTextSpacing
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTextSpacing(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        CssValue val;
        CssExpression spacexp = null;
        boolean has_trim = false;
        CssIdent id;
        char op;

        setByUser();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        expression.getValue(),
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
            } else if (getAllowedIdent(id) != null) {
                // the single values
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
            } else if (CssTextSpacingTrim.getSpacingTrimIdent(id) != null) {
                if (has_trim) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                has_trim = true;
                values.add(val);
            } else if (CssTextAutospace.getAutospaceIdent(id) != null) {
                if (spacexp == null) {
                    spacexp = new CssExpression();
                }
                // FIXME one way would be to add _all_ the matching idents
                // FIXME then refuse new ones (as avoid values being mixed between
                // FIXME autospace and spacing-trim)
                spacexp.addValue(val);
                spacexp.setOperator(op);
            } else {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        if (spacexp != null) {
            values.add(CssTextAutospace.checkAutoSpace(ac, spacexp, this));
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssTextSpacing(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

