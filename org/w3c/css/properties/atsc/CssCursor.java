// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.atsc;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;

import static org.w3c.css.values.CssOperator.COMMA;

/**
 * @spec http://www.w3.org/TR/2008/REC-CSS2-20080411/ui.html#cursor-props
 */
public class CssCursor extends org.w3c.css.properties.css.CssCursor {

    public static CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {
                "auto", "crosshair", "default", "pointer", "move",
                "e-resize", "ne-resize", "nw-resize", "n-resize",
                "se-resize", "sw-resize", "s-resize", "w-resize",
                "text", "wait", "help"};
        allowed_values = new CssIdent[_allowed_values.length];
        int i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
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
     * Create a new CssCursor
     */
    public CssCursor() {
    }

    /**
     * Creates a new CssCursor
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssCursor(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        CssValue val;
        char op;
        CssValue lastIdent = null;
        ArrayList<CssValue> values = new ArrayList<CssValue>();

        // same as CSS2 but with a warning.
        ac.getFrame().addWarning("atsc", expression.toString());

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_URL:
                    if (lastIdent != null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    if (inherit.equals(val)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    inherit.toString(),
                                    getPropertyName(), ac);
                        }
                        value = inherit;
                        break;
                    }
                    lastIdent = getMatchingIdent((CssIdent) val);
                    // not recognized... exit
                    if (lastIdent == null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    values.add(val);
                    break;
                default:
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (value != inherit) {
            // check if we got the mandatory ident
            if (lastIdent == null) {
                // TODO better errormsg
                throw new InvalidParamException("value",
                        expression.toString(),
                        getPropertyName(), ac);
            }
            value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
        }
    }

    public CssCursor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

