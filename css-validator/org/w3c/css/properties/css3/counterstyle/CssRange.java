//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2017.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3.counterstyle;

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
 * @spec https://www.w3.org/TR/2015/CR-css-counter-styles-3-20150611/#descdef-counter-style-range
 */
public class CssRange extends org.w3c.css.properties.css.counterstyle.CssRange {

    public static final CssIdent infinite, auto;

    static {
        infinite = CssIdent.getIdent("infinite");
        auto = CssIdent.getIdent("auto");
    }

    /**
     * Create a new CssRange
     */
    public CssRange() {
        value = initial;  // this is wrong...
    }

    /**
     * Creates a new CssRange
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssRange(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        CssValue val;
        char op = SPACE;
        ArrayList<CssValue> values = new ArrayList<>();
        ArrayList<CssValue> range;
        setByUser();

        if (expression.getCount() == 1) {
            val = expression.getValue();
            if (val.getType() == CssTypes.CSS_IDENT && auto.equals(val)) {
                value = auto;
            } else {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
        } else {
            if (expression.getCount() % 2 == 1) {
                // we need an odd number here
                throw new InvalidParamException("unrecognize", ac);
            }
            while (!expression.end()) {
                range = new ArrayList<>();

                for (int i = 0; i < 2; i++) {
                    val = expression.getValue();
                    op = expression.getOperator();
                    switch (val.getType()) {
                        case CssTypes.CSS_NUMBER:
                            range.add(val);
                            break;
                        case CssTypes.CSS_IDENT:
                            if (infinite.equals(val)) {
                                range.add(infinite);
                            }
                            break;
                        default:
                            throw new InvalidParamException("value",
                                    val.toString(),
                                    getPropertyName(), ac);
                    }
                    if (i == 0 && op != SPACE) {
                        throw new InvalidParamException("operator", op,
                                getPropertyName(), ac);
                    }
                    expression.next();
                }
                values.add(new CssValueList(range));
                if (op != COMMA && !expression.end()) {
                    throw new InvalidParamException("operator", op,
                            getPropertyName(), ac);
                }

            }
            if (values.isEmpty()) {
                throw new InvalidParamException("unrecognize", ac);
            }
            value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
        }
    }

    public CssRange(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

