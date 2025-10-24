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
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;


/**
 * @spec https://www.w3.org/TR/2015/CR-compositing-1-20150113/#propdef-background-blend-mode
 */
public class CssBackgroundBlendMode extends org.w3c.css.properties.css.CssBackgroundBlendMode {

    /**
     * Create a new CssBackgroundBlendMode
     */
    public CssBackgroundBlendMode() {
        value = initial;
    }

    /**
     * Creates a new CssBackgroundBlendMode
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssBackgroundBlendMode(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        CssIdent ident;
        char op;

        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT) {
                ident = val.getIdent();
                if (CssIdent.isCssWide(ident)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                } else {
                    if (CssMixBlendMode.getAllowedIdent(ident) == null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                }
            } else {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            expression.next();
            if (op != COMMA && !expression.end()) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssBackgroundBlendMode(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

