//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT W3C, 2026.
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
 * @spec https://www.w3.org/TR/2025/WD-css-overflow-3-20251007/#propdef-scrollbar-gutter
 */
public class CssScrollbarGutter extends org.w3c.css.properties.css.CssScrollbarGutter {

    private static CssIdent auto, stable, both_edges;

    static {
        auto = new CssIdent("auto");
        stable = new CssIdent("stable");
        both_edges = new CssIdent("both-edges");
    }

    /**
     * Create a new CssScrollbarGutter
     */
    public CssScrollbarGutter() {
        value = initial;
    }

    /**
     * Creates a new CssScrollbarGutter
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Expressions are incorrect
     */
    public CssScrollbarGutter(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        setByUser();

        boolean got_stable = false;
        boolean got_both_edges = false;
        CssValue val;
        char op;

        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        ArrayList<CssValue> v = new ArrayList<CssValue>();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();

            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id) || auto.equals(id)) {
                        if (expression.getCount() > 1) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        v.add(val);
                        break;
                    }
                    if (stable.equals(id)) {
                        if (!got_stable) {
                            v.add(val);
                            got_stable = true;
                            break;
                        }
                    }
                    if (both_edges.equals(id)) {
                        if (!got_both_edges) {
                            v.add(val);
                            got_both_edges = true;
                            break;
                        }
                    }
                    // let it fail
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            if (op != SPACE) {
                throw new InvalidParamException("operator", val,
                        getPropertyName(), ac);
            }
            expression.next();
        }
        // per grammar, both-edges cannot be alone
        if (got_both_edges && !got_stable) {
            throw new InvalidParamException("value", both_edges,
                    getPropertyName(), ac);
        }
        value = (v.size() == 1) ? v.get(0) : new CssValueList(v);
    }

    public CssScrollbarGutter(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }


}

