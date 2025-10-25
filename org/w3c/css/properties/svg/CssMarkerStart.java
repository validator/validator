//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2016.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.svg;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#MarkerStartProperty
 */
public class CssMarkerStart extends org.w3c.css.properties.css.CssMarkerStart {

    /**
     * Create a new CssMarkerStart
     */
    public CssMarkerStart() {
        value = initial;
    }

    /**
     * Creates a new CssMarkerStart
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMarkerStart(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssMarker.checkMarkerValue(this, ac, expression, check);
    }

    public CssMarkerStart(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

