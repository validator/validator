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

    /**
     * CSS Nesting: the declarations following a rule nested in another rule
     * (a nested declarations rule), shown without selectors
     */
    public static CssStyleRule newDeclarations(String indent, ArrayList<CssProperty> properties) {
        CssStyleRule rule = new CssStyleRule(indent, null, properties, false);
        rule.declarationsOnly = true;
        return rule;
    }

    /**
     * CSS Nesting: makes this rule an at-rule nested in a style rule, shown
     * with its prelude instead of selectors
     *
     * @param atRule    the at-rule's prelude
     * @param statement true for an at-rule without block, such as "@layer a;"
     */
    public void setAtRule(String atRule, boolean statement) {
        this.atRule = atRule;
        this.statement = statement;
    }

    /**
     * This function is only used inside the velocity template
     *
     * @return the prelude if this is an at-rule, "" otherwise
     */
    public String getAtRule() {
        return (atRule != null) ? atRule : "";
    }

    public String getAtRuleEscaped() {
        return Messages.escapeString(getAtRule());
    }

    public boolean isStatement() {
        return statement;
    }

    public boolean isDeclarationsOnly() {
        return declarationsOnly;
    }

    /**
     * CSS Nesting: adds a rule nested in this one, after the previous ones
     */
    public void addNestedRule(CssStyleRule rule) {
        nestedRules.add(rule);
    }

    /**
     * This function is only used inside the velocity template
     *
     * @return the rules nested in this one, in order
     */
    public ArrayList<CssStyleRule> getNestedRules() {
        return nestedRules;
    }

    public String toString() {
        return toString("");
    }

    private String toString(String pad) {
        StringBuilder ret = new StringBuilder();
        String header = (atRule != null) ? atRule : selectors;
        if (statement) {
            ret.append(pad).append(header).append("\n");
            return ret.toString();
        }
        if (header != null) {
            ret.append(pad);
            ret.append(header);
            ret.append(" {\n");
        }
        String inner = declarationsOnly ? pad : pad + indent + "   ";
        for (CssProperty property : properties) {
            ret.append(inner);
            ret.append(property.getPropertyName());
            ret.append(" : ");
            ret.append(property.toString());
            if (property.getImportant()) {
                ret.append(" !important");
            }
            ret.append(";\n");
        }
        for (CssStyleRule nested : nestedRules) {
            ret.append(nested.toString(nested.declarationsOnly ? inner : pad + indent + "   "));
        }
        if (header != null) {
            ret.append(pad);
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
    // CSS Nesting
    private String atRule = null;
    private boolean statement = false;
    private boolean declarationsOnly = false;
    private final ArrayList<CssStyleRule> nestedRules = new ArrayList<CssStyleRule>();

}
