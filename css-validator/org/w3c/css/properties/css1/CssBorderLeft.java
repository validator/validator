// $Id$
// @author Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css1;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssBorderLeftColor;
import org.w3c.css.properties.css.CssBorderLeftStyle;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS1-20080411/#border-left
 * @see CssBorder
 */
public class CssBorderLeft extends org.w3c.css.properties.css.CssBorderLeft {


    /**
     * Create a new CssBorderLeft
     */
    public CssBorderLeft() {
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
        CssBorder.SideValues values = CssBorder.checkBorderSide(ac, this, expression, check);
        if (values.width != null) {
            _width = new CssBorderLeftWidth();
            _width.setByUser();
            _width.value = values.width;
        }
        if (values.style != null) {
            _style = new CssBorderLeftStyle();
            _style.setByUser();
            _style.value = values.style;
        }
        if (values.color != null) {
            _color = new CssBorderLeftColor();
            _color.setByUser();
            _color.value = values.color;
        }
    }

    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        Css1Style css1Style = (Css1Style) style;
        css1Style.cssBorder.byUser = byUser;
        if (css1Style.cssBorder.borderLeft != null) {
            style.addRedefinitionWarning(ac, this);
        }
        css1Style.cssBorder.borderLeft = this;
        if (_width != null) {
            _width.addToStyle(ac, style);
        }
        if (_color != null) {
            if (css1Style.cssBorder.borderColor.left != null) {
                style.addRedefinitionWarning(ac, _color);
            }
            css1Style.cssBorder.borderColor.left = _color;
        }
        if (_style != null) {
            if (css1Style.cssBorder.borderStyle.left != null) {
                style.addRedefinitionWarning(ac, _style);
            }
            css1Style.cssBorder.borderStyle.left = _style;
        }
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        if (_width != null && inherit == _width.value) {
            return inherit.toString();
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
