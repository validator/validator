// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement at:
// https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document

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
 * @spec https://www.w3.org/TR/2021/WD-pointerevents3-20210901/#the-touch-action-css-property
 * @spec https://compat.spec.whatwg.org/#touch-action
 */

public class CssTouchAction extends org.w3c.css.properties.css.CssTouchAction {

    public static final CssIdent[] allowed_vertical_values;
    public static final CssIdent[] allowed_horizontal_values;
    public static final CssIdent[] allowed_values;
    public static final CssIdent pinch_zoom;

    static {
        String[] _allowed_vertical_values = {"pan-x", "pan-left", "pan-right"};
        allowed_vertical_values = new CssIdent[_allowed_vertical_values.length];
        int i = 0;
        for (String s : _allowed_vertical_values) {
            allowed_vertical_values[i++] = CssIdent.getIdent(s);
        }
        String[] _allowed_horizontal_values = {"pan-y", "pan-up", "pan-down"};
        allowed_horizontal_values = new CssIdent[_allowed_horizontal_values.length];
        i = 0;
        for (String s : _allowed_horizontal_values) {
            allowed_horizontal_values[i++] = CssIdent.getIdent(s);
        }
        String[] _allowed_values = {"auto", "none", "manipulation"};
        allowed_values = new CssIdent[_allowed_values.length];
        i = 0;
        for (String s : _allowed_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
        pinch_zoom = CssIdent.getIdent("pinch-zoom");
    }

    public static CssIdent getAllowedHorizontalIdent(CssIdent ident) {
        for (CssIdent id : allowed_horizontal_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
    }

    public static CssIdent getAllowedVerticalIdent(CssIdent ident) {
        for (CssIdent id : allowed_vertical_values) {
            if (id.equals(ident)) {
                return id;
            }
        }
        return null;
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
     * Create a new CssTouchAction
     */
    public CssTouchAction() {
        value = initial;
    }

    /**
     * Create a new CssTouchAction
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Incorrect value
     */
    public CssTouchAction(ApplContext ac, CssExpression expression,
                          boolean check) throws InvalidParamException {

        CssValue val;
        char op;
        CssIdent id, ident;
        boolean got_vertical = false;
        boolean got_horizontal = false;
        boolean got_pinch_zoom = false;

        setByUser();

        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> values = new ArrayList<>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            if (op != SPACE) {
                throw new InvalidParamException("operator", op,
                        getPropertyName(), ac);
            }

            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            id = val.getIdent();
            if (CssIdent.isCssWide(id)) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                continue;
            }
            // check for single values first.
            ident = getAllowedIdent(id);
            if (ident != null) {
                if (expression.getCount() > 1) {
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                values.add(val);
                expression.next();
                continue;
            }
            // then for the possible double values
            ident = getAllowedHorizontalIdent(id);
            if (ident != null) {
                if (got_horizontal) {
                    // we already got one
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_horizontal = true;
                values.add(val);
                expression.next();
                continue;
            }
            ident = getAllowedVerticalIdent(id);
            if (ident != null) {
                if (got_vertical) {
                    // we already got one
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_vertical = true;
                values.add(val);
                expression.next();
                continue;
            }
            if (pinch_zoom.equals(id)) {
                if (got_pinch_zoom) {
                    // we already got one
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
                }
                got_pinch_zoom = true;
                values.add(val);
                expression.next();
                continue;
            }
            throw new InvalidParamException("value", val.toString(),
                    getPropertyName(), ac);

        }
        // no need to check for single values as it was done earlier.
        value = (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }

    public CssTouchAction(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
