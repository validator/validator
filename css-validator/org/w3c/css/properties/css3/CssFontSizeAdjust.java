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

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2024/WD-css-fonts-5-20240206/#propdef-font-size-adjust
 */
public class CssFontSizeAdjust extends org.w3c.css.properties.css.CssFontSizeAdjust {

    public static final CssIdent[] optionalIdentValues;
    public static final CssIdent from_font;

    static {
        String[] _optionalIdentValues = {"ex-height", "cap-height", "ch-width",
                "ic-width", "ic-height"};
        optionalIdentValues = new CssIdent[_optionalIdentValues.length];
        for (int i = 0; i < optionalIdentValues.length; i++) {
            optionalIdentValues[i] = CssIdent.getIdent(_optionalIdentValues[i]);
        }
        from_font = CssIdent.getIdent("from-font");
    }

    public static final CssIdent getOptionalIdent(CssIdent ident) {
        for (CssIdent id : optionalIdentValues) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        if (from_font.equals(ident)) {
            return from_font;
        }
        return null;
    }

    /**
     * Create a new CssFontSizeAdjust
     */
    public CssFontSizeAdjust() {
        value = initial;
    }

    /**
     * Creates a new CssFontSizeAdjust
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontSizeAdjust(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        CssValueList vl = null;
        char op;
        boolean got_option = false;
        
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (expression.getCount() == 1) {
                        if (CssIdent.isCssWide(id) || (getAllowedIdent(id) != null) || none.equals(id)) {
                            value = val;
                            break;
                        }
                        throw new InvalidParamException("value",
                                val, getPropertyName(), ac);
                    } else {
                        if (!got_option) {
                            got_option = (getOptionalIdent(id) != null);
                            if (got_option) {
                                vl = new CssValueList();
                                vl.add(val);
                            } else {
                                throw new InvalidParamException("value",
                                        val, getPropertyName(), ac);
                            }
                        } else {
                            // got_option is true, so vl was created
                            if (getAllowedIdent(id) != null) {
                                vl.add(val);
                            } else {
                                throw new InvalidParamException("value",
                                        val, getPropertyName(), ac);
                            }
                        }
                    }
                    break;
                case CssTypes.CSS_NUMBER:
                    val.getCheckableValue().checkPositiveness(ac, getPropertyName());
                    if (got_option) {
                        vl.add(val);
                    } else {
                        value = val;
                    }
                    break;
                default:
                    throw new InvalidParamException("value",
                            val, getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        // reassign if we got multiple values
        if (got_option) {
            value = vl;
        }
    }

    public CssFontSizeAdjust(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

