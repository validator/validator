//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.fontface;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssString;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#descdef-font-face-font-family
 * @see org.w3c.css.properties.css3.CssFontFamily
 */
public class CssFontFamily extends org.w3c.css.properties.css.fontface.CssFontFamily {


    /**
     * Create a new CssFontFamily
     */
    public CssFontFamily() {
        value = initial;
    }

    /**
     * Creates a new CssFontFamily
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssFontFamily(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        char op;
        CssValue val;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_STRING:
                value = val;
                if (check && expression.getCount() > 1) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                break;
            case CssTypes.CSS_IDENT:
                // we can have multiple values here
                ArrayList<CssIdent> idval = new ArrayList<CssIdent>();
                idval.add(val.getIdent());
                // we add idents if separated by spaces...
                while (op == SPACE && expression.getRemainingCount() > 1) {
                    expression.next();
                    op = expression.getOperator();
                    val = expression.getValue();
                    if (val.getType() == CssTypes.CSS_IDENT) {
                        idval.add(val.getIdent());
                    } else {
                        throw new InvalidParamException("value", val,
                                getPropertyName(), ac);
                    }
                }
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                value = checkExpression(ac, idval);
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssFontFamily(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    private CssValue checkExpression(ApplContext ac, ArrayList<CssIdent> values)
            throws InvalidParamException {
        CssIdent val;
        if (values.size() > 1) {
            // create a value out of that. We could even create
            // a CssString for the output (TODO ?)
            StringBuilder sb = new StringBuilder("\"");
            boolean addSpace = false;
            for (CssIdent id : values) {
                if (addSpace) {
                    sb.append(' ');
                } else {
                    addSpace = true;
                }
                sb.append(id);
            }
            sb.append('"');
            ac.getFrame().addWarning("with-space", 1);
            return new CssString(sb.toString());
        } else {
            val = values.get(0);
            // could be done in the consistency check, but...
            if (null != org.w3c.css.properties.css3.CssFontFamily.getGenericFontName(val)) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            return val;
        }
    }

}

