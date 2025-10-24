//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT World Wide Web Consortium, 2024.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
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
 * @spec https://www.w3.org/TR/2024/WD-css-text-4-20240219/#propdef-text-wrap
 */
public class CssTextWrap extends org.w3c.css.properties.css.CssTextWrap {

    private CssTextWrapMode _longhand_mode;
    private CssTextWrapStyle _longhand_style;

    /**
     * Create a new CssTextWrap
     */
    public CssTextWrap() {
        value = initial;
        _longhand_mode = new CssTextWrapMode();
        _longhand_style = new CssTextWrapStyle();
    }

    /**
     * Creates a new CssTextWrap
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssTextWrap(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op;
        ArrayList<CssValue> values = new ArrayList<>();
        _longhand_mode = new CssTextWrapMode();
        _longhand_style = new CssTextWrapStyle();
        boolean _got_mode = false;
        boolean _got_style = false;

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        for (int i = 0; i < 2 && !expression.end(); i++) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            if (CssIdent.isCssWide(val.getIdent())) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
                _longhand_mode.value = val;
                _longhand_style.value = val;
                break;
            } else {
                CssIdent id = CssTextWrapMode.getAllowedIdent(val.getIdent());
                if (_got_mode && (id != null)) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                } else if (id != null) {
                    values.add(val);
                    _got_mode = true;
                    _longhand_mode.value = val;
                } else {
                    id = CssTextWrapStyle.getAllowedIdent(val.getIdent());
                    if (_got_style || (id == null)) {
                        throw new InvalidParamException("value",
                                expression.getValue(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                    _got_style = true;
                    _longhand_style.value = val;
                }
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssTextWrap(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        _longhand_mode.addToStyle(ac, style);
        _longhand_style.addToStyle(ac, style);
    }
}

