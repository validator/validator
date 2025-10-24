// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2013.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

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
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/aural.html#propdef-play-during
 */
public class CssPlayDuring extends org.w3c.css.properties.css.CssPlayDuring {

    public static final CssIdent auto, mix, repeat;

    static {
        auto = CssIdent.getIdent("auto");
        mix = CssIdent.getIdent("mix");
        repeat = CssIdent.getIdent("repeat");
    }

    public static final CssIdent getAllowedIdent(CssIdent ident) {
        if (auto.equals(ident)) {
            return auto;
        }
        if (none.equals(ident)) {
            return none;
        }
        return null;
    }

    /**
     * Create a new CssPlayDuring
     */
    public CssPlayDuring() {
    }

    /**
     * Creates a new CssPlayDuring
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssPlayDuring(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        if (check && expression.getCount() > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        CssValue val, repeatVal, mixVal;
        char op;

        val = expression.getValue();
        op = expression.getOperator();

        switch (val.getType()) {
            case CssTypes.CSS_URL:
                value = val;
                if (expression.getRemainingCount() > 1) {
                    expression.next();
                    if (op != SPACE) {
                        throw new InvalidParamException("operator",
                                Character.toString(op), ac);
                    }
                    val = expression.getValue();
                    op = expression.getOperator();
                    if (val.getType() != CssTypes.CSS_IDENT) {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    repeatVal = null;
                    mixVal = null;
                    if (repeat.equals(val)) {
                        repeatVal = repeat;
                    } else if (mix.equals(val)) {
                        mixVal = mix;
                    } else {
                        throw new InvalidParamException("value",
                                val.toString(),
                                getPropertyName(), ac);
                    }
                    // and now the second value...
                    if (expression.getRemainingCount() > 1) {
                        expression.next();
                        if (op != SPACE) {
                            throw new InvalidParamException("operator",
                                    Character.toString(op), ac);
                        }
                        val = expression.getValue();
                        if (val.getType() != CssTypes.CSS_IDENT) {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                        if (repeatVal == null && repeat.equals(val)) {
                            repeatVal = repeat;
                        } else if (mixVal == null && mix.equals(val)) {
                            mixVal = mix;
                        } else {
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                        }
                    }
                    ArrayList<CssValue> values = new ArrayList<CssValue>(4);
                    values.add(value);
                    if (mixVal != null) {
                        values.add(mixVal);
                    }
                    if (repeatVal != null) {
                        values.add(repeatVal);
                    }
                    value = new CssValueList(values);
                }
                break;
            case CssTypes.CSS_IDENT:
                if (expression.getCount() == 1) {
                    CssIdent id = (CssIdent) val;
                    if (inherit.equals(id)) {
                        value = inherit;
                        break;
                    }
                    value = getAllowedIdent(id);
                    if (value != null) {
                        break;
                    }
                }
            default:
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
        }
        expression.next();
    }

    public CssPlayDuring(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}

