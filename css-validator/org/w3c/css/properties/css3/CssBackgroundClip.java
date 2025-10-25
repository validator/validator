// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT 2010  World Wide Web Consortium (MIT, ERCIM, Keio University)
// Please first read the full copyright statement at
// http://www.w3.org/Consortium/Legal/copyright-software-19980720

package org.w3c.css.properties.css3;

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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background-clip
 * @spec https://compat.spec.whatwg.org/#the-webkit-background-clip-property
 */

public class CssBackgroundClip extends org.w3c.css.properties.css.CssBackgroundClip {

    public final static CssIdent border_box;
    public static CssIdent[] allowed_values;
    // from https://compat.spec.whatwg.org/#the-webkit-background-clip-property
    public static final CssIdent text = CssIdent.getIdent("text");

    static {
        String val[] = {"border-box", "padding-box", "content-box"};
        border_box = CssIdent.getIdent("border-box");
        allowed_values = new CssIdent[val.length];
        int i = 0;
        for (String s : val) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static boolean isMatchingIdent(CssIdent ident) {
        return (getMatchingIdent(ident) != null);

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
     * Create a new CssBackgroundClip
     */
    public CssBackgroundClip() {
        value = initial;
    }

    /**
     * Create a new CssBackgroundClip
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Incorrect value
     */
    public CssBackgroundClip(ApplContext ac, CssExpression expression,
                             boolean check) throws InvalidParamException {

        ArrayList<CssValue> values = new ArrayList<CssValue>();

        CssValue val;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_IDENT:
                    CssIdent ident = val.getIdent();
                    if (CssIdent.isCssWide(ident)) {
                        // if we got inherit after other values, fail
                        // if we got more than one value... fail
                        if ((values.size() > 0) || (expression.getCount() > 1)) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        values.add(val);
                        break;
                    }
                    if (getMatchingIdent(ident) != null) {
                        values.add(val);
                        break;
                    }
                    if (text.equals(ident)) {
                        ac.getFrame().addWarning("deprecated", val.toString());
                        values.add(val);
                        break;
                    }
                default:
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
            }
            expression.next();
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        if (values.size() == 1) {
            value = values.get(0);
        } else {
            value = new CssLayerList(values);
        }
    }

    public CssBackgroundClip(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (border_box == value);
    }

}
