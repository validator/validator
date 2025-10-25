//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.svg.colorprofile;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssFunction;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssOperator;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

/**
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/color.html#ColorProfileSrcProperty
 */
public class CssSrc extends org.w3c.css.properties.css.colorprofile.CssSrc {

    public static CssIdent sRGB = CssIdent.getIdent("sRGB");

    /**
     * Create a new CssSrc
     */
    public CssSrc() {
        value = initial;
    }

    /**
     * Creates a new CssSrc
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssSrc(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();
        CssValue val;
        char op;

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> values = new ArrayList<>();
        boolean gotIRI = false;
        boolean gotLocal = false;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_URL:
                    if (gotIRI) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                    gotIRI = true;
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    if (gotLocal || gotIRI) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                    parseLocal((CssFunction) val, ac);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (sRGB.equals(val) && values.isEmpty()) {
                        value = sRGB;
                        // hack to avoid getting other values.
                        gotIRI = true;
                        gotLocal = true;
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
            }
            if (op != CssOperator.SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            expression.next();
        }
        if (!values.isEmpty()) {
            value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
        }
    }

    private void parseLocal(CssFunction f, ApplContext ac) throws InvalidParamException {
        if (!"local".equals(f.getName())) {
            throw new InvalidParamException("value", f, getPropertyName(), ac);
        }
        CssExpression exp = f.getParameters();
        if (exp.getCount() == 1) {
            if (exp.getValue().getType() == CssTypes.CSS_STRING) {
                return;
            }
        }
        // else fail...
        throw new InvalidParamException("value", f, getPropertyName(), ac);
    }

    public CssSrc(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

