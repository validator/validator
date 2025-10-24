// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, Beihang, 2012.
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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-white-space
 */
public class CssWhiteSpace extends org.w3c.css.properties.css.CssWhiteSpace {


    public static CssIdent[] allowed_values;

    static {
        String[] WHITESPACE = {
                "normal", "pre", "pre-wrap", "pre-line"
        };
        allowed_values = new CssIdent[WHITESPACE.length];
        int i = 0;
        for (String aWS : WHITESPACE) {
            allowed_values[i++] = CssIdent.getIdent(aWS);
        }
    }

    public static final CssIdent getSingleValueIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /*
     * Create a new CssWhiteSpace
     */
    public CssWhiteSpace() {
        value = initial;
    }

    /**
     * Create a new CssWhiteSpace
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException values are incorrect
     */
    public CssWhiteSpace(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        CssValue val;
        CssExpression trimexp = null;
        CssIdent id;
        char op;
        boolean got_collapse = false;
        boolean got_wrap_mode = false;
        //  we need 5 for <'white-space-collapse'> || <'text-wrap-mode'> || <'white-space-trim'>
        // as <'white-space-trim'> can contain as much as 3 values
        if (check && expression.getCount() > 5) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            // ident, so inherit, or allowed value
            if (CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
            }
            if ((id = getSingleValueIdent(val.getIdent())) != null) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
            } else if ((id = CssWhiteSpaceCollapse.getAllowedIdent(val.getIdent())) != null) {
                if (got_collapse) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_collapse = true;
                values.add(val);
            } else if ((id = CssTextWrapMode.getAllowedIdent(val.getIdent())) != null) {
                if (got_wrap_mode) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_wrap_mode = true;
                values.add(val);
            } else if ((id = CssTextWrapMode.getAllowedIdent(val.getIdent())) != null) {
                // TODO FIXME check if the values have to be contiguous or not
                if (trimexp == null) {
                    trimexp = new CssExpression();
                }
                trimexp.addValue(val);
                trimexp.setOperator(op);
            } else {
                // nothing we know...
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
        // we got everything, check now that the wrap-mode related values are valid
        if (trimexp != null) {
            values.add(CssWhiteSpaceTrim.checkWhiteSpaceTrim(ac, trimexp, this));
        }

        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssWhiteSpace(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}
