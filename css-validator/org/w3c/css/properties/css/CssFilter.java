// Author: Chris Rebert <css.validator@chrisrebert.com>
//
// (c) COPYRIGHT World Wide Web Consortium (MIT, ERCIM, Keio University, and Beihang University), 2015.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css3.Css3Style;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS4
 */
public class CssFilter extends CssProperty {


    /**
     * Create a new CssFilter
     */
    public CssFilter() {
    }

    /**
     * Create a new CssFilter
     *
     * @param expression The expression for this property
     * @throws InvalidParamException Values are incorrect
     */
    public CssFilter(ApplContext ac, CssExpression expression, boolean check) throws InvalidParamException {
        throw new InvalidParamException("unrecognize", ac);
    }

    public CssFilter(ApplContext ac, CssExpression expression) throws InvalidParamException {
        this(ac, expression, false);
    }

    /**
     * Returns the value of this property
     */
    public Object get() {
        return value;
    }

    /**
     * Returns the name of this property
     */
    public String getPropertyName() {
        return "filter";
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. its value equals inherit
     */
    public boolean isSoftlyInherited() {
        return value.equals(inherit);
    }

    /**
     * Is the value of this property a default value
     * It is used by all macro for the function <code>print</code>
     */
    public boolean isDefault() {
        return value == none;
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        return (property instanceof CssFilter &&
                value.equals(((CssFilter) property).value));
    }

    /**
     * Add this property to the CssStyle.
     *
     * @param style The CssStyle
     */
    public void addToStyle(ApplContext ac, CssStyle style) {
        if (((Css3Style) style).cssFilter != null) {
            style.addRedefinitionWarning(ac, this);
        }
        ((Css3Style) style).cssFilter = this;
    }

    /**
     * Get this property in the style.
     *
     * @param style   The style where the property is
     * @param resolve If true, resolve the style to find this property
     */
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        if (resolve) {
            return ((Css3Style) style).getFilter();
        } else {
            return ((Css3Style) style).cssFilter;
        }
    }
}
