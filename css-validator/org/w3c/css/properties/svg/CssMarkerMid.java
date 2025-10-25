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
 * @spec http://www.w3.org/TR/2011/REC-SVG11-20110816/painting.html#MarkerMidProperty
 */
public class CssMarkerMid extends org.w3c.css.properties.css.CssMarkerMid {

    /**
     * Create a new CssMarkerMid
     */
    public CssMarkerMid() {
        value = initial;
    }

    /**
     * Creates a new CssMarkerMid
     *
     * @param expression The expression for this property
     * @throws org.w3c.css.util.InvalidParamException
     *          Expressions are incorrect
     */
    public CssMarkerMid(ApplContext ac, CssExpression expression, boolean check)
            throws InvalidParamException {
        value = CssMarker.checkMarkerValue(this, ac, expression, check);
    }

    public CssMarkerMid(ApplContext ac, CssExpression expression)
            throws InvalidParamException {
        this(ac, expression, false);
    }

}

