//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;
import org.w3c.css.values.CssValueList;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec https://drafts.csswg.org/css-backgrounds-4/#propdef-background-position-x
 * @spec https://github.com/w3c/csswg-drafts/blob/9613c071460b8f4af6abbd65fa0488ea2cec6189/css-backgrounds-4/Overview.bs
 */
public class CssBackgroundPositionX extends org.w3c.css.properties.css.CssBackgroundPositionX {

    public static final CssIdent[] allowed_values;
    public static final CssIdent center;

    static {
        center = CssIdent.getIdent("center");

        String[] _allowed_values = {"left", "right", "x-start", "x-end"};
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
     * Create a new CssBackgroundPositionX
     */
    public CssBackgroundPositionX() {
        value = initial;
    }

    /**
     * Creates a new CssBackgroundPositionX
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Expressions are incorrect
     */
    public CssBackgroundPositionX(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {

        setByUser();

        ArrayList<CssValue> v = new ArrayList<>();
        CssValue val;
        char op;
        CssExpression nex = new CssExpression();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (val.getType() == CssTypes.CSS_IDENT) {
                CssIdent id = val.getIdent();
                if (CssIdent.isCssWide(id)) {
                    if (expression.getCount() > 1) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    value = val;
                    expression.next();
                    continue;
                }
            }
            nex.addValue(val);
            if (op == COMMA) {
                v.add(checkBackgroundPositionXLayer(ac, nex, this));
                nex = new CssExpression();
            } else if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
            expression.next();
        }
        // final value
        if (nex.getCount() != 0) {
            v.add(checkBackgroundPositionXLayer(ac, nex, this));
        }
        if (!v.isEmpty()) {
            value = (v.size() == 1) ? v.get(0) : new CssLayerList(v);
        }
    }

    public static CssValue checkBackgroundPositionXLayer(ApplContext ac, CssExpression expression,
                                                         CssProperty caller)
            throws InvalidParamException {
        ArrayList<CssValue> v = new ArrayList<>();
        CssValue val;
        boolean gotIdent = false;
        boolean gotCenter = false;
        boolean gotLP = false;

        if (expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();

            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    if (gotLP) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                caller.getPropertyName(), ac);
                    }
                    val.getCheckableValue().checkEqualsZero(ac, caller);
                case CssTypes.CSS_LENGTH:
                case CssTypes.CSS_PERCENTAGE:
                    gotLP = true;
                    v.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (center.equals(id) && !gotCenter && !gotIdent && !gotLP) {
                        gotCenter = true;
                        v.add(val);
                        break;
                    }
                    if (getAllowedIdent(id) != null && !gotIdent && !gotLP) {
                        gotIdent = true;
                        v.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            caller.getPropertyName(), ac);

            }
            expression.next();
        }
        return (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }

    public CssBackgroundPositionX(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

