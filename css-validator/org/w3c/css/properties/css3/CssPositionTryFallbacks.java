//
// (c) COPYRIGHT W3C, 2026.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

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
 * @spec https://drafts.csswg.org/css-anchor-position-1/#propdef-position-try-fallbacks retrieved 2026-11-03
 */
public class CssPositionTryFallbacks extends org.w3c.css.properties.css.CssPositionTryFallbacks {

    public static final CssIdent[] tryTactics;

    static {
        String[] _tactics = {"flip-block", "flip-inline", "flip-start",
                "flip-x", "flip-y"};
        tryTactics = new CssIdent[_tactics.length];
        int i = 0;
        for (String s : _tactics) {
            tryTactics[i++] = CssIdent.getIdent(s);
        }
    }

    /**
     * Create a new CssPositionTryFallbacks
     */
    public CssPositionTryFallbacks() {
        value = initial;
    }

    /**
     * Creates a new CssPositionTryFallbacks from the rest of the expression,
     * so that the position-try shorthand can hand it what follows its
     * position-try-order value.
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssPositionTryFallbacks(ApplContext ac, CssExpression expression,
                                   boolean check) throws InvalidParamException {
        setByUser();
        CssValue val = expression.getValue();
        char op;

        if (val.getType() == CssTypes.CSS_IDENT) {
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id) || none.equals(id)) {
                expression.next();
                if (!expression.end()) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                value = val;
                return;
            }
        }
        ArrayList<CssValue> values = new ArrayList<>();
        CssExpression fallback = null;
        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (fallback == null) {
                fallback = new CssExpression();
            }
            fallback.addValue(val);
            fallback.setOperator(op);
            expression.next();
            if (!expression.end()) {
                if (op == COMMA) {
                    fallback.setOperator(SPACE);
                    values.add(parseFallback(ac, fallback));
                    fallback = null;
                } else if (op != SPACE) {
                    throw new InvalidParamException("operator",
                            Character.toString(op), ac);
                }
            }
        }
        if (fallback != null) {
            values.add(parseFallback(ac, fallback));
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssPositionTryFallbacks(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Parses one fallback: [ <dashed-ident> || <try-tactic> ] | <position-area>
     */
    private CssValue parseFallback(ApplContext ac, CssExpression fallback)
            throws InvalidParamException {
        CssValue val = fallback.getValue();
        if ((val.getType() == CssTypes.CSS_IDENT)
                && CssPositionArea.isAreaKeyword(val.getIdent())) {
            return CssPositionArea.parsePositionArea(ac, fallback,
                    getPropertyName());
        }
        // <try-tactic> = flip-block || flip-inline || flip-start || flip-x || flip-y
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssIdent> tactics = new ArrayList<>();
        boolean gotName = false;
        while (!fallback.end()) {
            val = fallback.getValue();
            if (val.getType() == CssTypes.CSS_DASHED_IDENT && !gotName) {
                gotName = true;
            } else if (val.getType() == CssTypes.CSS_IDENT
                    && CssAnchorName.isOneOf(val.getIdent(), tryTactics)
                    && !tactics.contains(val.getIdent())) {
                tactics.add(val.getIdent());
            } else {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            values.add(val);
            fallback.next();
        }
        return (values.size() == 1) ? values.get(0) : new CssValueList(values);
    }
}
