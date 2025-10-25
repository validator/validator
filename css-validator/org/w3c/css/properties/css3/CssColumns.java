//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM and Keio)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css3;

import org.w3c.css.parser.CssStyle;
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
 * @spec https://www.w3.org/TR/2021/WD-css-multicol-1-20210212/#propdef-columns
 * @see org.w3c.css.properties.css3.CssColumnWidth
 * @see org.w3c.css.properties.css3.CssColumnCount
 */

public class CssColumns extends org.w3c.css.properties.css.CssColumns {

    public static final CssIdent auto = CssIdent.getIdent("auto");

    CssColumnWidth width = null;
    CssColumnCount count = null;

    /**
     * Create a new CssColumns
     */
    public CssColumns() {
        value = initial;
    }

    /**
     * Create a new CssColumns
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      if checking is enforced
     * @throws org.w3c.css.util.InvalidParamException
     *          Incorrect values
     */
    public CssColumns(ApplContext ac, CssExpression expression,
                      boolean check) throws InvalidParamException {

        CssValue val;
        ArrayList<CssValue> values = new ArrayList<>();
        char op;
        int nb_val = expression.getCount();
        int nb_auto = 0;

        if (check && nb_val > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }
        setByUser();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (op != SPACE) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
            switch (val.getType()) {
                case CssTypes.CSS_NUMBER:
                    if (count != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    count = new CssColumnCount(ac, expression, false);
                    values.add(val);
                    break;
                case CssTypes.CSS_FUNCTION:
                case CssTypes.CSS_LENGTH:
                    if (width != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    width = new CssColumnWidth(ac, expression, false);
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        if (nb_val > 1) {
                            throw new InvalidParamException("unrecognize", ac);
                        }
                        value = val;
                        expression.next();
                        break;
                    }
                    if (auto.equals(ident)) {
                        nb_auto++;
                        values.add(val);
                        expression.next();
                        break;
                    }
                    // otherwise it should be a width.
                    if (width != null) {
                        throw new InvalidParamException("unrecognize", ac);
                    }
                    width = new CssColumnWidth(ac, expression, false);
                    values.add(val);
                    break;
                default:
                    throw new InvalidParamException("value",
                            expression.getValue(),
                            getPropertyName(), ac);
            }
        }
        if (nb_val == 1) {
            if (!values.isEmpty()) {
                value = values.get(0);
            }
        } else {
            value = new CssValueList(values);
            // fill the other values.
            if (nb_auto == 2) {
                count = new CssColumnCount();
                count.value = auto;
                width = new CssColumnWidth();
                width.value = auto;
            } else if (nb_auto == 1) {
                if (count != null) {
                    width = new CssColumnWidth();
                    width.value = auto;
                } else {
                    count = new CssColumnCount();
                    count.value = auto;
                }
            }
        }
    }

    public CssColumns(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Add this property to the CssStyle
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        super.addToStyle(ac, style);
        if (count != null) {
            count.addToStyle(ac, style);
        }
        if (width != null) {
            width.addToStyle(ac, style);
        }
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */

    public boolean isDefault() {
        return (value == initial);
    }

}
