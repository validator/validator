// $Id$
// @author Yves Lafon <ylafon@w3.org>
//
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
 * @spec https://www.w3.org/TR/2021/CRD-css-backgrounds-3-20210726/#propdef-background-image
 */
public class CssBackgroundImage extends org.w3c.css.properties.css.CssBackgroundImage {


    public static boolean isMatchingIdent(CssIdent ident) {
        return none.equals(ident);
    }

    /**
     * Create a new CssBackgroundImage
     */
    public CssBackgroundImage() {
        value = initial;
    }

    /**
     * Creates a new CssBackgroundImage
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      boolean
     * @throws org.w3c.css.util.InvalidParamException Values are incorrect
     */
    public CssBackgroundImage(ApplContext ac, CssExpression expression,
                              boolean check) throws InvalidParamException {

        ArrayList<CssValue> values = new ArrayList<CssValue>();
        setByUser();

        CssValue val;
        char op;

        while (!expression.end()) {
            val = expression.getValue();
            op = expression.getOperator();
            switch (val.getType()) {
                case CssTypes.CSS_URL:
                case CssTypes.CSS_IMAGE:
                    values.add(val);
                    break;
                case CssTypes.CSS_IDENT:
                    CssIdent id = val.getIdent();
                    if (CssIdent.isCssWide(id)) {
                        // if we got inherit after other values, fail
                        // if we got more than one value... fail
                        if ((values.size() > 0) || (expression.getCount() > 1)) {
                            throw new InvalidParamException("value", val,
                                    getPropertyName(), ac);
                        }
                        values.add(val);
                        break;
                    } else if (isMatchingIdent(id)) {
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
        value = (values.size() == 1) ? values.get(0) : new CssLayerList(values);
    }

    public CssBackgroundImage(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Is the value of this property is a default value.
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return (value == none);
    }

}
