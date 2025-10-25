// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2020.
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
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-outline
 * @see org.w3c.css.properties.css3.CssBorderStyle
 * @see org.w3c.css.properties.css3.CssBorderWidth
 */
public class CssOutline extends org.w3c.css.properties.css.CssOutline {

    /**
     * Create a new CssOutline
     */
    public CssOutline() {
        value = initial;
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

        CssValue colorValue = null;
        CssValue widthValue = null;
        CssValue styleValue = null;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                // temporary, until the parser fixes rgba hsl and others for good
                case CssTypes.CSS_FUNCTION:
                    if (colorValue == null) {
                        CssExpression ex = new CssExpression();
                        ex.addValue(val);
                        _color = new CssOutlineColor(ac, ex, check);
                        colorValue = _color.value;
                        break;
                    }
                    // else, we already got one...
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                    if (widthValue == null) {
                        CssExpression ex = new CssExpression();
                        ex.addValue(val);
                        _width = new CssOutlineWidth(ac, ex, check);
                        widthValue = _width.value;
                        break;
                    }
                    // else, we already got one...
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_COLOR:
                    if (colorValue == null) {
                        CssExpression ex = new CssExpression();
                        ex.addValue(val);
                        _color = new CssOutlineColor(ac, ex, check);
                        colorValue = _color.value;
                        break;
                    }
                    // else, we already got one...
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        if (expression.getCount() != 1) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    // let's try to find which ident we have...
                    if (styleValue == null) {
                        if (CssOutlineStyle.getMatchingIdent(ident) != null) {
                            styleValue = val;
                            break;
                        }
                    }
                    if (widthValue == null) {
                        if (CssBorderWidth.getAllowedIdent(ident) != null) {
                            widthValue = val;
                            break;
                        }
                    }
                    if (colorValue == null) {
                        if (CssOutlineColor.getMatchingIdent(ident) != null) {
                            colorValue = val;
                            break;
                        } else {
                            CssExpression ex = new CssExpression();
                            ex.addValue(val);
                            _color = new CssOutlineColor(ac, ex, check);
                            colorValue = _color.value;
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
        if (_width == null) {
            _width = new CssOutlineWidth();
        }
        if (_style == null) {
            _style = new CssOutlineStyle();
        }
        if (_color == null) {
            _color = new CssOutlineColor();
        }
        // now construct the value...
        if (expression.getCount() == 1) {
            if (widthValue != null) {
                value = widthValue;
                _width.value = widthValue;
            } else if (styleValue != null) {
                value = styleValue;
                _style.value = styleValue;
            } else if (colorValue != null) {
                value = colorValue;
                _color.value = colorValue;
            }  // else value is inherit
        } else {
            ArrayList<CssValue> values = new ArrayList<CssValue>(4);
            if (widthValue != null) {
                values.add(widthValue);
                _width.value = widthValue;
            }
            if (styleValue != null) {
                values.add(styleValue);
                _style.value = styleValue;
            }
            if (colorValue != null) {
                values.add(colorValue);
                _color.value = colorValue;
            }
            value = new CssValueList(values);
        }
    }

    public CssOutline(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

