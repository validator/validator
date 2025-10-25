//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;

/**
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#StrokeProperty
 */
public class CssStroke extends org.w3c.css.properties.css.CssStroke {

    public static final CssIdent currentColor;

    static {
        currentColor = CssIdent.getIdent("currentColor");
    }


    /**
     * Create a new CssStroke
     */
    public CssStroke() {
        value = initial;
    }

    /**
     * Creates a new CssStroke
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssStroke(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssFill.parsePaint(ac, expression, check, this);
    }

    public CssStroke(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

