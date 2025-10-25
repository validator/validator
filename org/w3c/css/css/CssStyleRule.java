// $Id$
// Author: Sijtsche de Jong
// (c) COPYRIGHT MIT, ERCIM and Keio, 2003.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.css.css;

import org.w3c.css.properties.css.CssProperty;
import org.w3c.css.util.Messages;

import java.util.ArrayList;

public class CssStyleRule {

    public CssStyleRule(String indent, String selectors,
                        ArrayList<CssProperty> properties, boolean important) {
        this.selectors = selectors;
        this.properties = properties;
        this.indent = indent;
    }

    /**
     * This function is only used inside the velocity template
     *
     * @return the list of selectors in a string
     */
    public String getSelectors() {
        return selectors;
    }

    public String getSelectorsEscaped() {
        return Messages.escapeString(selectors);
    }

    /**
     * This function is only used inside the velocity template
     *
     * @return the list of properties in a Vector
     */
    public ArrayList<CssProperty> getProperties() {
        return properties;
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (selectors != null) {
            ret.append(selectors);
            ret.append(" {\n");
        }

        for (CssProperty property : properties) {
            ret.append(indent);
            ret.append("   ");
            ret.append(property.getPropertyName());
            ret.append(" : ");
            ret.append(property.toString());
            if (property.getImportant()) {
                ret.append(" !important");
            }
            ret.append(";\n");
        }
        if (selectors != null) {
            ret.append(indent);
            ret.append("}\n\n");
        }
        return ret.toString();
    }

    public String toStringEscaped() {
        return Messages.escapeString(toString());
    }

    /**
     * This method returns a part of the style sheet to be displayed
     * Some identation (\t) was necessary to maintain the correct formatting
     * of the html output.
     */

    private String indent;
    private String selectors;
    private ArrayList<CssProperty> properties;

}
