//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2015.
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
 * @spec https://www.w3.org/TR/2021/WD-css-ui-4-20210316/#propdef-cursor
 */
public class CssCursor extends org.w3c.css.properties.css.CssCursor {

    public static CssIdent[] allowed_values;

    static {
        String[] _allowed_values = {
                "auto", "default", "none", "context-menu", "help",
                "pointer", "progress", "wait", "cell", "crosshair",
                "text", "vertical-text", "alias", "copy", "move",
                "no-drop", "not-allowed", "grab", "grabbing",
                "e-resize", "n-resize", "ne-resize", "nw-resize",
                "s-resize", "se-resize", "sw-resize", "w-resize",
                "ew-resize", "ns-resize", "nesw-resize", "nwse-resize",
                "col-resize", "row-resize", "all-scroll", "zoom-in",
                "zoom-out"};
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
        value = initial;
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

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_URL:
                case CssTypes.CSS_IMAGE:
                    if (lastIdent != null) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    // we got an URL, now let's see if we have
                    // a < x y > after that...
                    if (op == SPACE) {
                        CssExpression exp = new CssExpression();
                        exp.addValue(val);
                        while (expression.getRemainingCount() > 1 && (op == SPACE)) {
                            expression.next();
                            exp.addValue(expression.getValue());
                            op = expression.getOperator();
                        }
                        values.add(parseCursorURI(ac, exp, this));
                    } else {
                        // just a plain ol' URI
                        values.add(val);
                    }
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value",
                                    inherit.toString(),
                                    getPropertyName(), ac);
                        }
                        value = val;
                        break;
                    }
                    if (lastIdent == null) {
                        lastIdent = getMatchingIdent(id);
                        // not recognized... exit
                        if (lastIdent == null) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        values.add(val);
                        break;
                    }
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
                        expression.toStringFromStart(),
                        getPropertyName(), ac);
            }
            value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
        }
    }

    public CssCursor(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    private static final CssValue parseCursorURI(ApplContext ac, CssExpression expression, CssProperty caller)
            throws InvalidParamException {
        if (expression.getCount() != 3) {
            throw new InvalidParamException("value",
                    expression.toStringFromStart(),
                    caller.getPropertyName(), ac);
        }
        // we must have <url number number>
        CssValue val = expression.getValue();
        ArrayList<CssValue> values = new ArrayList<CssValue>();
        if (val.getType() != CssTypes.CSS_URL && val.getType() != CssTypes.CSS_IMAGE) {
            throw new InvalidParamException("value",
                    val.toString(),
                    caller.getPropertyName(), ac);
        }
        values.add(val);
        expression.next();
        for (int i = 0; i < 2; i++) {
            val = expression.getValue();
            if (val.getType() != CssTypes.CSS_NUMBER) {
                throw new InvalidParamException("value",
                        val.toString(),
                        caller.getPropertyName(), ac);
            }
            values.add(val);
            expression.next();
        }
        return new CssValueList(values);
    }
}

