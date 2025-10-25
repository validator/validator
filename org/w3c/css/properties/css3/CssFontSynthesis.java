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
import java.util.Arrays;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-synthesis
 */
public class CssFontSynthesis extends org.w3c.css.properties.css.CssFontSynthesis {

    public static final CssIdent[] allowedValues;

    static {
        String[] _allowedValues = {"weight", "style", "small-caps"};
        allowedValues = new CssIdent[_allowedValues.length];
        for (int i = 0; i < allowedValues.length; i++) {
            allowedValues[i] = CssIdent.getIdent(_allowedValues[i]);
        }
        Arrays.sort(allowedValues);
    }

    public static final CssIdent getAllowedValue(CssIdent ident) {
        for (CssIdent id : allowedValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssFontSynthesis
     */
    public CssFontSynthesis() {
        value = initial;
    }

    /**
     * Creates a new CssFontSynthesis
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontSynthesis(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > allowedValues.length) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        CssIdent okIdent;
        char op;
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssIdent> ids = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id) || none.equals(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                value = val;
            } else {
                okIdent = getAllowedValue(id);
                if (okIdent == null) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                if (ids.contains(okIdent)) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                values.add(val);
                ids.add(okIdent);

            }
            expression.next();
            if (!expression.end() && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (!values.isEmpty()) {
            value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        }
    }

    public CssFontSynthesis(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

