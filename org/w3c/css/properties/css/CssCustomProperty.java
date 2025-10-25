//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;

/**
 * @since CSS3
 */
public abstract class CssCustomProperty extends CssProperty {

    public String variable_name = null;

    /**
     * Create a new CssCustomProperty
     */
    public CssCustomProperty() {
    }

    public CssCustomProperty(ApplContext ac, String variablename, CssExpression expression)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                expression.getValue().toString(),
                getPropertyName(), ac);
    }

    public CssCustomProperty(ApplContext ac, String variablename, String unparsed_expression)
            throws InvalidParamException {
        throw new InvalidParamException("value",
                unparsed_expression,
                getPropertyName(), ac);
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
    public final String getPropertyName() {
        return variable_name;
    }

    /**
     * Returns true if this property is "softly" inherited
     * e.g. his value is equals to inherit
     */
    public boolean isSoftlyInherited() {
        return false;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return value.toString();
    }

    /**
     * Compares two properties for equality.
     *
     * @param property The other property.
     */
    public boolean equals(CssProperty property) {
        if (property instanceof CssCustomProperty) {
            return value.equals(property.value);
        }
        return false;
    }

}

