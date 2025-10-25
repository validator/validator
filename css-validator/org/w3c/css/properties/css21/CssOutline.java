// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

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
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/ui.html#propdef-outline
 * @see org.w3c.css.properties.css21.CssBorderStyle
 * @see org.w3c.css.properties.css21.CssBorderWidth
 */
public class CssOutline extends org.w3c.css.properties.css.CssOutline {

    /**
     * Create a new CssOutline
     */
    public CssOutline() {
        _color = new CssOutlineColor();
        _style = new CssOutlineStyle();
        _width = new CssOutlineWidth();
    }

    /**
     * Creates a new CssOutline
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssOutline(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        setByUser();

        CssValue val;
        char op;

        _color = new CssOutlineColor();
        _style = new CssOutlineStyle();
        _width = new CssOutlineWidth();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                    if (_width.value == null) {
                        CssExpression ex = new CssExpression();
                        ex.addValue(val);
                        _width = new CssOutlineWidth(ac, ex, check);
                        break;
                    }
                    // else, we already got one...
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_COLOR:
                    if (_color.value == null) {
                        CssExpression ex = new CssExpression();
                        ex.addValue(val);
                        _color = new CssOutlineColor(ac, ex, check);
                        break;
                    }
                    // else, we already got one...
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = inherit;
                        break;
                    }
                    CssIdent ident = (CssIdent) val;
                    // let's try to find which ident we have...
                    if (_style.value == null) {
                        CssIdent match = CssBorderStyle.getMatchingIdent(ident);
                        if (match != null) {
                            _style.value = match;
                            break;
                        }
                    }
                    if (_width.value == null) {
                        CssIdent match = CssBorderWidth.getMatchingIdent(ident);
                        if (match != null) {
                            _width.value = match;
                            break;
                        }
                    }
                    if (_color.value == null) {
                        CssIdent match = CssOutlineColor.getMatchingIdent(ident);
                        if (match != null) {
                            _color.value = match;
                            break;
                        } else {
                            CssExpression ex = new CssExpression();
                            ex.addValue(val);
                            _color = new CssOutlineColor(ac, ex, check);
                            break;
                        }
                    }
                    // unrecognized... fail
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
        }
        if (expression.getCount() == 1) {
            if (_width.value != null) {
                value = _width.value;
            } else if (_style.value != null) {
                value = _style.value;
            } else if (_color.value != null) {
                value = _color.value;
            }
        } else {
            ArrayList<CssValue> values = new ArrayList<CssValue>(4);
            if (_width.value != null) {
                values.add(_width.value);
            }
            if (_style.value != null) {
                values.add(_style.value);
            }
            if (_color.value != null) {
                values.add(_color.value);
            }
            value = new CssValueList(values);
        }
    }

    public CssOutline(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

