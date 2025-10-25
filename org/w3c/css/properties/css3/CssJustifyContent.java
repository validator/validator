//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
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
 * @spec https://www.w3.org/TR/2020/WD-css-align-3-20200421/#propdef-justify-content
 */
public class CssJustifyContent extends org.w3c.css.properties.css.CssJustifyContent {

    public static final CssIdent[] content_position_extras;
    public static final CssIdent normal;

    static {
        normal = CssIdent.getIdent("normal");
        String[] _content_position_extras_values = {"left", "right"};
        content_position_extras = new CssIdent[_content_position_extras_values.length];
        int i = 0;
        for (String s : _content_position_extras_values) {
            content_position_extras[i++] = CssIdent.getIdent(s);
        }
    }

    public static CssIdent getContentPositionAndExtras(CssIdent ident) {
        for (CssIdent id : content_position_extras) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return CssAlignContent.getContentPosition(ident);
    }

    /**
     * Create a new CssJustifyContent
     */
    public CssJustifyContent() {
        value = initial;
    }

    /**
     * Creates a new CssJustifyContent
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssJustifyContent(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        value = parseJustifyContent(ac, expression, this);
        if (!expression.end()) {
            throw new InvalidParamException("unrecognize", ac);
        }
    }

    public static CssValue parseJustifyContent(ApplContext ac, CssExpression expression,
                                               CssProperty caller)
            throws InvalidParamException {
        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                expression.next();
                return val;
            }
            if (normal.equals(ident)) {
                expression.next();
                return val;
            }
            if (CssAlignContent.getContentDistribution(ident) != null) {
                expression.next();
                return val;
            }
            // now try the potential two-values position, starting first with only one.
            if (getContentPositionAndExtras(ident) != null) {
                expression.next();
                return val;
            }
            // ok, at that point we need two values.
            if (CssAlignContent.getOverflowPosition(ident) != null) {
                values.add(val);
                if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
                expression.next();
                if (expression.end()) {
                    throw new InvalidParamException("unrecognize", ac);
                }
                val = expression.getValue();
                if (val.getType() != CssTypes.CSS_IDENT) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                if (getContentPositionAndExtras(val.getIdent()) == null) {
                    throw new InvalidParamException("value", val.toString(),
                            caller.getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                return new CssValueList(values);
            }
        }
        throw new InvalidParamException("value",
                val.toString(),
                caller.getPropertyName(), ac);
    }


    public CssJustifyContent(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

