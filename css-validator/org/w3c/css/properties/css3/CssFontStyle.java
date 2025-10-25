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

import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://www.w3.org/TR/2021/WD-css-fonts-4-20210729/#propdef-font-style
 */
public class CssFontStyle extends org.w3c.css.properties.css.CssFontStyle {

    static final String[] _allowed_values = {"italic", "normal", "oblique"};
    static final ArrayList<CssIdent> allowed_values;
    static public final CssIdent oblique = CssIdent.getIdent("oblique");

    static {
        allowed_values = new ArrayList<CssIdent>(_allowed_values.length);
        for (String s : _allowed_values) {
            allowed_values.add(CssIdent.getIdent(s));
        }
    }

    public static CssIdent getMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    /**
     * Create a new CssFontStyle
     */
    public CssFontStyle() {
        value = initial;
    }

    /**
     * Creates a new CssFontStyle
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssFontStyle(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val;
        CssIdent id;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                value = val;
            } else {
                id = getMatchingIdent(ident);
                if (id == null) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                if (!oblique.equals(id)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    value = val;
                } else {
                    expression.next();
                    // check for a possible angle
                    if (!expression.end()) {
                        if (op != SPACE) {
                            throw new InvalidParamException("operator",
                                    Character.toString(op), ac);
                        }
                        CssValue v = expression.getValue();
                        // FIXME TODO check for NUMBER 0 ?
                        if (v.getType() != CssTypes.CSS_ANGLE) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        CssValueList values = new CssValueList();
                        values.add(val);
                        values.add(v);
                        value = values;
                    } else {
                        value = val;
                    }
                }
            }
        } else {
            throw new InvalidParamException("value",
                    val.toString(),
                    getPropertyName(), ac);
        }
        expression.next();
    }

    public CssFontStyle(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

