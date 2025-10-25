//
// $Id$
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
//
// rewritten 2018 by Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2018.
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
 * @spec https://www.w3.org/TR/2021/WD-css-ruby-1-20210310/#propdef-ruby-position
 */

public class CssRubyPosition extends org.w3c.css.properties.css.CssRubyPosition {
    public static final CssIdent[] allowed_values;

    public static final CssIdent inter_char = CssIdent.getIdent("inter-character");
    public static final CssIdent alternate = CssIdent.getIdent("alternate");

    static {
        String[] _allowed_values = {"over", "under"};
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
     * Create new CssRubyPosition
     */
    public CssRubyPosition() {
        value = initial;
    }

    /**
     * Create new CssRubyPosition
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssRubyPosition(ApplContext ac, CssExpression expression,
                           boolean check) throws InvalidParamException {
        CssValue val;
        char op;
        boolean gotAlt = false;
        boolean gotLine = false;
        ArrayList<CssValue> v = new ArrayList<>();
        setByUser();
        if (check && expression.getCount() > 2) {
            throw new InvalidParamException("unrecognize", ac);
        }

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value",
                        expression.getValue(),
                        getPropertyName(), ac);
            }
            CssIdent ident = val.getIdent();
            if (CssIdent.isCssWide(ident) || inter_char.equals(ident)) {
                // single values
                if (expression.getCount() != 1) {
                    throw new InvalidParamException("value",
                            val.toString(),
                            getPropertyName(), ac);
                }
                value = val;
            } else if (!gotAlt && alternate.equals(ident)) {
                gotAlt = true;
                v.add(val);
            }  else if (!gotLine && (getAllowedIdent(ident) != null)) {
                gotLine = true;
                v.add(val);
            } else {
                throw new InvalidParamException("value",
                        val.toString(),
                        getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != SPACE)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (!v.isEmpty()) {
            value = (v.size() == 1) ? v.get(0): new CssValueList(v);
        }
    }


    public CssRubyPosition(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }
}
