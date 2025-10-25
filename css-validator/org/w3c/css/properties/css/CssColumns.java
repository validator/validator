//
// From Sijtsche de Jong (sy.de.jong@let.rug.nl)
// Rewritten 2010 Yves Lafon <ylafon@w3.org>
//
// COPYRIGHT (c) 1995-2018 World Wide Web Consortium, (MIT, ERCIM and Keio)
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css3.Css3Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @see CssColumnWidth
 * @see CssColumnCount
 * @since CSS3
 */

public class CssColumns extends CssProperty {

    private static final String propertyName = "columns";

    /**
     * Create a new CssColumns
     */
    public CssColumns() {
    }

    /**
     * Create a new CssColumns
     *
     * @param ac         the context
     * @param expression The expression for this property
     * @param check      if checking is enforced
     * @throws InvalidParamException Incorrect values
     */
    public CssColumns(ApplContext ac, CssExpression expression,
                      boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
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
        if (((Css3Style) style).cssColumns != null)
            style.addRedefinitionWarning(ac, this);
        ((Css3Style) style).cssColumns = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve if true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getColumns();
        } else {
            return ((Css3Style) style).cssColumns;
        }
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssColumnCount &&
                value.equals(((CssColumnCount) property).value));
    }

    /**
     * Returns the name of this property
     */
    public final String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns true if this property is "softly" inherited
     */
    public boolean isSoftlyInherited() {
        return (inherit == value);
    }

    /**
     * Returns a string representation of the object
     */
    public String toString() {
        return value.toString();
    }
}
