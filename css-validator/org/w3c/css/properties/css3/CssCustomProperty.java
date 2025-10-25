//
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM, Keio, Beihang, 2021.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css3;

import org.w3c.css.css.StyleSheet;
import org.w3c.css.parser.CssStyle;
import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssVariableDefinition;

/**
 * @spec
 */
public class CssCustomProperty extends org.w3c.css.properties.css.CssCustomProperty {

    public CssCustomProperty(ApplContext ac, String variablename, CssExpression expression)
            throws InvalidParamException {
        variable_name = variablename;
        value = new CssVariableDefinition(expression);
    }

    public CssCustomProperty(ApplContext ac, String variablename, String unparsed_expression)
            throws InvalidParamException {
        variable_name = variablename;
        value = new CssVariableDefinition(unparsed_expression);
    }

    @Override
    public void addToStyle(ApplContext ac, CssStyle style) {
        StyleSheet s = ac.getStyleSheet();
        assert s != null;
        CssCustomProperty p = (CssCustomProperty) s.addCustomProperty(getPropertyName(), this, false);
        if (p != null) {
            // duplicate def
            // add a warning?
            s.addCustomProperty(getPropertyName(), this, true);
        }
    }

    @Override
    public CssProperty getPropertyInStyle(CssStyle style, boolean resolve) {
        Css3Style s = (Css3Style) style;
        return null;
    }
}

