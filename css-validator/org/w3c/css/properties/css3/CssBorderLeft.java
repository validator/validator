// $Id$
// @author Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssValueList;

/**
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-border-left
 * @see CssBorder
 */
public class CssBorderLeft extends org.w3c.css.properties.css.CssBorderLeft {


    /**
     * Create a new CssBorderLeft
     */
    public CssBorderLeft() {
        value = initial;
    }

    /**
     * Set the value of the property<br/>
     * Does not check the number of values
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderLeft(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Set the value of the property
     *
     * @param expression The expression for this property
     * @param check      set it to true to check the number of values
     * @throws org.w3c.css.util.InvalidParamException
     *          The expression is incorrect
     */
    public CssBorderLeft(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {
        CssBorder.SideValues values = CssBorder.parseBorderSide(ac, expression, check, this);
        CssValueList vl = new CssValueList();

        if (values.width != null) {
            _width = new CssBorderLeftWidth();
            _width.setByUser();
            _width.value = values.width;
            vl.add(values.width);
        }
        if (values.style != null) {
            _style = new CssBorderLeftStyle();
            _style.setByUser();
            _style.value = values.style;
            vl.add(values.style);
        }
        if (values.color != null) {
            _color = new CssBorderLeftColor();
            _color.setByUser();
            _color.value = values.color;
            vl.add(values.color);
        }
        if (inherit.equals(vl.get(0))) {
            value = inherit;
        } else {
            value = (vl.size() == 1) ? vl.get(0) : vl;
        }
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (_width != null) {
            if (inherit == _width.value) {
                return inherit.toString();
            }
            if (initial == _width.value) {
                return initial.toString();
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (_width != null) {
            sb.append(_width);
            first = false;
        }
        if (_style != null) {
            if (first) {
                sb.append(_style);
            } else {
                sb.append(' ').append(_style);
            }
            first = false;
        }
        if (_color != null) {
            if (first) {
                sb.append(_color);
            } else {
                sb.append(' ').append(_color);
            }
        }
        return sb.toString();
    }
}
