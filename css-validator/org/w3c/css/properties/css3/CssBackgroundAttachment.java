// $Id$
// @author Yves Lafon <ylafon@w3.org>

// (c) COPYRIGHT MIT, ERCIM and Keio, 2010.
// Please first read the full copyright statement in file COPYRIGHT.html

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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background-attachment
 */
public class CssBackgroundAttachment extends org.w3c.css.properties.css.CssBackgroundAttachment {

    private static final CssIdent[] allowed_values;

    static {
        String[] id_values = {"scroll", "fixed", "local"};
        allowed_values = new CssIdent[id_values.length];
        int i = 0;
        for (String s : id_values) {
            allowed_values[i++] = CssIdent.getIdent(s);
        }
    }

    public static boolean isMatchingIdent(CssIdent ident) {
        for (CssIdent id : allowed_values) {
            if (id.equals(ident)) {
                return true;
            }
        }
        return false;
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
     * Create a new CssBackgroundAttachment
     */
    public CssBackgroundAttachment() {
        value = initial;
    }

    /**
     * Creates a new CssBackgroundAttachment
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      if some length checking is required
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssBackgroundAttachment(ApplContext ac, CssExpression expression,
                                   boolean check) throws InvalidParamException {

        ArrayList<CssValue> values = new ArrayList<CssValue>();
        char op;
        CssValue val;

        setByUser();

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            if (val.getType() != CssTypes.CSS_IDENT) {
                throw new InvalidParamException("value", val,
                        getPropertyName(), ac);
            }
            CssIdent id = val.getIdent();
            if (CssIdent.isCssWide(id)) {
                // if we got inherit after other values, fail
                // if we got more than one value... fail
                if ((values.size() > 0) || (expression.getCount() > 1)) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                values.add(val);
            } else {
                // check that it's in the allowed values
                if (getMatchingIdent(id) == null) {
                    throw new InvalidParamException("value", val,
                            getPropertyName(), ac);
                }
                values.add(val);
            }
            expression.next();
            // and check that values are separated by commas
            if (!expression.end() && (op != COMMA)) {
                throw new InvalidParamException("operator",
                        Character.toString(op), ac);
            }
        }
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssBackgroundAttachment(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (initial == value);
    }
}
