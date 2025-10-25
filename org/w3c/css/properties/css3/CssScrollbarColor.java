//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
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
 * @spec https://www.w3.org/TR/2021/CR-css-scrollbars-1-20211209/#propdef-scrollbar-color
 */
public class CssScrollbarColor extends org.w3c.css.properties.css.CssScrollbarColor {

    public static final CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {"auto"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
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
     * Create a new CssScrollbarColor
     */
    public CssScrollbarColor() {
        value = initial;
    }

    /**
     * Creates a new CssScrollbarColor
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssScrollbarColor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        org.w3c.css.values.CssColor c;
        CssColor tcolor;
        char op;

        val = expression.getValue();
        op = expression.getOperator();
        ArrayList<CssValue> values = new ArrayList<>();

        switch (val.getType()) {
            case CssTypes.CSS_IDENT:
                if (CssIdent.isCssWide(val.getIdent())) {
                    if (expression.getCount() != 1) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    value = val;
                    break;
                } else {
                    CssIdent ident = getAllowedIdent(val.getIdent());
                    // auto can be there once
                    if (ident != null) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                }
                // it must be a color instead
                try {
                    c = new org.w3c.css.values.CssColor(ac, val.getIdent().toString());
                } catch (InvalidParamException e) {
                    // we recreate the exception, as it will have
                    // the wrong property name otherwise
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
                break;
            case CssTypes.CSS_FUNCTION:
                try {
                    tcolor = new CssColor(ac, expression, check);
                    value = val;
                    break;
                } catch (InvalidParamException e) {
                    // we recreate the exception, as it will have
                    // the wrong property name otherwise
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
            case CssTypes.CSS_HASH_IDENT:
                c = new org.w3c.css.values.CssColor();
                c.setShortRGBColor(ac, val.getHashIdent().toString());
                values.add(val);
                break;
            case CssTypes.CSS_COLOR:
                values.add(val);
                break;
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();

        if (!expression.end()) {
            // second value
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            val = expression.getValue();
            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    if (CssIdent.isCssWide(val.getIdent())) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    } else {
                        CssIdent ident = getAllowedIdent(val.getIdent());
                        // auto can be there first (and alone)
                        if (ident != null) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                    }
                    // it must be a color instead
                    try {
                        c = new org.w3c.css.values.CssColor(ac, val.getIdent().toString());
                    } catch (InvalidParamException e) {
                        // we recreate the exception, as it will have
                        // the wrong property name otherwise
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                    try {
                        tcolor = new CssColor(ac, expression, check);
                        value = val;
                        break;
                    } catch (InvalidParamException e) {
                        // we recreate the exception, as it will have
                        // the wrong property name otherwise
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                case CssTypes.CSS_HASH_IDENT:
                    c = new org.w3c.css.values.CssColor();
                    c.setShortRGBColor(ac, val.getHashIdent().toString());
                    values.add(val);
                    break;
                case CssTypes.CSS_COLOR:
                    values.add(val);
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
        } else {
            if (values.size() != 0) {
                // we got ony one color
                throw new InvalidParamException("unrecognize", ac);
            }
        }
        if (values.size() > 0) {
            value = new CssValueList(values);
        }
    }

    public CssScrollbarColor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

