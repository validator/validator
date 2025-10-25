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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-text-transform
 */
public class CssTextTransform extends org.w3c.css.properties.css.CssTextTransform {

    private static CssIdent[] allowed_action_values;
    private static CssIdent fullWidth, fullSizeKana;


    static {
        fullWidth = CssIdent.getIdent("full-width");
        fullSizeKana = CssIdent.getIdent("full-size-kana");

        String id_values[] = {"capitalize", "uppercase", "lowercase"};
        allowed_action_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_action_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getMatchingActionIdent(CssIdent ident) {
        for (CssIdent id : allowed_action_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssTextTransform
     */
    public CssTextTransform() {
        value = initial;
    }

    /**
     * Creates a new CssTextTransform
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssTextTransform(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();
        char op;
        ArrayList<CssValue> values = new ArrayList<>();
        boolean got_action = false;
        boolean got_full_width = false;
        boolean got_full_size_kana = false;

        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

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
            } else if (none.equals(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
            } else if (fullWidth.equals(val.getIdent()) && !got_full_width) {
                got_full_width = true;
                values.add(val);
            } else if (fullSizeKana.equals(val.getIdent()) && !got_full_size_kana) {
                got_full_size_kana = true;
                values.add(val);
            } else if (!got_action) {
                if (getMatchingActionIdent(val.getIdent()) == null) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_action = true;
                values.add(val);
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
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssTextTransform(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

