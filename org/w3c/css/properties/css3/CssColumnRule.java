// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 1995-2010  World Wide Web Consortium (MIT, ERCIM and Keio)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

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
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-column-rule
 */

public class CssColumnRule extends org.w3c.css.properties.css.CssColumnRule {


    /**
     * Create a new CssColumnRule
     */
    public CssColumnRule() {
    }

    /**
     * Create a new CssColumnRule
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Incorrect values
     */
    public CssColumnRule(ApplContext ac, CssExpression expression,
                         boolean check) throws InvalidParamException {

        CssValue val;
        char op;
        int nb_val = expression.getCount();

        if (check && nb_val > 3) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op),
                        ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_HASH_IDENT:
                case CssTypes.CSS_FUNCTION:
                case CssTypes.CSS_COLOR:
                    if (rule_color != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    rule_color = new CssColumnRuleColor(ac, expression, false);
                    break;
                case CssTypes.CSS_NUMBER:
                case CssTypes.CSS_LENGTH:
                    if (rule_width != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    rule_width = new CssColumnRuleWidth(ac, expression, false);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        if (nb_val > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        expression.next();
                        break;
                    }
                    if (rule_color == null) {
                        try {
                            rule_color = new CssColumnRuleColor(ac, expression, false);
                            break;
                        } catch (Exception ex) {
                        }
                    }
                    if (rule_width == null) {
                        try {
                            rule_width = new CssColumnRuleWidth(ac, expression);
                            break;
                        } catch (Exception ex) {
                        }
                    }
                    if (rule_style == null) {
                        try {
                            rule_style = new CssColumnRuleStyle(ac, expression);
                            break;
                        } catch (Exception ex) {
                        }
                    }

                default:
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
            }
        }
        if (rule_color != null || rule_width != null || rule_style != null) {
            ArrayList<CssValue> v = new ArrayList<>();
            if (rule_width != null) {
                v.add(rule_width.value);
            }
            if (rule_style != null) {
                v.add(rule_style.value);
            }
            if (rule_color != null) {
                v.add(rule_color.value);
            }
            value = new CssValueList(v);
        }
    }

    public CssColumnRule(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return false;
    }

}
